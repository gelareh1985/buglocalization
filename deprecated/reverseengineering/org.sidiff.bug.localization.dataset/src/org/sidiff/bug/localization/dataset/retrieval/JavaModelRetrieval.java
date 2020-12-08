package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.common.utilities.logging.LoggerUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.ChangeLocationDiscoverer;
import org.sidiff.bug.localization.dataset.changes.ChangeLocationMatcher;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.retrieval.util.ProjectChangeProvider;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2JavaSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.ChangeProvider;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.IncrementalJavaParser;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

/**
 * @deprecated Use {@link DirectSystemModelRetrieval}
 */
@Deprecated
public class JavaModelRetrieval {
	
	private JavaModelRetrievalProvider provider;
	
	private DataSet dataset;
	
	private Path datasetPath;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private Path javaModelRepositoryPath;
	
	private IncrementalJavaParser javaParser;
	
	private ExecutorService commitSystemModelVersionThread;
	
	private RunnableFuture<Void> commitSystemModelVersionTask;
	
	public JavaModelRetrieval(JavaModelRetrievalProvider provider, DataSet dataset, Path datasetPath) {
		this.provider = provider;
		this.dataset = dataset;
		this.datasetPath = datasetPath;
		this.javaParser = new IncrementalJavaParser(provider.isIgnoreMethodBodies());
	}
	
	public void retrieve() {
		retrieve(-1);
	}

	public void retrieve(int resume) {
		History history = dataset.getHistory();
		List<Version> versions = history.getVersions();
		
		if (resume == -1) {
			resume = versions.size();
		}
		
		// Storage:
		this.codeRepository = provider.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL, dataset);
		this.javaModelRepositoryPath = javaModelRepository.getRepositoryPath();
		
		try {
			this.commitSystemModelVersionThread = Executors.newSingleThreadExecutor();
			
			// Iterate from old to new versions:
			int counter = versions.size() - resume;
			
			for (int i = resume; i-- > 0;) {
				Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
				Version version = versions.get(i);
				Version newerVersion = (i > 0) ? versions.get(i - 1) : null;
				counter++;

				long time = System.currentTimeMillis();
				
				Workspace workspace = retrieveWorkspaceVersion(history, olderVersion, version);
				time = stopTime("Checkout Workspace Version: ", time);
				
				// Clean up older version:
				List<Project> removedProjects = javaModelRepository.removeMissingProjects(olderVersion, version);
				javaParser.update(removedProjects.stream().map(Project::getName).collect(Collectors.toList()));
				
				// Workspace -> Java Models
				retrieveWorkspaceJavaModelVersion(olderVersion, version, newerVersion, workspace);
				time = stopTime("Discover Java Model Version: ", time);
				
				// Store system model workspace as revision:
				waitForCommit();
				commit(olderVersion, version);
				
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "Discovered system model version " + (versions.size() - i) + " of " + versions.size() + " versions");
				}
				
				// Intermediate save:
				if ((provider.getIntermediateSave() > 0) && ((counter % provider.getIntermediateSave()) == 0)) {
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
					
					// Prevent resource leaks - clear cached Java CompilationUnits:
					javaParser.reset();
				}
			}
		} finally {
			// Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
			codeRepository.reset();
			commitSystemModelVersionThread.shutdown();
		}
	}

	private void commit(Version olderVersion, Version version) {
		commitSystemModelVersionTask = new FutureTask<>(() -> javaModelRepository.commitVersion(version, olderVersion), null);
		commitSystemModelVersionThread.execute(commitSystemModelVersionTask);
	}

	private void waitForCommit() {
		// Wait for last version to be commited:
		if (commitSystemModelVersionTask != null) {
			try {
				long time = System.currentTimeMillis();
				commitSystemModelVersionTask.get();
				this.commitSystemModelVersionTask = null;
				time = stopTime("Commit System Model Version (waiting): ", time);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void retrieveWorkspaceJavaModelVersion(Version olderVersion, Version version, Version newerVersion, Workspace workspace) {
		for (Project project : workspace.getProjects()) {

			// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
			// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
			ChangeLocationMatcher changeLocationMatcher  = null;

			if ((newerVersion != null) && (newerVersion.hasBugReport())) {
				changeLocationMatcher = new ChangeLocationMatcher(
						project.getName(), newerVersion.getBugReport().getBugLocations(), provider.getFileChangeFilter());
			}

			// Project -> Java Model
			try {
				retrieveProjectJavaModelVersion(olderVersion, version, project, changeLocationMatcher);
			} catch (Throwable e) {
				e.printStackTrace();

				if (Activator.getLogger().isLoggable(Level.SEVERE)) {
					Activator.getLogger().log(Level.SEVERE, "Could not discover system model: " + project);
				}
			}
		}
	}

	private Workspace retrieveWorkspaceVersion(History history, Version oldVersion, Version version) {
		
		// Clean up older version:
		javaModelRepository.removeMissingProjects(oldVersion, version);
		
		// Load newer version:
		codeRepository.checkout(history, version);
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Retrieve Workspace: " + version);
		}
		
		Workspace discoveredWorkspace = version.getWorkspace();
		WorkspaceBuilder workspaceDiscoverer = new WorkspaceBuilder(discoveredWorkspace, codeRepositoryPath);
		workspaceDiscoverer.cleanWorkspace();
		
		// Load and filter workspace:
		ProjectFilter projectFilter = provider.createProjectFilter();
		Workspace loadedWorkspace = workspaceDiscoverer.createProjects(projectFilter);
		version.setWorkspace(loadedWorkspace);
		
		return loadedWorkspace;
	}

	private void retrieveProjectJavaModelVersion(Version olderVersion, Version version, Project project, ChangeLocationMatcher changeLocationMatcher) 
			throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		Path systemModelFile = javaModelRepository.getSystemModelFile(project, true);
		
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if (HistoryUtil.hasChanges(project, olderVersion, version, provider.getFileChangeFilter())) {
			IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
			
			// Calculate changed files in project for incremental AST parser:
			List<FileChange> projectFileChanges = HistoryUtil.getChanges(project, version.getFileChanges(), provider.getFileChangeFilter());
			ChangeProvider changeProvider = new ProjectChangeProvider(projectFileChanges);
			javaParser.update(changeProvider.getChanges(workspaceProject));
			
			// Discover the Java AST of the project version:
			JavaProject2JavaSystemModel systemModelDiscoverer = new JavaProject2JavaSystemModel(javaParser);
			SystemModel systemModel;
			
			if (changeLocationMatcher != null) {
				// Discover with change locations:
				ChangeLocationDiscoverer changeLocationDiscoverer = new ChangeLocationDiscoverer(changeLocationMatcher);
				systemModel = systemModelDiscoverer.discover(workspaceProject, changeLocationDiscoverer, new NullProgressMonitor());
				systemModel.getViewByKind(ViewDescriptions.JAVA_MODEL).getChanges().addAll(changeLocationDiscoverer.getChanges());
			} else {
				systemModel = systemModelDiscoverer.discover(workspaceProject, null, new NullProgressMonitor());
			}
			
			// Store system model in data set:
			storeSystemModel(systemModelFile, systemModel);
		} else {
			
			// Clear changes or no system model?
			if (Files.exists(systemModelFile)) {
				// Optimization: Clear only if changes were written for last version
				if (HistoryUtil.hasChanges(project, olderVersion.getFileChanges(), provider.getFileChangeFilter())) {
					SystemModel systemModel = javaModelRepository.getSystemModel(project);
					clearSystemModelChanges(systemModel, systemModelFile);
				}
			} else {
				systemModelFile = null;
			}
		}
		
		// Store path in data set:
		if (systemModelFile != null) {
			project.setSystemModel(javaModelRepository.getRepositoryPath().relativize(systemModelFile));
		} else {
			project.setSystemModel(null); // no system model
		}
	}
	
	private void clearSystemModelChanges(SystemModel systemModel, Path systemModelFile) throws IOException {
		boolean hasChanged = false;
		
		for (View view : systemModel.getViews()) {
			if (!view.getChanges().isEmpty()) {
				view.getChanges().clear();
				hasChanged = true;
			}
		}
		
		if (hasChanged) {
			storeSystemModel(systemModelFile, systemModel);
		}
	}
	
	private void storeSystemModel(Path systemModelFile, SystemModel systemModel) {
		waitForCommit(); // synchronize
		
		// Save model:
		systemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		systemModel.saveAll(Collections.emptyMap());
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
		if (Activator.getLogger().isLoggable(LoggerUtil.PERFORMANCE)) {
			Activator.getLogger().log(LoggerUtil.PERFORMANCE,  text + (System.currentTimeMillis() - time) + "ms");
		}
		return System.currentTimeMillis();
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getJavaModelRepositoryPath() {
		return javaModelRepositoryPath;
	}
}
