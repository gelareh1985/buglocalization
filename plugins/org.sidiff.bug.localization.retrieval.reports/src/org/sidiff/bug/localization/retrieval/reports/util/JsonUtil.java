package org.sidiff.bug.localization.retrieval.reports.util;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonUtil {
	
	private static GsonBuilder builderInstance;
	
	private static GsonBuilder getBuilderInstance() {
		
		if (builderInstance == null) {
			JsonUtil.builderInstance = new GsonBuilder();
			builderInstance.registerTypeAdapter(Instant.class, new JsonInstantDeserializer());
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

	public static String print(JsonElement jsonElement) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonElement);
	}
}
