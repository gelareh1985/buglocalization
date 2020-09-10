package org.sidiff.bug.localization.dataset.workspace.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Workspace {

	/**
	 * All project contained in the workspace.
	 */
	private List<Project> projects;

	public Workspace() {
		this.projects = new ArrayList<>();
	}

	public List<Project> getProjects() {
		
		if (projects == null) {
			return Collections.emptyList();
		}
		
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
	
	public boolean containsProject(Project otherProject) {
		for (Project project : projects) {
			if (project.getName().equals(otherProject.getName())) {
				return true;
			}
		}
		return false;
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
