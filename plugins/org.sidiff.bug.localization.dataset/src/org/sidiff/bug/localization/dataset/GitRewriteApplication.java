package org.sidiff.bug.localization.dataset;

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
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
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

	private GitRepository sourceRepository;

	private GitRepository targetRepository;

	@Override
	public Object start(IApplicationContext context) throws Exception {

		this.datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);

		Path sourceRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_SOURCE_REPOSITORY);
		this.sourceRepository = new GitRepository(sourceRepositoryPath.toFile()); 

		Path targetRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_TARGET_REPOSITORY, false);
		this.targetRepository = new GitRepository(targetRepositoryPath.toFile());

		HistoryIterator historyIterator = new HistoryIterator(dataset.getHistory());

		while (historyIterator.hasNext()) {
			System.out.println("Remaining Versions: " + (historyIterator.nextIndex() + 1));
			Version currentVersion = historyIterator.next();
			
			sourceRepository.checkout(dataset.getHistory(), currentVersion);
			RevCommit currentSourceCommit = sourceRepository.getCurrentCommit();

			// Copy changes of the current version from the source to the target repository:
			copyChangesFromSourceToTargetRepository(currentVersion);

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
			currentVersion.setIdentification(currentTargetCommit.getId().getName());
		}

		// Save rewritten data set:
		saveDataSet();

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private void copyChangesFromSourceToTargetRepository(Version currentVersion) throws IOException {
		for (FileChange fileChange : sourceRepository.getChanges(currentVersion, false)) {
			Path sourceFile = getSourceRepositoryFile(fileChange.getLocation());
			Path targetFile = getTargetRepositoryFile(fileChange.getLocation());
			
			if (fileChange.getType().equals(FileChangeType.DELETE) 
					|| fileChange.getType().equals(FileChangeType.RENAME)) {
				deleteFile(targetFile);
			} 
			
			if (fileChange.getType().equals(FileChangeType.ADD) 
					|| fileChange.getType().equals(FileChangeType.RENAME)
					|| fileChange.getType().equals(FileChangeType.MODIFY)
					|| fileChange.getType().equals(FileChangeType.COPY)) {
				copyFile(sourceFile, targetFile);
			}
		}
	}

	protected void processVersion(Version olderVersion, Version currentVersion, Version newerVersion) throws Exception {
		// TODO: Perform updates on the current version in the target repository:
		
//		// Clean up system model references in data set...
//		for (Project project : currentVersion.getWorkspace().getProjects()) {
//			Path systemModel = getTargetRepositoryFile(project.getSystemModel());
//			
//			if (!Files.exists(systemModel)) {
//				project.setSystemModel(null);
//				
//				if (Activator.getLogger().isLoggable(Level.WARNING)) {
//					Activator.getLogger().log(Level.WARNING, "Clean up system model reference: " + currentVersion + " " + project);
//				}
//			}
//		}
//		
//		// Clean up system model changes...
//		SystemModelRetrievalProvider systemModelProvider = new SystemModelRetrievalProvider();
//		
//		for (Project project : currentVersion.getWorkspace().getProjects()) {
//			if (!HistoryUtil.hasChanges(project, currentVersion.getFileChanges(), systemModelProvider.getFileChangeFilter())) {
//				if (HistoryUtil.hasChanges(project, olderVersion.getFileChanges(), systemModelProvider.getFileChangeFilter())) {
//					if (project.getSystemModel() != null) {
//						Path systemModelFile = getTargetRepositoryFile(project.getSystemModel());
//						SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel(systemModelFile);
//						boolean hasChanged = clearSystemModelChanges(systemModel, systemModelFile);
//						
//						if (hasChanged && Activator.getLogger().isLoggable(Level.WARNING)) {
//							Activator.getLogger().log(Level.WARNING, "Clean up system model changes: " + systemModelFile);
//						}
//					}
//				}
//			}
//		}
	}
	
//	private boolean clearSystemModelChanges(SystemModel systemModel, Path systemModelFile) throws IOException {
//		boolean hasChanged = false;
//		
//		for (View view : systemModel.getViews()) {
//			if (!view.getChanges().isEmpty()) {
//				view.getChanges().clear();
//				hasChanged = true;
//			}
//		}
//		
//		if (hasChanged) {
//			storeSystemModel(systemModelFile, systemModel);
//			return true;
//		}
//		
//		return false;
//	}
	
	protected void storeSystemModel(Path systemModelFile, SystemModel systemModel) {
		systemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		systemModel.saveAll(Collections.emptyMap());
	}
	
	protected void copyFile(Path source, Path target) throws IOException {
		Files.createDirectories(target.getParent());
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
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

	public DataSet getDataset() {
		return dataset;
	}

	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
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
