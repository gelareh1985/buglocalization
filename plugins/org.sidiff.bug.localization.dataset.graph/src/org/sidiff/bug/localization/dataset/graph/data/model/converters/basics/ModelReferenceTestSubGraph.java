package org.sidiff.bug.localization.dataset.graph.data.model.converters.basics;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.ModelReferenceTest;

public class ModelReferenceTestSubGraph implements ModelReferenceTest {

	private ModelElement2ID modelElement2ID;
	
	public ModelReferenceTestSubGraph(ModelElement2ID modelElement2ID) {
		this.modelElement2ID = modelElement2ID;
	}

	@Override
	public boolean test(EObject source, EReference reference, EObject target) {
		return modelElement2ID.contains(target);
	}
}
