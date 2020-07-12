package org.sidiff.bug.localization.dataset.history.report.util.placeholder;

public class NoBugReportFound extends BugReportPlaceholder {
	
	private static final String MESSAGE = ">> No Bug Report Found <<";
	
	private static NoBugReportFound instance;
	
	public static NoBugReportFound getInstance() {
		
		if (instance == null) {
			instance = new NoBugReportFound();
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
