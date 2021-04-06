package org.sidiff.bug.localization.dataset.workspace.filter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public class ProjectNameFilter implements ProjectFilter {

	private ProjectFilter parentFilter;
	
	private Set<String> projectNames;
	
	public ProjectNameFilter(ProjectFilter parentFilter, Collection<String> projectNames) {
		this.parentFilter = parentFilter;
		this.projectNames = new HashSet<>(projectNames);
	}

	@Override
	public boolean filter(IProject project) {
		return parentFilter.filter(project);
	}
	
	@Override
	public boolean filter(String name, Path path) {
		return parentFilter.filter(name, path) || projectNames.contains(name);
	}

}
