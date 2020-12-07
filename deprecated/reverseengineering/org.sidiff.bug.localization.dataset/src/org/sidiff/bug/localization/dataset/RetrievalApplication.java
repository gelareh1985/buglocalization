package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.BugFixHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;

/**
 * Converts a Java project including bug reports into an MoDisco model.
 * 
 * @deprecated Use {@link DirectRetrievalApplication}
 */
@Deprecated
public class RetrievalApplication implements IApplication {

	/*
	 *  Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json" -bughistory -workspacehistory -javamodelhistory -systemmodelhistory
	 */
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_CONFIGURATION = "-retrieval";
	
	public static final String ARGUMENT_BUG_HISTORY = "-bughistory";
	
	public static final String ARGUMENT_WORKSPACE_HISTORY = "-workspacehistory";
	
	public static final String ARGUMENT_JAVA_MODEL_HISTORY = "-javamodelhistory";
	
	public static final String ARGUMENT_SYSTEM_MODEL_HISTORY = "-systemmodelhistory";
	
	/**
	 * Phase 01: Retrieves the bug fixes from the Git repository and the bug reports from the bug tracker.
	 */
	private boolean retrieveBugFixHistory = true;
	
	/**
	 * Phase 02: Retrieves the history of project in the workspace and whether the
	 * content of a project was since the last version.
	 */
	private boolean retrieveWorkspaceHistory = true;
	
	/**
	 * Phase 03: Retrieves the history of (MoDisco) Java models from the Java code
	 * projects. If the phase 01 is not calculated within one run, the data set file
	 * path need to be updated in the program argument to the output file of phase 01.
	 */
	private boolean retrieveJavaModelHistory = true;
	
	/**
	 * Phase 04: Retrieves the system model (UML) from the (MoDisco) Java models.
	 */
	private boolean retrieveSystemModelHistory = true;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Path dataSetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		Path retrievalConfigurationPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_CONFIGURATION);
		RetrievalConfiguration retrievalConfiguration = JsonUtil.parse(retrievalConfigurationPath, RetrievalConfiguration.class);
		
		this.retrieveBugFixHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_BUG_HISTORY);
		this.retrieveWorkspaceHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_WORKSPACE_HISTORY);
		this.retrieveJavaModelHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_JAVA_MODEL_HISTORY);
		this.retrieveSystemModelHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_SYSTEM_MODEL_HISTORY);
		
		start(dataSetPath, dataSet, retrievalConfigurationPath, retrievalConfiguration);
		
		return IApplication.EXIT_OK;
	}
	
	protected void start(Path datasetPath, DataSet dataset, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {
		String codeRepositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataset.getName());
		
		// Bug fixes:
		if (retrieveBugFixHistory) {
			BugFixHistoryRetrievalProvider bugFixHistoryConfig = new BugFixHistoryRetrievalProvider(
					codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataset.getBugtrackerProducts());
			BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugFixHistoryConfig, dataset, datasetPath);
			
			try {
				bugFixHistory.retrieve();
			} finally {
				bugFixHistory.saveDataSet();
			}
			
			// update data set path to output file:
			datasetPath = bugFixHistory.getDatasetPath();
		}
		
		// Workspace:
		if (retrieveWorkspaceHistory) {
			WorkspaceHistoryRetrievalProvider workspaceHistoryRetrievalProvider = new WorkspaceHistoryRetrievalProvider(codeRepositoryPath);
			WorkspaceHistoryRetrieval workspaceHistoryRetrieval = new WorkspaceHistoryRetrieval(workspaceHistoryRetrievalProvider, dataset, datasetPath);
			
			try {
				workspaceHistoryRetrieval.retrieve();
			} finally {
				workspaceHistoryRetrieval.saveDataSet();
			}
			
			// update data set path to output file
			datasetPath = workspaceHistoryRetrieval.getDatasetPath();
		}
		
		// Java model:
		if (retrieveJavaModelHistory) {
			JavaModelRetrievalProvider javaModelFactory = new JavaModelRetrievalProvider(codeRepositoryPath);
			JavaModelRetrieval javaModelRetrieval = new JavaModelRetrieval(javaModelFactory, dataset, datasetPath);
			
			try {
				javaModelRetrieval.retrieve();
				
				// Resume on last intermediate save:
//				{
//					datasetPath = Paths.get(datasetPath.getParent().toString(),
//							"DataSet_20200914170431_20200914201948.json_8400_509692f4edb0ce705fd505934a81ec54e8a7a49f_5616ba0159b1fbae6dcb9169f80ccbb2bf230ea6");
//					dataset = DataSetStorage.load(datasetPath);
//					javaModelRetrieval = new JavaModelRetrieval(javaModelFactory, dataset, datasetPath);
//					systemModelRetrieval.retrieve(resume(dataset, "5616ba0159b1fbae6dcb9169f80ccbb2bf230ea6"));
//				}
			} finally {
				javaModelRetrieval.saveDataSet();
			}
		}
		
		// System model:
		if (retrieveSystemModelHistory) {
			SystemModelRetrievalProvider systemModelFactory = new SystemModelRetrievalProvider();
			SystemModelRetrieval systemModelRetrieval = new SystemModelRetrieval(systemModelFactory, codeRepositoryPath, dataset, datasetPath);
			
			try {
				systemModelRetrieval.retrieve();
				
				// Resume on last intermediate save:
//				{
//					datasetPath = Paths.get(datasetPath.getParent().toString(),
//							"DataSet_20200914170431_20200914201948.json_8400_509692f4edb0ce705fd505934a81ec54e8a7a49f_5616ba0159b1fbae6dcb9169f80ccbb2bf230ea6");
//					dataset = DataSetStorage.load(datasetPath);
//					systemModelRetrieval = new SystemModelRetrieval(systemModelFactory, codeRepositoryPath, dataset, datasetPath);
//					systemModelRetrieval.retrieve(resume(dataset, "5616ba0159b1fbae6dcb9169f80ccbb2bf230ea6"));
//				}
			} finally {
				systemModelRetrieval.saveDataSet();
			}
		}
		
		Activator.getLogger().log(Level.INFO, "Retrieval Finished");
		Activator.getLogger().log(Level.INFO, "To optimize disc space run: git gc --auto");
	}
	
	@SuppressWarnings("unused")
	private int resume(DataSet dataset, String versionID) {
		List<Version> originalHistory = dataset.getHistory().getVersions();
		int resumeIndex = -1;
		
		for (Version version : originalHistory) {
			if (version.getIdentification().equals(versionID)) {
				resumeIndex = originalHistory.indexOf(version);
			}
		}
		
		return resumeIndex;
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
	
	public boolean isRetrieveWorkspaceHistory() {
		return retrieveWorkspaceHistory;
	}
	
	public void setRetrieveWorkspaceHistory(boolean retrieveWorkspaceHistory) {
		this.retrieveWorkspaceHistory = retrieveWorkspaceHistory;
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
