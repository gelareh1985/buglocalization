package org.sidiff.bug.localization.dataset.reports.bugtracker;

import org.sidiff.bug.localization.common.utilities.web.WebUtil;

public class EclipseBugzillaBugtracker extends BugzillaBugtracker {

	public final static String ECLIPSE_REST_API_URL = "https://bugs.eclipse.org/bugs/rest.cgi";
	
	public EclipseBugzillaBugtracker() {
		super(ECLIPSE_REST_API_URL);
	}

	@Override
	protected String extractJsonFromResponse(String response) {
		// https://bugs.eclipse.org/bugs/rest.cgi wraps the response in an HTML page:
		String jsonLine = response.substring(response.indexOf("<pre>") + "<pre>".length(), response.indexOf("</pre>"));
		jsonLine = WebUtil.unescapeHtml4(jsonLine);
		return jsonLine;
	}
}
