package org.sidiff.bug.localization.model2adjlist.converter;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.model2adjlist.format.ModelAdjacencyList;

public interface Model2AdjListConverter {

	/**
	 * @param contents Iterates over the models AST.
	 * @return The model converted into an adjacency list.
	 */
	ModelAdjacencyList convert(Iterable<EObject> contents);

}