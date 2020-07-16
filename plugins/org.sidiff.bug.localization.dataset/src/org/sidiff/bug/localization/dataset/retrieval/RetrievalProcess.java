package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Files;
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
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
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
	
	protected GitRepository repository;
	
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
		GitRepository repository = retrieveRepository();
		retrieveBugFixes(repository);
	}

	protected GitRepository retrieveRepository() {
		this.repositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		this.localRepositoryPath = Paths.get(configuration.getLocalRepositoryPath().toFile() + "/" + dataset.getName());
		this.repository = new GitRepository(localRepositoryPath.toFile());
	
		if (!repository.exists()) {
			repository.clone(repositoryURL);
		} else {
			if (Activator.getLogger().isLoggable(Level.WARNING)) {
				Activator.getLogger().log(Level.WARNING, "A repository already exists in "
						+ configuration.getLocalRepositoryPath() + " for " + repositoryURL);
			}
		}
		return repository;
	}

	protected void retrieveBugFixes(GitRepository repository) {
		
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
		
		for (Version version : history.getVersions()) {
			Workspace workspace = retrieveWorkspaceVersion(history, version);
			
			for (Project project : workspace.getProjects()) {
				try {
					retrieveJavaASTVersion(version, project);
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
		}
	}

	protected Workspace retrieveWorkspaceVersion(History history, Version version) {
		repository.checkout(history, version);
		
		Path projectSearchPath = this.localRepositoryPath;
		Workspace workspace = new Workspace();
		ProjectFilter projectFilter = new TestProjectFilter(new PDEProjectFilter());
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace, projectSearchPath);
		workspaceBuilder.findProjects(projectSearchPath, projectFilter);
		
		version.setWorkspace(workspace);
		return workspace;
	}

	protected void retrieveJavaASTVersion(Version version, Project project) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Model Discovery: " + version.getDate() + " - " + project.getName());
		}
		
		// Storage paths:
		Path systemModelPath = Files.createDirectories(
				Paths.get(datasetStorage.getParent().toString(), 
						getSysteModelFolder(version), 
						project.getName()));
		Path systemModelFile = Paths.get(systemModelPath.toString(), 
				project.getName() + "." + MultiViewSystemModel.MULITVIEW_MODEL_FILE_EXTENSION); 
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
	
	protected String getSysteModelFolder(Version version) {
		String versionDate = version.getDate().toString().replace(":", "-");
		return versionDate + "_" + version.getIdentification();
	}

	public void retrieveSystemModels() {
		History history = dataset.getHistory();
		
		for (Version version : history.getVersions()) {
			for (Project project : version.getWorkspace().getProjects()) {
				try {
					retrieveSystemModelVersion(version, project);
				} catch (DiscoveryException e) {
					if (Activator.getLogger().isLoggable(Level.SEVERE)) {
						Activator.getLogger().log(Level.SEVERE, "Could not discover system model for '"
								+ project.getName() + "' version " + version.getIdentification());
					}
					e.printStackTrace();
				}
			}
		}
	}

	protected void retrieveSystemModelVersion(Version version, Project project) throws DiscoveryException {
		
		// Discover the multi-view system model of the project version:
		MultiViewSystemModel multiViewSystemModel = new MultiViewSystemModel(datasetStorage.getParent().resolve(project.getSystemModel()));
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer();
		
		Resource javaResource = multiViewSystemModel.getViewByKind(ViewDescriptions.JAVA_AST);
		multiViewModelDiscoverer.discoverUMLClassDiagram(multiViewSystemModel, javaResource, new NullProgressMonitor());
//		multiViewModelDiscoverer.discoverUMLOperationControlFlow(multiViewSystemModel, javaResource, new NullProgressMonitor()); // TODO: discover UML Operation Control Flow
		
		// Store system model in data set:
		multiViewSystemModel.saveAll(Collections.emptyMap(), Collections.singleton(javaResource));
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

	public GitRepository getRepository() {
		return repository;
	}
}
