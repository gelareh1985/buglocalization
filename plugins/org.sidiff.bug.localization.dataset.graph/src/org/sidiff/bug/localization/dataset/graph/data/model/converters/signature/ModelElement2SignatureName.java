package org.sidiff.bug.localization.dataset.graph.data.model.converters.signature;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;

public class ModelElement2SignatureName implements ModelElement2SignatureTyped<EObject> {

	@Override
	public String convert(EObject modelElement) {
		EStructuralFeature lableFeature = getLabelFeature(modelElement.eClass());
		Object value = modelElement.eGet(lableFeature);
		
		if (value != null) {
			return value.toString();
		} else {
			return "";
		}
	}

	protected EStructuralFeature getLabelFeature(EClass eClass) {
		EAttribute result = null;
		
		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			if (!eAttribute.isMany() && eAttribute.getEType().getInstanceClass() != FeatureMap.Entry.class) {
				if ("name".equalsIgnoreCase(eAttribute.getName())) {
					result = eAttribute;
					break;
				} else if (result == null) {
					result = eAttribute;
				} else if (eAttribute.getEAttributeType().getInstanceClass() == String.class
						&& result.getEAttributeType().getInstanceClass() != String.class) {
					result = eAttribute;
				}
			}
		}
		return result;
	}
}
