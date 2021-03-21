package org.sidiff.bug.localization.dataset.database.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherAttributeValueDelta extends ModelCypherDelta {

	// NOTE: Nodes that have changed attribute values need to be copied inside 
	//       of the database to get also all none resource internal edges.
	
	/* TEST (attribute value changes, cross-resource containment, added and removed incident nodes):
		"identity": 96445,
		"labels": ["Class"],
		"properties": {
			"visibility": "public",
			"name": "CompilationUnitSorter",
			"isActive": false,
			"isLeaf": true,
			"isAbstract": false,
			"__model__element__id__": "org.eclipse.jdt.core/Lorg/eclipse/jdt/core/util/CompilationUnitSorter;",
			"isFinalSpecialization": true,
			"__initial__version__": 474
		}
	 */
	
	/**
	 * Node-Label -> Batch[{Parameter -> Value}]
	 */
	private Map<String, List<Map<String, Object>>> attributeValueChangeBatches;
	
	public ModelCypherAttributeValueDelta(
			int oldVersion, URI oldBaseURI, Map<XMLResource, XMLResource> oldResourcesMatch, 
			int newVersion, URI newBaseURI, Map<XMLResource, XMLResource> newResourcesMatch) {
		
		super(oldVersion, oldBaseURI, oldResourcesMatch, newVersion, newBaseURI, newResourcesMatch);
		this.attributeValueChangeBatches = new HashMap<>();
		deriveAttributeValueDeltas();
	}

	private void deriveAttributeValueDeltas() {
		
		// Process matched resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() != null)) {
				deriveAttributeValueDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
	}
	
	private void deriveAttributeValueDelta(XMLResource oldResource, XMLResource newResource) {
		for (EObject newModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(newResource, true)) {
			deriveAttributeValueChange(newResource, newModelElement);
		}
	}
	
	private void deriveAttributeValueChange(XMLResource newResource, EObject modelElementNew) {
		
		// Model ID of the new version node:
		String modelElementID = getModelElementID(newResource, modelElementNew);
		
		// Model element of the old version node:
		XMLResource oldResource = getOldResource(newResource);
		EObject modelElementOld = getModelElement(oldResource, modelElementNew.eClass(), modelElementID);
		
		// Create query for changed node:
		if (hasValueChanges(modelElementOld, modelElementNew)) {
			deriveAttributeValueChangeBatchQuery(modelElementOld, modelElementNew, modelElementID);
		}
	}
	
	private boolean hasValueChanges(EObject objOld, EObject objNew) {
		if (objOld != null) {
			for (EAttribute attribute : objOld.eClass().getEAllAttributes()) {
				if (isConsideredFeature(attribute)) {
					Object oldValue = objOld.eGet(attribute);
					Object newValue = objNew.eGet(attribute);
					
					// compare values:
					if (hasValueChanged(oldValue, newValue)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean hasValueChanged(Object oldValue, Object newValue) {
		if (oldValue == newValue) {
			return false; // includes null == null
		} else {
			return (oldValue == null) || !oldValue.equals(newValue);
		}
	}
	
	private void deriveAttributeValueChangeBatchQuery(EObject modelElementOld, EObject modelElementNew, String modelElementID) {
		Map<String, Object> removeNode = new HashMap<>(2);
		removeNode.put("id", modelElementID);
		
		Map<String, Object> attributeValueChangeProperties = new HashMap<>(1);
		removeNode.put("properties", attributeValueChangeProperties);
		attributeValueChangeProperties.put("__initial__version__", getNewVersion());
		
		for (EAttribute attribute : modelElementOld.eClass().getEAllAttributes()) {
			if (isConsideredFeature(attribute)) {
				Object oldValue = modelElementOld.eGet(attribute);
				Object newValue = modelElementNew.eGet(attribute);
				
				// compare values:
				if (hasValueChanged(oldValue, newValue)) {
					attributeValueChangeProperties.put(attribute.getName(), toCypherValue(modelElementNew, attribute));
				}
			}
		}
		
		String label = toCypherLabel(modelElementOld.eClass());
		List<Map<String, Object>> removeNodesOfLabel = attributeValueChangeBatches.getOrDefault(label, new ArrayList<>());
		removeNodesOfLabel.add(removeNode);
		attributeValueChangeBatches.put(label, removeNodesOfLabel);
	}
	
	public Map<String, Map<String, Object>> constructAttriuteValueChangeQuery() {
		if (!attributeValueChangeBatches.isEmpty()) {
			Map<String, Map<String, Object>> attributeValueChangeQueriesByLabel = new HashMap<>();
			
			for (Entry<String, List<Map<String, Object>>> removeNodesForLabel : attributeValueChangeBatches.entrySet()) {
				String query = constructAttributeValueChangeQuery(removeNodesForLabel.getKey());
				
				Map<String, Object> removeNodesBatch = new HashMap<>();
				removeNodesBatch.put("batch", removeNodesForLabel.getValue());
				
				attributeValueChangeQueriesByLabel.put(query, removeNodesBatch);
			}
			
			return attributeValueChangeQueriesByLabel;
		} else {
			return Collections.emptyMap();
		}
	}
	
	private String constructAttributeValueChangeQuery(String label) {
		StringBuffer query = new StringBuffer();
		query.append("UNWIND $batch as entry ");
		
		query.append("MATCH (n: ");
		query.append(label);
		query.append(" {__model__element__id__: entry.id}) ");
		
		// Match by index:
		query.append("USING INDEX n:");
		query.append(label);
		query.append("(__model__element__id__) ");
		
		query.append("WHERE NOT EXISTS(n.__last__version__) "); // not removed
		
		// Copy node with attribute value change:
		query.append("WITH entry, COLLECT(DISTINCT n) AS oldNodes CALL apoc.refactor.cloneNodes(oldNodes, true) YIELD input, output ");
		
		// Remove old changed node (after copy):
		query.append("WITH entry, oldNodes, output AS newNodes UNWIND oldNodes AS oldNode SET oldNode.__last__version__ = ");
		query.append(getOldVersion());
		query.append(" ");
		
		// Apply attribute value change:
		query.append("WITH entry, oldNodes, newNodes UNWIND newNodes AS newNode ");
		query.append("SET newNode += entry.properties ");
		
		// Copy only edges that exists in the new version:
		query.append("WITH oldNodes, newNodes  UNWIND newNodes AS newNode ");
		query.append("MATCH (newNode)-[de]-() WHERE de.__last__version__ <= ");
		query.append(getOldVersion());
		query.append(" ");
		query.append("DELETE de ");
		
		// Set versions of new changed node edges:
		query.append("WITH oldNodes, newNodes  UNWIND newNodes AS newNode ");
		query.append("MATCH (newNode)-[ne]-() ");
		query.append("SET ne.__initial__version__ = ");
		query.append(getNewVersion());
		query.append(" ");
		query.append("REMOVE ne.__last__version__ ");
		
		// Set versions of old changed node edges:
		query.append("WITH oldNodes, newNodes  UNWIND oldNodes AS oldNode ");
		query.append("MATCH (oldNode)-[oe]-() ");
		query.append("SET oe.__last__version__ = ");
		query.append(getOldVersion());
		
		return query.toString();
	}
}
