package org.sidiff.bug.localization.dataset.workspace.filter;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;

public interface ProjectFilter {

	/**
	 * @return <code>true</code> if the project should be filtered out;
	 *         <code>false</code> otherwise.
	 */
	boolean filter(IProject project);
	
	/**
	 * @param name The project name.
	 * @param path The repository relative path to the project (including the
	 *             project folder).
	 * @return <code>true</code> if the project should be filtered out;
	 *         <code>false</code> otherwise.
	 */
	boolean filter(String name, Path path);

}
