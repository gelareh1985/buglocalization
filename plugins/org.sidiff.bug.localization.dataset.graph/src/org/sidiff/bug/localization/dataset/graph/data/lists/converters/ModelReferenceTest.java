package org.sidiff.bug.localization.dataset.graph.data.lists.converters;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public interface ModelReferenceTest {
	
	/**
	 * @param source    The source model element of the reference.
	 * @param reference The type of the model element reference.
	 * @param target    The target model element of the reference.
	 * @return <code>true</code> if the reference passes the test (should be
	 *         considered); <code>false</code> otherwise.
	 */
	boolean test(EObject source, EReference reference, EObject target);
}
