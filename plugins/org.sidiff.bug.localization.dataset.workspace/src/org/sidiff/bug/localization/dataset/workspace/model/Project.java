package org.sidiff.bug.localization.dataset.workspace.model;

import java.nio.file.Path;

public class Project {

	/**
	 * Name of the project.
	 */
	private String name;

	/**
	 * Folder containing the project relative to the workspace (repository) folder.
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

	public void setFolder(Path local, Path folder) {
		this.folder = local.relativize(folder);
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", folder=" + folder + "]";
	}
}
