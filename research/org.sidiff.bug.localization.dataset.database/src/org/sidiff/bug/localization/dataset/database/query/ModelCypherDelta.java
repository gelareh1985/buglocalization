package org.sidiff.bug.localization.dataset.database.query;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.sidiff.bug.localization.dataset.database.model.ModelDeltaUtil;

public class ModelCypherDelta {
	
	/**
	 * URI protocol for model IDs generated from URIs.
	 */
	private static final String URI_PROTOCOL = "file:/";

	private int oldVersion;
	
	private int newVersion;
	
	private Map<XMLResource, XMLResource> oldResourcesMatch;
	
	private Map<XMLResource, XMLResource> newResourcesMatch;
	
	/**
	 * Path to the model folder of the old version.
	 */
	private URI oldBaseURI;
	
	/**
	 * Path to the model folder of the new version.
	 */
	private URI newBaseURI;
	
	/**
	 * Path to the model folder of the new version.
	 */
	private URI commonBaseURI;
	
	public ModelCypherDelta(
			int oldVersion, URI oldBaseURI, Map<XMLResource, XMLResource> oldResourcesMatch,
			int newVersion, URI newBaseURI, Map<XMLResource, XMLResource> newResourcesMatch) {
		this.oldVersion = oldVersion;
		this.oldResourcesMatch = oldResourcesMatch;
		this.newVersion = newVersion;
		this.newResourcesMatch = newResourcesMatch;
		this.oldBaseURI = oldBaseURI;
		this.newBaseURI = newBaseURI;
		
		if ((oldBaseURI != null) && oldBaseURI.equals(newBaseURI)) {
			this.commonBaseURI = newBaseURI;
		}
	}
	
	private URI getBaseURI(XMLResource resource) {
		if (commonBaseURI != null) {
			return commonBaseURI;
		} else {
			if (oldResourcesMatch.containsKey(resource)) {
				if (oldBaseURI != null) {
					return oldBaseURI;
				}
			} else if (newResourcesMatch.containsKey(resource)) {
				if (newBaseURI != null) {
					return newBaseURI;
				}
			}
		}
		return null;
	}
	
	public String getModelElementID(XMLResource resource, EObject modelElement) {
		String id = resource.getID(modelElement);
		
		if (id == null) {
			URI relativeURI = ModelDeltaUtil.getRelativeURI(getBaseURI(resource), EcoreUtil.getURI(modelElement));
			id = URI_PROTOCOL + relativeURI.toString();
		}
		
		return id;
	}
	
	public EObject getModelElement(XMLResource resource, EClass type, String modelElementID) {
		if (resource == null) {
			return null;
		}
		
		EObject modelElement = null;
		
		if (modelElementID.startsWith(URI_PROTOCOL)) {
			try {
				modelElement = resource.getEObject(URI.createURI(modelElementID).fragment());
			} catch (IllegalArgumentException e) {
				// e.g. if no value for //parameterTypes/@upperValue URI
				return null;
			}
		} else {
			modelElement = resource.getEObject(modelElementID);
		}
		
		if ((modelElement != null) && (modelElement.eClass() == type)) {
			return modelElement;
		} else {
			return null;
		}
	}
	
	public XMLResource getOldResource(Resource newResource) {
		return newResourcesMatch.get(newResource);
	}
	
	public Map<XMLResource, XMLResource> getOldResourcesMatch() {
		return oldResourcesMatch;
	}
	
	public boolean containsOldResource(Resource oldResource) {
		return oldResourcesMatch.containsKey(oldResource);
	}

	public int getOldVersion() {
		return oldVersion;
	}

	public XMLResource getNewResource(Resource oldResource) {
		return oldResourcesMatch.get(oldResource);
	}
	
	public boolean containsNewResource(Resource newResource) {
		return newResourcesMatch.containsKey(newResource);
	}
	
	public Map<XMLResource, XMLResource> getNewResourcesMatch() {
		return newResourcesMatch;
	}

	public int getNewVersion() {
		return newVersion;
	}
	
	protected String toCypherLabel(EClass type) {
		return type.getName();
	}

	protected String toCypherLabel(EReference type) {
		return type.getName();
	}

	protected Object toCypherValue(EObject modelElement, EAttribute attribute) {
		if (modelElement != null) {
			Object value = modelElement.eGet(attribute);
			
			if (value != null) {
				if ((value instanceof Boolean) || (value instanceof Number)) {
					return value;
				} else {
					if (attribute.isMany()) {
						@SuppressWarnings("unchecked")
						List<Object> values = (List<Object>) value;
						Object[] cypherValues = new Object[values.size()];
						
						for (int i = 0; i < values.size(); i++) {
							cypherValues[i] = toCypherValue(values.get(i), attribute);
						}
						
						return cypherValues;
					} else {
						return toCypherValue(value, attribute);
					}
				}
			}
		}
		return null;
	}
	
	protected Object toCypherValue(Object value, EAttribute attribute) {
		if ((value instanceof Boolean) || (value instanceof Number)) {
			return value;
		} else {
			EDataType valueType = attribute.getEAttributeType();
			EFactory converter = valueType.getEPackage().getEFactoryInstance();
			return converter.convertToString(attribute.getEAttributeType(), value);
		}
	}
	
	protected boolean isConsideredFeature(EStructuralFeature feature) {
		return !feature.isDerived() && !feature.isTransient() && !feature.isVolatile();
	}
}
