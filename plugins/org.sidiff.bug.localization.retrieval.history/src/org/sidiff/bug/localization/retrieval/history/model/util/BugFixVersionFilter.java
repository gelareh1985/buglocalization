package org.sidiff.bug.localization.retrieval.history.model.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BugFixVersionFilter implements VersionFilter {

	private List<String> patterns;
	
	private Pattern pattern;
	
	private boolean caseSensitive = false;
	
	public BugFixVersionFilter() {
		this.patterns = Arrays.asList(new String[] {".*?bug.*?\\d", ".*?fix.*?\\d", ".*?\\d.*?bug.*?", ".*?\\d.*?fix.*?"});
		compilePatterns();
	}

	private void compilePatterns() {
		if (caseSensitive) {
			this.pattern = Pattern.compile("(" + String.join(")|(", patterns) + ")");
		} else {
			this.pattern = Pattern.compile("(" + String.join(")|(", patterns) + ")", Pattern.CASE_INSENSITIVE);
		}
	}
	
	@Override
	public boolean filter(String url, Calendar date, String author, String commitMessage) {
		return !pattern.matcher(commitMessage).find();
	}
	
	public List<String> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}
	
	public boolean removePattern(String pattern) {
		if (patterns.remove(pattern)) {
			compilePatterns();
			return true;
		}
		return false;
	}
	
	public boolean addPattern(String pattern) {
		if (!pattern.contains(pattern)) {
			compilePatterns();
			return true;
		}
		return false;
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		if (this.caseSensitive != caseSensitive) {
			this.caseSensitive = caseSensitive;
			compilePatterns();			
		}
	}
}
