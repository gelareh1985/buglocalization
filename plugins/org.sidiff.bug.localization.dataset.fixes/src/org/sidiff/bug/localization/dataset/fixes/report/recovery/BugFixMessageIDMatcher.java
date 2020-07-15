package org.sidiff.bug.localization.dataset.fixes.report.recovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sidiff.bug.localization.dataset.history.Activator;

/**
 * Extracts a bug ID from a commit message.
 */
public class BugFixMessageIDMatcher {

	private List<String> patterns;
	
	private List<String> bugIdGroupNames;
	
	private Pattern pattern;
	
	private boolean caseSensitive = false;
	
	public BugFixMessageIDMatcher() {
		this.patterns = new ArrayList<>();
		this.bugIdGroupNames = new ArrayList<>();
		
		addKeyword("bug", true, true);
		addKeyword("fix", true, true);
		compilePatterns();
	}
	
	public void addKeyword(String keyword, boolean searchIDBefore, boolean searchIDAfter) {
		if (searchIDBefore) {
			String bugIdGroupName = getNewBugIdGroupName();
			addPattern(".*?(?<" + bugIdGroupName + ">\\d+).*?" + keyword + ".*?", bugIdGroupName);
		}
		if (searchIDAfter) {
			String bugIdGroupName = getNewBugIdGroupName();
			addPattern(".*?" + keyword + ".*?(?<" + bugIdGroupName + ">\\d+).*?", bugIdGroupName);
		}
	}
	
	public boolean containesBugID(String commitMessage) {
		return pattern.matcher(commitMessage).find();
	}
	
	public int matchBugID(String commitMessage) {
		Matcher matcher = pattern.matcher(commitMessage);
		
		if (matcher.find( )) {
			try {
				for (String groupName : bugIdGroupNames) {
					String matchedBugID = matcher.group(groupName);
					
					if (matchedBugID != null) {
						return Integer.valueOf(matchedBugID);
					}
				}
			} catch (NumberFormatException e) {
				Activator.getLogger().log(Level.WARNING,
						"Clould not parse bug ID: " + matcher.group(1) + " From message: " + commitMessage);
			}
		}
		
		return -1;
	}
	
	private void compilePatterns() {
		if (caseSensitive) {
			this.pattern = Pattern.compile("(" + String.join(")|(", patterns) + ")");
		} else {
			this.pattern = Pattern.compile("(" + String.join(")|(", patterns) + ")", Pattern.CASE_INSENSITIVE);
		}
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
	
	public String getNewBugIdGroupName() {
		int index = 0;
		String name = "ID";
		
		while(bugIdGroupNames.contains(name + index)) {
			++index;
		}
		
		return name + index;
	}
	
	public boolean addPattern(String pattern, String bugIdGroupName) {
		if (!patterns.contains(pattern)) {
			patterns.add(pattern);
			bugIdGroupNames.add(bugIdGroupName);
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
