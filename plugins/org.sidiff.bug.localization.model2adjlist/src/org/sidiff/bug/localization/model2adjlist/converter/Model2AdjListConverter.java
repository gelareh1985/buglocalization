package org.sidiff.bug.localization.model2adjlist.converter;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.model2adjlist.format.AdjacencyList;

public interface Model2AdjListConverter {

	AdjacencyList convert(TreeIterator<EObject> contents);

}