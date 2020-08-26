package org.sidiff.bug.localization.dataset.model.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;
import org.sidiff.bug.localization.dataset.history.model.changes.json.JsonLineChangeDeserializer;
import org.sidiff.bug.localization.dataset.history.model.changes.json.JsonLineChangeSerializer;
import org.sidiff.bug.localization.dataset.model.DataSet;

import com.google.gson.GsonBuilder;

public class DataSetStorage {

	public static void save(Path datasetPath, DataSet dataset) throws IOException {
		GsonBuilder builder = JsonUtil.createBuilderInstance();
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeDeserializer());
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeSerializer());
		
		JsonUtil.save(dataset, datasetPath, builder);
	}
	
	public static DataSet load(Path datasetPath) throws FileNotFoundException {
		GsonBuilder builder = JsonUtil.createBuilderInstance();
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeDeserializer());
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeSerializer());
		
		return JsonUtil.parse(datasetPath, DataSet.class, builder);
	}
}
