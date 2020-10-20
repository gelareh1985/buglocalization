package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.workspace.filter.PDEProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectNameFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.TestProjectFilter;

@SuppressWarnings("deprecation")
public class TestDriverApplication extends RetrievalApplication {
	
	private static int SHRINK_TO_HISTORY_SIZE = 4;
	
	private static String[] FILTER_PROJECTS = {}; // {"org.eclipse.jdt.core", "org.eclipse.jdt.apt.core"};

	@Override
	protected void start(Path datasetPath, DataSet dataset, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {		
		String codeRepositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataset.getName());
		
		// Bug fixes:
		{
			BugFixHistoryRetrievalProvider bugHistoryRetrievalProvider = new BugFixHistoryRetrievalProvider(
					codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataset.getBugtrackerProducts());
			BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugHistoryRetrievalProvider, dataset, datasetPath);
			bugFixHistory.retrieveHistory();
			
			{
				shrinkHistory(bugFixHistory.getDataset(), SHRINK_TO_HISTORY_SIZE);
			}
			
			bugFixHistory.retrieveBugReports();
			bugFixHistory.retrieveBugFixLocations();
			bugFixHistory.cleanUpHistory();
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
		
		// Java model:
		{
			JavaModelRetrievalProvider javaModelRetrievalProvider = new JavaModelRetrievalProvider(codeRepositoryPath);
			javaModelRetrievalProvider.setProjectFilter(() -> new TestProjectFilter(new PDEProjectFilter()));
			
			{
				filterProjects(javaModelRetrievalProvider, FILTER_PROJECTS);
			}
			
			JavaModelRetrieval javaModel = new JavaModelRetrieval(javaModelRetrievalProvider, dataset, datasetPath);
			javaModel.retrieve();
			javaModel.saveDataSet();
		}
		
		// System model:
		{
			SystemModelRetrievalProvider systemModelRetrievalProvider = new SystemModelRetrievalProvider();
			SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelRetrievalProvider, codeRepositoryPath, dataset, datasetPath);
			systemModel.retrieve();
			systemModel.saveDataSet();
		}
	}

	private void filterProjects(JavaModelRetrievalProvider provider, String... projectNames) {
		ProjectFilter parentProjectFilter = provider.createProjectFilter();
		provider.setProjectFilter(() -> new ProjectNameFilter(
				parentProjectFilter, new HashSet<>(Arrays.asList(projectNames))));
	}

	private void shrinkHistory(DataSet dataset, int historySize) {
		dataset.getHistory().setVersions(dataset.getHistory().getVersions().subList(0, historySize));
	}
	
}
