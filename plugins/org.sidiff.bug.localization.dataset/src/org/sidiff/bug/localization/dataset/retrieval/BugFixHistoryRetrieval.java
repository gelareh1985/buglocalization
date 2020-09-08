package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.fixes.report.request.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.fixes.report.request.placeholders.BugReportPlaceholder;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;

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
		retrieveBugFixChanges();
		retrieveBugReports();
		cleanUp(dataset);
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

	public void retrieveBugFixChanges() {
		for (Version version : (Iterable<Version>) () -> getBugFixes()) {
			List<FileChange> fixChanges = codeRepository.getChanges(version);
			version.setChanges(fixChanges);
		}
	}

	public void retrieveBugReports() {
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(
				factory.createBugtracker(), 
				factory.createBugReportFilter(), 
				factory.createBugFixMessageIDMatcher());
		bugReportRequestsExecutor.request(getBugFixes());
		bugReportRequestsExecutor.setPlaceholders();
	}

	public static void cleanUp(DataSet dataset) {
		for (Iterator<Version> iterator = dataset.getHistory().getVersions().iterator(); iterator.hasNext();) {
			Version version = iterator.next();
			
			if (version.getBugReport() instanceof BugReportPlaceholder) {
				if (Activator.getLogger().isLoggable(Level.WARNING)) {
					Activator.getLogger().log(Level.WARNING,
							"Version with bug report removed (" + version.getBugReport() + "):" + version);
				}
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
	
	public void saveDataSet() {
		try {
			this.datasetPath = DataSetStorage.save(datasetPath, dataset, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Iterator<Version> getBugFixes() {
		// visible == commit message contains bug report ID
		// !visible == version retained as previous revision of a bug fix
		return dataset.getHistory().getVersions().stream().filter(Version::isVisible).iterator();
	}
}
