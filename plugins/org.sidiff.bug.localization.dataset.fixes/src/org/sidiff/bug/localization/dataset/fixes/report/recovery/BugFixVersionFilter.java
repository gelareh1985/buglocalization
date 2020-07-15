package org.sidiff.bug.localization.dataset.fixes.report.recovery;

import java.time.Instant;

import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;

/**
 * Filters versions that do not report bug fixes.
 */
public class BugFixVersionFilter implements VersionFilter {

	private BugFixMessageIDMatcher matcher;

	public BugFixVersionFilter(BugFixMessageIDMatcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public boolean filter(String url, Instant date, String author, String commitMessage) {
		return !matcher.containesBugID(commitMessage);
	}

	public BugFixMessageIDMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(BugFixMessageIDMatcher matcher) {
		this.matcher = matcher;
	}
}
