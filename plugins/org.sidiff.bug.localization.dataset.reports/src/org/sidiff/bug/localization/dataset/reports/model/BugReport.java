package org.sidiff.bug.localization.dataset.reports.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class BugReport {
	
	// The field names need to be equal to the JSON attribute names!

	private int id;
	
	private String product;
	
	private String component;
	
	private Instant creation_time;
	
	private String creator; // mail address
	
	private String assigned_to; // mail address
	
	private String severity;
	
	private String resolution; // e.g. FIXED
	
	private String status; // e.g. VERIFIED
	
	private String summary;
	
	private List<BugReportComment> comments;
	
	// Needs empty argument constructor for JSON deserialization!

	public BugReport() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
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

	public String getAssignedTo() {
		return assigned_to;
	}

	public void setAssignedTo(String assigned_to) {
		this.assigned_to = assigned_to;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<BugReportComment> getComments() {
		
		if (comments == null) {
			return Collections.emptyList();
		}
		
		return comments;
	}

	public void setComments(List<BugReportComment> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "BugReport [id=" + id + ", product=" + product + ", component=" + component + ", creation_time="
				+ creation_time + ", creator=" + creator + ", assigned_to=" + assigned_to + ", severity=" + severity
				+ ", resolution=" + resolution + ", status=" + status + ", summary=" + summary + ", comments.size="
				+ (comments != null ? comments.size() : "n/a") + "]";
	}
	
}
