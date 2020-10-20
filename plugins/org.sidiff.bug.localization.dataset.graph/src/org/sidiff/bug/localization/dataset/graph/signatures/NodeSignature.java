package org.sidiff.bug.localization.dataset.graph.signatures;

import org.eclipse.emf.ecore.EObject;

public interface NodeSignature<E extends EObject> {

	public String createSignature(E element);
}
