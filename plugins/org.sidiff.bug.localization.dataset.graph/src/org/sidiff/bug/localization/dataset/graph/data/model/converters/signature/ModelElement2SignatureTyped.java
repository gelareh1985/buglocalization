package org.sidiff.bug.localization.dataset.graph.data.model.converters.signature;

import org.eclipse.emf.ecore.EObject;

public interface ModelElement2SignatureTyped<E extends EObject> {

	/**
	 * @param modelElement A model element.
	 * @return The converted signature of the model element.
	 */
	String convert(E modelElement);

}
