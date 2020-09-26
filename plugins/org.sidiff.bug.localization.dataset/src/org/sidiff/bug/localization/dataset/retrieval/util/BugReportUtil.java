package org.sidiff.bug.localization.dataset.retrieval.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sidiff.bug.localization.common.utilities.web.WebUtil;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.model.BugReportComment;

public class BugReportUtil {

	public static final Predicate<BugReportComment> DEFAULT_BUG_REPORT_COMMENT_FILTER = 
			(comment) -> 	comment.getText().contains("New Gerrit change created: http")
						|| 	comment.getText().contains("Gerrit change http");

	public static String getPlainText(String string) {
		return WebUtil.unescape(string).replace("\n", "").replace("\r", "");
	}
	
	public static String getFullPlainText(Version version, Predicate<BugReportComment> commentFilter) {
		StringBuilder fullText = new StringBuilder();
		
//		fullText.append(BugReportUtil.getPlainText(version.getCommitMessage()));
//		fullText.append(" ");
		fullText.append(BugReportUtil.getPlainText(version.getBugReport().getSummary()));
		fullText.append(" ");
		
		for (BugReportComment comment : version.getBugReport().getComments()) {
			if (!commentFilter.test(comment)) {
				fullText.append(BugReportUtil.getPlainText(comment.getText()));
				fullText.append(" ");
			}
		}
		
		return fullText.toString();
	}
	
	/**
	 * Matches all words containing only alphabetic characters and '-'. The word
	 * needs to start with a space ' ', quotes '"', or parenthesis '(' and must end with
	 * space ' ', quotes '"', dots with space '. ', ': ', or parenthesis ')'.
	 * 
	 * @param text The text to be split into words.
	 * @return A list of words.
	 */
	public static List<String> getWords(String text) {
		List<String> words  = new ArrayList<>();
		
		String startsWith = "((?<!\\S)|[\"\\(])";
		String wordContains = "([\\p{Alpha}-']+)";
		String endsWith = "((?!\\S)|(\\.\\s)|(\\:\\s)|[\"\\)])";
		
		Pattern pattern = Pattern.compile(startsWith + wordContains + endsWith);
		Matcher matcher = pattern.matcher(text);
		
		while (matcher.find()){
			String word = matcher.group(2);

			if (!word.equals("-") && !word.equals("'")) {
				words.add(word); 
			}
		} 
		
		return words;
	}

}
