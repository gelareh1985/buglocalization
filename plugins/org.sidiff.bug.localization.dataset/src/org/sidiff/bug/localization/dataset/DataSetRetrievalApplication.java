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
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;

/**
 * Converts a Java project including bug reports into an UML model (via MoDisco and ATL transformation).
 */
public class DataSetRetrievalApplication implements IApplication {

	/*
	 *  Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json" -bughistory -workspacehistory -systemmodelhistory
	 */
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_CONFIGURATION = "-retrieval";
	
	public static final String ARGUMENT_BUG_HISTORY = "-bughistory";
	
	public static final String ARGUMENT_WORKSPACE_HISTORY = "-workspacehistory";
	
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
	 * Phase 03: Retrieves the system model (UML) from the (MoDisco) Java models.
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
		
		// Direct System model (Java Model -> System Model):
		if (retrieveSystemModelHistory) {
			SystemModelRetrievalProvider systemModelProvider = new SystemModelRetrievalProvider(codeRepositoryPath);
			SystemModelRetrieval systemModelRetrieval = new SystemModelRetrieval(systemModelProvider, dataset, datasetPath);
			
			try {
//				// Resume on last intermediate save:
//				{
//					datasetPath = Paths.get(datasetPath.getParent().toString(),
//							"DataSet_20201120213103_20201121033529.json_3800_b5c1652db351290a42a75d3cdd3241441a4413e2_0e32179056318498bab8548c6d40017d6c717dfd");
//					dataset = DataSetStorage.load(datasetPath);
//					
//					systemModelRetrieval = new SystemModelRetrieval(systemModelProvider, dataset, datasetPath);
//					systemModelRetrieval.retrieve(resume(dataset, "0e32179056318498bab8548c6d40017d6c717dfd"));
//				}
				
				systemModelRetrieval.retrieve();
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

	public boolean isRetrieveSystemModelHistory() {
		return retrieveSystemModelHistory;
	}

	public void setRetrieveSystemModelHistory(boolean retrieveSystemModelHistory) {
		this.retrieveSystemModelHistory = retrieveSystemModelHistory;
	}
}
