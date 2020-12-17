package org.sidiff.bug.localization.dataset.database.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherNodeDelta extends ModelCypherDelta {
	
	// https://medium.com/neo4j/5-tips-tricks-for-fast-batched-updates-of-graph-structures-with-neo4j-and-cypher-73c7f693c8cc
	
	/**
	 * Node-Label -> Batch[{Parameter -> Value}]
	 */
	private Map<String, List<Map<String, Object>>> createdNodesBatches;

	/**
	 * Node-Label -> Batch[{Parameter -> Value}]
	 */
	private Map<String, List<Map<String, Object>>> removedNodesBatches;
	
	/**
	 * Label to be indexed for edge generation.
	 */
	private Set<String> nodeLabels = new HashSet<>();
	
	/**
	 * Nodes with attribute value changes.
	 */
	private Set<EObject> modifiedNodes;
	
	public ModelCypherNodeDelta(
			int oldVersion, URI oldBaseURI, Map<XMLResource, XMLResource> oldResourcesMatch, 
			int newVersion, URI newBaseURI,Map<XMLResource, XMLResource> newResourcesMatch) {
		super(oldVersion, oldBaseURI, oldResourcesMatch, newVersion, newBaseURI, newResourcesMatch);
		this.createdNodesBatches = new HashMap<>();
		this.removedNodesBatches = new HashMap<>();
		this.modifiedNodes = new HashSet<>();
		deriveNodeDeltas();
	}
	
	public Set<EObject> getModifiedNodes() {
		return modifiedNodes;
	}
	
	private void deriveNodeDeltas() {
		
		// Process matched resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() != null)) {
				deriveNodeDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched old resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				deriveNodeDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched new resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getNewResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				deriveNodeDelta(resourceMatch.getValue(), resourceMatch.getKey());
			}
		}
	}

	private void deriveNodeDelta(XMLResource oldResource, XMLResource newResource) {
		
		// Process old nodes to be removed:
		if (oldResource != null) { // initial version?
			for (EObject oldModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(oldResource, true)) {
				deriveRemovedNode(oldResource, oldModelElement);
			}
		}
		
		// Process new/changed nodes to be created:
		if (newResource != null) { // removed resource?
			for (EObject newModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(newResource, true)) {
				deriveCreatedNode(newResource, newModelElement);
			}
		}
	}

	private void deriveCreatedNode(XMLResource newResource, EObject modelElementNew) {
		
		// Model ID of the new version node:
		String modelElementID = getModelElementID(newResource, modelElementNew);
		
		// Model element of the old version node:
		EObject modelElementOld = null;
		XMLResource oldResource = getOldResource(newResource);
		
		if (oldResource != null) {
			modelElementOld = getModelElement(oldResource, modelElementID);
		}
		
		// Create query for new node:
		if (isNewNode(modelElementOld, modelElementNew)) {
			deriveCreatedNodeBatchQuery(modelElementOld, modelElementNew, modelElementID);
		} else {
			if (hasValueChanges(modelElementOld, modelElementNew)) {
				deriveCreatedNodeBatchQuery(modelElementOld, modelElementNew, modelElementID);
				
				// Mark old element with changed attribute value as removed for this version:
				deriveRemovedNodeBatchQuery(modelElementOld, modelElementID);
				modifiedNodes.add(modelElementOld);
				modifiedNodes.add(modelElementNew);
			} 
		}
	}

	private void deriveCreatedNodeBatchQuery(EObject modelElementOld, EObject modelElementNew, String modelElementID) {
		Map<String, Object> createNodeProperties = new HashMap<>();

		// meta-attributes:
		createNodeProperties.put("__model__ns__uri__", modelElementNew.eClass().getEPackage().getNsURI());
		createNodeProperties.put("__model__element__id__", modelElementID);
		createNodeProperties.put("__initial__version__", getNewVersion());
		createNodeProperties.put("__last__version__", getNewVersion()); // initialize

		for (EAttribute attribute : modelElementNew.eClass().getEAllAttributes()) {
			if (isConsideredFeature(attribute)) {
				String newValue = toCypherValue(modelElementNew, attribute);

				if (newValue != null) {
					createNodeProperties.put(attribute.getName(), newValue);
				}
			}
		}

		// label:
		String label = toCypherLabel(modelElementNew.eClass());

		// store:
		List<Map<String, Object>> createNodesOfLabel = createdNodesBatches.getOrDefault(label, new ArrayList<>());
		createNodesOfLabel.add(createNodeProperties);
		createdNodesBatches.put(label, createNodesOfLabel);

		nodeLabels.add(label);
	}
	
	private boolean isNewNode(EObject objOld, EObject objNew) {
		return (objOld == null); 
	}
	
	private boolean hasValueChanges(EObject objOld, EObject objNew) {
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
		return false;
	}

	private boolean hasValueChanged(Object oldValue, Object newValue) {
		if (oldValue == newValue) {
			return false; // includes null == null
		} else {
			return (oldValue == null) || !oldValue.equals(newValue);
		}
	}

	private void deriveRemovedNode(XMLResource oldResource, EObject oldModelElement) {
		
		// model ID of the old version node:
		String modelElementID = getModelElementID(oldResource, oldModelElement);
		
		// Is removed model element?
		XMLResource newResource = getNewResource(oldResource);
		
		if ((newResource == null) || (getModelElement(newResource, modelElementID) == null)) {
			deriveRemovedNodeBatchQuery(oldModelElement, modelElementID);
		}
	}

	private void deriveRemovedNodeBatchQuery(EObject oldModelElement, String modelElementID) {
		Map<String, Object> removeNode = new HashMap<>(2);
		removeNode.put("id", modelElementID);
		
		Map<String, Object> removeNodeProperties = new HashMap<>(1);
		removeNode.put("properties", removeNodeProperties);
		removeNodeProperties.put("__last__version__", getOldVersion());
		
		String label = toCypherLabel(oldModelElement.eClass());
		List<Map<String, Object>> removeNodesOfLabel = removedNodesBatches.getOrDefault(label, new ArrayList<>());
		removeNodesOfLabel.add(removeNode);
		removedNodesBatches.put(label, removeNodesOfLabel);
	}

	public String constructIncreaseVersionQuery() {
		StringBuffer query = new StringBuffer();
		query.append("MATCH (n {__last__version__: ");
		query.append(getOldVersion());
		query.append("}) SET n.__last__version__ = ");
		query.append(getNewVersion());
		return query.toString();
	}

	public List<String> constructKeyAttributesQuery() {
		List<String> nodeIndex = new ArrayList<>();

		for (String label : nodeLabels) {

			// NOTE: None unique index -> A node is identified by model element ID and its initial version:
			nodeIndex.add("CREATE INDEX " + label + "___model__element__id__ IF NOT EXISTS FOR (n:" + label + ") ON (n.__model__element__id__)");

			// NOTE: Key attributes with multiple attributes are only allowed in Neo4j enterprise edition.
			//       With this solution it is not needed to add index hints and node labels on the edge queries...
			// nodeIndex.add("CREATE CONSTRAINT IF NOT EXISTS ON (n:" + label + ") ASSERT (n.__model__element__id__, n.__initial__version__) IS NODE KEY");
		}

		return nodeIndex;
	}

	public Map<String, Map<String, Object>> constructCreatedNodesQueries() {
		if (!createdNodesBatches.isEmpty()) {
			Map<String, Map<String, Object>> createNodeQueriesByLabel = new HashMap<>();
			
			for (Entry<String, List<Map<String, Object>>> createNodesForLabel : createdNodesBatches.entrySet()) {
				String query = constructCreatedNodesBatchQuery(createNodesForLabel.getKey());
				
				Map<String, Object> createNodesBatch = new HashMap<>(1);
				createNodesBatch.put("batch", createNodesForLabel.getValue());
				
				createNodeQueriesByLabel.put(query, createNodesBatch);
			}
			
			return createNodeQueriesByLabel;
		} else {
			return Collections.emptyMap();
		}
	}

	private String constructCreatedNodesBatchQuery(String label) {
		StringBuffer query = new StringBuffer();
		query.append("UNWIND $batch as entry CREATE (n:");
		query.append(label);
		query.append(") SET n += entry");
		return query.toString();
	}

	public Map<String, Map<String, Object>> constructRemovedNodesQueries() {
		if (!removedNodesBatches.isEmpty()) {
			Map<String, Map<String, Object>> removeNodeQueriesByLabel = new HashMap<>();
			
			for (Entry<String, List<Map<String, Object>>> removeNodesForLabel : removedNodesBatches.entrySet()) {
				String query = constructRemovedNodesBatchQuery(removeNodesForLabel.getKey());
				
				Map<String, Object> removeNodesBatch = new HashMap<>();
				removeNodesBatch.put("batch", removeNodesForLabel.getValue());
				
				removeNodeQueriesByLabel.put(query, removeNodesBatch);
			}
			
			return removeNodeQueriesByLabel;
		} else {
			return Collections.emptyMap();
		}
	}

	private String constructRemovedNodesBatchQuery(String label) {
		StringBuffer query = new StringBuffer();
		query.append("UNWIND $batch as entry MATCH (n:");
		query.append(label);
		
		// NOTE: Assuming that version number is already increased constructIncreaseVersionQuery():
		query.append(" {__model__element__id__: entry.id, __last__version__: " + getNewVersion() + "}) ");
		
		query.append("USING INDEX n:");
		query.append(label);
		query.append("(__model__element__id__) ");
		
		query.append("SET n += entry.properties");
		
		return query.toString();
	}

	public static String constructClearNodesQuery() {
		return "MATCH (a) DELETE a";
	}

	public static String constructShowIndexQuery() {
		return ":schema";
	}
	
}
