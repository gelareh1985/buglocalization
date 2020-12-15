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
		
		// Process old nodes to be removed:
		if (oldResource != null) { // initial version?
			for (EObject oldModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(oldResource, true)) {
				String modelElementID = getModelElementID(oldResource, oldModelElement);
				EObject newModelElement = (newResource != null) ? getModelElement(newResource, modelElementID) : null;
				deriveRemovedEdges(modelElementID, newModelElement, oldModelElement);
			}
		}
		
		// Process new/changed edges to be created:
		if (newResource != null) { // removed resource?
			for (EObject newModelElement : (Iterable<EObject>) () -> EcoreUtil.getAllProperContents(newResource, true)) {
				String modelElementID = getModelElementID(newResource, newModelElement);
				EObject oldModelElement = (oldResource != null) ? getModelElement(oldResource, modelElementID) : null;
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
					
					for (Object target : targets) {
						if (target instanceof EObject) {
							setLastVersionIfDeltedEdge(modelElementID, newModelElement, oldModelElement, reference, (EObject) target);
						}
					}
				} else {
					Object target = oldModelElement.eGet(reference);
					
					if (target instanceof EObject) {
						setLastVersionIfDeltedEdge(modelElementID, newModelElement, oldModelElement, reference, (EObject) target);
					}
				}
			}
		}
	}

	private void setLastVersionIfDeltedEdge(
			String sourceID, EObject sourceNew, EObject sourceOld, EReference reference, EObject targetOld) {
	
		Resource targetResourceOld = targetOld.eResource();
	
		if (targetResourceOld instanceof XMLResource) {
			String targetID = getModelElementID((XMLResource) targetResourceOld, targetOld);
			XMLResource targetResourceNew = getNewResource(targetResourceOld);
	
			if (isRemovedEdge(sourceNew, reference, targetResourceNew, targetID)) {
				Map<String, Object> properties = new HashMap<>();
				properties.put("__last__version__", getOldVersion());
				
				addEdgeToBatch(sourceID, sourceOld, reference, targetID, targetOld, properties, removedEdgesBatches);
			}
		}
	}

	private boolean isRemovedEdge(EObject sourceNew, EReference reference, XMLResource targetResourceNew, String targetID) {
		
		// removed source node?
		if ((sourceNew == null) || (targetResourceNew == null)) {
			return true; // all edges are removed
		} else {
			EObject targetNew = getModelElement(targetResourceNew, targetID);
			
			// removed target node?
			if (targetNew == null) {
				return true; // edge is removed
			} else {
				return !edgeExists(sourceNew, reference, targetNew);
			}
		}
	}

	private void deriveCreatedEdges(String modelElementID, EObject oldModelElement, EObject newModelElement) {
		for (EReference reference : newModelElement.eClass().getEAllReferences()) {
			if (isConsideredFeature(reference)) {
				if (reference.isMany()) {
					@SuppressWarnings("unchecked")
					Collection<Object> targets = (Collection<Object>) newModelElement.eGet(reference);
					
					for (Object target : targets) {
						if (target instanceof EObject) {
							createIfNewEdge(modelElementID, oldModelElement, newModelElement, reference, (EObject) target);
						}
					}
				} else {
					Object target = newModelElement.eGet(reference);
					
					if (target instanceof EObject) {
						createIfNewEdge(modelElementID, oldModelElement, newModelElement, reference, (EObject) target);
					}
				}
			}
		}
	}

	private void createIfNewEdge(String sourceID, EObject sourceOld, EObject sourceNew, EReference reference, EObject targetNew) {
		Resource targetResourceNew = targetNew.eResource();
	
		if (targetResourceNew instanceof XMLResource) {
			String targetID = getModelElementID((XMLResource) targetResourceNew, targetNew);
			XMLResource targetResourceOld = getOldResource(targetResourceNew);
	
			if (isNewEdge(sourceOld, reference, targetResourceOld, targetID)) {
				
				Map<String, Object> createEdgeProperties = new HashMap<>();
				createEdgeProperties.put("__containment__", reference.isContainment());
				createEdgeProperties.put("__container__", reference.isContainer());
				createEdgeProperties.put("__initial__version__", getNewVersion());
				createEdgeProperties.put("__last__version__", getNewVersion());
				
				addEdgeToBatch(sourceID, sourceNew, reference, targetID, targetNew, createEdgeProperties, createdEdgesBatches);
			}
		}
	}

	private boolean isNewEdge(EObject sourceOld, EReference reference, XMLResource targetResourceOld, String targetID) {
		
		// new source node?
		if ((sourceOld == null) || (targetResourceOld == null)) {
			return true; // all edges are new
		} else {
			EObject targetOld = getModelElement(targetResourceOld, targetID);
			
			// new target node?
			if (targetOld == null) {
				return true; // edge is new
			} else {
				return !edgeExists(sourceOld, reference, targetOld);
			}
		}
	}

	private boolean edgeExists(EObject source, EReference reference, EObject target) {
		if (reference.isMany()) {
			@SuppressWarnings("unchecked")
			Collection<Object> targets = (Collection<Object>) source.eGet(reference);
			return targets.contains(target);
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

	public String increaseVersion() {
		return "MATCH (from)-[rel {__last__version__: " + getOldVersion() + "}]->(to) SET rel.__last__version__ = " + getNewVersion();
	}

	public Map<String, Map<String, Object>> createdEdgesDelta() {
		return computeEdgeBatchQueries(false, createdEdgesBatches);
	}
	
	public Map<String, Map<String, Object>> removedEdgesDelta() {
		return computeEdgeBatchQueries(true, removedEdgesBatches);
	}
	
	private Map<String, Map<String, Object>> computeEdgeBatchQueries(boolean match, Map<String, Map<String, Map<String, List<Map<String, Object>>>>> edges) {
		if (!edges.isEmpty()) {
			Map<String, Map<String, Object>> createEdgeQueriesByLabel = new HashMap<>();
			
			for (Entry<String, Map<String, Map<String, List<Map<String, Object>>>>> edgeLabel2SourceLabel : edges.entrySet()) {
				for (Entry<String, Map<String, List<Map<String, Object>>>> sourceLabel2TargetLabel : edgeLabel2SourceLabel.getValue().entrySet()) {
					for (Entry<String, List<Map<String, Object>>> targetLabel2Parameter : sourceLabel2TargetLabel.getValue().entrySet()) {
						List<Map<String, Object>> parameterBatch = targetLabel2Parameter.getValue();
						String query = computeEdgeBatchQuery(sourceLabel2TargetLabel.getKey(), edgeLabel2SourceLabel.getKey(), targetLabel2Parameter.getKey(), match);
						
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
	
	private String computeEdgeBatchQuery(String sourceLabel, String edgeLabel, String targetLabel, boolean match) {
		StringBuilder query = new StringBuilder();
		query.append("UNWIND $batch as entry ");
		
		// TODO: Combine labels: MATCH n-[r:LABEL1|LABEL2]->t !?
		query.append("MATCH (from:");
		query.append(sourceLabel);
		query.append(" {__model__element__id__: entry.from, __last__version__: ");
		query.append(getNewVersion());
		query.append("}) ");
		
		query.append("USING INDEX from:");
		query.append(sourceLabel);
		query.append("(__model__element__id__) ");
		
		query.append("MATCH (to:");
		query.append(targetLabel);
		query.append(" {__model__element__id__: entry.to, __last__version__: ");
		query.append(getNewVersion());
		query.append("}) ");
		
		query.append("USING INDEX to:");
		query.append(targetLabel);
		query.append("(__model__element__id__) ");
		
		if (match) {
			query.append("MATCH (from)-[rel:");
			query.append(edgeLabel);
			query.append("]->(to) ");
		} else {
			query.append("CREATE (from)-[rel:");
			query.append(edgeLabel);
			query.append(" { __last__version__: ");
			query.append(getNewVersion());
			query.append("}]->(to) "); 
		}
		
		query.append("SET rel += entry.properties");
				
		return query.toString();
	}
	
	public static String clearEdges() {
		return "MATCH (a) -[r] -> () DELETE a, r";
	}
}
