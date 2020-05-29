package org.sidiff.bug.localization.model2adjlist.converter.impl;

import org.sidiff.bug.localization.model2adjlist.converter.Object2StringConverter;

public class Object2StringConverterImpl implements Object2StringConverter {

	private int counter = 0;
	
	@Override
	public String convert(Object object) {
		return ++counter + "";
	}
}
