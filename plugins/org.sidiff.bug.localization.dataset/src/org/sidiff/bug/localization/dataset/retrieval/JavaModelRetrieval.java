package org.sidiff.bug.localization.dataset.retrieval;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2SystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.views.SystemModel;
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
			this.dataset = JsonUtil.parse(datasetPath, DataSet.class);
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
		
		Version previousVersion = null;
		
		for (Version version : history.getVersions()) {
			Workspace workspace = retrieveWorkspaceVersion(history, version);
			
			for (Project project : workspace.getProjects()) {
				try {
					retrieveJavaModelVersion(project);
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
			previousVersion = javaModelRepository.commitVersion(version, previousVersion);
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

	private void retrieveJavaModelVersion(Project project) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		// Storage:
		Path systemModelFile = javaModelRepository.getSystemModelFile(project);
		URI mulitviewFile = URI.createFileURI(systemModelFile.toFile().getAbsolutePath());
		
		// Discover the Java AST of the project version:
		SystemModel systemModel = new SystemModel(mulitviewFile);
		
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
		JavaProject2SystemModelDiscoverer multiViewModelDiscoverer = new JavaProject2SystemModelDiscoverer(mulitviewFile);
		multiViewModelDiscoverer.discoverJavaModel(systemModel, workspaceProject, new NullProgressMonitor());
		
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
