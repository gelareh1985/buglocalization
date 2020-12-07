package org.sidiff.bug.localization.dataset.retrieval.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.ViewDescription;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class SystemModelRepository {
	
	public static final String DATA_SET_FILE_NAME = "DataSet.json";

	private Repository repository;
	
	private Path repositoryPath;
	
	private DataSet dataset;
	
	public SystemModelRepository(Path repositoryPath, DataSet dataset) {
		this.repositoryPath = repositoryPath;
		this.repository = new GitRepository(repositoryPath.toFile());
		this.dataset = dataset;
	}
	
	public SystemModelRepository(Path originalRepositoryPath, ViewDescription view, DataSet dataset) {
		this.repositoryPath = getModelRepositoryPath(originalRepositoryPath, view);
		this.repository = new GitRepository(repositoryPath.toFile());
		this.dataset = dataset;
	}
	
	public void checkout(Version version) {
		repository.checkout(dataset.getHistory(), version);
	}
	
	public Version commitVersion(Version currentVersion, Version previousVersion, List<Path> resources) {
		String identification = null;
			
		// Commit to repository:
		if (resources != null) {
			identification = repository.commit(
					currentVersion.getIdentification(), 
					currentVersion.getDate().toString(), 
					currentVersion.getCommitMessage(),
					null, null, resources);
		} else {
			identification = repository.commit(
					currentVersion.getIdentification(), 
					currentVersion.getDate().toString(), 
					currentVersion.getCommitMessage(), 
					null, null);
		}
		
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
	
	public Path getProjectPath(Project project, boolean createDir) {
		Path projectPath = Paths.get(repositoryPath.toString(), project.getName());
		
		if (createDir && !Files.exists(projectPath)) {
			try {
				Files.createDirectories(projectPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return projectPath;
	}
	
	public Path getSystemModelFile() {
		String modelFileName = dataset.getName() + "." + SystemModel.FILE_EXTENSION;
		Path systemModelFile = Paths.get(modelFileName);
		return systemModelFile;
	}
	
	public Path getSystemModelPath() {
		return getRepositoryPath().resolve(getSystemModelFile());
	}
	
	public SystemModel getSystemModel() throws IOException {
		return SystemModelFactory.eINSTANCE.createSystemModel(getSystemModelPath(), true);
	}
	
	public boolean resetRepository() {
		return repository.reset();
	}
}
