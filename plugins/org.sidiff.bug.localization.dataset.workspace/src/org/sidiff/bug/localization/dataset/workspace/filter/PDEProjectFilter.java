package org.sidiff.bug.localization.dataset.workspace.filter;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil;

public class PDEProjectFilter implements ProjectFilter  {

	@Override
	public boolean filter(IProject project) {
		try {
			return !ProjectUtil.isPlugInProject(project);
		} catch (CoreException e) {
		}
		return true;
	}
	
	@Override
	public boolean filter(String name, Path path) {
		return false;
	}
	
}
