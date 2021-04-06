package org.sidiff.bug.localization.dataset.workspace.filter;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;

public class TestProjectFilter implements ProjectFilter {

	private ProjectFilter parentFilter;
	
	public TestProjectFilter(ProjectFilter parentFilter) {
		this.parentFilter = parentFilter;
	}

	@Override
	public boolean filter(IProject project) {
		return parentFilter.filter(project);
	}
	
	@Override
	public boolean filter(String name, Path path) {
		return parentFilter.filter(name, path)
				|| name.contains(".test") 
				|| ((path.getParent() != null) && (path.getParent().toString().matches(".*?test.*?|.*?Test.*?")));
	}
}
