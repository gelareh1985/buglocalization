package org.sidiff.bug.localization.dataset.database.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherEdgeDelta;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherNodeDelta;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jUtil;

public class ModelDelta {
	
	public static boolean PROFILE = true;
	
	private Neo4jTransaction transaction;
	
	public ModelDelta(Neo4jTransaction transaction) {
		this.transaction = transaction;
	}
	
	public void clearDatabase() {
		Neo4jUtil.clearDatabase(transaction);
	}

	public void commitDelta(int version, URI oldBaseURI, List<Resource> oldResources, URI newBaseURI, List<Resource> newResources) {
		long time = System.currentTimeMillis();
		
		Map<XMLResource, XMLResource> oldResourcesMatch = matchResources(oldBaseURI, oldResources, newBaseURI, newResources);
		Map<XMLResource, XMLResource> newResourcesMatch = matchResources(newBaseURI, newResources, oldBaseURI, oldResources);
		
		time = stopTime(time, "Compute Resource Match");
		
		ModelCypherNodeDelta nodeDeltaDerivation = new ModelCypherNodeDelta(
				version - 1, oldBaseURI, oldResourcesMatch,
				version, newBaseURI, newResourcesMatch);
		
		Map<String, Map<String, Object>> removedNodeQueries = nodeDeltaDerivation.constructRemovedNodesQueries();
		Map<String, Map<String, Object>> createdNodeQueries = nodeDeltaDerivation.constructCreatedNodesQueries();
		
		ModelCypherEdgeDelta edgeDeltaDerivation = new ModelCypherEdgeDelta(
				version - 1, oldBaseURI, oldResourcesMatch,
				version, newBaseURI, newResourcesMatch, 
				nodeDeltaDerivation.getModifiedNodes());
		
		Map<String, Map<String, Object>> removedEdgeQueries = edgeDeltaDerivation.constructRemovedEdgesQuery();
		Map<String, Map<String, Object>> createdEdgeQueries = edgeDeltaDerivation.constructCreatedEdgesQuery();
		
		time = stopTime(time, "Compute Delta Queries");
		
		/*
		 * Make sure all nodes have an index for creation of edges.
		 */
		createNodeKeyAttributes(transaction, nodeDeltaDerivation);
		
		/*
		 *  Increase all latest version IDs and remove nodes in the next step.
		 */
		transaction.execute(nodeDeltaDerivation.constructIncreaseVersionQuery());
		transaction.execute(edgeDeltaDerivation.constructIncreaseVersionQuery());
		
		/*
		 * NOTE: Remove edges before removing nodes, otherwise source and target nodes
		 * might have different version number.
		 */
		transaction.execute(removedEdgeQueries);
		
		/*
		 * NOTE: Remove old nodes before creating new nodes, e.g., to correctly match
		 * node with same ID but changed attribute values.
		 */
		transaction.execute(removedNodeQueries);
		transaction.execute(createdNodeQueries);
		transaction.execute(createdEdgeQueries);
		time = stopTime(time, "Execute Transaction");
		
		/*
		 * Atomic commit of new model version.
		 */
		transaction.commit();
		time = stopTime(time, "Commit Transaction");
	}
	
	private Map<XMLResource, XMLResource> matchResources(URI baseUriA, List<Resource> resourceSetA, URI baseUriB, List<Resource> resourceSetB) {
		Map<XMLResource, XMLResource> resourcesMatch = new HashMap<>();
		
		if (resourceSetA == null) {
			resourceSetA = Collections.emptyList();
		}
		
		if (resourceSetB == null) {
			resourceSetB = Collections.emptyList();
		}
		
		for (Resource resourceA : resourceSetA) {
			resourcesMatch.put((XMLResource) resourceA, (XMLResource) matchResource(baseUriA, resourceA, baseUriB, resourceSetB));
		}
		
		return resourcesMatch;
	}

	protected Resource matchResource(URI baseUriA, Resource resourceA, URI baseUriB, List<Resource> resourceSetB) {
		URI uriA =  ModelDeltaUtil.getRelativeURI(baseUriA, resourceA.getURI());
		
		for (Resource resourceB : resourceSetB) {
			URI uriB = ModelDeltaUtil.getRelativeURI(baseUriB, resourceB.getURI());
			
			if (uriA.equals(uriB)) {
				return resourceB;
			}
		}
		
		return null;
	}
	
	private void createNodeKeyAttributes(Neo4jTransaction transaction, ModelCypherNodeDelta nodeDeltaDerivation) {
		transaction.commit();
		
		List<String> nodeKeyAttributes = nodeDeltaDerivation.constructKeyAttributesQuery();
		
		for (String string : nodeKeyAttributes) {
			transaction.execute(string);
			transaction.commit();
		}
		
		if (!nodeKeyAttributes.isEmpty()) {
			transaction.awaitIndexOnline();
		}
	}

	private long stopTime(long startTime, String text) {
		if (PROFILE) {
			System.out.println(text + ": " + (System.currentTimeMillis() - startTime) + "ms");
			return System.currentTimeMillis();
		}
		return -1;
	}
}
