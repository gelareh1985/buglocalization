package org.sidiff.bug.localization.model2adjlist.converter.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.bug.localization.model2adjlist.converter.Model2AdjListConverter;
import org.sidiff.bug.localization.model2adjlist.converter.Object2StringMapper;
import org.sidiff.bug.localization.model2adjlist.format.AdjacencyList;

public class Model2AdjListConverterImpl implements Model2AdjListConverter {

	private AdjacencyList adjacencyList;
	
	public Model2AdjListConverterImpl(Object2StringMapper object2StringMapperImpl) {
		this.adjacencyList = new AdjacencyList(object2StringMapperImpl);
	}
	
	@Override
	public AdjacencyList convert(TreeIterator<EObject> contents) {
		
		for (EObject modelElement : (Iterable<EObject>) () -> contents) {
			Set<EObject> adjacent = getAdjacent(modelElement);
			adjacencyList.add(modelElement, adjacent, EcoreUtil.getURI(modelElement).fragment().toString());
		}
		
		return adjacencyList;
	}

	@SuppressWarnings("unchecked")
	private Set<EObject> getAdjacent(EObject modelElement) {
		Set<EObject> adjacents = new LinkedHashSet<>(); // ordered set for deterministic results
		
		for (EReference reference : modelElement.eClass().getEAllReferences()) {
			if (reference.isMany()) {
				for (EObject adjacent : (Iterable<EObject>) modelElement.eGet(reference)) {
					adjacents.add(adjacent);
				}
			} else {
				Object adjacent =  modelElement.eGet(reference);
				
				if (adjacent != null) {
					assert adjacent instanceof EObject;
					adjacents.add((EObject) adjacent);
				}
			}
		}
		
		return adjacents;
	}
	
	public AdjacencyList getAdjacencyList() {
		return adjacencyList;
	}
}
