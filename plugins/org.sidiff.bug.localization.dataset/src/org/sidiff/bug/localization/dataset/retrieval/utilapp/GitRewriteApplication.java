package org.sidiff.bug.localization.dataset.retrieval.utilapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jgit.revwalk.RevCommit;
import org.sidiff.bug.localization.common.utilities.workspace.ApplicationUtil;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;

/**
 * Helper application to post-process a Git repository.
 */
public class GitRewriteApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";

	public static final String ARGUMENT_SOURCE_REPOSITORY = "-sourcerepository";

	public static final String ARGUMENT_TARGET_REPOSITORY = "-targetrepository";

	private Path datasetPath;
	
	private DataSet dataset;

	private History history;

	private GitRepository sourceRepository;

	private GitRepository targetRepository;

	@Override
	public Object start(IApplicationContext context) throws Exception {

		this.datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);
		this.history = dataset.getHistory();

		Path sourceRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_SOURCE_REPOSITORY);
		this.sourceRepository = new GitRepository(sourceRepositoryPath.toFile()); 

		Path targetRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_TARGET_REPOSITORY, false);
		this.targetRepository = new GitRepository(targetRepositoryPath.toFile());

//		this.history = sourceRepository.getHistory(VersionFilter.FILTER_NOTHING);
		HistoryIterator historyIterator = new HistoryIterator(history);

		while (historyIterator.hasNext()) {
			System.out.println("Remaining Versions: " + (historyIterator.nextIndex() + 1));
			Version currentVersion = historyIterator.next();
			
			sourceRepository.checkout(history, currentVersion);
			RevCommit currentSourceCommit = sourceRepository.getCurrentCommit();

			// Copy changes of the current version from the source to the target repository:
			copyChangesFromSourceToTargetRepository(historyIterator.getOlderVersion(), currentVersion);

			// Do changes to current version in the target repository:
			processVersion(
					historyIterator.getOlderVersion(), 
					historyIterator.getCurrentVersion(), 
					historyIterator.getNewerVersion());

			// Commit rewritten commit:
			targetRepository.commit(
					currentSourceCommit.getAuthorIdent().getName(),
					currentSourceCommit.getAuthorIdent().getEmailAddress(), 
					currentSourceCommit.getFullMessage(), null, null);
			
			// Update data set version ID:
			RevCommit currentTargetCommit = targetRepository.getCurrentCommit();
			currentVersion.setIdentificationTrace(currentTargetCommit.getId().getName());
		}
		
		for (Version version : history.getVersions()) {
			String trace = version.getIdentification();
			version.setIdentification(version.getIdentificationTrace());
			version.setIdentificationTrace(trace);
		}

		// Save rewritten data set:
		saveDataSet();

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private void copyChangesFromSourceToTargetRepository(Version olderVersion, Version currentVersion) throws IOException {
		for (FileChange fileChange : sourceRepository.getChanges(olderVersion, currentVersion, false)) {
		// for (FileChange fileChange : sourceRepository.getChangesFlattenCommitTree(olderVersion, currentVersion, false)) {
			Path sourceFile = getSourceRepositoryFile(fileChange.getLocation());
			Path targetFile = getTargetRepositoryFile(fileChange.getLocation());
			
			if (fileChange.getType().equals(FileChangeType.DELETE)) {
				deleteFile(targetFile);
			} 
			
			if (fileChange.getType().equals(FileChangeType.ADD) 
					|| fileChange.getType().equals(FileChangeType.MODIFY)) {
				copyFile(sourceFile, targetFile);
			}
		}
	}

	protected void processVersion(Version olderVersion, Version currentVersion, Version newerVersion) throws Exception {
		// TODO: Perform updates on the current version in the target repository:
	}
	
	protected void storeSystemModel(Path systemModelFile, SystemModel systemModel) {
		systemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		systemModel.saveAll(Collections.emptyMap());
	}
	
	protected void copyFile(Path source, Path target) throws IOException {
		if (Files.exists(source)) {
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	protected void deleteFile(Path target) throws IOException {
		Files.deleteIfExists(target);
	}

	public void saveDataSet() {
		// Store and commit data set for Java model:
		try {
			DataSetStorage.save(Paths.get(
					datasetPath.toString()), 
					dataset, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Path getDatasetPath() {
		return datasetPath;
	}

	public void setDatasetPath(Path datasetPath) {
		this.datasetPath = datasetPath;
	}

	public GitRepository getSourceRepository() {
		return sourceRepository;
	}
	
	public Path getSourceRepositoryFile(Path localPath) {
		return sourceRepository.getWorkingDirectory().resolve(localPath);
	}

	public void setSourceRepository(GitRepository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}

	public GitRepository getTargetRepository() {
		return targetRepository;
	}
	
	public Path getTargetRepositoryFile(Path localPath) {
		return targetRepository.getWorkingDirectory().resolve(localPath);
	}

	public void setTargetRepository(GitRepository targetRepository) {
		this.targetRepository = targetRepository;
	}
}
