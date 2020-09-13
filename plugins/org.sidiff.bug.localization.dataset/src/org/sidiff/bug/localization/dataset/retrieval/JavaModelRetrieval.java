package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.ChangeLocationDiscoverer;
import org.sidiff.bug.localization.dataset.changes.ChangeLocationMatcher;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.retrieval.util.ProjectChangeProvider;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2JavaSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.ChangeProvider;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.IncrementalJavaParser;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class JavaModelRetrieval {
	
	private JavaModelRetrievalProvider provider;
	
	private DataSet dataset;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private Path javaModelRepositoryPath;
	
	private IncrementalJavaParser javaParser;
	
	public JavaModelRetrieval(JavaModelRetrievalProvider provider, DataSet dataset, Path datasetPath) {
		this.provider = provider;
		this.dataset = dataset;
		this.javaParser = new IncrementalJavaParser(provider.isIgnoreMethodBodies());
	}

	public void retrieve() {
		History history = dataset.getHistory();
		List<Version> versions = history.getVersions();
		
		// Storage:
		this.codeRepository = provider.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL, dataset);
		this.javaModelRepositoryPath = javaModelRepository.getRepositoryPath();
		
		try {
			// Iterate from old to new versions:
			for (int i = versions.size(); i-- > 0;) {
				Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
				Version version = versions.get(i);
				Version newerVersion = (i > 0) ? versions.get(i - 1) : null;
				
				Workspace workspace = retrieveWorkspaceVersion(history, version);
				
				// Workspace -> Java Models
				retrieveWorkspaceJavaModelVersion(olderVersion, version, newerVersion, workspace);
				
				// Store Java AST model workspace as revision:
				javaModelRepository.commitVersion(version, olderVersion);
				
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "Discovered version " + (versions.size() - i) + " of " + versions.size() + " versions");
				}
			}
		} finally {
			// Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
			codeRepository.reset();
		}
	}

	private void retrieveWorkspaceJavaModelVersion(Version olderVersion, Version version, Version newerVersion, Workspace workspace) {
		if (!workspace.getProjects().isEmpty()) { // optimization
			for (Project project : workspace.getProjects()) {
				
				// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
				// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
				ChangeLocationMatcher changeLocationMatcher  = null;
				
				if ((newerVersion != null) && (newerVersion.hasBugReport())) {
					changeLocationMatcher = new ChangeLocationMatcher(
							project.getName(), newerVersion.getBugReport().getBugLocations(), provider.getFileChangeFilter());
				}
				
				// Project -> Java Model
				try {
					retrieveProjectJavaModelVersion(olderVersion, version, project, changeLocationMatcher);
				} catch (Throwable e) {
					e.printStackTrace();

					if (Activator.getLogger().isLoggable(Level.SEVERE)) {
						Activator.getLogger().log(Level.SEVERE, "Could not discover system model: " + project);
					}
				}
			}
		}
	}

	private Workspace retrieveWorkspaceVersion(History history, Version version) {
		codeRepository.checkout(history, version);
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Retrieve Workspace: " + version);
		}
		
		Workspace discoveredWorkspace = version.getWorkspace();
		WorkspaceBuilder workspaceDiscoverer = new WorkspaceBuilder(discoveredWorkspace, codeRepositoryPath);
		workspaceDiscoverer.cleanWorkspace();
		
		// Load and filter workspace:
		ProjectFilter projectFilter = provider.createProjectFilter();
		Workspace loadedWorkspace = workspaceDiscoverer.createProjects(projectFilter);
		version.setWorkspace(loadedWorkspace);
		
		return loadedWorkspace;
	}

	private void retrieveProjectJavaModelVersion(Version olderVersion, Version version, Project project, ChangeLocationMatcher changeLocationMatcher) 
			throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		Path systemModelFile = javaModelRepository.getSystemModelFile(project);
		
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if (HistoryUtil.hasChanges(project, olderVersion, version, provider.getFileChangeFilter())) {
			
			// Calculate changed files in project for incremental AST parser:
			List<FileChange> projectFileChanges = HistoryUtil.getChanges(project, version.getFileChanges(), provider.getFileChangeFilter());
			ChangeProvider changeProvider = new ProjectChangeProvider(projectFileChanges);
			
			// Discover the Java AST of the project version:
			IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
			JavaProject2JavaSystemModel systemModelDiscoverer = new JavaProject2JavaSystemModel(javaParser, changeProvider);
			SystemModel systemModel;
			
			if (changeLocationMatcher != null) {
				// Discover with change locations:
				ChangeLocationDiscoverer changeLocationDiscoverer = new ChangeLocationDiscoverer(changeLocationMatcher);
				systemModel = systemModelDiscoverer.discover(workspaceProject, changeLocationDiscoverer, new NullProgressMonitor());
				systemModel.getViewByKind(ViewDescriptions.JAVA_MODEL).getChanges().addAll(changeLocationDiscoverer.getChanges());
			} else {
				systemModel = systemModelDiscoverer.discover(workspaceProject, null, new NullProgressMonitor());
			}
			
			// Store system model in data set:
			URI systemModelURI = URI.createFileURI(systemModelFile.toFile().getAbsolutePath());
			systemModel.eResource().setURI(systemModelURI);
			systemModel.saveAll(Collections.emptyMap());
		}
		
		// Store path in data set:
		project.setSystemModel(javaModelRepository.getDataSetPath().getParent().relativize(systemModelFile));
	}
	
	public void saveDataSet() {
		// Store and commit data set for Java model:
		try {
			javaModelRepository.saveDataSet(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getJavaModelRepositoryPath() {
		return javaModelRepositoryPath;
	}
}
