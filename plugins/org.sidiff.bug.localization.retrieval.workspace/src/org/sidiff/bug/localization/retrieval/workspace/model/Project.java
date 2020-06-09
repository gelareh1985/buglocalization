package org.sidiff.bug.localization.retrieval.workspace.model;

import java.nio.file.Path;

public class Project {

	/**
	 * Name of the project.
	 */
	private String name;

	/**
	 * Folder containing the project relative to the workspace folder.
	 */
	private Path folder;

	public Project() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Path getFolder() {
		return folder;
	}

	public void setFolder(Path folder) {
		this.folder = folder;
	}

	public void setFolder(Workspace workspace, Path folder) {
		this.folder = workspace.getFolder().relativize(folder);
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", folder=" + folder + "]";
	}
}
