package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;

public class RetrievalApplication implements IApplication {

	// Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json"
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_CONFIGURATION = "-retrieval";
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Path dataSetPath = getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		Path retrievalConfigurationPath = getPathFromProgramArguments(context, ARGUMENT_CONFIGURATION);
		RetrievalConfiguration retrievalConfiguration = JsonUtil.parse(retrievalConfigurationPath, RetrievalConfiguration.class);
		
		start(dataSetPath, dataSet, retrievalConfigurationPath, retrievalConfiguration);
		
		return IApplication.EXIT_OK;
	}

	private Path getPathFromProgramArguments(IApplicationContext context, String argumentName) throws FileNotFoundException {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(argumentName)) {
				Path dataSetPath = Paths.get(args[i + 1]);
				
				if (!Files.exists(dataSetPath)) {
					throw new FileNotFoundException(args[i + 1]);
				}
				
				return dataSetPath;
			}
		}
		
		throw new FileNotFoundException("Program argument '" + argumentName + "' not specified.");
	}
	
	protected void start(Path dataSetPath, DataSet dataSet, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {
		String codeRepositoryURL = dataSet.getRepositoryHost() + dataSet.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataSet.getName());
		
		// Bug fixes:
		BugFixHistoryRetrievalProvider bugFixHistoryConfig = new BugFixHistoryRetrievalProvider(
				codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataSet.getBugtrackerProduct());
		BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugFixHistoryConfig, dataSet, dataSetPath);
		bugFixHistory.retrieve();
		bugFixHistory.saveDataSet();
		
		// Java model:
		JavaModelRetrievalProvider javaModelFactory = new JavaModelRetrievalProvider(bugFixHistory.getCodeRepositoryPath());
		JavaModelRetrieval javaModel = new JavaModelRetrieval(javaModelFactory, bugFixHistory.getDatasetPath());
		javaModel.retrieve();
		javaModel.saveDataSet();
		
		// System model:
		SystemModelRetrievalProvider systemModelFactory = new SystemModelRetrievalProvider();
		SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelFactory, bugFixHistory.getCodeRepositoryPath());
		systemModel.retrieve();
		systemModel.saveDataSet();
	}

	@Override
	public void stop() {
	}

}
