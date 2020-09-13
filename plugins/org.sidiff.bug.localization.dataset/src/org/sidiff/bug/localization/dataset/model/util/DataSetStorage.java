package org.sidiff.bug.localization.dataset.model.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.changes.model.LineChange;
import org.sidiff.bug.localization.dataset.changes.model.json.JsonLineChangeDeserializer;
import org.sidiff.bug.localization.dataset.changes.model.json.JsonLineChangeSerializer;
import org.sidiff.bug.localization.dataset.model.DataSet;

import com.google.gson.GsonBuilder;

public class DataSetStorage {
	
	private static final String TIMESTAMP_SEPARATOR = "_";
	
	public static Path save(Path datasetPath, DataSet dataset, boolean appendTimestamp) throws IOException {
		GsonBuilder builder = JsonUtil.createBuilderInstance();
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeDeserializer());
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeSerializer());
		
		if (appendTimestamp && (dataset.getTimestamp() != null)) {
			String filename = datasetPath.getFileName().toString();
			String stampedFilename = filename.substring(0, filename.lastIndexOf(".")) + TIMESTAMP_SEPARATOR + dataset.getTimestamp();
			stampedFilename = stampedFilename + "." + filename.substring(filename.lastIndexOf(".") + 1, filename.length());
			datasetPath = Paths.get(datasetPath.getParent().toString(), stampedFilename);
		}
		
		JsonUtil.save(dataset, datasetPath, builder);
		
		return datasetPath;
	}
	
	public static DataSet load(Path datasetPath) throws FileNotFoundException {
		GsonBuilder builder = JsonUtil.createBuilderInstance();
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeDeserializer());
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeSerializer());
		
		return JsonUtil.parse(datasetPath, DataSet.class, builder);
	}
	
}
