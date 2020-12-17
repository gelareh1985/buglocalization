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

	public ModelHistory2Neo4j(Repository modelRepository, Neo4jTransaction transaction) {
		this.modelRepository = modelRepository;
		this.transaction = transaction;
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

			if (LOGGING) {
				System.out.println("> Remaining Versions: " + (historyIterator.nextIndex() + 1));
			}

			// Check out the next version from the repositoy:
			Version currentVersion = historyIterator.next();
			Version nextVersion = historyIterator.getNewerVersion();

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
