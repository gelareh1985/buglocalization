package org.sidiff.bug.localization.dataset.database.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherEdgeDelta extends ModelCypherDelta {
	
	/**
	 * Edge-Label -> Source-Label -> Target-Label -> Batch[{Parameter -> Value}]
	 */
	private Map<String, Map<String, Map<String, List<Map<String, Object>>>>> createdEdgesBatches;
	
	/**
	 * Edge-Label -> Source-Label -> Target-Label -> Batch[{Parameter -> Value}]
	 */
	private Map<String, Map<String, Map<String, List<Map<String, Object>>>>> removedEdgesBatches;
	
	public ModelCypherEdgeDelta(
			int oldVersion, URI oldBaseURI, Map<XMLResource, XMLResource> oldResourcesMatch, 
			int newVersion, URI newBaseURI,Map<XMLResource, XMLResource> newResourcesMatch) {
		super(oldVersion, oldBaseURI, oldResourcesMatch, newVersion, newBaseURI, newResourcesMatch);
		this.createdEdgesBatches = new HashMap<>();
		this.removedEdgesBatches = new HashMap<>();
		deriveEdgeDeltas();
	}

	/**
	 * @return <code>true</code> to delete and create a new edge if the index of an
	 *         edge in the containing list has changed; <code>false</code> otherwise.
	 */
	public boolean considerEdgeIndex(EReference reference) {
		return false; // A list of sorting relevant references, e.g., parameter lists.  
	}

	private void deriveEdgeDeltas() {

		// Process matched resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() != null)) {
				deriveEdgeDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched old resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				deriveEdgeDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched new resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getNewResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				deriveEdgeDelta(resourceMatch.getValue(), resourceMatch.getKey());
			}
		}
	}

	private void deriveEdgeDelta(XMLResource oldResource, XMLResource newResource) {
		
		// Process old edges to be removed:
		if (oldResource != null) { // initial version?
			for (EObject oldModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(oldResource, true)) {
				String modelElementID = getModelElementID(oldResource, oldModelElement);
				EObject newModelElement = (newResource != null) ? getModelElement(newResource, oldModelElement.eClass(), modelElementID) : null;
				deriveRemovedEdges(modelElementID, newModelElement, oldModelElement);
			}
		}
		
		// Process new/changed edges to be created:
		if (newResource != null) { // removed resource?
			for (EObject newModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(newResource, true)) {
				String modelElementID = getModelElementID(newResource, newModelElement);
				EObject oldModelElement = (oldResource != null) ? getModelElement(oldResource, newModelElement.eClass(), modelElementID) : null;
				deriveCreatedEdges(modelElementID, oldModelElement, newModelElement);
			}
		}
	}

	private void deriveRemovedEdges(String modelElementID, EObject newModelElement, EObject oldModelElement) {
		for (EReference reference : oldModelElement.eClass().getEAllReferences()) {
			if (isConsideredFeature(reference)) {
				if (reference.isMany()) {
					@SuppressWarnings("unchecked")
					Collection<Object> targets = (Collection<Object>) oldModelElement.eGet(reference);
					int index = 0;
					
					for (Object target : targets) {
						if (target instanceof EObject) {
							deriveRemovedEdgeBatchQuery(index, modelElementID, newModelElement, oldModelElement, reference, (EObject) target);
						}
						++index;
					}
				} else {
					Object oldTarget = oldModelElement.eGet(reference);
					
					if (oldTarget instanceof EObject) {
						deriveRemovedEdgeBatchQuery(-1, modelElementID, newModelElement, oldModelElement, reference, (EObject) oldTarget);
					}
				}
			}
		}
	}

	private void deriveRemovedEdgeBatchQuery(
			int index, String sourceID, EObject sourceNew, 
			EObject sourceOld, EReference reference, EObject targetOld) {
	
		Resource targetResourceOld = targetOld.eResource();
	
		if ((targetResourceOld instanceof XMLResource) && isInOldResourceScope(targetResourceOld)) {
			String targetID = getModelElementID((XMLResource) targetResourceOld, targetOld);
	
			if (isRemovedEdge(index, sourceNew, reference,
					targetResourceOld, targetID, targetOld)) {
				Map<String, Object> properties = new HashMap<>();
				properties.put("__last__version__", getOldVersion());
				
				addEdgeToBatch(sourceID, sourceOld, reference, targetID, targetOld, properties, removedEdgesBatches);
			}
		}
	}

	private boolean isRemovedEdge(int index, EObject sourceNew, EReference reference, 
			Resource targetResourceOld, String targetID, EObject targetOld) {
		
		// removed source node?
		if (sourceNew == null) {
			return true; // all edges are removed
		} else {
			XMLResource targetResourceNew = getNewResource(targetResourceOld);
			
			if (targetResourceNew == null) {
				return true; // all edges are removed
			} else {
				EObject targetNew = getModelElement(targetResourceNew, targetOld.eClass(), targetID);
				
				// removed target node?
				if (targetNew == null) {
					return true; // edge is removed
				} else {
					return !isExistingEdge(index, sourceNew, reference, targetNew);
				}
			}
		}
	}

	private void deriveCreatedEdges(String modelElementID, EObject oldModelElement, EObject newModelElement) {
		for (EReference reference : newModelElement.eClass().getEAllReferences()) {
			if (isConsideredFeature(reference)) {
				if (reference.isMany()) {
					@SuppressWarnings("unchecked")
					Collection<Object> targets = (Collection<Object>) newModelElement.eGet(reference);
					int index = 0;
					
					for (Object target : targets) {
						if (target instanceof EObject) {
							deriveCreatedEdgeBatchQuery(index, modelElementID, oldModelElement, newModelElement, reference, (EObject) target);
						}
						++index;
					}
				} else {
					Object newTarget = newModelElement.eGet(reference);
					
					if (newTarget instanceof EObject) {
						deriveCreatedEdgeBatchQuery(-1, modelElementID, oldModelElement, newModelElement, reference, (EObject) newTarget);
					}
				}
			}
		}
	}

	private void deriveCreatedEdgeBatchQuery(int index, String sourceID, EObject sourceOld, EObject sourceNew, EReference reference, EObject targetNew) {
		Resource targetResourceNew = targetNew.eResource();
	
		if ((targetResourceNew instanceof XMLResource) && isInNewResourceScope(targetResourceNew)) {
			String targetID = getModelElementID((XMLResource) targetResourceNew, targetNew);
	
			if (isCreatedEdge(index, sourceOld, reference, targetResourceNew, targetID, targetNew)) {
				Map<String, Object> createEdgeProperties = new HashMap<>();
				createEdgeProperties.put("__containment__", reference.isContainment());
				createEdgeProperties.put("__container__", reference.isContainer());
				createEdgeProperties.put("__lower__bound__", reference.getLowerBound());
				createEdgeProperties.put("__upper__bound__", reference.getUpperBound());
				
				if (considerEdgeIndex(reference) && (index != -1)) {
					createEdgeProperties.put("__index__", index);
				}
				
				createEdgeProperties.put("__initial__version__", getNewVersion());
				
				addEdgeToBatch(sourceID, sourceNew, reference, targetID, targetNew, createEdgeProperties, createdEdgesBatches);
			}
		}
	}

	private boolean isCreatedEdge(int index, EObject sourceOld, EReference reference, 
			Resource targetResourceNew, String targetID, EObject targetNew) {
		
		// new source node?
		if (sourceOld == null) {
			return true; // all edges are new
		} else {
			XMLResource targetResourceOld = getOldResource(targetResourceNew);
			
			if (targetResourceOld == null) {
				return true; // all edges are new
			} else {
				EObject targetOld = getModelElement(targetResourceOld, targetNew.eClass(), targetID);
				
				// new target node?
				if (targetOld == null) {
					return true; // edge is new
				} else {
					return !isExistingEdge(index, sourceOld, reference, targetOld);
				}
			}
		}
	}

	private boolean isExistingEdge(int index, EObject source, EReference reference, EObject target) {
		if (reference.isMany()) {
			@SuppressWarnings("unchecked")
			List<Object> targets = (List<Object>) source.eGet(reference);
			
			if (considerEdgeIndex(reference)) {
				// TODO: This might be optimized by just storing and reconstructing the index by
				// its index number at insertion time. All old edges, which do not change their
				// relative position to each other (moving edge), will be untouched.
				if (targets.size() > index) {
					return (targets.get(index) == target);
				} else {
					return false;
				}
			} else {
				if ((index < targets.size()) && (targets.get(index) == target)) {
					return true; // for optimization
				} else {
					return targets.contains(target);
				}
			}
		} else {
			return (source.eGet(reference) == target);
		}
	}

	private void addEdgeToBatch(String sourceID, EObject source, EReference reference, String targetID, EObject target, 
			Map<String, Object> properties, Map<String, Map<String, Map<String, List<Map<String, Object>>>>> edgesBatch) {
		
		// source target:
		Map<String, Object> createEdge = new HashMap<>();
		createEdge.put("from", sourceID);
		createEdge.put("to", targetID);
	
		// properties:
		createEdge.put("properties", properties);
		
		// labels:
		String sourceLabel = toCypherLabel(source.eClass());
		String targetLabel = toCypherLabel(target.eClass());
		String edgelabel = toCypherLabel(reference);
	
		// store query by labels:
		Map<String, Map<String, List<Map<String, Object>>>> edgeLabel2SourceLabel = edgesBatch.getOrDefault(edgelabel, new HashMap<>());
		edgesBatch.put(edgelabel, edgeLabel2SourceLabel);
		
		Map<String, List<Map<String, Object>>> sourceLabel2TargetLabel = edgeLabel2SourceLabel.getOrDefault(sourceLabel, new HashMap<>());
		edgeLabel2SourceLabel.put(sourceLabel, sourceLabel2TargetLabel);
		
		List<Map<String, Object>> targetLabel2Parameter = sourceLabel2TargetLabel.getOrDefault(targetLabel, new ArrayList<>());
		sourceLabel2TargetLabel.put(targetLabel, targetLabel2Parameter);
		
		targetLabel2Parameter.add(createEdge);
	}

	public Map<String, Map<String, Object>> constructCreatedEdgesQuery() {
		return constructEdgeBatchQueries(false, createdEdgesBatches);
	}
	
	public Map<String, Map<String, Object>> constructRemovedEdgesQuery() {
		return constructEdgeBatchQueries(true, removedEdgesBatches);
	}
	
	private Map<String, Map<String, Object>> constructEdgeBatchQueries(boolean match, Map<String, Map<String, Map<String, List<Map<String, Object>>>>> edges) {
		if (!edges.isEmpty()) {
			Map<String, Map<String, Object>> createEdgeQueriesByLabel = new HashMap<>();
			
			for (Entry<String, Map<String, Map<String, List<Map<String, Object>>>>> edgeLabel2SourceLabel : edges.entrySet()) {
				for (Entry<String, Map<String, List<Map<String, Object>>>> sourceLabel2TargetLabel : edgeLabel2SourceLabel.getValue().entrySet()) {
					for (Entry<String, List<Map<String, Object>>> targetLabel2Parameter : sourceLabel2TargetLabel.getValue().entrySet()) {
						List<Map<String, Object>> parameterBatch = targetLabel2Parameter.getValue();
						String query = constructEdgeBatchQuery(sourceLabel2TargetLabel.getKey(), edgeLabel2SourceLabel.getKey(), targetLabel2Parameter.getKey(), match);
						
						Map<String, Object> createEdgesBatch = new HashMap<>(1);
						createEdgesBatch.put("batch", parameterBatch);
						
						createEdgeQueriesByLabel.put(query, createEdgesBatch);
					}
				}
			}
			
			return createEdgeQueriesByLabel;
		} else {
			return Collections.emptyMap();
		}
	}	
	
	private String constructEdgeBatchQuery(String sourceLabel, String edgeLabel, String targetLabel, boolean match) {
		
		/*
		 * TODO: Combine multiple source and target labels!? Fewer queries, but less efficient index search.
		 * 
			CALL {
				MATCH (from:Package {__model__element__id__: 'workspace::eclipse.jdt.core', __last__version__: 994}) USING INDEX from:Package(__model__element__id__) 
				RETURN from 
				UNION MATCH (from:Model {__model__element__id__: 'workspace::eclipse.jdt.core', __last__version__: 994}) USING INDEX from:Model(__model__element__id__) 
				RETURN from 
			}
			CALL {
				MATCH (to:Package {__model__element__id__: 'project/P/org.eclipse.jdt.core.tests.builder', __last__version__: 994}) USING INDEX to:Package (__model__element__id__) 
				RETURN to
				UNION MATCH (to:Model {__model__element__id__: 'project/P/org.eclipse.jdt.core.tests.builder', __last__version__: 994}) USING INDEX to:Model(__model__element__id__) 
				RETURN to
			}
			RETURN from, to
		 */
		 
		StringBuilder query = new StringBuilder();
		query.append("UNWIND $batch as entry ");
		
		// Source:
//		query.append("CALL { WITH entry ");
		
		query.append("MATCH (from:");
		query.append(sourceLabel);
		query.append(" {__model__element__id__: entry.from}) ");
		
		query.append("USING INDEX from:");
		query.append(sourceLabel);
		query.append("(__model__element__id__) ");
		
		query.append("WHERE NOT EXISTS(from.__last__version__) ");
		
//		query.append("RETURN from LIMIT 1} ");
		
		// Target:
//		query.append("CALL { WITH entry ");
		
		query.append("MATCH (to:");
		query.append(targetLabel);
		query.append(" {__model__element__id__: entry.to}) ");
		
		query.append("USING INDEX to:");
		query.append(targetLabel);
		query.append("(__model__element__id__) ");
		
		query.append("WHERE NOT EXISTS(to.__last__version__) "); // not removed
		
//		query.append("RETURN to LIMIT 1} ");
		
		// Reference:
		if (match) {
//			query.append("CALL { ");
			
			query.append("MATCH (from)-[rel:");
			query.append(edgeLabel);
			query.append("]->(to) ");
			
			query.append("WHERE NOT EXISTS(to.__last__version__) "); // not removed
			
//			query.append("RETURN rel LIMIT 1} ");
		} else {
			query.append("CREATE (from)-[rel:");
			query.append(edgeLabel);
			query.append("]->(to) "); 
		}
		
		query.append("SET rel += entry.properties");
				
		return query.toString();
	}
	
	public static String constructClearEdgesQuery() {
		return "MATCH (a) -[r] -> () DELETE a, r";
	}
}
