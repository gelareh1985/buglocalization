package org.sidiff.bug.localization.dataset.workspace.model;

import java.nio.file.Path;

import org.sidiff.bug.localization.dataset.systemmodel.model.SystemModel;

public class Project {

	/**
	 * Name of the project.
	 */
	private String name;

	/**
	 * Folder containing the project relative to the workspace folder.
	 */
	private Path folder;
	
	/**
	 * The system model representing this project. 
	 */
	private SystemModel systemModel;

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

	public SystemModel getSystemModel() {
		return systemModel;
	}

	public void setSystemModel(SystemModel systemModel) {
		this.systemModel = systemModel;
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", folder=" + folder + ", systemModel(name)=" + systemModel.getName() + "]";
	}

}
