package org.sidiff.bug.localization.model2adjlist.trace.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.model2adjlist.trace.Number2ModelElementMapper;

public class Number2ModelElementMapperImpl implements Number2ModelElementMapper {

	private Map<Integer, EObject> numberToModelElement;
	
	public Number2ModelElementMapperImpl() {
		this.numberToModelElement = new HashMap<>();
	}
	
	@Override
	public void map(Integer number, EObject obj) {
		numberToModelElement.put(number, obj);
	}

	@Override
	public EObject getModelElement(int number) {
		return numberToModelElement.get(number);
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		
		for (Entry<Integer, EObject> mapped : numberToModelElement.entrySet()) {
			text.append(mapped.getKey());
			text.append("\n\t" + mapped.getValue() + "\n");
		}
		
		return text.toString();
	}
}
