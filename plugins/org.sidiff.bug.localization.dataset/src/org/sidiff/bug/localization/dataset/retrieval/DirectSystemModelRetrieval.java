package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.uml2.common.util.CacheAdapter;
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
import org.sidiff.bug.localization.dataset.retrieval.util.SystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2JavaSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.ChangeProvider;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.IncrementalJavaParser;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class DirectSystemModelRetrieval {
	
	private DataSet dataset;
	
	private Path datasetPath;
	
	private Repository codeRepository;
	
	private Path codeRepositoryPath;
	
	private JavaModelRetrievalProvider javaModelProvider;
	
	private IncrementalJavaParser javaParser;
	
	private SystemModelRetrievalProvider systemModelProvider;
	
	private SystemModelRepository systemModelRepository;
	
	private ExecutorService commitSystemModelVersionThread;
	
	private ExecutorService cleanUpSystemModelResourcesThread;
	
	private RunnableFuture<Void> commitSystemModelVersionTask;
	
	public DirectSystemModelRetrieval(
			JavaModelRetrievalProvider javaModelProvider, 
			SystemModelRetrievalProvider systemModelProvider, 
			DataSet dataset, Path datasetPath) {
		
		this.javaModelProvider = javaModelProvider;
		this.systemModelProvider = systemModelProvider;
		this.dataset = dataset;
		this.datasetPath = datasetPath;
		
		this.javaParser = new IncrementalJavaParser(javaModelProvider.isIgnoreMethodBodies());
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
		this.codeRepository = javaModelProvider.createCodeRepository();
		this.codeRepositoryPath = codeRepository.getWorkingDirectory();
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		
		try {
			this.commitSystemModelVersionThread = Executors.newSingleThreadExecutor();
			this.cleanUpSystemModelResourcesThread = Executors.newSingleThreadExecutor();
			
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
				List<Project> removedProjects = systemModelRepository.removeMissingProjects(olderVersion, version);
				javaParser.update(removedProjects.stream().map(Project::getName).collect(Collectors.toList()));
				
				// Workspace -> Java Models -> System Model:
				retrieveWorkspaceSystemModelVersion(olderVersion, version, newerVersion, workspace);
				time = stopTime("Discover Java and System Model Version: ", time);
				
				// Store system model workspace as revision:
				waitForCommit();
				commit(olderVersion, version);
				
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
					
					// Prevent resource leaks - clear cached Java CompilationUnits:
					javaParser.reset();
				}
			}
		} finally {
			// Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
			codeRepository.reset();
			commitSystemModelVersionThread.shutdown();
			cleanUpSystemModelResourcesThread.shutdown();
		}
	}

	private void commit(Version olderVersion, Version version) {
		commitSystemModelVersionTask = new FutureTask<>(() -> systemModelRepository.commitVersion(version, olderVersion), null);
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

	private void retrieveWorkspaceSystemModelVersion(Version olderVersion, Version version, Version newerVersion, Workspace workspace) {

		for (Project project : workspace.getProjects()) {
			long time = System.currentTimeMillis();

			try {

				/*
				 *  Project -> Java Model
				 */

				ChangeLocationMatcher changeLocationMatcher = getChangeLocationMatcher(newerVersion, project);
				SystemModel javaSystemModel = retrieveProjectJavaModelVersion(olderVersion, version, project, changeLocationMatcher);
				time = stopTime("Discover Java Model Project: ", time);

				/*
				 * Java Model -> System Model
				 */

				retrieveProjectSystemModelVersion(olderVersion, version, project, javaSystemModel);
				time = stopTime("Discover System Model Project: ", time);
			} catch (Throwable e) {
				e.printStackTrace();

				if (Activator.getLogger().isLoggable(Level.SEVERE)) {
					Activator.getLogger().log(Level.SEVERE, "Could not discover model: " + project);
				}
			}
		}
	}

	private ChangeLocationMatcher getChangeLocationMatcher(Version newerVersion, Project project) {
		
		// NOTE: We are only interested in the change location of the buggy version, i.e., the version before the bug fix.
		// NOTE: Changes V_Old -> V_New are stored in V_new as V_A -> V_B
		ChangeLocationMatcher changeLocationMatcher  = null;

		if ((newerVersion != null) && (newerVersion.hasBugReport())) {
			changeLocationMatcher = new ChangeLocationMatcher(
					project.getName(), newerVersion.getBugReport().getBugLocations(), javaModelProvider.getFileChangeFilter());
		}
		return changeLocationMatcher;
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
		ProjectFilter projectFilter = javaModelProvider.createProjectFilter();
		Workspace loadedWorkspace = workspaceDiscoverer.createProjects(projectFilter);
		version.setWorkspace(loadedWorkspace);
		
		return loadedWorkspace;
	}

	private SystemModel retrieveProjectJavaModelVersion(Version olderVersion, Version version, Project project, ChangeLocationMatcher changeLocationMatcher) 
			throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "Java Model Discovery: " + project.getName());
		}
		
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if (HistoryUtil.hasChanges(project, olderVersion, version, javaModelProvider.getFileChangeFilter())) {
			
			// Calculate changed files in project for incremental AST parser:
			List<FileChange> projectFileChanges = HistoryUtil.getChanges(project, version.getFileChanges(), javaModelProvider.getFileChangeFilter());
			ChangeProvider changeProvider = new ProjectChangeProvider(projectFileChanges);
			
			// Discover the Java AST of the project version:
			IProject workspaceProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName());
			JavaProject2JavaSystemModel systemModelDiscoverer = new JavaProject2JavaSystemModel(javaParser, changeProvider);
			SystemModel systemModel;
			
			if (changeLocationMatcher != null) {
				// Discover with change locations:
				ChangeLocationDiscoverer changeLocationDiscoverer = new ChangeLocationDiscoverer(changeLocationMatcher);
				systemModel = systemModelDiscoverer.discover(workspaceProject, changeLocationDiscoverer, new NullProgressMonitor());
				systemModel.getViewByKind(ViewDescriptions.JAVA_MODEL).getChanges().addAll(changeLocationDiscoverer.getChanges());
			} else {
				systemModel = systemModelDiscoverer.discover(workspaceProject, null, new NullProgressMonitor());
			}
			
			return systemModel;
		}
		
		return null;
	}
	
	private void retrieveProjectSystemModelVersion(Version olderVersion, Version version, Project project, SystemModel javaSystemModel) 
			throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		Path systemModelFile = systemModelRepository.getSystemModelFile(project, true);
		
		// javaSystemModel -> null: Java model has no changes or could not be computed in the previous step.
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if ((javaSystemModel != null) && HistoryUtil.hasChanges(project, olderVersion, version, javaModelProvider.getFileChangeFilter())) {
			
			// Discover the multi-view system model of the project version:
			SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel();
			systemModel.setName(project.getName());
			
			// START:
			discoverSystemModel(systemModel, javaSystemModel);
			
			// Store system model in data set:
			storeSystemModel(systemModelFile, systemModel);
		}
		
		// Update data set path:
		project.setSystemModel(systemModelRepository.getRepositoryPath().relativize(systemModelFile));
	}

	private void discoverSystemModel(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException {
		for (SystemModelDiscoverer systemModelDiscovery : systemModelProvider.getSystemModelDiscoverer()) {
			try {
				systemModelDiscovery.discover(systemModel, javaSystemModel);
			} catch (Throwable e) {
				e.printStackTrace();
				
				if (Activator.getLogger().isLoggable(Level.SEVERE)) {
					Activator.getLogger().log(Level.SEVERE, "Could not discover system model: " + javaSystemModel.eResource().getURI());
				}
			}
		}
	}
	
	private long stopTime(String text, long time) {
		if (Activator.getLogger().isLoggable(LoggerUtil.PERFORMANCE)) {
			Activator.getLogger().log(LoggerUtil.PERFORMANCE,  text + (System.currentTimeMillis() - time) + "ms");
		}
		return System.currentTimeMillis();
	}
	
	private void storeSystemModel(Path systemModelFile, SystemModel systemModel) {
		waitForCommit(); // synchronize
		
		// Save model:
		systemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		systemModel.saveAll(Collections.emptyMap());
		
		// Unload models for garbage collection -> prevent resource leaks:
		unloadSystemModel(systemModel);
	}

	@SuppressWarnings("rawtypes")
	private void unloadSystemModel(SystemModel systemModel) {
		
		// Clear resource from UML CacheAdapter:
		cleanUpSystemModelResourcesThread.execute(() -> {
			Set<Resource> resources = new HashSet<>();
			resources.addAll(systemModel.eResource().getResourceSet().getResources());
			
			// Make sure the (UML) model views are collected:
			for (View view : systemModel.getViews()) {
				if (view.getModel() != null) {
					if (view.getModel().eResource() != null) {
						if (view.getModel().eResource().getResourceSet() != null) {
							resources.addAll(view.getModel().eResource().getResourceSet().getResources());
						} else {
							resources.add(view.getModel().eResource());
						}
					}
				}
			}
			
			// CacheAdapter/ECrossReferenceAdapter resource leak: remove adapter, clear caches, unload all resources 
			for (Iterator<Resource> iterator = resources.iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
				ECrossReferenceAdapter crossReferenceAdapter = ECrossReferenceAdapter.getCrossReferenceAdapter(resource);
				
				if (crossReferenceAdapter != null) {
					crossReferenceAdapter.unsetTarget(resource);
				}
				
				if (CacheAdapter.getInstance() != null) {
					CacheAdapter.getInstance().clear(resource);
				}
				
				resource.unload();
			}
			
			// WORKAROUND: Clear resource leaks of UML CacheAdapter:
			try {
				CacheAdapter cacheAdapter = CacheAdapter.getInstance();
				cacheAdapter.clear();
				
				// Clear inverseCrossReferencer map:
				Field inverseCrossReferencerField = ECrossReferenceAdapter.class.getDeclaredField("inverseCrossReferencer");
				inverseCrossReferencerField.setAccessible(true);
				Object inverseCrossReferencerValue = inverseCrossReferencerField.get(cacheAdapter);
				
				if (inverseCrossReferencerValue instanceof Map) {
					((Map) inverseCrossReferencerValue).clear();
				}
				
				// Clear proxyMap map:
				Class<?> inverseCrossReferencerClass = Class.forName("org.eclipse.emf.ecore.util.ECrossReferenceAdapter$InverseCrossReferencer");
				Field proxyMapField = inverseCrossReferencerClass.getDeclaredField("proxyMap");
				proxyMapField.setAccessible(true);
				Object proxyMapValue = proxyMapField.get(inverseCrossReferencerValue);
				
				if (proxyMapValue instanceof Map) {
					((Map) proxyMapValue).clear();
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
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
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getSystemModelRepositoryPath() {
		return systemModelRepository.getRepositoryPath();
	}
}
