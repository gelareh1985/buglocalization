package org.sidiff.bug.localization.model2adjlist.trace;

import org.eclipse.emf.ecore.EObject;

public interface Number2ModelElementMapper {

	/**
	 * @param number The mapped numerical representation of the model element.
	 * @param obj    A model element.
	 */
	void map(Integer number, EObject obj);
	
	/**
	 * @param number The mapped numerical representation of the model element.
	 * @return The model element.
	 */
	EObject getModelElement(int number);
}
