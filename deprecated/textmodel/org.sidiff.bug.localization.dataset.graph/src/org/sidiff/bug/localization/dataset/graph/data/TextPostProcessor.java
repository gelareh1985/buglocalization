package org.sidiff.bug.localization.dataset.graph.data;

import org.sidiff.bug.localization.common.utilities.web.WebUtil;

public class TextPostProcessor {

	public String process(String text) {
		return "\"" + escape(text) + "\"";
	}
	
	public String escape(String text) {
		return WebUtil.escapeJson(text);
	}
	
	public String unescape(String text) {
		return WebUtil.unescapeJson(text);
	}
	
	public String removeLineBreaks(String text) {
		return text.replace("\n", " ").replace("\r", " ");
	}
	
}
