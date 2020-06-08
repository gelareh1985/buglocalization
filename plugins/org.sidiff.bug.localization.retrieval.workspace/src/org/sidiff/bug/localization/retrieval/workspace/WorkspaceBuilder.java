package org.sidiff.bug.localization.retrieval.workspace;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.pde.internal.core.natures.PDE;
import org.sidiff.bug.localization.retrieval.workspace.model.Project;
import org.sidiff.bug.localization.retrieval.workspace.model.Workspace;

@SuppressWarnings("restriction")
public class WorkspaceBuilder {

	private Workspace workspace;
	
	public WorkspaceBuilder(Workspace workspace) {
		this.workspace = workspace;
	}

	public void findProjects(File folder) {
		for (File containedFile : folder.listFiles()) {
			if (containedFile.isDirectory() && !containedFile.isHidden()) {
				findProjects(containedFile);
			} else if (isProjectFile(containedFile)) {
				try {
					IProject project = loadProject(containedFile);
					
					if (isPlugInProject(project)) {
						Project pluginProject = new Project();
						pluginProject.setName(project.getName());
						pluginProject.setFolder(containedFile.getParentFile());
						
						this.workspace.getProjects().add(pluginProject);
					}
				} catch (Throwable e) {
					e.printStackTrace();
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
	
	private IProject loadProject(File projectFile) throws CoreException {
		Path projectPath = new Path(projectFile.getAbsolutePath());
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		JavaCapabilityConfigurationPage.createProject(project, description.getLocationURI(), null);
		return project;
	}

	private boolean isProjectFile(File file) {
		if (file.isFile()) {
			if (file.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				return true;
			}
		}
		return false;
	}
}
