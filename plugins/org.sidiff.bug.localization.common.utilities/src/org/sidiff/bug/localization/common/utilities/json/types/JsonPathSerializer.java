package org.sidiff.bug.localization.common.utilities.json.types;

import java.lang.reflect.Type;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonPathSerializer implements JsonSerializer<Path>  {

	@Override
	public JsonElement serialize(Path obj, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(obj.toString().replace("\\", "/"));
	}

}
