package org.sidiff.bug.localization.retrieval.history.model;

import java.time.Instant;

public class Version {

	private String url;
	
	private Instant date;
	
	private String author;
	
	private String commitMessage;

	public Version(String url, Instant date, String author, String commitMessage) {
		this.url = url;
		this.date = date;
		this.author = author;
		this.commitMessage = commitMessage;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String versionURL) {
		this.url = versionURL;
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

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		
		text.append("Version [URL=");
		text.append(url);
		text.append(", date=");
		text.append(date);
		text.append(", author=");
		text.append(author);
		text.append(", ");
		
		if (text.length() >= 150) {
			text.setLength(145);
			text.append("..., ");
		} else {
			text.setLength(150);
		}
		
		text.append("commitMessage=");
		text.append(commitMessage.replace("\n", "").replace("\r", ""));
		text.append("]");
		
		
		return text.toString();
	}
	
	
}
