package org.sidiff.bug.localization.dataset.graph.data.model.converters.basics;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.ModelReferenceConverter;

public class ModelReference2TargetID implements ModelReferenceConverter<Integer> {

	private ModelElement2ID modelElement2ID;
	
	public ModelReference2TargetID(ModelElement2ID modelElement2ID) {
		this.modelElement2ID = modelElement2ID;
	}

	@Override
	public Integer convert(EObject source, EReference type, EObject target) {
		return modelElement2ID.convert(target);
	}

}
