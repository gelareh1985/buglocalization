package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

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
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;

/**
 * Helper application to post-process a Git repository.
 */
public class GitRebaseApplication implements IApplication {

	public static final String ARGUMENT_SOURCE_REPOSITORY = "-sourcerepository";

	public static final String ARGUMENT_TARGET_REPOSITORY = "-targetrepository";

	private History history;

	private GitRepository sourceRepository;

	private GitRepository targetRepository;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {

		Path sourceRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_SOURCE_REPOSITORY);
		this.sourceRepository = new GitRepository(sourceRepositoryPath.toFile()); 

		Path targetRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_TARGET_REPOSITORY, false);
		this.targetRepository = new GitRepository(targetRepositoryPath.toFile());
		
		// Reset to latest version:
		sourceRepository.checkout("master");

		// Read history:
		this.history = sourceRepository.getHistory(VersionFilter.FILTER_NOTHING);
		HistoryIterator historyIterator = new HistoryIterator(history);
		
		DataSet dataset = new DataSet();
		dataset.setHistory(history);

		while (historyIterator.hasNext()) {
			System.out.println("Remaining Versions: " + (historyIterator.nextIndex() + 1));
			Version currentVersion = historyIterator.next();
			
			sourceRepository.checkout(history, currentVersion);
			RevCommit currentSourceCommit = sourceRepository.getCurrentCommit();

			// Copy changes of the current version from the source to the target repository:
			List<Path> changedFiles = copyChangesFromSourceToTargetRepository(historyIterator.getOlderVersion(), currentVersion);

			// Do changes to current version in the target repository:
			processVersion(
					historyIterator.getOlderVersion(), 
					historyIterator.getCurrentVersion(), 
					historyIterator.getNewerVersion());

			// Commit rewritten commit:
			targetRepository.commit(
					currentSourceCommit.getAuthorIdent().getName(),
					currentSourceCommit.getAuthorIdent().getEmailAddress(), 
					currentSourceCommit.getFullMessage(), null, null,
					changedFiles); // Set modified files -> JGit add . is slow: https://bugs.eclipse.org/bugs/show_bug.cgi?id=494323 
			
			// Update data set version ID:
			RevCommit currentTargetCommit = targetRepository.getCurrentCommit();
			currentVersion.setIdentificationTrace(currentTargetCommit.getId().getName());
		}
		
		for (Version version : history.getVersions()) {
			String trace = version.getIdentification();
			version.setIdentification(version.getIdentificationTrace());
			version.setIdentificationTrace(trace);
		}
		
		String traceFileName = sourceRepository.getWorkingDirectory().getFileName() + "_dataset_rebased.json";
		saveDataSet(dataset, targetRepository.getWorkingDirectory().getParent().resolve(traceFileName));
		
		Activator.getLogger().log(Level.INFO, "Rebase Finished");
		Activator.getLogger().log(Level.INFO, "To optimize disc space run: git gc --auto");
		Activator.getLogger().log(Level.INFO, "Trace saved to: " + traceFileName);
		
		return IApplication.EXIT_OK;
	}
	
	public Path saveDataSet(DataSet dataset, Path datasetPath) {
		try {
			return DataSetStorage.save(datasetPath, dataset, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void stop() {
	}

	private List<Path> copyChangesFromSourceToTargetRepository(Version olderVersion, Version currentVersion) throws IOException {
		List<Path> changedFiles = new ArrayList<>();
		
		for (FileChange fileChange : sourceRepository.getChangesFlattenCommitTree(olderVersion, currentVersion, false)) {
			Path sourceFile = getSourceRepositoryFile(fileChange.getLocation());
			Path targetFile = getTargetRepositoryFile(fileChange.getLocation());
			
			if (fileChange.getType().equals(FileChangeType.DELETE)) {
				deleteFile(targetFile);
				changedFiles.add(fileChange.getLocation());
			} 
			
			else if (fileChange.getType().equals(FileChangeType.ADD) 
					|| fileChange.getType().equals(FileChangeType.MODIFY)) {
				copyFile(sourceFile, targetFile);
				changedFiles.add(fileChange.getLocation());
			}
		}
		
		return changedFiles;
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
