package org.sidiff.bug.localization.dataset.database.query;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jUtil;

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
	
	protected ModelCypherAttributeValueDelta() {
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
		Map<String, Object> avcNode = new HashMap<>(2);
		avcNode.put("id", modelElementID);
		
		Map<String, Object> attributeValueChangeProperties = new HashMap<>(1);
		avcNode.put("properties", attributeValueChangeProperties);
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
		List<Map<String, Object>> avcNodesOfLabel = attributeValueChangeBatches.getOrDefault(label, new ArrayList<>());
		avcNodesOfLabel.add(avcNode);
		attributeValueChangeBatches.put(label, avcNodesOfLabel);
	}
	
	public Map<String, Map<String, Object>> constructAttriuteValueChangeQuery() {
		if (!attributeValueChangeBatches.isEmpty()) {
			Map<String, Object> attributeValueChangeBatchesByLabel = new HashMap<>();
			attributeValueChangeBatchesByLabel.put("oldVersion", getOldVersion());
			attributeValueChangeBatchesByLabel.put("newVersion", getNewVersion());
			
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
			
			// Copy node with attribute value change:
			query.append("CALL apoc.refactor.cloneNodes([n], false) YIELD input, output ");
			query.append("SET output += entry.properties "); // Apply attribute value change and node version.
			
			if (i == 0) {
				query.append("WITH COLLECT(n) AS nodeOrigins, COLLECT([toString(input), output]) AS nodes ");
			} else {
				query.append("WITH nodeOrigins + COLLECT(n) AS nodeOrigins, nodes + COLLECT([toString(input), output]) AS nodes ");
			}
		}
		
		query.append("RETURN apoc.map.fromPairs(nodes) AS nodeTrace, nodeOrigins ");
		query.append("} ");
		
		// Remove old changed node (after copy):
		query.append("WITH nodeTrace, nodeOrigins ");
		query.append("FOREACH(nodeOrigin IN nodeOrigins | SET nodeOrigin.__last__version__ = $oldVersion) ");
		
		// Collect edges to be copied:
		query.append("WITH nodeTrace, nodeOrigins");
		
		// Collect all edges between copied origin nodes and not copied boundary nodes:
		query.append(", [(nodeOrigin)-[outgoingBoundaryRel]->(boundaryNode) WHERE nodeOrigin IN nodeOrigins AND NOT boundaryNode IN nodeOrigins AND NOT EXISTS(outgoingBoundaryRel.__last__version__) ");
		query.append("| [outgoingBoundaryRel, apoc.map.get(nodeTrace, toString(ID(nodeOrigin))), type(outgoingBoundaryRel), properties(outgoingBoundaryRel), boundaryNode]]");
		
		// Collect all edges between copied origin nodes:
		query.append(" + [(nodeOrigin)<-[incomingBoundaryRel]-(boundaryNode) WHERE nodeOrigin IN nodeOrigins AND NOT boundaryNode IN nodeOrigins AND NOT EXISTS(incomingBoundaryRel.__last__version__) ");
		query.append("| [incomingBoundaryRel, boundaryNode, type(incomingBoundaryRel), properties(incomingBoundaryRel), apoc.map.get(nodeTrace, toString(ID(nodeOrigin)))]]");
		
		query.append(" + [(nodeOrigin)-[originRel]->(originTarget) WHERE nodeOrigin IN nodeOrigins AND originTarget IN nodeOrigins AND NOT EXISTS(originRel.__last__version__) ");
		query.append("| [originRel, apoc.map.get(nodeTrace, toString(ID(nodeOrigin))), type(originRel), properties(originRel), apoc.map.get(nodeTrace, toString(ID(originTarget)))]] ");
		query.append("AS copyRels ");
		
		//// query.append(" CREATE (result:Result {result: apoc.convert.toString(tasks)}) "); //// for debugging
		
		// Copy edges [rel, source, type, properties, target]:
		query.append("UNWIND copyRels AS copyRel ");
		query.append("CALL apoc.create.relationship(copyRel[1], copyRel[2], copyRel[3], copyRel[4]) YIELD rel AS copiedRel ");
		query.append("CALL apoc.create.setRelProperty(copyRel[0], '__last__version__', $oldVersion) YIELD rel AS originRel ");
		query.append("SET copiedRel.__initial__version__ = $newVersion ");
		
		return query.toString();
	}
	
	// TEST //
	public static void main(String[] args) {
		String databaseURI = "bolt://localhost:7687";
		String databaseName = "neo4j";
		String databasePassword = "password";
		
		int example = 1;
		
		// Create test data:
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseURI, databaseName, databasePassword)) {
			Neo4jUtil.clearDatabase(transaction);
		}
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseURI, databaseName, databasePassword)) {
			for (String label : new String[] {"A", "B", "C", "X"}) {
				StringBuffer indexQuery = new StringBuffer();
				indexQuery.append("CREATE INDEX " + label + "___model__element__id__ IF NOT EXISTS FOR (n:" + label + ") ON (n.__model__element__id__)");
				transaction.execute(indexQuery.toString());
			}
			
			transaction.commit();
			transaction.awaitIndexOnline();
		}
		
		StringBuffer createQuery = new StringBuffer();
		
		// Nodes:
		createQuery.append("CREATE (a:A {name:'a', __model__element__id__:'a'}) ");
		createQuery.append("SET a.__initial__version__ = 22 ");
		
		createQuery.append("CREATE (b:B {name:'b', __model__element__id__:'b'}) ");
		createQuery.append("SET b.__initial__version__ = 23 ");
		
		createQuery.append("CREATE (c:C {name:'c', __model__element__id__:'c'}) ");
		createQuery.append("SET c.__initial__version__ = 0 ");
		
		createQuery.append("CREATE (d:B {name:'d', __model__element__id__:'d'}) ");
		createQuery.append("SET d.__initial__version__ = 30 ");
		
		createQuery.append("CREATE (e:A {name:'e', __model__element__id__:'e'}) ");
		createQuery.append("SET e.__initial__version__ = 42 ");
		
		createQuery.append("CREATE (x:X {name:'x', __model__element__id__:'X'}) ");
		createQuery.append("SET x.__initial__version__ = 23 ");
		createQuery.append("SET x.__last__version__ = 42 ");
		
		// Edges:
		createQuery.append("CREATE (a)-[relAB:r]->(b) ");
		createQuery.append("SET relAB.__initial__version__ = 23 ");
		createQuery.append("SET relAB.name = 'relAB' ");
		
		createQuery.append("CREATE (b)-[relBC:t]->(c) ");
		createQuery.append("SET relBC.__initial__version__ = 23 ");
		createQuery.append("SET relBC.name = 'relBC' ");
		
		createQuery.append("CREATE (c)<-[relCD:e]-(d) ");
		createQuery.append("SET relCD.__initial__version__ = 31 ");
		createQuery.append("SET relCD.name = 'relAB' ");
		
		createQuery.append("CREATE (d)<-[relDE:r]-(e) ");
		createQuery.append("SET relDE.__initial__version__ = 42 ");
		createQuery.append("SET relDE.name = 'relDE' ");
		
		createQuery.append("CREATE (c)-[relCX:e]->(x) ");
		createQuery.append("SET relCX.__initial__version__ = 25 ");
		createQuery.append("SET relCX.__last__version__ = 42 ");
		createQuery.append("SET relCX.name = 'relCX' ");
		
		if (example >= 1) {
			createQuery.append("CREATE (e)<-[invRelDE:r]-(d) ");
			createQuery.append("SET invRelDE.__initial__version__ = 42 ");
			createQuery.append("SET invRelDE.name = 'invRelDE' ");
			
			createQuery.append("CREATE (d)-[relDB:e]->(b) ");
			createQuery.append("SET relDB.__initial__version__ = 42 ");
			createQuery.append("SET relDB.name = 'relDB' ");
		}
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseURI, databaseName, databasePassword)) {
			transaction.execute(createQuery.toString());
			transaction.commit();
		}
		
		// Create test query:
		ModelCypherAttributeValueDelta avcDelta = new ModelCypherAttributeValueDelta();
		String avcQuery = avcDelta.constructAttributeValueChangeQuery(Arrays.asList(new String[] {"B", "C"}));
		
		Map<String, Object> attributeValueChangeBatchesByLabel = new HashMap<>();
		attributeValueChangeBatchesByLabel.put("oldVersion", 42);
		attributeValueChangeBatchesByLabel.put("newVersion", 43);
		
		// AVC Node B:
		Map<String, Object> avcNodeB = new HashMap<>(2);
		avcNodeB.put("id", "b");
		
		Map<String, Object> attributeValueChangePropertiesB = new HashMap<>(1);
		avcNodeB.put("properties", attributeValueChangePropertiesB);
		attributeValueChangePropertiesB.put("__initial__version__", 43);
		
		List<Object> batch_B = new ArrayList<>();
		batch_B.add(avcNodeB);
		attributeValueChangeBatchesByLabel.put("batch_B", batch_B);
		
		// AVC Node C:
		Map<String, Object> avcNodeC = new HashMap<>(2);
		avcNodeC.put("id", "c");
		
		Map<String, Object> attributeValueChangePropertiesC = new HashMap<>(1);
		avcNodeC.put("properties", attributeValueChangePropertiesC);
		attributeValueChangePropertiesC.put("__initial__version__", 43);
		
		List<Object> batch_C = new ArrayList<>();
		batch_C.add(avcNodeC);
		attributeValueChangeBatchesByLabel.put("batch_C", batch_C);
		
		Map<String, Map<String, Object>> attributeValueChangeQuery = new HashMap<>();
		attributeValueChangeQuery.put(avcQuery, attributeValueChangeBatchesByLabel);
		
		// AVC Node D:
		Map<String, Object> avcNodeD = new HashMap<>(2);
		avcNodeD.put("id", "d");
		
		Map<String, Object> attributeValueChangePropertiesD = new HashMap<>(1);
		avcNodeD.put("properties", attributeValueChangePropertiesD);
		attributeValueChangePropertiesD.put("__initial__version__", 43);
		
		batch_B.add(avcNodeD);
		
		// Run test query:
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseURI, databaseName, databasePassword)) {
			transaction.execute(attributeValueChangeQuery);
			transaction.commit();
		}
		
	}
}
