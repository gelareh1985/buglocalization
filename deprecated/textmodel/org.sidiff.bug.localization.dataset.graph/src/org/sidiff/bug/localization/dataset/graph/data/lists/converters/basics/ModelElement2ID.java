package org.sidiff.bug.localization.dataset.graph.data.lists.converters.basics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.graph.data.lists.converters.ModelElementConverter;

public class ModelElement2ID implements ModelElementConverter<Integer> {

	private int counter = 0;
	
	private Map<EObject, Integer> modelElement2ID;
	
	public ModelElement2ID() {
		this.modelElement2ID = new HashMap<>();
	}
	
	public ModelElement2ID(int minimumNumber) {
		this();
		this.counter = minimumNumber;
	}
	
	@Override
	public Integer convert(EObject modelElement) {
		Integer id = modelElement2ID.get(modelElement);
		
		if (id == null) {
			id = ++counter;
			modelElement2ID.put(modelElement, id);
		}
		
		return id;
	}

	@Override
	public String toString() {
		return "ModelElement2NumberConverterImpl [counter=" + counter + "]";
	}
	
	public Map<EObject, Integer> getTrace() {
		return modelElement2ID;
	}

	public boolean contains(EObject modelElement) {
		return modelElement2ID.containsKey(modelElement);
	}
	
}
