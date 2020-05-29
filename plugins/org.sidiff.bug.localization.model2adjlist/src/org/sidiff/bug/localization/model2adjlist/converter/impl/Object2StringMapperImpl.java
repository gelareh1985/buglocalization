package org.sidiff.bug.localization.model2adjlist.converter.impl;

import java.util.HashMap;
import java.util.Map;

import org.sidiff.bug.localization.model2adjlist.converter.Object2StringConverter;
import org.sidiff.bug.localization.model2adjlist.converter.Object2StringMapper;

public class Object2StringMapperImpl implements Object2StringMapper {

	private Map<Object, String> objectToString;
	
	private Map<String, Object> stringToObject;
	
	private Object2StringConverter object2StringConverterImpl;
	
	public Object2StringMapperImpl(Object2StringConverter object2StringConverterImpl) {
		this.objectToString = new HashMap<>();
		this.stringToObject = new HashMap<>();
		this.object2StringConverterImpl = object2StringConverterImpl;
	}
	
	@Override
	public String getString(Object obj) {
		String objectString = objectToString.get(obj);
		
		if (objectString == null) {
			objectString = object2StringConverterImpl.convert(obj);
			objectToString.put(obj, objectString);
			stringToObject.put(objectString, obj);
		}
		
		return objectString;
	}
	
	@Override
	public Object getObject(String string) {
		return stringToObject.get(string);
	}
}
