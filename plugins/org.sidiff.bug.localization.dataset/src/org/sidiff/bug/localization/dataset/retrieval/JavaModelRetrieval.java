package org.sidiff.bug.localization.dataset.retrieval;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.ChangeLocationDiscoverer;
import org.sidiff.bug.localization.dataset.changes.ChangeLocationMatcher;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2JavaSystemModel;
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
	
	private Predicate<Path> fileChangeFilter;
	
	public JavaModelRetrieval(JavaModelRetrievalProvider factory, Path datasetPath) {
		this.provider = factory;
		this.fileChangeFilter = factory.createFileChangeFilter();
		
		try {
			this.dataset = DataSetStorage.load(datasetPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
				
				for (Project project : workspace.getProjects()) {
					
					// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
					// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
					ChangeLocationMatcher changeLocationMatcher  = null;
					
					if ((newerVersion != null) && (newerVersion.hasBugReport())) {
						changeLocationMatcher = new ChangeLocationMatcher(project.getName(), newerVersion.getChanges());
					}
					
					try {
						retrieveJavaModelVersion(olderVersion, version, project, changeLocationMatcher);
					} catch (DiscoveryException e) {
						if (Activator.getLogger().isLoggable(Level.SEVERE)) {
							Activator.getLogger().log(Level.SEVERE, "Could not discover Java model for '"
									+ project.getName() + "' version " + version.getIdentification());
						}
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				// Store Java AST model workspace as revision:
				javaModelRepository.commitVersion(version, olderVersion);
			}
		} finally {
			// Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
			codeRepository.reset();
		}
	}

	private Workspace retrieveWorkspaceVersion(History history, Version version) {
		codeRepository.checkout(history, version);
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + version);
		}
		
		Workspace workspace = new Workspace();
		ProjectFilter projectFilter = provider.createProjectFilter();
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace, codeRepositoryPath);
		workspaceBuilder.findProjects(codeRepositoryPath, projectFilter);
		
		version.setWorkspace(workspace);
		return workspace;
	}

	private void retrieveJavaModelVersion(Version olderVersion, Version version, Project project, ChangeLocationMatcher changeLocationMatcher) 
			throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		Path systemModelFile = javaModelRepository.getSystemModelFile(project);
		
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if (!version.hasPreviousVersion(olderVersion, project) || version.hasChanges(project, fileChangeFilter)) {
			
			// Discover the Java AST of the project version:
			IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
			JavaProject2JavaSystemModel systemModelDiscoverer = new JavaProject2JavaSystemModel();
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
