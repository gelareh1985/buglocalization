package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Path;

import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.RetrievalProcess;

public class TestDriverApplication extends RetrievalApplication {

	@Override
	protected void start(Path dataSetPath, DataSet dataSet, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {
		RetrievalProcess retrievalProcess = new RetrievalProcess(retrievalConfiguration, dataSet, dataSetPath);
		
		retrievalProcess.retrieveHistory();
		retrievalProcess.getDataset().getHistory().setVersions(
				retrievalProcess.getDataset().getHistory().getVersions().subList(0, 2));
		retrievalProcess.retrieveBugReports();
		retrievalProcess.cleanUp(); // TODO: Split Data Set by Placeholders
//		retrievalProcess.saveDataSet(); // TODO: multi view dummy
		
		retrievalProcess.retrieveJavaAST();
//		retrievalProcess.saveDataSet(); // TODO: multi view dummy
		
 		retrievalProcess.retrieveSystemModels();
		
	}
	
}
