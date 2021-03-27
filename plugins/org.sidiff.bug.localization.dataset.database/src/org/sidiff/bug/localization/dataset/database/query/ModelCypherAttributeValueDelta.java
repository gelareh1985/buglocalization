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
			Map<String, Object> attributeValueChangeBatchesByLabel = new HashMap<>();
			List<String> labels = new ArrayList<>();
			
			for (Entry<String, List<Map<String, Object>>> avcNodesForLabel : attributeValueChangeBatches.entrySet()) {
				String label = avcNodesForLabel.getKey();
				labels.add(label);
				attributeValueChangeBatchesByLabel.put("batch_" + label, avcNodesForLabel.getValue());
			}
			
			Map<String, Map<String, Object>> attributeValueChangeQuery = new HashMap<>();
			String query = constructAttributeValueChangeQuery(labels);
			attributeValueChangeQuery.put(query, attributeValueChangeBatchesByLabel);
			return attributeValueChangeQuery;
		} else {
			return Collections.emptyMap();
		}
	}
	
	private String constructAttributeValueChangeQuery(List<String> labels) {
		StringBuffer query = new StringBuffer();
		
		// Match by index per label:
		// NOTE: The nodes need to be copied in a single call of cloneNode to also copy the edges between them.
		query.append("CALL {");
		
		for (int i = 0; i < labels.size(); i++) {
			String label = labels.get(i);
			
			query.append(" UNWIND $batch_");
			query.append(label);
			query.append(" AS entry ");
			
			query.append("MATCH (n: ");
			query.append(label);
			query.append(" {__model__element__id__: entry.id}) ");
			
			query.append("USING INDEX n:");
			query.append(label);
			query.append("(__model__element__id__) ");
			
			query.append("WHERE NOT EXISTS(n.__last__version__) "); // not removed
			
			query.append("WITH COLLECT([n, entry]) AS nodes_");
			query.append(label);
			
			for (int j = 0; j < i; j++) {
				query.append(", nodes_");
				query.append(labels.get(j));
			}
		}
		
		query.append(" RETURN ");
		
		for (int i = 0; i < labels.size(); i++) {
			query.append("nodes_");
			query.append(labels.get(i));
			
			if (i < (labels.size() - 1)) {
				query.append(" + ");
			}
		}

		// Copy node with attribute value change:
		query.append(" AS nodes_entries } WITH nodes_entries  ");
		query.append("UNWIND nodes_entries AS node_entry WITH node_entry, COLLECT(node_entry[0]) AS oldNodes ");
		query.append("CALL apoc.refactor.cloneNodes(oldNodes, true) YIELD input, output ");
		query.append("SET output += node_entry[1].properties "); // Apply attribute value change and node version.
		
		// Remove old changed node (after copy):
		query.append("WITH oldNodes, output AS newNodes FOREACH( oldNode IN oldNodes | SET oldNode.__last__version__ = ");
		query.append(getOldVersion());
		query.append(" ) ");
		
		// Set versions of old changed node edges:
		query.append("WITH oldNodes, newNodes  UNWIND oldNodes AS oldNode ");
		query.append("MATCH (oldNode)-[oe]-()  ");
		query.append("SET oe.__last__version__ = ");
		query.append(getOldVersion());
		
		// Set versions of new changed node edges:
		// NOTE: WHERE NOT newTarget IN oldNodes -> to be deleted
		query.append("WITH oldNodes, newNodes ");
		query.append("MATCH (newNodes)-[ne]-(newTarget) WHERE NOT newTarget IN oldNodes "); 
		query.append("SET ne.__initial__version__ = ");
		query.append(getNewVersion());
		
		// - Copy only edges that exists in the new version:
		// - If neighbor nodes are copied, copy the edges between them, not the origin:
		//   -> Mark all edges of old node as with last version.
		//   -> Remove edges between cloned nodes and origin nodes.
		// FIXME: For some unknown reason this has to be executed last; otherwise the subsequent queries have no effect?!
		query.append("WITH oldNodes, newNodes ");
		query.append("MATCH (newNodes)-[de]-() WHERE de.__last__version__ <= ");
		query.append(getOldVersion());
		query.append(" DELETE de ");
		
		// NOTE: Remove edges between cloned nodes and origin nodes -> mark and removed by previous queries!
		// query.append("WITH collect(input) AS origins, collect(output) AS cloned ");
		//...
		// query.append("WITH origins, cloned ");
		// query.append("MATCH (clone)-[edgeClone]-(cloneTarget) WHERE clone IN cloned AND ID(cloneTarget) IN origins ");
		// query.append("DELETE edgeClone ");
		
		return query.toString();
	}
}
