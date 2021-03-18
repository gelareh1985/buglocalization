package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.sidiff.bug.localization.common.utilities.logging.LoggerUtil;
import org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.retrieval.util.WorkspaceUtil;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class SystemModelRetrieval {
	
	private DataSet dataset;
	
	private Path datasetPath;
	
	private Path codeRepositoryPath;
	
	private Repository codeRepository;
	
	private SystemModelRepository systemModelRepository;
	
	private SystemModelRetrievalProvider systemModelProvider;
	
	private JavaProject2SystemModel transformation;
	
	/**
	 * Optimization flag - false -> incremental commit and workspace refresh
	 */
	private boolean newProjectAdded = true;
	
	private Set<String> transformedProjects = new HashSet<>();
	
	// Threads:
	
	private ExecutorService commitSystemModelVersionThread;
	
	private RunnableFuture<Void> commitSystemModelVersionTask;
	
	public SystemModelRetrieval(
			SystemModelRetrievalProvider systemModelProvider, 
			DataSet dataset, Path datasetPath) {
		
		this.systemModelProvider = systemModelProvider;
		this.dataset = dataset;
		this.datasetPath = datasetPath;
		
		this.newProjectAdded = true; 
	}
	
	public void retrieve() throws IOException {
		retrieve(-1);
	}

	public void retrieve(int resume) throws IOException {
		History history = dataset.getHistory();
		List<Version> versions = history.getVersions();
		
		if (resume != -1) {
			versions = versions.subList(0, resume);
			this.newProjectAdded = true; 
		}
		
		// Storage:
		this.codeRepository = systemModelProvider.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		
		try {
			this.commitSystemModelVersionThread = Executors.newSingleThreadExecutor();
			
			SystemModel systemModel = systemModelRepository.getSystemModel();
			dataset.setSystemModel(systemModelRepository.getSystemModelFile());
			
			this.transformation = new JavaProject2SystemModel(
					systemModelRepository.getRepositoryPath(), 
					dataset.getName(),
					systemModelProvider.isIncludeMethodBodies(), 
					systemModel);
			
			// Iterate from old to new versions:
			int counter = 0;
			
			for (int i = versions.size(); i-- > 0;) {
				Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
				Version version = versions.get(i);
				Version newerVersion = (i > 0) ? versions.get(i - 1) : null;
 				counter++;

				long time = System.currentTimeMillis();
				
				Workspace workspace = retrieveWorkspaceVersion(history, version);
				WorkspaceUtil.refreshWorkspace(version, workspace, newProjectAdded);
				
				// Clean up older version:
				clearSystemModelVersion(systemModel); // of last version
				time = stopTime("Checkout Workspace Version: ", time);
				
				/* Synchronize before storing new changes in the model repository */
				waitForCommit(); 
				time = stopTime("Commit System Model Version (waiting): ", time);
				
				// Workspace -> System Model:
				List<Path> removedModelResources = removeMissingProjects(olderVersion, version);
				List<Path> modifiedModelResources = retrieveWorkspaceSystemModelVersion(olderVersion, version, newerVersion, workspace, systemModel);
				transformation.saveModel();
				time = stopTime("Discover System Model Workspace: ", time);
				
				// Commit new revision none blocking
				if (newProjectAdded) {
					commit(olderVersion, version, null); 
					this.newProjectAdded = false;
				} else {
					modifiedModelResources.addAll(removedModelResources);
					commit(olderVersion, version, modifiedModelResources);
				}
				
				time = stopTime("Commit Model Version: ", time);
				
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "Discovered system model version " + (versions.size() - i) + " of " + versions.size() + " versions");
				}
				
				// Intermediate save:
				if ((systemModelProvider.getIntermediateSave() > 0) && ((counter % systemModelProvider.getIntermediateSave()) == 0)) {
					try {
						waitForCommit();
						DataSetStorage.save(Paths.get(
								datasetPath.toString() + "_" 
								+ counter + "_"
								+ version.getIdentificationTrace() + "_"
								+ version.getIdentification()), 
								dataset, false);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			// Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
			if (codeRepository != null) {
				codeRepository.reset();
			}
			if (commitSystemModelVersionThread != null) {
				commitSystemModelVersionThread.shutdown();
			}
		}
	}
	
	private void commit(Version olderVersion, Version version, List<Path> modelResources) {
		commitSystemModelVersionTask = new FutureTask<>(() -> systemModelRepository.commitVersion(version, olderVersion, modelResources), null);
		commitSystemModelVersionThread.execute(commitSystemModelVersionTask);
	}

	private void waitForCommit() {
		// Wait for last version to be commited:
		if (commitSystemModelVersionTask != null) {
			try {
				commitSystemModelVersionTask.get();
				this.commitSystemModelVersionTask = null;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	private Workspace retrieveWorkspaceVersion(History history, Version version) {
		
		// Load newer version:
		boolean versionCheckeout = codeRepository.checkout(history, version);
		
		if (!versionCheckeout) {
			throw new RuntimeException("Could not check out version: " + version);
		} 
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Retrieve Workspace: " + version);
		}
		
		Workspace discoveredWorkspace = version.getWorkspace();
		WorkspaceBuilder workspaceDiscoverer = new WorkspaceBuilder(discoveredWorkspace, codeRepositoryPath);
		workspaceDiscoverer.cleanWorkspace();
		
		// Load and filter workspace:
		ProjectFilter projectFilter = systemModelProvider.createProjectFilter();
		Workspace loadedWorkspace = workspaceDiscoverer.createProjects(projectFilter);
		version.setWorkspace(loadedWorkspace);
		
		return loadedWorkspace;
	}
	
	private List<Path> retrieveWorkspaceSystemModelVersion(
			Version olderVersion, 
			Version version, 
			Version newerVersion,
			Workspace workspace,
			SystemModel systemModel) {
	
		// NOTE: version      := buggy version
		//       newerVersion := fixed version
		
		// Log files to be commited:
		List<Path> modelFiles = new ArrayList<>();
		modelFiles.add(systemModelRepository.getSystemModelFile());
		
		Set<String> workspaceProjectScope = workspace.getProjects().stream().map(Project::getName).collect(Collectors.toSet());
		
		for (Project project : workspace.getProjects()) {
			try {
				long time = System.currentTimeMillis();
				
				// javaSystemModel -> null: Java model has no changes or could not be computed in the previous step.
				// OPTIMIZATION: Recalculate changed projects only (and initial versions).
				if (HistoryUtil.hasChanges(project, olderVersion, version, systemModelProvider.getFileChangeFilter())) {
					
					// Java Code -> System Model
					List<Path> modelProjectFiles = retrieveProjectSystemModelVersion(
							olderVersion, version, newerVersion, project, workspaceProjectScope, systemModel);
					modelFiles.addAll(modelProjectFiles);
					time = stopTime("Discover System Model Project (Name: " + project.getName() + ", Model Files: "
							+ ((olderVersion == null) ? "*" : modelProjectFiles.size()) + "): ", time);
				}
			} catch (Throwable e) {
				e.printStackTrace();

				if (Activator.getLogger().isLoggable(Level.SEVERE)) {
					Activator.getLogger().log(Level.SEVERE, "Could not discover model: " + project);
				}
			}
		}
		
		return modelFiles;
	}

	private List<Path> retrieveProjectSystemModelVersion(
			Version olderVersion, 
			Version version, 
			Version newerVersion, 
			Project project,
			Set<String> workspaceProjectScope,
			SystemModel systemModel) 
					throws IOException {
		
		// NOTE: version      := buggy version
		//       newerVersion := fixed version
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());

		if ((workspaceProject != null) && ProjectUtil.isJavaProject(workspaceProject)) {
			
			// Calculate changed files in project:
			// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
			// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
			List<FileChange> projectFileChanges =  HistoryUtil.getChanges(project, version.getFileChanges(), systemModelProvider.getFileChangeFilter());
			List<FileChange> projectBugLocations = HistoryUtil.getChanges(project, getBugLocations(version, newerVersion), systemModelProvider.getFileChangeFilter());
			
			// Discover the system model of the project version:
			boolean initialVersion = !transformedProjects.contains(project.getFolder().toString());
			
			List<Path> resultFiles = transformation.discover(
					workspaceProject, 
					project.getFolder(), 
					workspaceProjectScope, 
					systemModel, 
					projectFileChanges,
					projectBugLocations,
					version,
					newerVersion,
					initialVersion);
			
			if (initialVersion) {
				transformedProjects.add(project.getFolder().toString());
				this.newProjectAdded = true;
			}
			
			return resultFiles;
		}
		
		return Collections.emptyList();
	}

	private List<Path> removeMissingProjects(Version olderVersion, Version currentVersion) {
		List<Path> removed = new ArrayList<>();
		
		if (olderVersion != null) { // initial version
			Workspace olderWorkspace = olderVersion.getWorkspace();
			Workspace currentWorkspace = currentVersion.getWorkspace();
			
			for (Project oldProject : olderWorkspace.getProjects()) {
				if (!currentWorkspace.containsProject(oldProject)) {
					removed.addAll(transformation.removeProject(oldProject.getName()));
					transformedProjects.remove(oldProject.getFolder().toString());
				}
			}
		}
		
		return removed;
	}

	private List<FileChange> getBugLocations(Version version, Version newerVersion) {
		if ((version != null) && (newerVersion != null) && (newerVersion.hasBugReport())) {
			return newerVersion.getBugReport().getBugLocations();
		} else {
			return Collections.emptyList();
		}
	}

	private void clearSystemModelVersion(SystemModel systemModel) {
		JavaProject2SystemModel.clearSysteModelVersion(systemModel);
	}
	
	public void saveDataSet() {
		waitForCommit(); // synchronize
		
		// Store and commit data set for Java model:
		try {
			this.datasetPath = DataSetStorage.save(Paths.get(
					datasetPath.toString()), 
					dataset, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Path getDatasetPath() {
		return datasetPath;
	}
	
	private long stopTime(String text, long time) {
		if (Activator.getLogger().isLoggable(LoggerUtil.PERFORMANCE)) {
			Activator.getLogger().log(LoggerUtil.PERFORMANCE,  text + (System.currentTimeMillis() - time) + "ms");
		}
		return System.currentTimeMillis();
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getSystemModelRepositoryPath() {
		return systemModelRepository.getRepositoryPath();
	}

}
