package org.sidiff.bug.localization.dataset.model.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;
import org.sidiff.bug.localization.dataset.history.model.changes.json.JsonLineChangeDeserializer;
import org.sidiff.bug.localization.dataset.history.model.changes.json.JsonLineChangeSerializer;
import org.sidiff.bug.localization.dataset.model.DataSet;

import com.google.gson.GsonBuilder;

public class DataSetStorage {

	public static Path save(Path datasetPath, DataSet dataset, boolean timestamp) throws IOException {
		GsonBuilder builder = JsonUtil.createBuilderInstance();
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeDeserializer());
		builder.registerTypeAdapter(LineChange.class, new JsonLineChangeSerializer());
		
		if (timestamp) {
			DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
			String currentTimestamp = timeFormat.format(Instant.now());
			String filename = datasetPath.getFileName().toString();
			String stampedFilename = filename.substring(0, filename.lastIndexOf(".")) + "_" + currentTimestamp;
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
