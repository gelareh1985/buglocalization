package org.sidiff.bug.localization.common.utilities.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.text.StringEscapeUtils;

public class WebUtil {
	
	public static String unescapeHtml4(String response) {
		return StringEscapeUtils.unescapeHtml4(response);
	}
	
	public static String unescapeJson(String response) {
		return StringEscapeUtils.unescapeJson(response);
	}
	
	public static String escapeJson(String response) {
		return StringEscapeUtils.escapeJson(response);
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
			throw new IOException("URL:" + requestURL + " Status: " + status + ": " + content.toString());
		} else {
			return content.toString();
		}
	}
}
