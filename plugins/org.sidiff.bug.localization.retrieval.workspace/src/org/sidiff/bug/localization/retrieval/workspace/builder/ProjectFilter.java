package org.sidiff.bug.localization.retrieval.workspace.builder;

import java.nio.file.Path;

public interface ProjectFilter {

	/**
	 * @param name The project name.
	 * @param path The repository relative path to the project (including the
	 *             project folder).
	 * @return <code>true</code> if the project should be filtered;
	 *         <code>false</code> otherwise.
	 */
	boolean filter(String name, Path path);

}
