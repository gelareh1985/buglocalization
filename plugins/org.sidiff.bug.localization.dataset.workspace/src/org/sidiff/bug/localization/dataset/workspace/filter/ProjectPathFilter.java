package org.sidiff.bug.localization.dataset.workspace.filter;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;

public class ProjectPathFilter implements ProjectFilter {

	private ProjectFilter parentFilter;
	
	private String projectPath;
	
	public ProjectPathFilter(ProjectFilter parentFilter, String projectPath) {
		this.parentFilter = parentFilter;
		this.projectPath = projectPath;
	}

	@Override
	public boolean filter(IProject project) {
		return parentFilter.filter(project);
	}
	
	@Override
	public boolean filter(String name, Path path) {
		return parentFilter.filter(name, path)
				|| ((path.getParent() != null) && (path.toString().matches(projectPath)));
	}
}
