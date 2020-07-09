package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixMatcher;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2MultiViewModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.model.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView;
import org.sidiff.bug.localization.dataset.systemmodel.util.MultiViewModelStorage;
import org.sidiff.bug.localization.dataset.workspace.builder.TestProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class RetrievalProcess {
	
	private RetrievalConfiguration configuration; 

	private DataSet dataset;
	
	private GitRepository repository;

	public RetrievalProcess(RetrievalConfiguration configuration, DataSet dataset) {
		this.configuration = configuration;
		this.dataset = dataset;
	}
	
	public void retrieve() {
		retrieveHistory();
		retrieveBugReports();
		removeVersionsWithoutBugReport();
		
		retrieveSystemModels();
	}

	private void retrieveHistory() {
		GitRepository repository = retrieveRepository();
		retrieveBugFixes(repository);
	}

	private GitRepository retrieveRepository() {
		String repositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		this.repository = new GitRepository(configuration.getLocalRepositoryPath().toFile());
	
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

	private void retrieveBugFixes(GitRepository repository) {
		
		// Retrieve commits with bug fixes in their comments:
		BugFixMatcher bugFixMatcher = new BugFixMatcher();
		BugFixVersionFilter bugFixVersionFilter = new BugFixVersionFilter(bugFixMatcher); // VersionFilter.FILTER_NOTHING
		
		History history = repository.getHistory(bugFixVersionFilter);
		dataset.setHistory(history);
	}

	private void retrieveBugReports() {
		History history = dataset.getHistory();
		BugFixMatcher bugFixMatcher = new BugFixMatcher();
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();

		List<Callable<Object>> requestReportTasks = new ArrayList<>();

		for (Version version : history.getVersions()) {
			if (version.getCommitMessage() != null) {
				int bugID = bugFixMatcher.matchBugID(version.getCommitMessage());

				if (bugID != -1) {
					requestReportTasks.add(() -> {
						try {
							BugReport bugReport = bugtracker.getBugReport(bugID);
							
							if (bugReport != null) {
								version.setBugReport(bugReport);
							} else {
								Activator.getLogger().log(Level.SEVERE, "Bug tracker returned <null> for bug ID: " + bugID);
							}
						} catch (NoSuchElementException e) {
							Activator.getLogger().log(Level.WARNING, "Bug ID not found: " + bugID);
						} catch (Throwable e) {
							Activator.getLogger().log(Level.SEVERE, "Bug ID request failed: " + bugID, e);
							e.printStackTrace();
						}
						return null;
					});
				}
			}
		}
		
		// NOTE: If too many requests are made at once, some of them will not be answered by the server!
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		
		try {
			executorService.invokeAll(requestReportTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			executorService.shutdown();
		}
	}

	private void removeVersionsWithoutBugReport() {
		
		// TODO: remove, mark or retry on report!?
		
	}

	private void retrieveSystemModels() {
		History history = dataset.getHistory();
		
		for (Version version : history.getVersions()) {
			Workspace workspace = retrieveWorkspaceVersion(history, version);
			
			for (Project project : workspace.getProjects()) {
				try {
					retrieveSystemModelVersion(project);
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

	private Workspace retrieveWorkspaceVersion(History history, Version version) {
		repository.checkout(history, version);
		
		Path projectSearchPath = configuration.getLocalRepositoryPath();
		Workspace workspace = new Workspace(projectSearchPath);
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace);
		workspaceBuilder.findProjects(projectSearchPath, new TestProjectFilter());
		
		version.setWorkspace(workspace);
		return workspace;
	}

	private void retrieveSystemModelVersion(Project project) throws DiscoveryException {
		
		// Discover the multi-view system model of the project version:
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer();
		multiViewModelDiscoverer.discoverElement(workspaceProject, new NullProgressMonitor());
		
		Resource umlResource = multiViewModelDiscoverer.getTargetModel();
		MultiView multiViewSystemModel = (MultiView) umlResource.getContents().get(0);

		MultiViewModelStorage.saveAll(multiViewSystemModel, Collections.emptyMap());
		
		// Store system model in data set:
		SystemModel systemModel = new SystemModel();
		systemModel.setName(multiViewSystemModel.getName());
		systemModel.setDescription(multiViewSystemModel.getDescription());
		systemModel.setModel(Paths.get(multiViewSystemModel.eResource().getURI().toFileString()));
		
		project.setSystemModel(systemModel);
	}
}
