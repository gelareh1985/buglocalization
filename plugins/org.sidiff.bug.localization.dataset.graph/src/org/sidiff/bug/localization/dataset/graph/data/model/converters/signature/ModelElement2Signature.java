package org.sidiff.bug.localization.dataset.graph.data.model.converters.signature;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.ModelElementConverter;

public class ModelElement2Signature implements ModelElementConverter<String> {

	private Map<EClass, ModelElement2SignatureTyped<EObject>> typedConverter;
	
	private ModelElement2SignatureTyped<EObject> defaultType2Signature;

	public ModelElement2Signature(ModelElement2SignatureTyped<EObject> defaultConverter) {
		this.defaultType2Signature = defaultConverter;
		this.typedConverter = new HashMap<>();
	}

	@Override
	public String convert(EObject modelElement) {
		return typedConverter.getOrDefault(modelElement.eClass(), defaultType2Signature).convert(modelElement);
	}
	
	@SuppressWarnings("unchecked")
	public void addTypedConverter(EClass type, ModelElement2SignatureTyped<? extends EObject> typedConverter) {
		this.typedConverter.put(type, (ModelElement2SignatureTyped<EObject>) typedConverter);
	}
}
