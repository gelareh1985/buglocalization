package org.sidiff.bug.localization.dataset.graph.data.lists.converters.signature;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.common.utilities.emf.ModelUtil;

public class ModelElement2SignatureName implements ModelElement2SignatureTyped<EObject> {

	@Override
	public String convert(EObject modelElement) {
		return ModelUtil.getLabel(modelElement);
	}
}
