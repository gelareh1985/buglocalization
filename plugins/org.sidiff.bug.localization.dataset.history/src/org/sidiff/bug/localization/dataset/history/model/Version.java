package org.sidiff.bug.localization.dataset.history.model;

import java.time.Instant;

import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class Version {

	private String identification;
	
	private String identificationTrace;
	
	private Instant date;
	
	private String author;
	
	private String commitMessage;
	
	private BugReport bugReport;
	
	private Workspace workspace;

	public Version(String url, Instant date, String author, String commitMessage) {
		this.identification = url;
		this.date = date;
		this.author = author;
		this.commitMessage = commitMessage;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}
	
	public String getIdentificationTrace() {
		return identificationTrace;
	}
	
	public void setIdentificationTrace(String identificationTrace) {
		this.identificationTrace = identificationTrace;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public BugReport getBugReport() {
		return bugReport;
	}

	public void setBugReport(BugReport bugReport) {
		this.bugReport = bugReport;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		
		text.append("Version [ID=");
		text.append(identification);
		text.append("Version [ID-Trace=");
		text.append(identificationTrace);
		text.append(", date=");
		text.append(date);
		text.append(", workspace(size) = ");
		text.append((workspace != null) ? workspace.getProjects().size() : "n.a.");
		text.append(", (has)bugReport:");
		text.append((bugReport != null) ? "YES" : "NO");
		text.append(", author=");
		text.append(author);
		
		if (text.length() >= 150) {
			text.setLength(147);
			text.append("...");
		} else {
			text.setLength(150);
		}
		
		text.append(", commitMessage=");
		text.append(commitMessage.replace("\n", "").replace("\r", ""));
		text.append("]");
		
		return text.toString();
	}
	
	
}
