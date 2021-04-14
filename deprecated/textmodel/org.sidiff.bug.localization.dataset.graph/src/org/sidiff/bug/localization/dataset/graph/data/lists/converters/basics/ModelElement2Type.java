package org.sidiff.bug.localization.dataset.graph.data.lists.converters.basics;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.graph.data.lists.converters.ModelElementConverter;

public class ModelElement2Type implements ModelElementConverter<String> {

	@Override
	public String convert(EObject modelElement) {
		return modelElement.eClass().getName();
	}

}
