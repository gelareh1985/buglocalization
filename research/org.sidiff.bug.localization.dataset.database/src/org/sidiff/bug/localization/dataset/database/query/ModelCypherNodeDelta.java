package org.sidiff.bug.localization.dataset.database.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherNodeDelta extends ModelCypherDelta {
	
	private Map<String, Integer> modelElementIDToCypherVariable;
	
	public ModelCypherNodeDelta(
			int oldVersion, Map<XMLResource, XMLResource> oldResourcesMatch,
			int newVersion, Map<XMLResource, XMLResource> newResourcesMatch) {
		super(oldVersion, oldResourcesMatch, newVersion, newResourcesMatch);
		this.modelElementIDToCypherVariable = new HashMap<>();
	}
	
	public String clearNodes() {
		return "MATCH (a) delete a";
	}
	
	public String createNodesDelta() {
		internal_createNodesDeltas();
		return compileQuery();
	}
	
	protected void internal_createNodesDeltas() {
		
		// Process matched resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() != null)) {
				internal_createNodesDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched old resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getOldResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				internal_createNodesDelta(resourceMatch.getKey(), resourceMatch.getValue());
			}
		}
		
		// Process unmatched new resources:
		for (Entry<XMLResource, XMLResource> resourceMatch : getNewResourcesMatch().entrySet()) {
			if ((resourceMatch.getKey() != null) && (resourceMatch.getValue() == null)) {
				internal_createNodesDelta(resourceMatch.getValue(), resourceMatch.getKey());
			}
		}
	}

	private void internal_createNodesDelta(XMLResource oldResource, XMLResource newResource) {
		
		// Process old nodes to be removed:
		if (oldResource != null) { // initial version?
			for (EObject oldModelElement : (Iterable<EObject>) () -> oldResource.getAllContents()) {
				removedNodes(oldResource, oldModelElement);
			}
		}
		
		// Process new/changed nodes to be created:
		if (newResource != null) { // removed resource?
			for (EObject newModelElement : (Iterable<EObject>) () -> newResource.getAllContents()) {
				updateNodes(newResource, newModelElement);
			}
		}
	}
	
	protected Integer getNode(String modelElementID, EObject modelElement) {
		Integer cypherVariable = modelElementIDToCypherVariable.get(modelElementID);
		
		if (cypherVariable == null) {
			cypherVariable = createNewVariable();
			getMatchQueries().put(toCypherNodeMatch(modelElement, modelElementID, cypherVariable), cypherVariable);
		}
		
		return cypherVariable;
	}
	
	private String toCypherNodeMatch(EObject modelElement, String modelElementID, int cypherVariable) {
		StringBuffer query = new StringBuffer();
		query.append("MATCH (");
		query.append("v");
		query.append(cypherVariable);
		query.append(":");
		query.append(toCypherLabel(modelElement.eClass()));
		
		// model ID:
		query.append(" { ");
		query.append("__model__element__id__: '");
		query.append(modelElementID);
		query.append("' }");
		
		query.append(")");
		
		modelElementIDToCypherVariable.put(modelElementID, cypherVariable);
		return query.toString();
	}

	protected String toCypherLabel(EClass type) {
		return type.getName();
	}
	
	private void removedNodes(XMLResource oldResource, EObject oldModelElement) {
		
		// model ID of the old version node:
		String modelElementID = oldResource.getID(oldModelElement);
		
		// Is removed model element?
		XMLResource newResource = getNewResource(oldResource);
		
		if ((newResource == null) || newResource.getEObject(modelElementID) == null) {
			int cypherVariable = getNode(modelElementID, oldModelElement);
			
			// Set last version:
			StringBuilder setQuery = new StringBuilder();
			setQuery.append("SET ");
			setQuery.append("v");
			setQuery.append(cypherVariable);
			setQuery.append(".__last__version__ = ");
			setQuery.append(getOldVersion());
			
			getSetQueries().add(setQuery.toString());
		}
	}
	
	private void updateNodes(XMLResource newResource, EObject newModelElement) {
		
		// model ID of the new version node:
		String modelElementID = newResource.getID(newModelElement);
		
		// model element of the old version node:
		EObject modelElementOld = null;
		XMLResource oldResource = getOldResource(newResource);
		
		if (oldResource != null) {
			modelElementOld = oldResource.getEObject(modelElementID);
		}
		
		// create query for new node:
		createNode(modelElementOld, newModelElement, modelElementID);
	}

	/**
	 * @return A create-query if the object is new or has changed attributes; <code>null</code> otherwise.
	 */
	private void createNode(EObject modelElementOld, EObject modelElementNew, String modelElementID) {
		StringBuffer query = new StringBuffer();
		
		// type:
		query.append(":");
		query.append(toCypherLabel(modelElementNew.eClass()));
		
		// attributes:
		query.append("{ ");
		
		/// model namespace:
		query.append("__model__ns__uri__: '");
		query.append(modelElementNew.eClass().getEPackage().getNsURI());
		query.append("', ");
		
		// model ID:
		query.append("__model__element__id__: '");
		query.append(modelElementID);
		query.append("', ");
		
		// version:
		query.append("__initial__version__: ");
		query.append(getNewVersion());
		query.append(", ");
		
		// model attributes:
		boolean hasChanged = false;
		
		for (EAttribute attribute : modelElementNew.eClass().getEAllAttributes()) {
			String oldValue = toCypherValue(modelElementOld, attribute);
			String newValue = toCypherValue(modelElementNew, attribute);
			
			if (hasValueChanged(modelElementOld, oldValue, newValue)) {
				hasChanged = true;
			}
			
			query.append(attribute.getName());
			query.append(": ");
			query.append(newValue);
			query.append(", ");
		}
		
		query.deleteCharAt(query.length() - 2); // last ,~
		query.append("}");
		query.append(")");
		
		// new/changed node?
		if (hasChanged) {
			
			// variable:
			Integer cypherVariable = createNewVariable(); // the associated query variable
			query.insert(0, "(v" + cypherVariable);
			
			modelElementIDToCypherVariable.put(modelElementID, cypherVariable);
			getCreateQueries().add(query.toString());
		}
	}

	private boolean hasValueChanged(EObject objOld, String oldValue, String newValue) {
		return (objOld == null) 
				|| ((oldValue == null) && (newValue != null))
				|| ((newValue == null) && (oldValue != null))
				|| ((oldValue != null) && !oldValue.equals(newValue));
	}

	private String toCypherValue(EObject obj, EAttribute attribute) {
		if (obj != null) {
			Object value = obj.eGet(attribute);
			
			if (value != null) {
				if ((value instanceof Boolean) || (value instanceof Number)) {
					return value.toString();
				} else {
					return "'" + value.toString() + "'";
				}
			}
		}
		return null;
	}
	
}
