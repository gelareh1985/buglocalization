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
	
	/**
	 * The system model representing this project. 
	 */
	private Path systemModel;

	public Project() {
	}
	
	public boolean hasSystemModel() {
		return systemModel != null;
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

	public Path getSystemModel() {
		return systemModel;
	}

	public void setSystemModel(Path systemModel) {
		this.systemModel = systemModel;
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", folder=" + folder + ", systemModel=" + systemModel + "]";
	}
}
