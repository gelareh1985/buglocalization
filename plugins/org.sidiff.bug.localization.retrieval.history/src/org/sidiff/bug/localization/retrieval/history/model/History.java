package org.sidiff.bug.localization.retrieval.history.model;

import java.util.ArrayList;
import java.util.List;

public class History {

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

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder("History [size=" + versions.size() + "]:\n");

		for (Version version : versions) {
			text.append(version);
			text.append("\n");
		}
		
		return text.toString();
	}
}
