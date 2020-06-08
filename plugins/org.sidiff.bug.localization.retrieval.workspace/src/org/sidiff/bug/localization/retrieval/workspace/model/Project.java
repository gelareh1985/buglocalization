package org.sidiff.bug.localization.retrieval.workspace.model;

import java.io.File;

public class Project {
	
	private String name;
	
	private File folder;
	
	public Project() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", folder=" + folder + "]";
	}
}
