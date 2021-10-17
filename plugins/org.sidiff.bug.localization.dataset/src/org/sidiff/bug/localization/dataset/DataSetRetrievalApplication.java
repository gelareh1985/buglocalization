package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.common.utilities.workspace.ApplicationUtil;
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

/**
 * Converts a Java project including bug reports into an UML model.
 */
public class DataSetRetrievalApplication implements IApplication {

	/*
	 *  Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json" -bughistory -workspacehistory -systemmodelhistory
	 */
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_DATASET_TRACE = "-datasettrace";
	
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
		DataSet dataSetTrace = null;
		
		if (ApplicationUtil.containsProgramArgument(context, ARGUMENT_DATASET_TRACE)) {
			Path dataSetTracePath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET_TRACE);
			dataSetTrace = DataSetStorage.load(dataSetTracePath);
		}
		
		Path retrievalConfigurationPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_CONFIGURATION);
		RetrievalConfiguration retrievalConfiguration = JsonUtil.parse(retrievalConfigurationPath, RetrievalConfiguration.class);
		
		this.retrieveBugFixHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_BUG_HISTORY);
		this.retrieveWorkspaceHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_WORKSPACE_HISTORY);
		this.retrieveSystemModelHistory = ApplicationUtil.containsProgramArgument(context, ARGUMENT_SYSTEM_MODEL_HISTORY);
		
		start(dataSetPath, dataSet, dataSetTrace, retrievalConfigurationPath, retrievalConfiguration);
		
		return IApplication.EXIT_OK;
	}
	
	protected void start(Path datasetPath, DataSet dataset, DataSet dataSetTrace, Path retrievalConfigurationPath, RetrievalConfiguration retrievalConfiguration) throws IOException {
		String codeRepositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		Path codeRepositoryPath = Paths.get(retrievalConfiguration.getLocalRepositoryPath().toString(), dataset.getName());
		
		try {
			enableAutoBuild(false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		// Bug fixes:
		if (retrieveBugFixHistory) {
			String datasetFileName = dataset.getName() + "_dataset_bughistory_" + LocalDate.now() + ".json";
			datasetPath = datasetPath.getParent().resolve(datasetFileName);
			
			BugFixHistoryRetrievalProvider bugFixHistoryConfig = new BugFixHistoryRetrievalProvider(
					codeRepositoryURL, codeRepositoryPath, () -> new EclipseBugzillaBugtracker(), dataset.getBugtrackerProducts());
			BugFixHistoryRetrieval bugFixHistory = new BugFixHistoryRetrieval(bugFixHistoryConfig, dataset, datasetPath);
			
			try {
				bugFixHistory.retrieve();
			} finally {
				bugFixHistory.saveDataSet();
				
				// update data set path to output file:
				datasetPath = bugFixHistory.getDatasetPath();
			}
		}
		
		// Workspace:
		if (retrieveWorkspaceHistory) {
			String datasetFileName = dataset.getName() + "_dataset_workspacehistory_" + LocalDate.now() + ".json";
			datasetPath = datasetPath.getParent().resolve(datasetFileName);
			
			WorkspaceHistoryRetrievalProvider workspaceHistoryRetrievalProvider = new WorkspaceHistoryRetrievalProvider(codeRepositoryPath);
			WorkspaceHistoryRetrieval workspaceHistoryRetrieval = new WorkspaceHistoryRetrieval(workspaceHistoryRetrievalProvider, dataset, datasetPath);
			
			try {
				workspaceHistoryRetrieval.retrieve();
			} finally {
				workspaceHistoryRetrieval.saveDataSet();
				
				// update data set path to output file
				datasetPath = workspaceHistoryRetrieval.getDatasetPath();
			}
		}
		
		// TODO: To run all phases in once, we should clear the workspace at this point, i.e., at least remove all existing projects. 
		
		// Direct System model (Java Model -> System Model):
		if (retrieveSystemModelHistory) {
			String datasetFileName = dataset.getName() + "_dataset_systemmodel_" + LocalDate.now() + ".json";
			datasetPath = datasetPath.getParent().resolve(datasetFileName);
			boolean includeMethodBodies = false;
			
			SystemModelRetrievalProvider systemModelProvider = new SystemModelRetrievalProvider(
					codeRepositoryPath, dataset.getProjectNameFilter(), dataset.getProjectPathFilter(), includeMethodBodies);
			SystemModelRetrieval systemModelRetrieval = new SystemModelRetrieval(systemModelProvider, datasetPath, dataset, dataSetTrace);
			
			try {
//				// Resume on last intermediate save:
//				// (1) Uncomment resume code and comment systemModelRetrieval.retrieve(); 
//				// (2) Copy last commit <Model Repo> from latest data set checkpoint file name to resume(...): DataSet_<Timestamo>.json_<Version Counter>_<Code Repo>_<Model Repo>
//				// (3) To avoid long path names rename data set checkpoint file to DataSet_checkpoint.json
//				// (4) Reset mode Git repository to <Model Repo> commit
//				// (5) Restart application 
//				{
//					datasetPath = Paths.get(datasetPath.getParent().toString(), "DataSet_checkpoint.json");
//					dataset = DataSetStorage.load(datasetPath);
//					
//					systemModelRetrieval = new SystemModelRetrieval(systemModelProvider, dataset, datasetPath);
//					
//					//// modelDatasetToCodeDataset(dataset);
//					int resume = resume(dataset, "0e32179056318498bab8548c6d40017d6c717dfd");
//					systemModelRetrieval.retrieve(resume);
//				}
				
				systemModelRetrieval.retrieve();
			} finally {
				systemModelRetrieval.saveDataSet();
				
				// update data set path to output file
				datasetPath = systemModelRetrieval.getDatasetPath();
			}
		}
		
		Activator.getLogger().log(Level.INFO, "Retrieval Finished");
		Activator.getLogger().log(Level.INFO, "To optimize disc space run: git gc --auto");
	}
	
	public static boolean enableAutoBuild(boolean enable) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		boolean isAutoBuilding = desc.isAutoBuilding();
		
		if (isAutoBuilding != enable) {
			desc.setAutoBuilding(enable);
			workspace.setDescription(desc);
		}
		return isAutoBuilding;
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
	
	@SuppressWarnings("unused")
	private void modelDatasetToCodeDataset(DataSet dataset) {
		for (Version version : dataset.getHistory().getVersions()) {
			version.setIdentification(version.getIdentificationTrace());
			version.setIdentificationTrace(null);
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
