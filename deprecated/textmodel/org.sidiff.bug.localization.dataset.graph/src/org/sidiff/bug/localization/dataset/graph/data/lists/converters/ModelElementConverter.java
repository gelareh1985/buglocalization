package org.sidiff.bug.localization.dataset.graph.data.lists.converters;

import org.eclipse.emf.ecore.EObject;

public interface ModelElementConverter<T> {

	/**
	 * @param modelElement A model element.
	 * @return The converted representation of the model element.
	 */
	T convert(EObject modelElement);

}