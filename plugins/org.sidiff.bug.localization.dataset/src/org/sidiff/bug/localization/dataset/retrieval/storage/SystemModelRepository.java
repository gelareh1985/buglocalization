package org.sidiff.bug.localization.dataset.retrieval.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.ViewDescription;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class SystemModelRepository {
	
	public static final String DATA_SET_FILE_NAME = "DataSet.json";

	private Repository repository;
	
	private Path repositoryPath;
	
	private DataSet dataset;
	
	public SystemModelRepository(Path originalRepositoryPath, ViewDescription view, DataSet dataset) {
		this.repositoryPath = getModelRepositoryPath(originalRepositoryPath, view);
		this.repository = new GitRepository(repositoryPath.toFile());
		this.dataset = dataset;
	}
	
	public SystemModelRepository(Path originalRepositoryPath, ViewDescription view) {
		this.repositoryPath = getModelRepositoryPath(originalRepositoryPath, view);
		this.repository = new GitRepository(repositoryPath.toFile());
	}
	
	public DataSet getDataSet() {
		if (dataset == null) {
			try {
				this.dataset = DataSetStorage.load(getDataSetPath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return dataset;
	}
	
	public void saveDataSet(boolean commit) throws IOException {
		DataSetStorage.save(getDataSetPath(), dataset, false);
		
		if (commit) {
			String message = "DATA SET " + dataset.getTimestamp();
			repository.commit(message, "na@na", message, null, null);
		}
	}
	
	public void checkout(Version version) {
		repository.checkout(getDataSet().getHistory(), version);
	}
	
	public Version commitVersion(Version currentVersion, Version previousVersion) {
		
		// Commit to repository:
		String identification = repository.commit(currentVersion.getIdentification(), currentVersion.getDate().toString(), currentVersion.getCommitMessage(), null, null);
		
		// Trace commit IDs to original repository:
		if (currentVersion.getIdentificationTrace() == null) {
			currentVersion.setIdentificationTrace(currentVersion.getIdentification());
		}
		currentVersion.setIdentification(identification);
		
		return currentVersion;
	}
	
	/*
	 * Path conventions:
	 */
	
	public Path getRepositoryPath() {
		return repositoryPath;
	}
	
	protected Path getModelRepositoryPath(Path originalRepositoryPath, ViewDescription view) {
		return Paths.get(originalRepositoryPath.toString() + "_" + view.getViewKind());
	}
	
	public Path getDataSetPath() {
		return Paths.get(repositoryPath.toString(), DATA_SET_FILE_NAME);
	}
	
	public Path getProjectPath(Project project) {
		Path projectPath = Paths.get(repositoryPath.toString(), project.getName());
		
		if (!Files.exists(projectPath)) {
			try {
				Files.createDirectories(projectPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return projectPath;
	}
	
	public Path getSystemModelFile(Project project) throws IOException {
		String modelFileName = project.getName() + "." + SystemModel.FILE_EXTENSION;
		Path systemModelFile = Paths.get(getProjectPath(project).toString(), modelFileName);
		return systemModelFile;
	}
	
	public void removeMissingProjects(Version olderVersion, Version currentVersion) {
		if (olderVersion != null) { // initial version
			Workspace olderWorkspace = olderVersion.getWorkspace();
			Workspace currentWorkspace = currentVersion.getWorkspace();
			
			for (Project oldProject : olderWorkspace.getProjects()) {
				if (!currentWorkspace.containsProject(oldProject)) {
					try {
						Path projectPath = repositoryPath.resolve(oldProject.getFolder());
						Files.walk(projectPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public boolean resetRepository() {
		return repository.reset();
	}
}
