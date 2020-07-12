package org.sidiff.bug.localization.common.utilities.json.types;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonInstantSerializer implements JsonSerializer<Instant>  {

	@Override
	public JsonElement serialize(Instant obj, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(obj.toString());
	}

}
