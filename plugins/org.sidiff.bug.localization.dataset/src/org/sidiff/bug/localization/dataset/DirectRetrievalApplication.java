package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
import org.sidiff.bug.localization.dataset.retrieval.DirectSystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.JavaModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrieval;
import org.sidiff.bug.localization.dataset.retrieval.WorkspaceHistoryRetrievalProvider;

public class DirectRetrievalApplication implements IApplication {

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
		
		Path dataSetPath = getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		Path retrievalConfigurationPath = getPathFromProgramArguments(context, ARGUMENT_CONFIGURATION);
		RetrievalConfiguration retrievalConfiguration = JsonUtil.parse(retrievalConfigurationPath, RetrievalConfiguration.class);
		
		this.retrieveBugFixHistory = containsProgramArgument(context, ARGUMENT_BUG_HISTORY);
		this.retrieveWorkspaceHistory = containsProgramArgument(context, ARGUMENT_WORKSPACE_HISTORY);
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
		
//		List<Version> originalHistory = reduceToHistoryChunk(dataset, 0, 1000);
		
		// Java model:
		if (retrieveSystemModelHistory) {
			JavaModelRetrievalProvider javaModelProvider = new JavaModelRetrievalProvider(codeRepositoryPath);
			SystemModelRetrievalProvider systemModelProvider = new SystemModelRetrievalProvider();
			
			DirectSystemModelRetrieval javaModel = new DirectSystemModelRetrieval(javaModelProvider, systemModelProvider, dataset, datasetPath);
			
			try {
//				javaModel.retrieve();
				try {
					dataset = DataSetStorage.load(Paths.get("C:\\Users\\manue\\git\\buglocalization\\research\\org.sidiff.bug.localization.dataset.domain.eclipse\\datasets\\eclipse.jdt.core\\"
							+ "DataSet_20200914170431_20200914201948.json_1800_ee27da3913a0be1d36796f232a0aef2f2ab51540_e6c85345366d4cbd0288513afcee0881e1305fb0"));
					javaModel.retrieve(resume(dataset, "e6c85345366d4cbd0288513afcee0881e1305fb0"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
//				dataset.getHistory().setVersions(originalHistory);
				javaModel.saveDataSet();
			}
		}
		
		// System model:
		if (retrieveSystemModelHistory) {
			SystemModelRetrievalProvider systemModelFactory = new SystemModelRetrievalProvider();
			SystemModelRetrieval systemModel = new SystemModelRetrieval(systemModelFactory, codeRepositoryPath);
			
			try {
				systemModel.retrieve();
			} finally {
				systemModel.saveDataSet();
			}
		}
		
		Activator.getLogger().log(Level.INFO, "Retrieval Finished");
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

	/**
	 * @param dataset   The data set to be cut.
	 * @param chunk     Chunk number, counting from 0.
	 * @param chunkSize Constant size of the chunks.
	 * @return The original history.
	 */
	@SuppressWarnings("unused")
	private List<Version> reduceToHistoryChunk(DataSet dataset, int chunk, int chunkSize) {
		
		// low endpoint (inclusive) of the history:
		int fromVersion = dataset.getHistory().getVersions().size() - ((chunk + 1) * chunkSize); 
		fromVersion = fromVersion >= 0 ? fromVersion : 0;
		
		// high endpoint (exclusive) of the history
		int toVersion = dataset.getHistory().getVersions().size() - (chunk * chunkSize);

		List<Version> originalHistory = dataset.getHistory().getVersions();
		
		if (toVersion > 0) {
			List<Version> historyChunk = dataset.getHistory().getVersions().subList(fromVersion, toVersion);
			dataset.getHistory().setVersions(historyChunk);
		} else {
			dataset.getHistory().setVersions(Collections.emptyList());
			
			if (Activator.getLogger().isLoggable(Level.SEVERE)) {
				Activator.getLogger().log(Level.SEVERE, "No more history chunk for number: " + chunk);
			}
		}
		
		return originalHistory;
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
