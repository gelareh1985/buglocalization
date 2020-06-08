package org.sidiff.bug.localization.retrieval.workspace.model;

import java.util.ArrayList;
import java.util.List;

public class Workspace {

	private List<Project> projects;
	
	public Workspace() {
		this.projects = new ArrayList<>();
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
