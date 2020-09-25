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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.uml2.common.util.CacheAdapter;
import org.sidiff.bug.localization.common.utilities.logging.LoggerUtil;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.retrieval.util.SystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class SystemModelRetrieval {
	
	private SystemModelRetrievalProvider provider;
	
	private DataSet dataset;
	
	private Path datasetPath;

	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private SystemModelRepository systemModelRepository;
	
	private ExecutorService commitSystemModelVersionThread;
	
	private ExecutorService cleanUpSystemModelResourcesThread;
	
	private RunnableFuture<Void> commitSystemModelVersionTask;
	
	public SystemModelRetrieval(SystemModelRetrievalProvider provider, Path codeRepositoryPath, DataSet dataset, Path datasetPath) {
		this.provider = provider;
		this.codeRepositoryPath = codeRepositoryPath;
		this.dataset = dataset;
		this.datasetPath = datasetPath;
	}

	public void retrieve() {
		retrieve(-1);
	}

	public void retrieve(int resume) {
		
		// Storage:
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL, dataset);
		List<Version> versions = dataset.getHistory().getVersions();
		
		if (resume == -1) {
			resume = versions.size();
		}
		
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		
		try {
			this.commitSystemModelVersionThread = Executors.newSingleThreadExecutor();
			this.cleanUpSystemModelResourcesThread = Executors.newSingleThreadExecutor();
			
			// Iterate from old to new versions:
			int counter = versions.size() - resume;
			
			for (int i = versions.size(); i-- > 0;) {
				Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
				Version version = versions.get(i);
				counter++;

				long time = System.currentTimeMillis();
				
				// Clean up older version:
				systemModelRepository.removeMissingProjects(olderVersion, version);
				
				// Load newer version:
				javaModelRepository.checkout(version);
				time = stopTime("Checkout Version: ", time);
				
				// Discover projects:
				for (Project project : version.getWorkspace().getProjects()) {
					try {
						retrieveSystemModelVersion(olderVersion, version, project);
					} catch (DiscoveryException e) {
						if (Activator.getLogger().isLoggable(Level.SEVERE)) {
							Activator.getLogger().log(Level.SEVERE, "Could not discover system model for '"
									+ project.getName() + "' version " + version.getIdentification());
						}
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				time = stopTime("Discover System Model Version: ", time);
				
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
				}
			}
		} finally {
			// Always reset head pointer of the code repository to its original position, e.g., if the iteration fails.
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

	private void retrieveSystemModelVersion(Version olderVersion, Version version, Project project) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		Path systemModelFile = systemModelRepository.getSystemModelFile(project, true);
		
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if (HistoryUtil.hasChanges(project, olderVersion, version, provider.getFileChangeFilter())) {
			
			// Discover the multi-view system model of the project version:
			SystemModel javaSystemModel = SystemModelFactory.eINSTANCE.createSystemModel(javaModelRepository.getSystemModelFile(project, true));
			SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel();
			systemModel.setName(project.getName());
			
			// START:
			discover(systemModel, javaSystemModel);
			
			// Store system model in data set:
			storeSystemModel(systemModelFile, systemModel);
		}
		
		// Update data set path:
		project.setSystemModel(systemModelRepository.getRepositoryPath().relativize(systemModelFile));
	}
	
	private void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException {
		for (SystemModelDiscoverer systemModelDiscovery : provider.getSystemModelDiscoverer()) {
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
		return javaModelRepository.getRepositoryPath();
	}
	
	public Path getSystemModelRepositoryPath() {
		return systemModelRepository.getRepositoryPath();
	}
}
