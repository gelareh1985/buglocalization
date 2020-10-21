package org.sidiff.bug.localization.dataset.graph.data.model.converters;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public interface ModelReferenceConverter<T> {
	
	/**
	 * @param source The source model element of the reference.
	 * @param type   The type of the reference.
	 * @param target The target model element of the reference.
	 * @return The converted representation of the model element reference.
	 */
	T convert(EObject source, EReference type, EObject target);
}
