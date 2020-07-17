package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixMessageIDMatcher;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugReportProductMatchingFilter;
import org.sidiff.bug.localization.dataset.fixes.report.request.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.fixes.report.request.placeholders.BugReportPlaceholder;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
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

public class RetrievalProcess {
	
	protected RetrievalConfiguration configuration; 

	protected DataSet dataset;
	
	protected Path datasetStorage;
	
	protected Repository repository;
	
	protected String repositoryURL;
	
	protected Path localRepositoryPath;
	
	public RetrievalProcess(RetrievalConfiguration configuration, DataSet dataset, Path datasetStorage) {
		this.configuration = configuration;
		this.dataset = dataset;
		this.datasetStorage = datasetStorage;
	}
	
	public void retrieve() {
		retrieveHistory();
		retrieveBugReports();
		cleanUp();
		retrieveJavaAST();
		retrieveSystemModels();
	}
	
	public void retrieveHistory() {
		retrieveRepository();
		retrieveBugFixes();
	}

	protected Repository retrieveRepository() {
		this.repositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		this.localRepositoryPath = Paths.get(configuration.getLocalRepositoryPath().toFile() + "/" + dataset.getName());
		this.repository = new GitRepository(repositoryURL, localRepositoryPath.toFile());
		return repository;
	}

	protected void retrieveBugFixes() {
		
		// Retrieve commits with bug fixes in their comments:
		BugFixMessageIDMatcher bugFixMessageIDMatcher = new BugFixMessageIDMatcher();
		VersionFilter bugFixVersionFilter = new BugFixVersionFilter(bugFixMessageIDMatcher);
		
		History history = repository.getHistory(bugFixVersionFilter);
		dataset.setHistory(history);
	}

	public void retrieveBugReports() {
		BugFixMessageIDMatcher bugFixMessageIDMatcher = new BugFixMessageIDMatcher();
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();
		BugReportFilter productMatcher = new BugReportProductMatchingFilter(dataset.getBugtrackerProduct());
		
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(bugtracker, productMatcher, bugFixMessageIDMatcher);
		bugReportRequestsExecutor.request(dataset.getHistory().getVersions());
		bugReportRequestsExecutor.setPlaceholders();
	}

	public void cleanUp() {
		for (Iterator<Version> iterator = dataset.getHistory().getVersions().iterator(); iterator.hasNext();) {
			Version version = iterator.next();
			
			if (version.getBugReport() instanceof BugReportPlaceholder) {
				iterator.remove();
			}
		}
	}
	
	public void retrieveJavaAST() {
		History history = dataset.getHistory();
		
		// Storage:
		SystemModelRepository javaModelRepository = new SystemModelRepository(localRepositoryPath, ViewDescriptions.JAVA_MODEL, dataset);
		Version previousVersion = null;
		
		for (Version version : history.getVersions()) {
			Workspace workspace = retrieveWorkspaceVersion(history, version);
			
			for (Project project : workspace.getProjects()) {
				if (project.getName().equals("org.eclipse.jdt.core")) continue; // TODO: TEST
				
				try {
					retrieveJavaASTVersion(project, javaModelRepository.getSystemModelFile(project));
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

	protected Workspace retrieveWorkspaceVersion(History history, Version version) {
		repository.checkout(history, version);
		
		Workspace workspace = new Workspace();
		ProjectFilter projectFilter = new TestProjectFilter(new PDEProjectFilter());
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace, localRepositoryPath);
		workspaceBuilder.findProjects(localRepositoryPath, projectFilter);
		
		version.setWorkspace(workspace);
		return workspace;
	}

	protected void retrieveJavaASTVersion(Project project, Path systemModelFile) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		// Storage:
		URI mulitviewFile = URI.createFileURI(systemModelFile.toFile().getAbsolutePath());
		
		// Discover the Java AST of the project version:
		MultiViewSystemModel multiViewSystemModel = new MultiViewSystemModel(mulitviewFile);
		
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer(mulitviewFile);
		multiViewModelDiscoverer.discoverJavaAST(multiViewSystemModel, workspaceProject, new NullProgressMonitor());
		
		// Store system model in data set:
		multiViewSystemModel.saveAll(Collections.emptyMap());
		
		// data set:
		project.setSystemModel(datasetStorage.getParent().relativize(systemModelFile));
	}
	
	public void retrieveSystemModels() {
		
		// Storage:
		SystemModelRepository javaModelRepository = new SystemModelRepository(localRepositoryPath, ViewDescriptions.JAVA_MODEL);
		DataSet dataset = javaModelRepository.getDataSet();
		
		SystemModelRepository umllRepository = new SystemModelRepository(localRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		Version previousVersion = null;
		
		for (Version version : dataset.getHistory().getVersions()) {
			for (Project project : version.getWorkspace().getProjects()) {
				if (project.getName().equals("org.eclipse.jdt.core")) continue; // TODO: TEST
				
				try {
					retrieveSystemModelVersion(project, 
							javaModelRepository.getSystemModelFile(project),
							umllRepository.getSystemModelFile(project));
				} catch (DiscoveryException e) {
					if (Activator.getLogger().isLoggable(Level.SEVERE)) {
						Activator.getLogger().log(Level.SEVERE, "Could not discover system model for '"
								+ project.getName() + "' version " + version.getIdentification());
					}
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Store Java AST model workspace as revision:
			previousVersion = umllRepository.commitVersion(version, previousVersion);
		}
		
		// Store data set for UML model:
		try {
			umllRepository.saveDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void retrieveSystemModelVersion(Project project, Path javaSystemModelFile, Path systemModelFile) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		// Discover the multi-view system model of the project version:
		MultiViewSystemModel multiViewSystemModel = new MultiViewSystemModel(javaSystemModelFile);
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer();
		
		Resource javaResource = multiViewSystemModel.getViewByKind(ViewDescriptions.JAVA_MODEL);
		multiViewModelDiscoverer.discoverUMLClassDiagram(multiViewSystemModel, javaResource, new NullProgressMonitor());
//		multiViewModelDiscoverer.discoverUMLOperationControlFlow(multiViewSystemModel, javaResource, new NullProgressMonitor()); // FIXME: discover UML Operation Control Flow
		
		// Remove java model:
		multiViewSystemModel.removeViewKind(ViewDescriptions.JAVA_MODEL);
		
		// Store system model in data set:
		multiViewSystemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		multiViewSystemModel.saveAll(Collections.emptyMap());
	}

	public void saveDataSet() throws IOException {
		JsonUtil.save(dataset, datasetStorage);
	}

	public RetrievalConfiguration getConfiguration() {
		return configuration;
	}

	public DataSet getDataset() {
		return dataset;
	}

	public Repository getRepository() {
		return repository;
	}
}
