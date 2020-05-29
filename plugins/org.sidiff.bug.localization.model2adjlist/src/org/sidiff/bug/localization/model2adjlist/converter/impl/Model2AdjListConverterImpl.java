package org.sidiff.bug.localization.model2adjlist.converter.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.sidiff.bug.localization.model2adjlist.converter.Model2AdjListConverter;
import org.sidiff.bug.localization.model2adjlist.converter.ModelElement2NumberConverter;
import org.sidiff.bug.localization.model2adjlist.format.ModelAdjacencyList;
import org.sidiff.bug.localization.model2adjlist.format.impl.ModelAdjacencyListImpl;
import org.sidiff.bug.localization.model2adjlist.trace.ModelElement2NumberMapper;
import org.sidiff.bug.localization.model2adjlist.trace.Number2ModelElementMapper;
import org.sidiff.bug.localization.model2adjlist.trace.impl.ModelElement2NumberMapperImpl;
import org.sidiff.bug.localization.model2adjlist.trace.impl.Number2ModelElementMapperImpl;

public class Model2AdjListConverterImpl implements Model2AdjListConverter {

	private boolean INITIALIZE_MAPPING = true;
	
	private ModelElement2NumberConverter modelElement2Number;
	
	public Model2AdjListConverterImpl(ModelElement2NumberConverter modelElement2Number) {
		this.modelElement2Number = modelElement2Number;
	}
	
	@Override
	public ModelAdjacencyList convert(Iterable<EObject> contents) {
		ModelElement2NumberMapper modelElement2NumberMapper = new ModelElement2NumberMapperImpl(modelElement2Number);
		Number2ModelElementMapper number2ModelElementMapper = new Number2ModelElementMapperImpl(); 
		
		// NOTE: Optionally, just initializes the mapping for a continuously numbering of the model elements:
		if (INITIALIZE_MAPPING) {
			initialize(contents, modelElement2NumberMapper, number2ModelElementMapper);
		}
		
		ModelAdjacencyList adjacencyList = new ModelAdjacencyListImpl(modelElement2NumberMapper, number2ModelElementMapper);
		
		for (EObject modelElement : contents) {
			Set<EObject> adjacent = getAdjacent(modelElement);
			adjacencyList.addModelElement(modelElement, adjacent);
		}
		
		return adjacencyList;
	}

	private void initialize(
			Iterable<EObject> contents,
			ModelElement2NumberMapper modelElement2NumberMapper,
			Number2ModelElementMapper number2ModelElementMapper) {
		
		for (EObject modelElement : contents) {
			Integer modelElementNumerical = modelElement2NumberMapper.getNumber(modelElement);
			number2ModelElementMapper.map(modelElementNumerical, modelElement);
		}
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
}
