package org.sidiff.reverseengineering.java.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * Some convenient functionality to analyze workspace resources. 
 * 
 * @author Manuel Ohrndorf
 */
public class WorkspaceUtil {

	/**
	 * @param project A (opened) project of the Eclipse workspace.
	 * @return <code>true</code> if the project is configured as a project with Java nature.
	 * @throws CoreException
	 */
	public static boolean isJavaProject(IProject project) {
		try {
			if (project.isOpen()) {
				return project.hasNature(JavaCore.NATURE_ID);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param folder A folder path.
	 * @return <code>false</code> if at least one file exists in the folder;
	 *         <code>true</code> otherwise.
	 */
	public static boolean isEmptyFolder(Path folder) {
		try (Stream<Path> files = Files.list(folder)) {
		    return files.count() == 0;
		} catch (Throwable e) {
			return true;
		}
	}
}
