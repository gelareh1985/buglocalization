package org.sidiff.bug.localization.retrieval.reports;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {

		// https://wiki.mozilla.org/Bugzilla:REST_API

		int bugID = 2000;

		String getBug = "https://bugs.eclipse.org/bugs/rest.cgi/bug/" + bugID;
		String getComments = getBug + "/comment";

		// Bug:
		String responseBug = request(getBug);
		JsonElement jsonElementBug = parse(responseBug);

		System.out.println(print(jsonElementBug));
		System.out.println();
		
		// Comment:
		String responseComments = request(getComments);
		JsonElement jsonElementComments = parse(responseComments);

		System.out.println(print(jsonElementComments));

		return IApplication.EXIT_OK;
	}

	public static String request(String requestURL) throws IOException {
		URL url = new URL(requestURL);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		int status = con.getResponseCode();

		Reader streamReader = null;

		if (status > 299) {
			streamReader = new InputStreamReader(con.getErrorStream());
		} else {
			streamReader = new InputStreamReader(con.getInputStream());
		}

		StringBuilder content = new StringBuilder();

		try (Scanner scanner = new Scanner(streamReader)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (!line.isEmpty()) {
					content.append(line);
				}
			}
		} finally {
			con.disconnect();
		}

		if (status > 299) {
			throw new IOException("Status: " + status + ": " + content.toString());
		} else {
			return content.toString();
		}
	}

	public static JsonElement parse(String response) {
		String jsonLine = response.substring(response.indexOf("<pre>") + "<pre>".length(), response.indexOf("</pre>"));
		jsonLine = StringEscapeUtils.unescapeHtml4(jsonLine);
		return JsonParser.parseString(jsonLine);
	}

	public static String print(JsonElement jsonElement) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonElement);
	}

	@Override
	public void stop() {
	}

}
