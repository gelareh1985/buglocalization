package org.sidiff.bug.localization.dataset.retrieval.utilapp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import org.sidiff.bug.localization.dataset.DataSetRetrievalApplication;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectNameFilter;

public class TestDriverApplication extends DataSetRetrievalApplication {
	
	private static int SHRINK_TO_HISTORY_SIZE = 10;
	
	private static String[] FILTER_PROJECTS = {}; // {"org.eclipse.jdt.core", "org.eclipse.jdt.apt.core"};

	@Override
	protected void start(Path datasetPath, DataSet dataset, DataSet datasetTrace, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {		
		String codeRepositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataset.getName());
		
		// Bug fixes:
		{
			BugFixHistoryRetrievalProvider bugHistoryRetrievalProvider = new BugFixHistoryRetrievalProvider(
					codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataset.getBugtrackerProducts());
			bugHistoryRetrievalProvider.setVersionFilter(() -> VersionFilter.FILTER_NOTHING);
			
			BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugHistoryRetrievalProvider, dataset, datasetPath);
			bugFixHistory.retrieveHistory();
			
			{
				shrinkHistory(bugFixHistory.getDataset(), SHRINK_TO_HISTORY_SIZE);
			}
			
			bugFixHistory.retrieveBugReports();
			bugFixHistory.retrieveBugFixLocations();
//			bugFixHistory.cleanUpHistory();
			bugFixHistory.saveDataSet();
			
			// update data set path to output file
			datasetPath = bugFixHistory.getDatasetPath();
		}
		
		// Workspace:
		{
			WorkspaceHistoryRetrievalProvider workspaceHistoryRetrievalProvider = new WorkspaceHistoryRetrievalProvider(codeRepositoryPath);
			WorkspaceHistoryRetrieval workspaceHistoryRetrieval = new WorkspaceHistoryRetrieval(workspaceHistoryRetrievalProvider, dataset, datasetPath);
			workspaceHistoryRetrieval.retrieve();
			workspaceHistoryRetrieval.saveDataSet();
			
			// update data set path to output file
			datasetPath = workspaceHistoryRetrieval.getDatasetPath();
		}
		
		// System model:
		{
			SystemModelRetrievalProvider systemModelRetrievalProvider = new SystemModelRetrievalProvider(
					codeRepositoryPath, dataset.getProjectNameFilter(), dataset.getProjectPathFilter());
			{
				filterProjects(systemModelRetrievalProvider, FILTER_PROJECTS);
			}
			
			SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelRetrievalProvider, datasetPath, dataset, datasetTrace);
			systemModel.retrieve();
			systemModel.saveDataSet();
		}
	}

	private void filterProjects(SystemModelRetrievalProvider provider, String... projectNames) {
		ProjectFilter parentProjectFilter = provider.createProjectFilter();
		provider.setProjectFilter(() -> new ProjectNameFilter(
				parentProjectFilter, new HashSet<>(Arrays.asList(projectNames))));
	}

	private void shrinkHistory(DataSet dataset, int historySize) {
		if (dataset.getHistory().getVersions().size() > historySize) {
			dataset.getHistory().setVersions(dataset.getHistory().getVersions().subList(0, historySize));
		}
	}
	
}
