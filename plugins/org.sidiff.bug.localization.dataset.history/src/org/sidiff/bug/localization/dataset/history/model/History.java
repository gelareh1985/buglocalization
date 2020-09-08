package org.sidiff.bug.localization.dataset.history.model;

import java.util.ArrayList;
import java.util.List;

public class History {
	
	private String identification;

	private List<Version> versions;

	public History() {
		this.versions = new ArrayList<>();
	}
	
	public List<Version> getVersions() {
		return versions;
	}

	public void setVersions(List<Version> versions) {
		this.versions = versions;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder("History [identification=" + identification + ", versions.size=" + versions.size() + "]:\n");

		for (Version version : versions) {
			text.append(version);
			text.append("\n");
		}
		
		return text.toString();
	}
}
