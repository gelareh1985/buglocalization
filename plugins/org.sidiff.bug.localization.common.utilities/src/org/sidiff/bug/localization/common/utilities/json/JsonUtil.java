package org.sidiff.bug.localization.common.utilities.json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Instant;

import org.sidiff.bug.localization.common.utilities.json.types.JsonInstantDeserializer;
import org.sidiff.bug.localization.common.utilities.json.types.JsonInstantSerializer;
import org.sidiff.bug.localization.common.utilities.json.types.JsonPathDeserializer;
import org.sidiff.bug.localization.common.utilities.json.types.JsonPathSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonUtil {
	
	private static GsonBuilder builderInstance;
	
	private static GsonBuilder getBuilderInstance() {
		
		if (builderInstance == null) {
			JsonUtil.builderInstance = createBuilderInstance();
		}
		
		return builderInstance;
	}
	
	public static GsonBuilder createBuilderInstance()  {
		GsonBuilder builderInstance = new GsonBuilder();
		builderInstance.setPrettyPrinting();
		builderInstance.registerTypeAdapter(Instant.class, new JsonInstantDeserializer());
		builderInstance.registerTypeAdapter(Instant.class, new JsonInstantSerializer());
		builderInstance.registerTypeAdapter(Path.class, new JsonPathDeserializer());
		builderInstance.registerTypeAdapter(Path.class, new JsonPathSerializer());
		return builderInstance;
	}
	
	public static JsonElement parse(String jsonLine) {
		return JsonParser.parseString(jsonLine);
	}
	
	public static <T> T parse(String jsonLine, Class<T> type) {
		return parse(jsonLine, type, getBuilderInstance());
	}
	
	public static <T> T parse(String jsonLine, Class<T> type, GsonBuilder builder) {
		return builder.create().fromJson(jsonLine, type);
	}
	
	public static <T> T parse(JsonElement jsonElement, Class<T> type) {
		return parse(jsonElement, type, getBuilderInstance());
	}
	
	public static <T> T parse(JsonElement jsonElement, Class<T> type, GsonBuilder builder) {
		return builder.create().fromJson(jsonElement, type);
	}
	
	public static  <T> T parse(Path dataSetPath, Class<T> type) throws FileNotFoundException {
		return parse(dataSetPath, type, getBuilderInstance());
	}
	
	public static  <T> T parse(Path dataSetPath, Class<T> type, GsonBuilder builder) throws FileNotFoundException {
		try (JsonReader reader = new JsonReader(new FileReader(dataSetPath.toFile()))) {
			T data = builder.create().fromJson(reader, type);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void save(Object object, Path path) throws IOException {
		save(object, path, getBuilderInstance());
	}
	
	public static void save(Object object, Path path, GsonBuilder builder) throws IOException {
		try (Writer writer = new FileWriter(path.toFile())) {
		    Gson gson = builder.create();
		    gson.toJson(object, writer);
		}
	}

	public static String print(JsonElement jsonElement) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonElement);
	}
}
