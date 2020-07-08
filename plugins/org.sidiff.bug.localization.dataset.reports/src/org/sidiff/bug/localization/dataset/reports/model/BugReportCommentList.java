package org.sidiff.bug.localization.dataset.reports.model;

import java.util.List;

public class BugReportCommentList {
	
	// The field names need to be equal to the JSON attribute names!

	private List<BugReportComment> comments;
	
	// Needs empty argument constructor for JSON deserialization!
	
	public BugReportCommentList() {
	}

	public List<BugReportComment> getComments() {
		return comments;
	}

	public void setComments(List<BugReportComment> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append("BugReportCommentList [comments=\n");
		
		for (BugReportComment comment : comments) {
			text.append("  ");
			text.append(comment.toString());
			text.append("\n");
		}
		
		text.append("]");
		return text.toString();
	}
	
	
}
