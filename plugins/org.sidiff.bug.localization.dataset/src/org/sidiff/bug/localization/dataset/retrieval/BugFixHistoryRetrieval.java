package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.fixes.report.request.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.fixes.report.request.DiscardedBugReports;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;

public class BugFixHistoryRetrieval {

	private BugFixHistoryRetrievalProvider provider; 

	private DataSet dataset;
	
	private Path datasetPath;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	private DiscardedBugReports discardedBugReports;
	
	public BugFixHistoryRetrieval(BugFixHistoryRetrievalProvider provider, DataSet dataset, Path datasetStorage) {
		this.provider = provider;
		this.dataset = dataset;
		this.datasetPath = datasetStorage;
	}
	
	public void retrieve() {
		retrieveHistory();
		retrieveBugReports();
		retrieveBugFixLocations();
		cleanUpHistory();
	}
	
	public void retrieveHistory() {
		retrieveRepository();
		retrieveBugFixes();
	}

	private Repository retrieveRepository() {
		this.codeRepository = provider.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		return codeRepository;
	}

	private void retrieveBugFixes() {
		
		// Retrieve commits with bug fixes in their comments:
		History history = codeRepository.getHistory(provider.createVersionFilter());
		dataset.setHistory(history);
	}

	public void retrieveBugFixLocations() {
		for (Version version : dataset.getHistory().getVersions()) {
			if (version.hasBugReport()) {
				List<FileChange> fixChanges = codeRepository.getChanges(version, true);
				version.getBugReport().setBugLocations(fixChanges);
			}
		}
	}

	public void retrieveBugReports() {
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(
				provider.createBugtracker(), 
				provider.createBugReportFilter(), 
				provider.createBugFixMessageIDMatcher());
		bugReportRequestsExecutor.request(getBugFixes());
		this.discardedBugReports = bugReportRequestsExecutor.getDiscardedBugReports();
		
		if (Activator.getLogger().isLoggable(Level.INFO)) {
			Activator.getLogger().log(Level.INFO, "Bug reports filtered for " + discardedBugReports.getFilteredReports().size() + " bug fixes.");
			Activator.getLogger().log(Level.INFO, "No bug reports found for " + discardedBugReports.getNoReports().size() + " bug fixes.");
			Activator.getLogger().log(Level.INFO, "Bug report request failed for " + discardedBugReports.getMissingReports().size() + " bug fixes.");
		}
	}

	public void cleanUpHistory() {
		
		// NOTE: isVisible() -> true -> version is considered as bug fix
		//       isVisible() -> false -> version is version previous to a bug fix
		
		for (Version missingReport : discardedBugReports.getMissingReports()) {
			if (Activator.getLogger().isLoggable(Level.SEVERE)) {
				Activator.getLogger().log(Level.SEVERE,
						"Version >> Bug Report Request Failed <<: " + missingReport);
			}
			missingReport.setVisible(false);
			missingReport.setBugReport(null);
		}
		
		for (Version noReport : discardedBugReports.getNoReports()) {
			if (Activator.getLogger().isLoggable(Level.WARNING)) {
				Activator.getLogger().log(Level.WARNING,
						"Version >> No Bug Report Found <<: " + noReport);
			}
			noReport.setVisible(false);
			noReport.setBugReport(null);
		}
		
		for (Version filteredReport : discardedBugReports.getFilteredReports()) {
			if (Activator.getLogger().isLoggable(Level.WARNING)) {
				Activator.getLogger().log(Level.WARNING,
						"Version >> Filtered Bug Report <<: " + filteredReport + ", Bug Report [summary: " + filteredReport.getBugReport().getSummary() + "]");
			}
			filteredReport.setVisible(false);
			filteredReport.setBugReport(null);
		}
		
		// Remove not needed intermediate versions:
		List<Version> versions = dataset.getHistory().getVersions();
		Set<Version> toBeRemoved = new LinkedHashSet<>();
		
		// Iterate from old to new versions:
		for (int i = versions.size(); i-- > 0;) {
			Version version = versions.get(i);
			Version newerVersion = (i > 0) ? versions.get(i - 1) : null;
			
			version.setVisible(true);
		
			if (!version.hasBugReport()) {
				if (newerVersion != null) {
					if (!newerVersion.hasBugReport()) {
						toBeRemoved.add(version);
					}
				} else {
					toBeRemoved.add(version);
				}
			}
		}

		versions.removeAll(toBeRemoved);
		
		if (Activator.getLogger().isLoggable(Level.FINE)) {
			for (Version version : toBeRemoved) {
				Activator.getLogger().log(Level.FINE, "Version Discarded: " + version);
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
			dataset.createTimestamp();
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
