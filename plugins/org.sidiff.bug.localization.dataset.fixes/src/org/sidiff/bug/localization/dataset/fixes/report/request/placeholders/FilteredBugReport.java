package org.sidiff.bug.localization.dataset.fixes.report.request.placeholders;

import org.sidiff.bug.localization.dataset.reports.model.BugReport;

public class FilteredBugReport extends BugReportPlaceholder {

	private static final String MESSAGE = ">> Filtered Bug Report <<";
	
	private BugReport filtered;
	
	public FilteredBugReport(BugReport filtered) {
		this.filtered = filtered;
	}
	
	public BugReport getFiltered() {
		return filtered;
	}

	@Override
	public String toString() {
		return MESSAGE;
	}
	
}
