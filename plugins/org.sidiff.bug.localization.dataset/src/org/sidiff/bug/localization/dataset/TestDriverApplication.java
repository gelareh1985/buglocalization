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
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrievalFactory;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrievalFactory;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalFactory;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectNameFilter;

public class TestDriverApplication extends RetrievalApplication {

	@Override
	protected void start(Path dataSetPath, DataSet dataSet, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {		
		String codeRepositoryURL = dataSet.getRepositoryHost() + dataSet.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataSet.getName());
		
		// Bug fixes:
		BugFixHistoryRetrievalFactory bugFixHistoryConfig = new BugFixHistoryRetrievalFactory(
				codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataSet.getBugtrackerProduct());
		BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugFixHistoryConfig, dataSet, dataSetPath);
		bugFixHistory.retrieveHistory();
		
		{
			shrinkHistory(bugFixHistory.getDataset(), 4);
		}
		
		bugFixHistory.retrieveBugFixChanges();
		bugFixHistory.retrieveBugReports();
		BugFixHistoryRetrieval.cleanUp(bugFixHistory.getDataset());
		bugFixHistory.saveDataSet();
		
		// Java model:
		JavaModelRetrievalFactory javaModelFactory = new JavaModelRetrievalFactory(bugFixHistory.getCodeRepositoryPath());
		
		{
			filterProjects(javaModelFactory, "org.eclipse.jdt.core");
			filterProjects(javaModelFactory, "org.eclipse.jdt.apt.core");
		}
		
		JavaModelRetrieval javaModel = new JavaModelRetrieval(javaModelFactory, bugFixHistory.getDatasetPath());
		javaModel.retrieve();
		javaModel.saveDataSet();
		
		// System model:
		SystemModelRetrievalFactory systemModelFactory = new SystemModelRetrievalFactory();
		SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelFactory, bugFixHistory.getCodeRepositoryPath());
		systemModel.retrieve();
		systemModel.saveDataSet();
	}

	private void filterProjects(JavaModelRetrievalFactory javaModelFactory, String... projectNames) {
		ProjectFilter parentProjectFilter = javaModelFactory.createProjectFilter();
		javaModelFactory.setProjectFilter(() -> new ProjectNameFilter(
				parentProjectFilter, new HashSet<>(Arrays.asList(projectNames))));
	}

	private void shrinkHistory(DataSet dataset, int historySize) {
		dataset.getHistory().setVersions(dataset.getHistory().getVersions().subList(0, historySize));
	}
	
}
