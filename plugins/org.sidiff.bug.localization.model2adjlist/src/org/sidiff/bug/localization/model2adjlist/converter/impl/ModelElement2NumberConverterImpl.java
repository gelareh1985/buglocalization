package org.sidiff.bug.localization.model2adjlist.converter.impl;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.model2adjlist.converter.ModelElement2NumberConverter;

public class ModelElement2NumberConverterImpl implements ModelElement2NumberConverter {

	private int counter = 0;
	
	public ModelElement2NumberConverterImpl() {
	}
	
	public ModelElement2NumberConverterImpl(int minimumNumber) {
		this.counter = minimumNumber;
	}
	
	@Override
	public int convert(EObject object) {
		return ++counter;
	}

	@Override
	public String toString() {
		return "ModelElement2NumberConverterImpl [counter=" + counter + "]";
	}
	
}
