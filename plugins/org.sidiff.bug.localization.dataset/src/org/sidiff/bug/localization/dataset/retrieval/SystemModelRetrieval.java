package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.sidiff.bug.localization.common.utilities.logging.LoggerUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2JavaSystemModel;
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
	
	private JavaProject2JavaSystemModel transformation;
	
	// Threads:
	
	private ExecutorService commitSystemModelVersionThread;
	
	private RunnableFuture<Void> commitSystemModelVersionTask;
	
	public SystemModelRetrieval(
			SystemModelRetrievalProvider systemModelProvider, 
			DataSet dataset, Path datasetPath) {
		
		this.systemModelProvider = systemModelProvider;
		this.dataset = dataset;
		this.datasetPath = datasetPath;
	}
	
	public void retrieve() throws IOException {
		retrieve(-1);
	}

	public void retrieve(int resume) throws IOException {
		History history = dataset.getHistory();
		List<Version> versions = history.getVersions();
		
		if (resume != -1) {
			versions = versions.subList(0, versions.size() - resume);
		}
		
		// Storage:
		this.codeRepository = systemModelProvider.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		
		try {
			this.commitSystemModelVersionThread = Executors.newSingleThreadExecutor();
			
			SystemModel systemModel = systemModelRepository.getSystemModel();
			dataset.setSystemModel(systemModelRepository.getSystemModelFile());
			
			this.transformation = new JavaProject2JavaSystemModel(
					systemModelRepository.getRepositoryPath(), 
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
				
				Workspace workspace = retrieveWorkspaceVersion(history, olderVersion, version);
				refreshWorkspace(olderVersion, version, workspace);
				time = stopTime("Checkout Workspace Version: ", time);
				
				// Clean up older version:
				systemModelRepository.removeMissingProjects(olderVersion, version); // FIXME needs migration
				clearSystemModelChanges(systemModel); // of last version
				time = stopTime("Cleanup Workspace Model: ", time);
				
				/* Synchronize before storing new changes in the model repository */
				waitForCommit(); 
				time = stopTime("Commit System Model Version (waiting): ", time);
				
				// Workspace -> System Model:
				List<Path> modelResources = retrieveWorkspaceSystemModelVersion(olderVersion, version, newerVersion, workspace, systemModel);
				transformation.saveModel();
				time = stopTime("Discover System Model Workspace: ", time);
				
				// Commit new revision none blocking
				if (olderVersion == null) { // initial version?
					commit(olderVersion, version, null); 
				} else {
					commit(olderVersion, version, modelResources); 
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

	private Workspace retrieveWorkspaceVersion(History history, Version oldVersion, Version version) {
		
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
	
	private void refreshWorkspace(Version olderVersion, Version version, Workspace workspace) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		// Make checked out resource changes visible:
		if (olderVersion == null) { // initial version?
			for (Project project : workspace.getProjects()) {
				try {
					workspaceRoot.getProject(project.getName()).refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		} else {
			for (Project project : workspace.getProjects()) {
				for (FileChange fileChange : version.getFileChanges()) {
					if (fileChange.getType().equals(FileChangeType.ADD) || fileChange.getType().equals(FileChangeType.DELETE)) {
						if (fileChange.getLocation().startsWith(project.getFolder())) {
							try {
								IProject workspaceProject = workspaceRoot.getProject(project.getName());
								Path fileToBeRefreshed = project.getFolder().relativize(fileChange.getLocation());
								
								if ((workspaceProject != null) && (fileToBeRefreshed != null)) {
									workspaceProject.getFile(fileToBeRefreshed.toString()).refreshLocal(IResource.DEPTH_ZERO, null);
								}
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
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
					List<Path> modelProjectFiles =retrieveProjectSystemModelVersion(
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

		// Calculate changed files in project:
		// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
		// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
		List<FileChange> projectFileChanges =  HistoryUtil.getChanges(project, version.getFileChanges(), systemModelProvider.getFileChangeFilter());
		List<FileChange> projectBugLocations = HistoryUtil.getChanges(project, getBugLocations(version, newerVersion), systemModelProvider.getFileChangeFilter());

		// Discover the system model of the project version:
		return transformation.discover(
				workspaceProject, 
				project.getFolder(), 
				workspaceProjectScope, 
				systemModel, 
				projectFileChanges,
				projectBugLocations,
				olderVersion == null);
	}

	private List<FileChange> getBugLocations(Version version, Version newerVersion) {
		if ((version != null) && (newerVersion != null) && (newerVersion.hasBugReport())) {
			return newerVersion.getBugReport().getBugLocations();
		} else {
			return Collections.emptyList();
		}
	}

	private void clearSystemModelChanges(SystemModel systemModel) {
		for (View view : systemModel.getViews()) {
			view.getChanges().clear();
		}
	}
	
	public void saveDataSet() {
		waitForCommit(); // synchronize
		
		// Store and commit data set for Java model:
		try {
			DataSetStorage.save(Paths.get(
					datasetPath.toString()), 
					dataset, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private long stopTime(String text, long time) {
		System.out.println(text + (System.currentTimeMillis() - time) + "ms"); // TODO
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
