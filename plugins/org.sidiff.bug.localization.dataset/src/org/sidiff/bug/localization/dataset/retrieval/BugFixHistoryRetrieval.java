package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.fixes.report.request.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.fixes.report.request.placeholders.BugReportPlaceholder;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;

public class BugFixHistoryRetrieval {

	private BugFixHistoryRetrievalFactory factory; 

	private DataSet dataset;
	
	private Path datasetPath;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	public BugFixHistoryRetrieval(BugFixHistoryRetrievalFactory factory, DataSet dataset, Path datasetStorage) {
		this.factory = factory;
		this.dataset = dataset;
		this.datasetPath = datasetStorage;
	}
	
	public void retrieve() {
		retrieveHistory();
		retrieveBugReports();
		cleanUp(dataset);
		
		try {
			saveDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void retrieveHistory() {
		retrieveRepository();
		retrieveBugFixes();
	}

	private Repository retrieveRepository() {
		this.codeRepository = factory.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		return codeRepository;
	}

	private void retrieveBugFixes() {
		
		// Retrieve commits with bug fixes in their comments:
		History history = codeRepository.getHistory(factory.createVersionFilter());
		dataset.setHistory(history);
	}

	public void retrieveBugReports() {
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(
				factory.createBugtracker(), factory.createBugReportFilter(), factory.createBugFixMessageIDMatcher());
		bugReportRequestsExecutor.request(dataset.getHistory().getVersions());
		bugReportRequestsExecutor.setPlaceholders();
	}

	public static void cleanUp(DataSet dataset) {
		for (Iterator<Version> iterator = dataset.getHistory().getVersions().iterator(); iterator.hasNext();) {
			Version version = iterator.next();
			
			if (version.getBugReport() instanceof BugReportPlaceholder) {
				iterator.remove();
			}
		}
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getDatasetPath() {
		return datasetPath;
	}
	
	public DataSet getDataset() {
		return dataset;
	}
	
	public void saveDataSet() throws IOException {
		JsonUtil.save(dataset, datasetPath);
	}
}
