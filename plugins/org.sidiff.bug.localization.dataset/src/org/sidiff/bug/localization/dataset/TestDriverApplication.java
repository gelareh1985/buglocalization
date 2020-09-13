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

public class TestDriverApplication extends RetrievalApplication {

	@Override
	protected void start(Path datasetPath, DataSet dataset, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {		
		String codeRepositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataset.getName());
		
		// Bug fixes:
		{
			BugFixHistoryRetrievalProvider bugHistoryRetrievalProvider = new BugFixHistoryRetrievalProvider(
					codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataset.getBugtrackerProduct());
			BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugHistoryRetrievalProvider, dataset, datasetPath);
			bugFixHistory.retrieveHistory();
			
			{
				shrinkHistory(bugFixHistory.getDataset(), 2);
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
//				filterProjects(javaModelRetrievalProvider, "org.eclipse.jdt.core", "org.eclipse.jdt.apt.core");
			}
			
			JavaModelRetrieval javaModel = new JavaModelRetrieval(javaModelRetrievalProvider, dataset, datasetPath);
			javaModel.retrieve();
			javaModel.saveDataSet();
		}
		
		// System model:
		{
			SystemModelRetrievalProvider systemModelRetrievalProvider = new SystemModelRetrievalProvider();
			SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelRetrievalProvider, codeRepositoryPath);
			systemModel.retrieve();
			systemModel.saveDataSet();
		}
	}

	@SuppressWarnings("unused")
	private void filterProjects(JavaModelRetrievalProvider provider, String... projectNames) {
		ProjectFilter parentProjectFilter = provider.createProjectFilter();
		provider.setProjectFilter(() -> new ProjectNameFilter(
				parentProjectFilter, new HashSet<>(Arrays.asList(projectNames))));
	}

	private void shrinkHistory(DataSet dataset, int historySize) {
		dataset.getHistory().setVersions(dataset.getHistory().getVersions().subList(0, historySize));
	}
	
}
