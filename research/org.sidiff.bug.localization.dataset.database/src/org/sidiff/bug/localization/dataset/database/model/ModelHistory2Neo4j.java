package org.sidiff.bug.localization.dataset.database.model;

import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jUtil;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;

public class ModelHistory2Neo4j {
	
	public static boolean LOGGING = true; 
	
	public static boolean PROFILE = true;
	
	private Repository modelRepository;
	
	private Neo4jTransaction transaction;
	
	private int startDatabaseVersion = -1;
	
	private int reopenSession = 200; // prevent resource leaks...

	public ModelHistory2Neo4j(Repository modelRepository, Neo4jTransaction transaction) {
		this.modelRepository = modelRepository;
		this.transaction = transaction;
	}

	public int getStartDatabaseVersion() {
		return startDatabaseVersion;
	}

	public void setStartDatabaseVersion(int startDatabaseVersion) {
		this.startDatabaseVersion = startDatabaseVersion;
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
		int databaseVersion = -1;
		
		Path systemModelPath = getRepositoryFile(dataset.getSystemModel());
		URI systemModelURI = URI.createFileURI(systemModelPath.toString());

		IncrementalModelDelta incrementalModelDelta = new IncrementalModelDelta(modelRepository, systemModelURI, transaction);

		while (historyIterator.hasNext()) {
			++databaseVersion;
			
			// Check out the next version from the repositoy:
			Version currentVersion = historyIterator.next();
			Version nextVersion = historyIterator.getNewerVersion();
			
			if ((startDatabaseVersion != -1) && (databaseVersion < startDatabaseVersion)) {
				
				// Initialize incremental delta computation one version earlier:
				if (databaseVersion == (startDatabaseVersion - 1)) {
					modelRepository.checkout(dataset.getHistory(), currentVersion);
					incrementalModelDelta.initialize(currentVersion, nextVersion, databaseVersion);
				}
				
				continue;
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
			incrementalModelDelta.commitDelta(currentVersion, nextVersion, databaseVersion);
		}
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
