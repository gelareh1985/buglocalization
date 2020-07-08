package org.sidiff.bug.localization.dataset.reports.model;

import java.util.List;

public class BugReportList {
	
	// The field names need to be equal to the JSON attribute names!

	private List<BugReport> bugs;
	
	// Needs empty argument constructor for JSON deserialization!
	
	public BugReportList() {
	}

	public List<BugReport> getBugs() {
		return bugs;
	}

	public void setBugs(List<BugReport> bugs) {
		this.bugs = bugs;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append("BugReportList [bugs=\n");
		
		for (BugReport bugReport : bugs) {
			text.append("  ");
			text.append(bugReport.toString());
			text.append("\n");
		}
		
		text.append("]");
		return text.toString();
	}
	
}
