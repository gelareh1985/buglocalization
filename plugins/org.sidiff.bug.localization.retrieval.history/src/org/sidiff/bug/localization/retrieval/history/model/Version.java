package org.sidiff.bug.localization.retrieval.history.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Version {

	private String url;
	
	private Calendar date;
	
	private String author;
	
	private String commitMessage;

	public Version(String url, Calendar date, String author, String commitMessage) {
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

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
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
		text.append(SimpleDateFormat.getInstance().format(date.getTime()));
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
