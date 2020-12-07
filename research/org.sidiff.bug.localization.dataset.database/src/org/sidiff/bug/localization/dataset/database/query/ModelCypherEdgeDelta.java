package org.sidiff.bug.localization.dataset.database.query;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherEdgeDelta extends ModelCypherNodeDelta {
	
	public ModelCypherEdgeDelta(
			int oldVersion, Map<XMLResource, XMLResource> oldResourcesMatch, 
			int newVersion, Map<XMLResource, XMLResource> newResourcesMatch) {
		super(oldVersion, oldResourcesMatch, newVersion, newResourcesMatch);
	}

	public String clearEdges() {
		return "MATCH (a) -[r] -> () delete a, r";
	}

	public String createEdgesDelta() {
		internal_createEdgesDeltas();
		return compileQuery();
	}
	
	protected void internal_createEdgesDeltas() {
		
		// Process matched resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() != null)) {
				internal_createEdgesDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched old resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				internal_createEdgesDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched new resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getNewResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				internal_createEdgesDelta(resourceMatch.getValue(), resourceMatch.getKey());
			}
		}
	}
	
	protected void internal_createEdgesDelta(XMLResource oldResource, XMLResource newResource) {
		
		// Process old nodes to be removed:
		if (oldResource != null) { // initial version?
			for (EObject oldModelElement : (Iterable<EObject>) () -> oldResource.getAllContents()) {
				String modelElementID = oldResource.getID(oldModelElement);
				EObject newModelElement = (newResource != null) ? newResource.getEObject(modelElementID) : null;
				removedEdges(modelElementID, newModelElement, oldModelElement);
			}
		}
		
		// Process new/changed edges to be created:
		if (newResource != null) { // removed resource?
			for (EObject newModelElement : (Iterable<EObject>) () -> newResource.getAllContents()) {
				String modelElementID = newResource.getID(newModelElement);
				EObject oldModelElement = (oldResource != null) ? oldResource.getEObject(modelElementID) : null;
				updateEdges(modelElementID, oldModelElement, newModelElement);
			}
		}
	}
	
	protected Integer getEdge(EObject source, String sourceID, EReference reference, EObject target, String targetID) {
		Integer cypherVariable = createNewVariable();
		getMatchQueries().put(toCypherEdgeMatch(source, sourceID, reference, target, targetID, cypherVariable), cypherVariable);
		return cypherVariable;
	}
	
	private String toCypherEdgeMatch(EObject source, String sourceID, EReference reference, EObject target, String targetID, int cypherVariable) {
		StringBuffer query = new StringBuffer();
		query.append("MATCH ");
		
		// source:
		query.append("(");
		query.append(" : ");
		query.append(toCypherLabel(source.eClass()));
		query.append(" { ");
		query.append("__model__element__id__: '");
		query.append(sourceID);
		query.append("' })");
		
		// edge variable:
		query.append("-[");
		query.append("v");
		query.append(cypherVariable);
		query.append(":");
		query.append(toCypherLabel(reference));
		query.append("]->");
		
		// target:
		query.append("(");
		query.append(" : ");
		query.append(toCypherLabel(target.eClass()));
		query.append(" { ");
		query.append("__model__element__id__: '");
		query.append(targetID);
		query.append("' })");
		
		return query.toString();
	}
	
	protected String toCypherLabel(EReference type) {
		return type.getName();
	}

	private void removedEdges(String modelElementID, EObject newModelElement, EObject oldModelElement) {
		for (EReference reference : oldModelElement.eClass().getEAllReferences()) {
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

	private void setLastVersionIfDeltedEdge(
			String sourceID, EObject sourceNew, EObject sourceOld, EReference reference, EObject targetOld) {

		Resource targetResourceOld = targetOld.eResource();

		if (targetResourceOld instanceof XMLResource) {
			String targetID = ((XMLResource) targetResourceOld).getID(targetOld);
			XMLResource targetResourceNew = getNewResource(targetResourceOld);

			if (isRemovedEdge(sourceNew, reference, targetResourceNew, targetID)) {
				removeEdge(sourceOld, sourceID, reference, targetOld, targetID);
			}
		}
	}
	
	private void removeEdge(EObject sourceOld, String sourceID, EReference reference, EObject targetOld, String targetID) {
		Integer cypherVariable = getEdge(sourceOld, sourceID, reference, targetOld, targetID);
		
		StringBuilder query = new StringBuilder();
		query.append("SET ");
		query.append("v");
		query.append(cypherVariable);
		query.append(".__last__version__ = ");
		query.append(getOldVersion());
		
		getSetQueries().add(query.toString());
	}

	private boolean isRemovedEdge(EObject sourceNew, EReference reference, XMLResource targetResourceNew, String targetID) {
		
		// removed source node?
		if (sourceNew == null) {
			return true; // all edges are removed
		} else {
			EObject targetNew = targetResourceNew.getEObject(targetID);
			
			// removed target node?
			if (targetNew == null) {
				return true; // edge is removed
			} else {
				return !edgeExists(sourceNew, reference, targetNew);
			}
		}
	}

	private void updateEdges(String modelElementID, EObject oldModelElement, EObject newModelElement) {
		StringBuffer query = new StringBuffer();
		
		for (EReference reference : newModelElement.eClass().getEAllReferences()) {
			if (reference.isMany()) {
				@SuppressWarnings("unchecked")
				Collection<Object> targets = (Collection<Object>) newModelElement.eGet(reference);

				for (Object target : targets) {
					if (target instanceof EObject) {
						String newEdgeQuery = createIfNewEdge(modelElementID, oldModelElement, newModelElement, reference, (EObject) target);
						
						if (newEdgeQuery != null) {
							query.append(newEdgeQuery);
							query.append(", ");
						}
					}
				}
			} else {
				Object target = newModelElement.eGet(reference);
				
				if (target instanceof EObject) {
					String newEdgeQuery = createIfNewEdge(modelElementID, oldModelElement, newModelElement, reference, (EObject) target);
					
					if (newEdgeQuery != null) {
						query.append(newEdgeQuery);
						query.append(", ");
					}
				}
			}
		}
		
		if (query.length() > 0) {
			query.deleteCharAt(query.length() - 1); // last ,~
			query.deleteCharAt(query.length() - 1);
			
			getCreateQueries().add(query.toString());
		}
	}

	private String createIfNewEdge(
			String sourceID, EObject sourceOld, EObject sourceNew, EReference reference, EObject targetNew) {

		Resource targetResourceNew = targetNew.eResource();

		if (targetResourceNew instanceof XMLResource) {
			String targetID = ((XMLResource) targetResourceNew).getID(targetNew);
			XMLResource targetResourceOld = getOldResource(targetResourceNew);

			if (isNewEdge(sourceOld, reference, targetResourceOld, targetID)) {
				return createEdge(sourceID, sourceNew, reference, targetID, targetNew);
			}
		}
		
		return null;
	}

	private boolean isNewEdge(EObject sourceOld, EReference reference, XMLResource targetResourceOld, String targetID) {
		
		// new source node?
		if (sourceOld == null) {
			return true; // all edges are new
		} else {
			EObject targetOld = targetResourceOld.getEObject(targetID);
			
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

	private String createEdge(String sourceID, EObject source, EReference reference, String targetID, EObject target) {
		StringBuffer query = new StringBuffer();
		
		// source
		query.append("(v");
		query.append(getNode(sourceID, source));
		query.append(")-[:");
		query.append(toCypherLabel(reference));
		
		// version:
		query.append(" { ");
		query.append("__initial__version__: ");
		query.append(getNewVersion());
		query.append(", ");
		
		// derived:
		query.append("__derived__: ");
		query.append(reference.isDerived());
		query.append(", ");
		
		// containment:
		query.append("__containment__: ");
		query.append(reference.isContainment());
		query.append("} ");
		
		// target
		query.append("]->(");
		query.append("v");
		query.append(getNode(targetID, target));
		query.append(")");
	
		return query.toString();
	}
}
