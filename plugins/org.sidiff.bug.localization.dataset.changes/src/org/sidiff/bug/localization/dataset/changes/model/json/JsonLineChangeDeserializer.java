package org.sidiff.bug.localization.dataset.changes.model.json;

import java.lang.reflect.Type;

import org.sidiff.bug.localization.dataset.changes.model.LineChange;
import org.sidiff.bug.localization.dataset.changes.model.LineChange.LineChangeType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class JsonLineChangeDeserializer implements JsonDeserializer<LineChange> {

	@Override
	public LineChange deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		String jsonChange = json.getAsString();
		String[] changeSegments = jsonChange.split(",");
		String[] changesA = changeSegments[1].split(":");
		String[] changesB = changeSegments[2].split(":");
		
		LineChange lineChange = new LineChange();
		lineChange.setType(LineChangeType.valueOf(changeSegments[0]));
		lineChange.setBeginA(Integer.valueOf(changesA[0]));
		lineChange.setEndA(Integer.valueOf(changesA[1]));
		lineChange.setBeginB(Integer.valueOf(changesB[0]));
		lineChange.setEndB(Integer.valueOf(changesB[1]));
		
		return lineChange;
	}
	
}
