package org.sidiff.bug.localization.model2adjlist.trace.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.model2adjlist.converter.ModelElement2NumberConverter;
import org.sidiff.bug.localization.model2adjlist.trace.ModelElement2NumberMapper;

public class ModelElement2NumberMapperImpl implements ModelElement2NumberMapper {

	private Map<EObject, Integer> modelElementToNumber;
	
	private ModelElement2NumberConverter modelElement2NumberConverter;
	
	public ModelElement2NumberMapperImpl(ModelElement2NumberConverter modelElement2NumberConverter) {
		this.modelElementToNumber = new HashMap<>();
		this.modelElement2NumberConverter = modelElement2NumberConverter;
	}
	
	@Override
	public void map(EObject obj, Integer number) {
		modelElementToNumber.put(obj, number);
	}
	
	@Override
	public Integer getNumber(EObject obj) {
		Integer objectString = modelElementToNumber.get(obj);
		
		if (objectString == null) {
			objectString = modelElement2NumberConverter.convert(obj);
			modelElementToNumber.put(obj, objectString);
		}
		
		return objectString;
	}
	
	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		
		for (Entry<EObject, Integer> mapped : modelElementToNumber.entrySet()) {
			text.append(mapped.getKey());
			text.append("\n\t" + mapped.getValue() + "\n");
		}
		
		return text.toString();
	}
	
}
