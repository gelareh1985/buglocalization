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

	// Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json" -bughistory -javamodelhistory -systemmodelhistory
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_CONFIGURATION = "-retrieval";
	
	public static final String ARGUMENT_BUG_HISTORY = "-bughistory";
	
	public static final String ARGUMENT_JAVA_MODEL_HISTORY = "-javamodelhistory";
	
	public static final String ARGUMENT_SYSTEM_MODEL_HISTORY = "-systemmodelhistory";
	
	/**
	 * Phase 01: Retrieves the bug fixes from the Git repository and the bug reports from the bug tracker.
	 */
	private boolean retrieveBugFixHistory = true;
	
	/**
	 * Phase 02: Retrieves the history of (MoDisco) Java models from the Java code
	 * projects. If the phase 01 is not calculated within one run, the data set file
	 * path need to be updated in the program argument to the output file of phase 01.
	 */
	private boolean retrieveJavaModelHistory = true;
	
	/**
	 * Phase 03: Retrieves the system model (UML) from the (MoDisco) Java models.
	 */
	private boolean retrieveSystemModelHistory = true;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Path dataSetPath = getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		Path retrievalConfigurationPath = getPathFromProgramArguments(context, ARGUMENT_CONFIGURATION);
		RetrievalConfiguration retrievalConfiguration = JsonUtil.parse(retrievalConfigurationPath, RetrievalConfiguration.class);
		
		this.retrieveBugFixHistory = containsProgramArgument(context, ARGUMENT_BUG_HISTORY);
		this.retrieveJavaModelHistory = containsProgramArgument(context, ARGUMENT_JAVA_MODEL_HISTORY);
		this.retrieveSystemModelHistory = containsProgramArgument(context, ARGUMENT_SYSTEM_MODEL_HISTORY);
		
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
	
	private boolean containsProgramArgument(IApplicationContext context, String argumentName) throws FileNotFoundException {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(argumentName)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void start(Path dataSetPath, DataSet dataSet, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {
		String codeRepositoryURL = dataSet.getRepositoryHost() + dataSet.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataSet.getName());
		
		// Bug fixes:
		if (retrieveBugFixHistory) {
			BugFixHistoryRetrievalProvider bugFixHistoryConfig = new BugFixHistoryRetrievalProvider(
					codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataSet.getBugtrackerProduct());
			BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugFixHistoryConfig, dataSet, dataSetPath);
			bugFixHistory.retrieve();
			bugFixHistory.saveDataSet();
			
			// update data set path to output file:
			dataSetPath = bugFixHistory.getDatasetPath();
		}
		
		// Java model:
		if (retrieveJavaModelHistory) {
			JavaModelRetrievalProvider javaModelFactory = new JavaModelRetrievalProvider(codeRepositoryPath);
			JavaModelRetrieval javaModel = new JavaModelRetrieval(javaModelFactory, dataSetPath);
			javaModel.retrieve();
			javaModel.saveDataSet();
		}
		
		// System model:
		if (retrieveSystemModelHistory) {
			SystemModelRetrievalProvider systemModelFactory = new SystemModelRetrievalProvider();
			SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelFactory, codeRepositoryPath);
			systemModel.retrieve();
			systemModel.saveDataSet();
		}
	}

	@Override
	public void stop() {
	}

	public boolean isRetrieveBugFixHistory() {
		return retrieveBugFixHistory;
	}

	public void setRetrieveBugFixHistory(boolean retrieveBugFixHistory) {
		this.retrieveBugFixHistory = retrieveBugFixHistory;
	}

	public boolean isRetrieveJavaModelHistory() {
		return retrieveJavaModelHistory;
	}

	public void setRetrieveJavaModelHistory(boolean retrieveJavaModelHistory) {
		this.retrieveJavaModelHistory = retrieveJavaModelHistory;
	}

	public boolean isRetrieveSystemModelHistory() {
		return retrieveSystemModelHistory;
	}

	public void setRetrieveSystemModelHistory(boolean retrieveSystemModelHistory) {
		this.retrieveSystemModelHistory = retrieveSystemModelHistory;
	}
}
