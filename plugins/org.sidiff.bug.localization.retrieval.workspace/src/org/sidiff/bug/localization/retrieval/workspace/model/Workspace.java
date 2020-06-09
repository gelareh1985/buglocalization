package org.sidiff.bug.localization.retrieval.workspace.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Workspace {

	/**
	 * Folder in which all projects are stored physically. In terms of Git this is
	 * typically the repository path. In terms of SVN this is typically the Eclipse
	 * workspace itself.
	 */
	private Path folder;

	/**
	 * All project contained in the workspace.
	 */
	private List<Project> projects;

	public Workspace(Path folder) {
		this.folder = folder;
		this.projects = new ArrayList<>();
	}

	public Path getFolder() {
		return folder;
	}

	public void setFolder(Path folder) {
		this.folder = folder;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append("Workspace [projects=");

		if (!projects.isEmpty()) {
			text.append("\n");
		}

		for (Project project : projects) {
			text.append("  ");
			text.append(project);
			text.append("\n");
		}

		text.append("]");
		return text.toString();
	}

}
