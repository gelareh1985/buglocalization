package org.sidiff.bug.localization.dataset.reports.model;

import java.time.Instant;

public class BugReportComment {
	
	// The field names need to be equal to the JSON attribute names!

	private Instant creation_time;
	
	private String creator;
	
	private String text;
	
	// Needs empty argument constructor for JSON deserialization!
	
	public BugReportComment() {
	}

	public Instant getCreationTime() {
		return creation_time;
	}

	public void setCreationTime(Instant creation_time) {
		this.creation_time = creation_time;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "BugReportComment [creation_time=" + creation_time + ", creator=" + creator + ", text=" + text.replace("\n", "") + "]";
	}
	
}
