package org.sidiff.reverseengineering.java.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.sidiff.reverseengineering.java.Activator;

/**
 * Handling of modeling resources.
 * 
 * @author Manuel Ohrndorf
 */
public class EMFHelper {

	/**
	 * @param resourceSet The resource set for loading the model.
	 * @param modelURI    The URI to be tested.
	 * @return <code>true</code> if the resource exists physically or in the
	 *         resource set; <code>false</code> otherwise.
	 */
	public static boolean resourceExists(ResourceSet resourceSet, URI modelURI) {
		return resourceSet.getURIConverter().exists(modelURI, null);
	}
	
	/**
	 * @param resourceSet The resource set for loading the model.
	 * @param modelURI    The URI to be tested.
	 * @return The loaded or new resource.
	 */
	public static XMLResource initializeResource(ResourceSet resourceSet, URI modelURI) {
		if (resourceExists(resourceSet, modelURI)) {
			return (XMLResource) resourceSet.getResource(modelURI, true);
		} else {
			return (XMLResource) resourceSet.createResource(modelURI);
		}
	}

	/**
	 * @param worspaceResource A resource in the workspace.
	 * @return The corresponding EMF platform resource URI.
	 */
	public URI getURI(IResource worspaceResource) {
		String projectName = worspaceResource.getProject().getName();
		String filePath = worspaceResource.getProjectRelativePath().toOSString();
		String platformPath = projectName + "/" + filePath;
	
		return URI.createPlatformResourceURI(platformPath, true);
	}

	/**
	 * Save a model with the Java binding keys as object IDs.
	 * 
	 * @param modelURI             The target URI of the new model.
	 * @param resourceSet          The resource set which will contain the model and
	 *                             which will be used to resolve cross-reference.
	 * @param modelBindings        Java binding keys mapped to model elements.
	 * @param newRootModelElements All root model elements of the new model.
	 * @param oldModel             The old corresponding model if object IDs should
	 *                             be matched and reused; <code>null</code> otherwise.
	 */
	public Resource saveModelWithBindings(URI modelURI, 
			ResourceSet resourceSet, Map<String, EObject> modelBindings,
			List<EObject> newRootModelElements, XMLResource oldModel) {

		// Creates UMLResourceImpl based on .uml file extension.
		Resource modelResource = resourceSet.createResource(modelURI);
		modelResource.getContents().addAll(newRootModelElements);

		// Set object IDs:
		if (modelResource instanceof XMLResource) {
			XMLResource newModel = (XMLResource) modelResource;

			// Set Java binding names as object serialization IDs:
			for (Entry<String, EObject> modelBinding : modelBindings.entrySet()) {
				newModel.setID(modelBinding.getValue(), modelBinding.getKey());
			}

			// Use object IDs of old resource matching:
			// NOTE: The binding keys might be utilized by the model matcher.
			if (oldModel != null) {
				try {
					reuseObjectIDs(oldModel, newModel, modelBindings);
				} catch(Throwable e) {
					if (Activator.isLoggable(Level.SEVERE)) {
						Activator.getLogger().log(Level.SEVERE, "Matching of XMI IDs failed for resource: " + modelURI);
					}
				}
			}
		} else {
			throw new RuntimeException(
					"Can not save bindings keys in resource of type " + Resource.class + ". " + "Requires to register "
							+ XMLResource.class + " in resource set for *." + modelURI.fileExtension() + ".");
		}
		
		// Save model:
		try {
			modelResource.save(null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return modelResource;
	}

	protected void reuseObjectIDs(XMLResource oldModel, XMLResource newModel, Map<String, EObject> modelBindings) {
		
		// Extract all none binding IDs that should be reused in the new version:
		Map<EObject, String> oldObjectIDs = new HashMap<>();
		
		// Collect none binding IDs and clear other IDs for comparison (UseIdentifiers.WHEN_AVAILABLE): 
		oldModel.getAllContents().forEachRemaining(oldModelElement -> {
			String oldObjectID = oldModel.getID(oldModelElement);
			
			if ((oldObjectID != null) && !oldObjectID.isEmpty()) {
				if (!modelBindings.containsKey(oldObjectID)) {
					oldObjectIDs.put(oldModelElement, oldObjectID);
					oldModel.setID(oldModelElement, null);
				}
			}
		});
		
		// IDs to be reused?
		if (!oldObjectIDs.isEmpty()) {
			
			// Clear other IDs for comparison (UseIdentifiers.WHEN_AVAILABLE): 
			newModel.getAllContents().forEachRemaining(newModelElement -> {
				String newObjectID = newModel.getID(newModelElement);
				
				if ((newObjectID != null) && !newObjectID.isEmpty()) {
					if (!modelBindings.containsKey(newObjectID)) {
						newModel.setID(newModelElement, null);
					}
				}
			});
			
			// Match old and new model and set old IDs to new matched model elements:
			List<Match> matching = getMatching(oldModel, newModel);
			reuseObjectIDs(oldModel, newModel, oldObjectIDs, matching);
		}
	}

	protected void reuseObjectIDs(XMLResource oldModel, XMLResource newModel, 
			Map<EObject, String> oldObjectIDs, List<Match> matching) {
		
		for (Match match : matching) {
			if ((match.getLeft() != null) && (match.getRight() != null)) {
				String reuseObjectID = oldObjectIDs.get(match.getLeft());
				
				// Old object ID to be reused?
				if (reuseObjectID != null) {
					newModel.setID(match.getRight(), reuseObjectID);
				}
			}
			reuseObjectIDs(oldModel, newModel, oldObjectIDs, match.getSubmatches());
		}
	}

	protected List<Match> getMatching(XMLResource oldModel, XMLResource newModel) {
		
		// https://www.eclipse.org/emf/compare/documentation/latest/developer/developer-guide.html
		
		IComparisonScope emfScope = new DefaultComparisonScope(oldModel, newModel, null);
		
		// Use only binding key IDs for comparison: 
		IMatchEngine.Factory.Registry registry = MatchEngineFactoryRegistryImpl.createStandaloneInstance();
		MatchEngineFactoryImpl matchEngineFactory = new MatchEngineFactoryImpl(UseIdentifiers.WHEN_AVAILABLE);
		matchEngineFactory.setRanking(20); // Default engine ranking is 10, must be higher to override.
		registry.add(matchEngineFactory);
		
		Comparison comparison = EMFCompare.builder()
				.setMatchEngineFactoryRegistry(registry)
				.build().compare(emfScope);
		
		return comparison.getMatches();
	}
}
