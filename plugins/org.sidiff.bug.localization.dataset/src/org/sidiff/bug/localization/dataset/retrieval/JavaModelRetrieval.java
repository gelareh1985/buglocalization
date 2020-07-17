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
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2MultiViewModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.views.MultiViewSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.filter.PDEProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.TestProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class JavaModelRetrieval {
	
	private DataSet dataset;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private Path javaModelRepositoryPath;
	
	public JavaModelRetrieval(Path datasetPath, Path codeRepositoryPath) {
		this.codeRepositoryPath = codeRepositoryPath;
		
		try {
			this.dataset = JsonUtil.parse(datasetPath, DataSet.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void retrieve() {
		History history = dataset.getHistory();
		
		// Storage:
		this.codeRepository = new GitRepository(codeRepositoryPath.toFile());
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
						Activator.getLogger().log(Level.SEVERE, "Could not discover Java AST model for '"
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
		ProjectFilter projectFilter = new TestProjectFilter(new PDEProjectFilter());
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
		MultiViewSystemModel multiViewSystemModel = new MultiViewSystemModel(mulitviewFile);
		
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer(mulitviewFile);
		multiViewModelDiscoverer.discoverJavaAST(multiViewSystemModel, workspaceProject, new NullProgressMonitor());
		
		// Store system model in data set:
		multiViewSystemModel.saveAll(Collections.emptyMap());
		
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
