package org.sidiff.bug.localization.dataset.database.model;

import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
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
	
	private boolean startWithFullVersion = false;
	
	private int reopenSession = 200; // prevent resource leaks...

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

	public boolean isStartWithFullVersion() {
		return startWithFullVersion;
	}

	public void setStartWithFullVersion(boolean startWithFullVersion) {
		this.startWithFullVersion = startWithFullVersion;
	}

	public int getReopenSession() {
		return reopenSession;
	}

	public void setReopenSession(int reopenSession) {
		this.reopenSession = reopenSession;
	}

	public void clearDatabase() {
		Neo4jUtil.clearDatabase(transaction);
	}

	public void commitHistory(DataSet dataset) {
		HistoryIterator historyIterator = new HistoryIterator(dataset.getHistory());
		
		Path systemModelPath = getRepositoryFile(dataset.getSystemModel());
		URI systemModelURI = URI.createFileURI(systemModelPath.toString());

		IncrementalModelDelta incrementalModelDelta = new IncrementalModelDelta(modelRepository, systemModelURI, transaction);
		int databaseVersion = initializeHistoryIteration(dataset.getHistory(), historyIterator, incrementalModelDelta);
		
		// Create history incrementally:
		while (historyIterator.hasNext()) {
			++databaseVersion;
			
			// Check out the next version from the repositoy:
			Version previousVersion = historyIterator.getOlderVersion();
			Version currentVersion = historyIterator.next();
			Version nextVersion = historyIterator.getNewerVersion();
			
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
				incrementalModelDelta.commitInitial(previousVersion, currentVersion, nextVersion, databaseVersion);
			} else {
				incrementalModelDelta.commitDelta(previousVersion, currentVersion, nextVersion, databaseVersion);
			}
		}
	}
	
	private int initializeHistoryIteration(History history, HistoryIterator historyIterator, IncrementalModelDelta incrementalModelDelta) {
		int databaseVersion = -1;
		
		if (restartWithVersion != -1)  {
			while (historyIterator.hasNext()) {
				++databaseVersion;
				
				// Check out the next version from the repositoy:
				Version previousVersion = historyIterator.getOlderVersion();
				Version currentVersion = historyIterator.next();
				Version nextVersion = historyIterator.getNewerVersion();
				
				// Initialize incremental delta computation one version earlier:
				if (databaseVersion == (restartWithVersion - 1)) {
					modelRepository.checkout(history, currentVersion);
					incrementalModelDelta.initialize(previousVersion, currentVersion, nextVersion, databaseVersion);
					break;
				}
			}
		}
		
		return databaseVersion;
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
