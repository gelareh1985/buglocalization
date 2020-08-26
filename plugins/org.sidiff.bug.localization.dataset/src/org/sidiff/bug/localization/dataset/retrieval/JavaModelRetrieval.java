package org.sidiff.bug.localization.dataset.retrieval;

import java.io.FileNotFoundException;
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
import org.sidiff.bug.localization.dataset.changes.ChangeResolver;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2SystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class JavaModelRetrieval {
	
	private JavaModelRetrievalFactory factory;
	
	private DataSet dataset;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private Path javaModelRepositoryPath;
	
	public JavaModelRetrieval(JavaModelRetrievalFactory factory, Path datasetPath) {
		this.factory = factory;
		
		try {
			this.dataset = DataSetStorage.load(datasetPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void retrieve() {
		History history = dataset.getHistory();
		
		// Storage:
		this.codeRepository = factory.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL, dataset);
		this.javaModelRepositoryPath = javaModelRepository.getRepositoryPath();
		
		// Iterate from old to new versions:
		// TODO: Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
		List<Version> versions = history.getVersions();
		
		for (int i = versions.size(); i-- > 0;) {
			Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
			Version version = versions.get(i);
			Version newerVersion = (i > 0) ? versions.get(i - 1) : null;
			
			Workspace workspace = retrieveWorkspaceVersion(history, version);
			ChangeResolver changeResolver = null; 
					
			if ((newerVersion != null) && (newerVersion.hasBugReport())) {
				// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
				// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
				changeResolver = new ChangeResolver(newerVersion.getChanges());
			}
			
			for (Project project : workspace.getProjects()) {
				if (changeResolver != null) {
					changeResolver.setProjectName(project.getName());
				}
				
				try {
					retrieveJavaModelVersion(project, changeResolver);
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
		
		// Store data set for Java model:
		try {
			javaModelRepository.saveDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Workspace retrieveWorkspaceVersion(History history, Version version) {
		codeRepository.checkout(history, version);
		
		Workspace workspace = new Workspace();
		ProjectFilter projectFilter = factory.createProjectFilter();
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace, codeRepositoryPath);
		workspaceBuilder.findProjects(codeRepositoryPath, projectFilter);
		
		version.setWorkspace(workspace);
		return workspace;
	}

	private void retrieveJavaModelVersion(Project project, ChangeResolver changeResolver) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		// Storage:
		Path systemModelFile = javaModelRepository.getSystemModelFile(project);
		URI mulitviewFile = URI.createFileURI(systemModelFile.toFile().getAbsolutePath());
		
		// Discover the Java AST of the project version:
		SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel(mulitviewFile);
		
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
		JavaProject2SystemModelDiscoverer systemModelDiscoverer = new JavaProject2SystemModelDiscoverer(mulitviewFile);
		
		if (changeResolver != null) {
			// Discover with change locations:
			ChangeLocationDiscoverer changeLocationDiscoverer = new ChangeLocationDiscoverer(changeResolver);
			systemModelDiscoverer.setJavaModelDiscovererListener(changeLocationDiscoverer);
			systemModelDiscoverer.discoverJavaModel(systemModel, workspaceProject, new NullProgressMonitor());
			systemModel.getViewByKind(ViewDescriptions.JAVA_MODEL).getChanges().addAll(changeLocationDiscoverer.getChanges());
		} else {
			systemModelDiscoverer.discoverJavaModel(systemModel, workspaceProject, new NullProgressMonitor());
		}
		
		// Store system model in data set:
		systemModel.saveAll(Collections.emptyMap());
		
		// Store path in data set:
		project.setSystemModel(javaModelRepository.getDataSetPath().getParent().relativize(systemModelFile));
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getJavaModelRepositoryPath() {
		return javaModelRepositoryPath;
	}
}
