package org.sidiff.bug.localization.retrieval.workspace.builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.pde.internal.core.natures.PDE;
import org.sidiff.bug.localization.retrieval.workspace.Activator;
import org.sidiff.bug.localization.retrieval.workspace.model.Project;
import org.sidiff.bug.localization.retrieval.workspace.model.Workspace;

@SuppressWarnings("restriction")
public class WorkspaceBuilder {

	private Workspace workspace;
	
	public WorkspaceBuilder(Workspace workspace) {
		this.workspace = workspace;
	}

	public void findProjects(Path folder, ProjectFilter projectFilter) {

		try (Stream<Path> paths = Files.list(folder)) {
			for (Path containedFile : (Iterable<Path>) () -> paths.iterator()) {
				if (Files.isDirectory(containedFile) && !Files.isHidden(containedFile)) {
					findProjects(containedFile, projectFilter);
				} else if (isProjectFile(containedFile)) {
					try {
						IProject project = loadProject(containedFile);
						foundProject(containedFile, project, projectFilter);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void foundProject(Path projectFile, IProject project, ProjectFilter projectFilter) throws CoreException {
		if (isPlugInProject(project)) {
			Project pluginProject = new Project();
			pluginProject.setName(project.getName());
			pluginProject.setFolder(workspace, projectFile.getParent());

			if (!projectFilter.filter(pluginProject.getName(), pluginProject.getFolder())) {
				this.workspace.getProjects().add(pluginProject);
			} else {
				if (Activator.getLogger().isLoggable(Level.FINER)) {
					Activator.getLogger().log(Level.FINER, "Filtered Project: " + pluginProject);
				}
			}
		}
	}

	private boolean isPlugInProject(IProject project) throws CoreException {
		try {
			return PDE.hasPluginNature(project);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private IProject loadProject(Path projectFile) throws CoreException {
		IPath projectPath = new org.eclipse.core.runtime.Path(projectFile.toString());
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		JavaCapabilityConfigurationPage.createProject(project, description.getLocationURI(), null);
		return project;
	}

	private boolean isProjectFile(Path file) {
		if (Files.isRegularFile(file)) {
			if (file.getFileName().toString().equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				return true;
			}
		}
		return false;
	}
}
