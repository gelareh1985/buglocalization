package org.sidiff.bug.localization.common.utilities.json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.time.Instant;

import org.sidiff.bug.localization.common.utilities.json.types.JsonInstantDeserializer;
import org.sidiff.bug.localization.common.utilities.json.types.JsonPathDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonUtil {
	
	private static GsonBuilder builderInstance;
	
	private static GsonBuilder getBuilderInstance() {
		
		if (builderInstance == null) {
			JsonUtil.builderInstance = new GsonBuilder();
			builderInstance.registerTypeAdapter(Instant.class, new JsonInstantDeserializer());
			builderInstance.registerTypeAdapter(Path.class, new JsonPathDeserializer());
		}
		
		return builderInstance;
	}
	
	public static JsonElement parse(String jsonLine) {
		return JsonParser.parseString(jsonLine);
	}
	
	public static <T> T parse(String jsonLine, Class<T> type) {
		return getBuilderInstance().create().fromJson(jsonLine, type);
	}
	
	public static <T> T parse(JsonElement jsonElement, Class<T> type) {
		return getBuilderInstance().create().fromJson(jsonElement, type);
	}
	
	public static  <T> T parse(Path dataSetPath, Class<T> type) throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(dataSetPath.toFile()));
		T data = getBuilderInstance().create().fromJson(reader, type);
		return data;
	}

	public static String print(JsonElement jsonElement) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonElement);
	}
}
