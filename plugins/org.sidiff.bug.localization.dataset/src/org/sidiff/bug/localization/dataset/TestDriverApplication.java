package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Path;

import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;

public class TestDriverApplication extends RetrievalApplication {

	@Override
	protected void start(Path dataSetPath, DataSet dataSet, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {		
		BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(retrievalConfiguration, dataSet, dataSetPath);
		bugFixHistory.retrieveHistory();
		
		shrinkHistory(bugFixHistory.getDataset(), 3);
		
		bugFixHistory.retrieveBugReports();
		BugFixHistoryRetrieval.cleanUp(bugFixHistory.getDataset());
		bugFixHistory.saveDataSet();
		
		JavaModelRetrieval javaModel = new JavaModelRetrieval(bugFixHistory.getDatasetPath(), bugFixHistory.getCodeRepositoryPath());
		javaModel.retrieve();
		
		SystemModelRetrieval systemModel = new SystemModelRetrieval(bugFixHistory.getCodeRepositoryPath());
		systemModel.retrieve();
	}

	private void shrinkHistory(DataSet dataset, int historySize) {
		dataset.getHistory().setVersions(dataset.getHistory().getVersions().subList(0, historySize));
	}
	
}
