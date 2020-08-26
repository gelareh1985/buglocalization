package org.sidiff.bug.localization.dataset.history.model.changes.json;

import java.lang.reflect.Type;

import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonLineChangeSerializer implements JsonSerializer<LineChange>  {

	@Override
	public JsonElement serialize(LineChange change, Type type, JsonSerializationContext context) {
		StringBuilder jsonChange = new StringBuilder();
		jsonChange.append(change.getType().toString());
		jsonChange.append(",");
		jsonChange.append(change.getBeginA());
		jsonChange.append(":");
		jsonChange.append(change.getEndA());
		jsonChange.append(",");
		jsonChange.append(change.getBeginB());
		jsonChange.append(":");
		jsonChange.append(change.getEndB());
		
		return new JsonPrimitive(jsonChange.toString());
	}

}
