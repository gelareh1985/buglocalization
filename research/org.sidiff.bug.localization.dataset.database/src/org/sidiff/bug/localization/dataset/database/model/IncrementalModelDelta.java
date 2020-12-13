package org.sidiff.bug.localization.dataset.database.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.Repository;

public class IncrementalModelDelta {

	private Repository modelRepository;
	
	private URI repositoryBaseURI;
	
	private ModelDelta modelDelta;
	
	private List<FileChange> nextModelVersionChanges;
	
	private List<Resource> previousResources;
	
	public IncrementalModelDelta(Repository modelRepository, Neo4jTransaction transaction) {
		this.modelRepository = modelRepository;
		this.repositoryBaseURI = URI.createFileURI(modelRepository.getWorkingDirectory().toString());
		this.modelDelta = new ModelDelta(transaction);
	}
	
	public void clearDatabase() {
		modelDelta.clearDatabase();
	}
	
	public void commitDelta(Version newModelVersion, Version nextModelVersion, int newDatabaseVersion) {
		
		// Get models which changed in this version:
		List<FileChange> newModelVersionChanges = nextModelVersionChanges;
		
		// Initial version?
		if (newModelVersionChanges == null) {
			newModelVersionChanges = modelRepository.getChanges(newModelVersion, false);
		}
		
		ResourceSet newResourceSet = new ResourceSetImpl();
		List<Resource> newResources = new ArrayList<>();
		Map<Path, Resource> newResourcesLocations = new HashMap<>();
		
		for (FileChange fileChange : newModelVersionChanges) {
			if (!fileChange.getType().equals(FileChangeType.DELETE)) {
				Resource newResource = loadModel(fileChange, newResourceSet);
				newResources.add(newResource);
				newResourcesLocations.put(fileChange.getLocation(), newResource);
			}
		}
		
		// Get old versions of models:
		List<Resource> oldResources = previousResources;
		
		// Compute model delta:
		modelDelta.commitDelta(newDatabaseVersion, repositoryBaseURI, oldResources, repositoryBaseURI, newResources);
		
		// Prepare for next version:
		this.nextModelVersionChanges = modelRepository.getChanges(nextModelVersion, false);
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
	}

	private Resource loadModel(FileChange fileChange, ResourceSet newResourceSet) {
		URI relativeModelURI = URI.createFileURI(fileChange.getLocation().toString());
		URI modelURI = repositoryBaseURI.appendSegments(relativeModelURI.segments());
		return newResourceSet.getResource(modelURI, true);
	}
}
