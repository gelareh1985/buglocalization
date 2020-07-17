package org.sidiff.bug.localization.dataset.retrieval.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.systemmodel.views.MultiViewSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescription;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

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
				this.dataset = JsonUtil.parse(getDataSetPath(), DataSet.class);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return dataset;
	}
	
	public void saveDataSet() throws IOException {
		JsonUtil.save(dataset, getDataSetPath());
	}
	
	public void checkout(Version version) {
		repository.checkout(getDataSet().getHistory(), version);
	}
	
	public Version commitVersion(Version currentVersion, Version previousVersion) {
		
		// Remove projects that do not exist in the current version:
		updateProjects(repositoryPath, currentVersion, previousVersion);
		
		// Commit to repository:
		String identification = repository.commit(currentVersion.getIdentification(), currentVersion.getDate().toString(), currentVersion.getCommitMessage(), null, null);
		
		// Trace commit IDs to original repository:
		if (currentVersion.getIdentificationTrace() == null) {
			currentVersion.setIdentificationTrace(currentVersion.getIdentification());
		}
		currentVersion.setIdentification(identification);
		
		return currentVersion;
	}

	private void updateProjects(Path javaASTRepositoryPath, Version currentVersion, Version previousVersion) {
		if (previousVersion != null) {
			for (Project project : previousVersion.getWorkspace().getProjects()) {
				if (!currentVersion.getWorkspace().containsProject(project)) {
					try {
						Path projectPath = javaASTRepositoryPath.resolve(project.getFolder());
						Files.walk(projectPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
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
		String modelFileName = project.getName() + "." + MultiViewSystemModel.MULITVIEW_MODEL_FILE_EXTENSION;
		Path systemModelFile = Paths.get(getProjectPath(project).toString(), modelFileName);
		return systemModelFile;
	}
}
