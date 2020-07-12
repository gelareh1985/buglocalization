package org.sidiff.bug.localization.dataset.history.report.util.placeholder;

public class MissingBugReport extends BugReportPlaceholder {
	
	private static final String MESSAGE = ">> Missing Bug Report <<";
	
	private static MissingBugReport instance;
	
	public static MissingBugReport getInstance() {
		
		if (instance == null) {
			instance = new MissingBugReport();
			instance.setProduct(MESSAGE);
			instance.setComponent(MESSAGE);
		}
		
		return instance;
	}

	@Override
	public String toString() {
		return MESSAGE;
	}
}
