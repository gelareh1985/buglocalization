package org.sidiff.bug.localization.dataset.graph.data.model.converters.signature;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.common.utilities.emf.ItemProviderUtil;

public class ModelElement2SignatureItemText implements ModelElement2SignatureTyped<EObject> {
	
	@Override
	public String convert(EObject modelElement) {
			
		// NOTE: Requires to load the *.edit plug-in (e.g. org.eclipse.uml2.uml.edit), uses the reflective item provider instead.
		String text = ItemProviderUtil.getTextByObject(modelElement);
		
		if ((text == null) || text.isEmpty()) {
			text = "";
		}
		
		return text;
	}

}
