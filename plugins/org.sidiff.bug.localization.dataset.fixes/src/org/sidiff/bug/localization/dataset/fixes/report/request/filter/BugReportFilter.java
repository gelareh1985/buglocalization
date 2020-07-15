package org.sidiff.bug.localization.dataset.fixes.report.request.filter;

import org.sidiff.bug.localization.dataset.reports.model.BugReport;

public interface BugReportFilter {

	public static final BugReportFilter FILTER_NOTHING = new BugReportFilter() {
		
		@Override
		public boolean filter(BugReport bugReport) {
			return false;
		}
	};
	
	/**
	 * @return <code>true</code> if the bug report should be filtered;
	 *         <code>false</code> otherwise.
	 */
	public boolean filter(BugReport bugReport);
}
