package org.sidiff.bug.localization.common.utilities.workspace;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.pde.internal.core.natures.PDE;

@SuppressWarnings("restriction")
public class ProjectUtil {

	/**
	 * @param project A project in the workspace.
	 * @return <code>true</code> if the project is a PDE plug-in project, i.e., has
	 *         the nature 'org.eclipse.pde.PluginNature' defined in the .project
	 *         file.
	 * @throws CoreException
	 */
	public static boolean isPlugInProject(IProject project) throws CoreException {
		try {
			return PDE.hasPluginNature(project);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isJavaProject(IProject project) throws CoreException {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param projectFile The .project file describing the project.
	 * @return The loaded/opened workspace project.
	 * @throws CoreException
	 */
	public static IProject loadProject(Path projectFile) throws CoreException {
		IPath projectPath = new org.eclipse.core.runtime.Path(projectFile.toString());
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		JavaCapabilityConfigurationPage.createProject(project, description.getLocationURI(), null);
		return project;
	}

	/**
	 * @param file A file.
	 * @return <code>true</code> if the file describes an eclipse project
	 *         (.project); <code>false</code> otherwise.
	 */
	public static boolean isProjectFile(Path file) {
		if (Files.isRegularFile(file)) {
			if (file.getFileName().toString().equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				return true;
			}
		}
		return false;
	}

}
