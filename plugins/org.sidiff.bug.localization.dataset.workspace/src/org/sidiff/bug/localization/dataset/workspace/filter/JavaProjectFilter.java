package org.sidiff.bug.localization.dataset.workspace.filter;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil;

public class JavaProjectFilter implements ProjectFilter {

	@Override
	public boolean filter(IProject project) {
		return !ProjectUtil.isJavaProject(project);
	}
	
	@Override
	public boolean filter(String name, Path path) {
		return false;
	}

}
