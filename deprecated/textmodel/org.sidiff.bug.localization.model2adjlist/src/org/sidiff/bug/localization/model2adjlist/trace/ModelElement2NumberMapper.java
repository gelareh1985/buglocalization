package org.sidiff.bug.localization.model2adjlist.trace;

import org.eclipse.emf.ecore.EObject;

public interface ModelElement2NumberMapper {

	/**
	 * @param obj    A model element.
	 * @param number The mapped numerical representation of the model element.
	 */
	void map(EObject obj, Integer number);

	/**
	 * @param obj A model element.
	 * @return The mapped numerical representation of the model element.
	 */
	Integer getNumber(EObject obj);

}