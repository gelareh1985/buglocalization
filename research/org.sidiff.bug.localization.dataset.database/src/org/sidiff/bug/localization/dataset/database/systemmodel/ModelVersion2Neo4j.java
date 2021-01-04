package org.sidiff.bug.localization.dataset.database.systemmodel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			Version previousVersion, Version newModelVersion, Version nextModelVersion, 
			BugReport bugReport, int newDatabaseVersion) {
		
		ResourceSet newResourceSet = createResourceSet();
		
		// TODO: Generalize this!?
		if (newResourceSet.getURIConverter().exists(systemModelURI, null)) {
			internal_commitDelta(
					previousVersion, newModelVersion, nextModelVersion, 
					bugReport, newDatabaseVersion, false);
			SystemModelFactory.eINSTANCE.createSystemModel(newResourceSet, systemModelURI, true); // load all resources...
			modelDelta.commitDelta(newDatabaseVersion, null, null, repositoryBaseURI, newResourceSet.getResources());
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
		
		// Get models which changed in this version:
		List<FileChange> newModelVersionChanges = nextModelVersionChanges;
		
		// Initial version?
		if (newModelVersionChanges == null) {
			newModelVersionChanges = modelRepository.getChanges(previousVersion, currentModelVersion, false);
		}
		
		ResourceSet newResourceSet = createResourceSet();
		List<Resource> newResources = new ArrayList<>();
		Map<Path, Resource> newResourcesLocations = new HashMap<>();
		
		for (FileChange fileChange : newModelVersionChanges) {
			if (!fileChange.getType().equals(FileChangeType.DELETE)) {
				Resource newResource = loadModel(fileChange, newResourceSet);
				
				if (newResource != null) {
					newResources.add(newResource);
					newResourcesLocations.put(fileChange.getLocation(), newResource);
				}
			}
		}
		
		// FIXME[WORKAROUND]: Introduce some general interface for patching of model versions.
		Resource systemModelResource = patchSystemModelVersion(newResourceSet, newResources, currentModelVersion, bugReport);
		
		// Get old versions of models:
		List<Resource> oldResources = previousResources;
		
		// Compute and commit model delta:
		if (commitToDatabase) {
			modelDelta.commitDelta(currentDatabaseVersion, repositoryBaseURI, oldResources, repositoryBaseURI, newResources);
		}
		
		// Avoids resource leaks...
		modelUnloadThread.execute(() -> UMLUtil.unloadUMLModels(oldResources));
//		UMLUtil.unloadUMLModels(previousResources);
		
		// Prepare for next version:
		if (nextModelVersion != null) {
			this.nextModelVersionChanges = modelRepository.getChanges(currentModelVersion, nextModelVersion, false);
			this.previousResources = new ArrayList<>();
			
			for (FileChange fileChange : nextModelVersionChanges) {
				if (!fileChange.getType().equals(FileChangeType.ADD)) {
					
					// Keep only models from this version which will be modified in the next version:
					Resource previousModel = newResourcesLocations.get(fileChange.getLocation());
					
					// Load models from this version which will be modified in the next version:
					if (previousModel == null) {
						previousModel = loadModel(fileChange, newResourceSet);
					}
					
					previousResources.add(previousModel);
				}
			}
			
			if (!previousResources.contains(systemModelResource)) {
				previousResources.add(systemModelResource);
			}
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
	protected Resource patchSystemModelVersion(
			ResourceSet newResourceSet, List<Resource> newResources, 
			Version currentModelVersion, BugReport bugReport) {
		
		if (newResourceSet.getURIConverter().exists(systemModelURI, null)) {
			Resource systemModelResource = newResourceSet.getResource(systemModelURI, true);
			
			if (!systemModelResource.getContents().isEmpty()) {
				SystemModel patchedSystemModel = (SystemModel) systemModelResource.getContents().get(0);
				patchedSystemModel.setName(systemModelName);
				
				TracedVersion modelVersion = dataSet2SystemModel.convertVersion(currentModelVersion, bugReport);
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
