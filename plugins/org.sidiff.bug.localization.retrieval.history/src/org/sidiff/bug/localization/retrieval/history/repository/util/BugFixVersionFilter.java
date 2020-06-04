package org.sidiff.bug.localization.retrieval.history.repository.util;

import java.time.Instant;

public class BugFixVersionFilter implements VersionFilter {

	private BugFixMatcher matcher;

	public BugFixVersionFilter(BugFixMatcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public boolean filter(String url, Instant date, String author, String commitMessage) {
		return !matcher.containesBugID(commitMessage);
	}

	public BugFixMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(BugFixMatcher matcher) {
		this.matcher = matcher;
	}
}
