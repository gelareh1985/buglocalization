package org.sidiff.bug.localization.model2adjlist.converter;

import org.eclipse.emf.ecore.EObject;

public interface ModelElement2NumberConverter {

	/**
	 * @param modelElement A model element.
	 * @return The numerical representation of the model element.
	 */
	int convert(EObject modelElement);

}