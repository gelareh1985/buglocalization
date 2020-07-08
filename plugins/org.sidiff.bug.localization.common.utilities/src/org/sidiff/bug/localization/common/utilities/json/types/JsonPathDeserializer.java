package org.sidiff.bug.localization.common.utilities.json.types;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class JsonPathDeserializer implements JsonDeserializer<Path>  {

		@Override
		public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return Paths.get(expandVariables(json.getAsString()));
		}
		
		protected String expandVariables(String text) {
			Map<String, String> envMap = System.getenv();

			String pattern = "\\$\\{(.*?)\\}";
			Pattern expr = Pattern.compile(pattern);
			Matcher matcher = expr.matcher(text);

			while (matcher.find()) {
				String variable = matcher.group(1);
				String envValue = envMap.get(variable);
				
				if (envValue == null) {
					envValue = System.getProperty(variable);
					
					if (envValue == null) {
						continue;
					}
				}
				
				envValue = envValue.replace("\\", "\\\\");
				Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
				text = subexpr.matcher(text).replaceAll(envValue);
			}

			return text;
		}
}
