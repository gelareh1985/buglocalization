package org.sidiff.bug.localization.dataset.retrieval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.report.util.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.history.report.util.placeholder.BugReportPlaceholder;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixMatcher;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2MultiViewModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.model.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView;
import org.sidiff.bug.localization.dataset.workspace.builder.TestProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class RetrievalProcess {
	
	protected RetrievalConfiguration configuration; 

	protected DataSet dataset;
	
	protected GitRepository repository;

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
	
	public void retrieveHistory() {
		GitRepository repository = retrieveRepository();
		retrieveBugFixes(repository);
	}

	protected GitRepository retrieveRepository() {
		String repositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		String localRepositoryPath = configuration.getLocalRepositoryPath().toFile() + "/" + dataset.getName();
		this.repository = new GitRepository(new File(localRepositoryPath));
	
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
		BugFixMatcher bugFixMatcher = new BugFixMatcher();
		BugFixVersionFilter bugFixVersionFilter = new BugFixVersionFilter(bugFixMatcher); // VersionFilter.FILTER_NOTHING
		
		History history = repository.getHistory(bugFixVersionFilter);
		dataset.setHistory(history);
	}

	public void retrieveBugReports() {
		BugFixMatcher bugFixMatcher = new BugFixMatcher();
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(bugtracker, bugFixMatcher);
		
		bugReportRequestsExecutor.request(dataset.getHistory().getVersions());
		bugReportRequestsExecutor.setPlaceholders();
	}

	public void removeVersionsWithoutBugReport() {
		for (Iterator<Version> iterator = dataset.getHistory().getVersions().iterator(); iterator.hasNext();) {
			Version version = iterator.next();
			
			if (version.getBugReport() instanceof BugReportPlaceholder) {
				iterator.remove();
			}
		}
	}

	public void retrieveSystemModels() {
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

	protected Workspace retrieveWorkspaceVersion(History history, Version version) {
		repository.checkout(history, version);
		
		Path projectSearchPath = configuration.getLocalRepositoryPath();
		Workspace workspace = new Workspace(projectSearchPath);
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace);
		workspaceBuilder.findProjects(projectSearchPath, new TestProjectFilter());
		
		version.setWorkspace(workspace);
		return workspace;
	}

	protected void retrieveSystemModelVersion(Project project) throws DiscoveryException {
		
		// Discover the multi-view system model of the project version:
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer();
		multiViewModelDiscoverer.discoverElement(workspaceProject, new NullProgressMonitor());
		
		Resource umlResource = multiViewModelDiscoverer.getTargetModel();
		MultiView multiViewSystemModel = (MultiView) umlResource.getContents().get(0);
		
		// Store system model in data set:
		SystemModel systemModel = new SystemModel();
		systemModel.setName(multiViewSystemModel.getName());
		systemModel.setDescription(multiViewSystemModel.getDescription());
		systemModel.setModel(Paths.get(multiViewSystemModel.eResource().getURI().toFileString()));
		
		project.setSystemModel(systemModel);
	}
	
	public void saveDataSet(Path path) throws IOException {
		JsonUtil.save(dataset, path);
	}
}
