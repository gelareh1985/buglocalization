package org.sidiff.bug.localization.dataset.graph.signatures;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Operation;
import org.sidiff.bug.localization.common.utilities.emf.ItemProviderUtil;

public class UMLElementSignature implements NodeSignature<EObject> {

	@Override
	public String createSignature(EObject modelElement) {
		
		// TODO: Avoid none local signatures!?
		if (modelElement instanceof Operation) {
			return "<Operation> " + ((Operation) modelElement).getName();
		}

		return ItemProviderUtil.getTextByObject(modelElement);
	}

}
