package org.sidiff.bug.localization.dataset.database.systemmodel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jUtil;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;

public class ModelHistory2Neo4j {
	
	public static boolean LOGGING = true; 
	
	public static boolean PROFILE = true;
	
	private Repository modelRepository;
	
	private Neo4jTransaction transaction;
	
	private int restartWithVersion = -1;
	
	private String restartWithGitVersion = null;
	
	private boolean startWithFullVersion = true;
	
	private String stopOnGitVersion = null;
	
	private int reopenSession = 10; // prevent resource leaks...
	
	private boolean onlyBuggyVersions = false;
	
	public ModelHistory2Neo4j(Repository modelRepository, Neo4jTransaction transaction) {
		this.modelRepository = modelRepository;
		this.transaction = transaction;
	}
	
	public int getRestartWithVersion() {
		return restartWithVersion;
	}

	public void setRestartWithVersion(int restartWithVersion) {
		this.restartWithVersion = restartWithVersion;
	}

	public String getRestartWithGitVersion() {
		return restartWithGitVersion;
	}

	public void setRestartWithGitVersion(String restartWithGitVersion) {
		this.restartWithGitVersion = restartWithGitVersion;
	}

	public boolean isStartWithFullVersion() {
		return startWithFullVersion;
	}

	public void setStartWithFullVersion(boolean startWithFullVersion) {
		this.startWithFullVersion = startWithFullVersion;
	}
	
	public String getStopOnGitVersion() {
		return stopOnGitVersion;
	}

	public void setStopOnGitVersion(String stopOnGitVersion) {
		this.stopOnGitVersion = stopOnGitVersion;
	}

	public int getReopenSession() {
		return reopenSession;
	}

	public void setReopenSession(int reopenSession) {
		this.reopenSession = reopenSession;
	}

	public boolean isOnlyBuggyVersions() {
		return onlyBuggyVersions;
	}

	public void setOnlyBuggyVersions(boolean onlyBuggyVersions) {
		this.onlyBuggyVersions = onlyBuggyVersions;
	}

	public void clearDatabase() {
		Neo4jUtil.clearDatabase(transaction);
	}

	public void commitHistory(DataSet dataset) {
		List<Version> history = getHistory(dataset.getHistory().getVersions());
		HistoryIterator historyIterator = new HistoryIterator(history);
		
		Path systemModelPath = getRepositoryFile(dataset.getSystemModel());
		URI systemModelURI = URI.createFileURI(systemModelPath.toString());

		ModelVersion2Neo4j incrementalModelDelta = new ModelVersion2Neo4j(modelRepository, systemModelURI, transaction);
		incrementalModelDelta.setOnlyBuggyVersions(isOnlyBuggyVersions());
		int databaseVersion = fastforwardHistoryIteration(dataset.getHistory(), historyIterator, incrementalModelDelta);
		
		// Create history incrementally:
		while (historyIterator.hasNext()) {
			++databaseVersion;
			historyIterator.next();
			
			// Check out the next version from the repository:
			Version previousVersion = historyIterator.getOlderVersion();
			Version currentVersion = historyIterator.getCurrentVersion();
			Version nextVersion = historyIterator.getNewerVersion();
			
			if ((stopOnGitVersion != null) && currentVersion.getIdentification().equals(stopOnGitVersion)) {
				break;
			}
			
			// reopen transaction to prevent resource leaks.
			if (databaseVersion % reopenSession == 0) {
				transaction.open();
			}
			
			if (LOGGING) {
				System.out.println("> Remaining Versions: " + (historyIterator.nextIndex() + 2));
				System.out.println("Version: " + databaseVersion + " - " + currentVersion.getIdentification());
			}

			long time = System.currentTimeMillis();
			modelRepository.checkout(dataset.getHistory(), currentVersion);
			time = stopTime(time, "Checkout");

			// Database: Compute and make an atomic commit of the new model version:
			if (startWithFullVersion && (databaseVersion == restartWithVersion)) {
				incrementalModelDelta.commitInitial(
						currentVersion, nextVersion, databaseVersion);
			} else {
				incrementalModelDelta.commitDelta(
						previousVersion, currentVersion, nextVersion, databaseVersion);
			}
		}
	}
	
	private List<Version> getHistory(List<Version> history) {
		if (onlyBuggyVersions) {
			List<Version> bugHistory = new ArrayList<>();
			HistoryIterator historyIterator = new HistoryIterator(history);
			
			while (historyIterator.hasNext()) {
				Version currentVersion = historyIterator.next();
				Version nextVersion = historyIterator.getNewerVersion();
				
				if ((nextVersion != null) && nextVersion.hasBugReport()) {
					if (containsJavaFile(nextVersion.getBugReport().getBugLocations())) {
						currentVersion.setBugReport(nextVersion.getBugReport());
						bugHistory.add(currentVersion);
					}
				}
			}
			
			Collections.reverse(bugHistory);
			return bugHistory;
		} else {
			return history;
		}
	}
	
	private boolean containsJavaFile(List<FileChange> bugLocations) {
		
		for (FileChange fileChange : bugLocations) {
			if (fileChange.getLocation().toString().endsWith(".java")) {
				return true;
			}
		}
		
		return false;
	}
	
	private int fastforwardHistoryIteration(History history, HistoryIterator historyIterator, ModelVersion2Neo4j incrementalModelDelta) {
		int datasetVersion = -1;
		
		if ((restartWithVersion != -1) || (restartWithGitVersion != null))  {
			while (historyIterator.hasNext()) {
				++datasetVersion;
				historyIterator.next();
				
				// Check out the next version from the repositoy:
				Version previousVersion = historyIterator.getOlderVersion();
				Version currentVersion = historyIterator.getCurrentVersion();
				Version nextVersion = historyIterator.getNewerVersion();
				
				// Initialize incremental delta computation one version earlier:
				if ((datasetVersion == (restartWithVersion - 1)) 
						|| (nextVersion.getIdentification().equals(restartWithGitVersion))) {
					
					modelRepository.checkout(history, currentVersion);
					
					if (!startWithFullVersion) {
						incrementalModelDelta.initialize(
								previousVersion, currentVersion, nextVersion, datasetVersion);
					}
					
					if (restartWithVersion == -1) {
						restartWithVersion = datasetVersion + 1; 
					}
					break;
				}
			}
		}
		
		return datasetVersion;
	}

	private Path getRepositoryFile(Path localPath) {
		return modelRepository.getWorkingDirectory().resolve(localPath);
	}

	private long stopTime(long startTime, String text) {
		if (PROFILE) {
			System.out.println(text + ": " + (System.currentTimeMillis() - startTime) + "ms");
			return System.currentTimeMillis();
		}
		return -1;
	}
}
