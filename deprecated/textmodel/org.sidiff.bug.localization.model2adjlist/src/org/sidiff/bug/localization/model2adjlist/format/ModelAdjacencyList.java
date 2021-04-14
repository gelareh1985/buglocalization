package org.sidiff.bug.localization.model2adjlist.format;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.model2adjlist.trace.ModelElement2NumberMapper;
import org.sidiff.bug.localization.model2adjlist.trace.Number2ModelElementMapper;

public interface ModelAdjacencyList extends AdjacencyList {

	/**
	 * @param source  The source model element.
	 * @param adjacent All adjacent model element.
	 */
	void addModelElement(EObject source, Collection<? extends EObject> adjacent);
	
	/**
	 * @return The mapping from numerical representations to model elements.
	 */
	Number2ModelElementMapper getNumber2ModelElementMapper();
	
	/**
	 * @return The mapping from model elements to numerical representations.
	 */
	ModelElement2NumberMapper getModelElement2NumberMapper();

}
