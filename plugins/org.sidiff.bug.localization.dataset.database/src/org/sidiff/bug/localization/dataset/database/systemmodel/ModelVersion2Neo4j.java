package org.sidiff.bug.localization.dataset.database.systemmodel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.database.model.ModelDelta;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.TracedVersion;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.DataSet2SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.util.UMLUtil;

public class ModelVersion2Neo4j {

	private Repository modelRepository;
	
	private URI repositoryBaseURI;
	
	private ModelDelta modelDelta;
	
	private List<FileChange> nextModelVersionChanges;
	
	private List<Resource> previousResources;
	
	protected URI systemModelURI;
	
	protected String systemModelName;
	
	protected DataSet2SystemModel dataSet2SystemModel;
	
	private boolean onlyBuggyVersions = false;
	
	private ExecutorService modelUnloadThread;
	
	public ModelVersion2Neo4j(Repository modelRepository, URI systemModelURI, Neo4jTransaction transaction) {
		this.modelRepository = modelRepository;
		this.repositoryBaseURI = URI.createFileURI(modelRepository.getWorkingDirectory().toString());
		this.modelDelta = new ModelDelta(transaction);
		this.systemModelURI = systemModelURI;
		this.systemModelName = systemModelURI.lastSegment().substring(0, systemModelURI.lastSegment().lastIndexOf("."));
		this.dataSet2SystemModel = new DataSet2SystemModel();
		this.modelUnloadThread = Executors.newSingleThreadExecutor();
	}

	public boolean isOnlyBuggyVersions() {
		return onlyBuggyVersions;
	}

	public void setOnlyBuggyVersions(boolean onlyBuggyVersions) {
		this.onlyBuggyVersions = onlyBuggyVersions;
	}

	public void clearDatabase() {
		modelDelta.clearDatabase();
	}

	public void initialize(
			Version previousVersion, Version newModelVersion, Version nextModelVersion, 
			BugReport bugReport, int newDatabaseVersion) {
		
		internal_commitDelta(
				previousVersion, newModelVersion, nextModelVersion, 
				bugReport, newDatabaseVersion, false);
	}

	public void commitInitial(
			Version newModelVersion, Version nextModelVersion, 
			BugReport bugReport, int newDatabaseVersion) {
		
		if (createResourceSet().getURIConverter().exists(systemModelURI, null)) {
			internal_commitDelta(
					null, newModelVersion, nextModelVersion, 
					bugReport, newDatabaseVersion, true);
		} else {
			System.err.println("Missing the system model:" + systemModelURI);
		}
	}
	
	public void commitDelta(
			Version previousVersion, Version currentModelVersion, Version nextModelVersion,
			BugReport bugReport, int newDatabaseVersion) {
		
		internal_commitDelta(
				previousVersion, currentModelVersion, nextModelVersion, 
				bugReport, newDatabaseVersion, true);
	}
	
	private void internal_commitDelta(
			Version previousVersion, Version currentModelVersion, Version nextModelVersion,
			BugReport bugReport, int currentDatabaseVersion, boolean commitToDatabase) {
		
		ResourceSet currentResourceSet = createResourceSet();
		List<Resource> currentResources = new ArrayList<>();
		
		// Initial version?
		if (previousVersion == null) {
			// Start new history -> load all resources:
			SystemModelFactory.eINSTANCE.createSystemModel(currentResourceSet, systemModelURI, true); // load all resources...
			currentResources.addAll(currentResourceSet.getResources());
		} else {
			// Append to history: Get models which changed in this version:
			List<FileChange> newModelVersionChanges = nextModelVersionChanges;
			
			// Restart history:
			if (newModelVersionChanges == null) {
				newModelVersionChanges = modelRepository.getChanges(previousVersion, currentModelVersion, false);
			}
			
			for (FileChange fileChange : newModelVersionChanges) {
				if (!fileChange.getType().equals(FileChangeType.DELETE)) { // created or modified
					Resource newResource = loadModel(fileChange, currentResourceSet);
					
					if (newResource != null) {
						currentResources.add(newResource);
					}
				}
			}
		}
		
		// Patch system model:
		Resource systemModelResource = patchSystemModelVersion(currentResourceSet, currentResources, currentModelVersion, nextModelVersion, bugReport);
		
		// Compute and commit model delta:
		if (commitToDatabase) {
			modelDelta.commitDelta(currentDatabaseVersion, repositoryBaseURI, previousResources, repositoryBaseURI, currentResources);
		}
		
		// Prepare for next version -> Compute old resources for next incremental resource delta:
		if (nextModelVersion != null) {
			this.nextModelVersionChanges = modelRepository.getChanges(currentModelVersion, nextModelVersion, false);
			
			Set<Resource> currentResourcesModifiedNext = new LinkedHashSet<>();
			currentResourcesModifiedNext.add(systemModelResource);
			
			// Load all model from this version which are needed for comparison in the next version:
			// (The versions can not be loaded after Git checkout.)
			for (FileChange fileChange : nextModelVersionChanges) {
				if (!fileChange.getType().equals(FileChangeType.ADD)) { // deleted or modified
					Resource currentResourceModifiedNext = loadModel(fileChange, currentResourceSet);
					currentResourcesModifiedNext.add(currentResourceModifiedNext);
				}
			}
			
			// Avoids resource leaks...
			if (previousResources != null) {
				// The lambda will directly access the current value in the objects field
				// 'previousResources' from within the thread, which will be changed in the next
				// step, so we need a copy; otherwise the wrong resources will be unloaded!
				List<Resource> resourcesToBeUnloaded = new ArrayList<>(previousResources);
				modelUnloadThread.execute(() -> UMLUtil.unloadUMLModels(resourcesToBeUnloaded));
			}
			
			this.previousResources = new ArrayList<>(currentResourcesModifiedNext);
		}
	}

	protected ResourceSet createResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		
		// FIXME[WORKAROUND]: Do not assign IDs while loading! Generalize this put("*", ...)? 
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("uml",
				new org.eclipse.emf.ecore.resource.Resource.Factory() {

					@Override
					public Resource createResource(URI uri) {
						return new UMLResourceIDPreserving(uri);
					}
				});
		
		return resourceSet;
	}

	private Resource loadModel(FileChange fileChange, ResourceSet newResourceSet) {
		try {
			URI relativeModelURI = URI.createFileURI(fileChange.getLocation().toString());
			URI modelURI = repositoryBaseURI.appendSegments(relativeModelURI.segments());
			return newResourceSet.getResource(modelURI, true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Append model version and bug report information from data set.
	 */
	// FIXME[WORKAROUND]: Compute this while reverse engineering.
	protected Resource patchSystemModelVersion(
			ResourceSet newResourceSet, List<Resource> newResources, 
			Version buggyVersion, Version fixedVersion, BugReport bugReport) {
		
		if (newResourceSet.getURIConverter().exists(systemModelURI, null)) {
			Resource systemModelResource = newResourceSet.getResource(systemModelURI, true);
			
			if (!systemModelResource.getContents().isEmpty()) {
				SystemModel patchedSystemModel = (SystemModel) systemModelResource.getContents().get(0);
				patchedSystemModel.setName(systemModelName);
				
				TracedVersion modelVersion = dataSet2SystemModel.convertVersion(buggyVersion, fixedVersion, bugReport);
				patchedSystemModel.setVersion(modelVersion);
				
				if (modelVersion.getBugreport() != null) {
					dataSet2SystemModel.relocateModelChanges(patchedSystemModel, modelVersion.getBugreport());
				}
				
				if (!newResources.contains(systemModelResource)) {
					newResources.add(systemModelResource);
				}
				
				// Store document types:
				for (View view : patchedSystemModel.getViews()) {
					if (view.getModel() != null) {
						view.getDocumentTypes().add(view.getModel().eClass().getEPackage().getNsURI());
					}
				}
				
				return systemModelResource;
			}
		}
		
		return null;
	}
}