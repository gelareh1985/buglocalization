package org.sidiff.bug.localization.dataset.workspace.builder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil;
import org.sidiff.bug.localization.dataset.workspace.Activator;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class WorkspaceBuilder {

	private Workspace workspace;
	
	private Path codeRepositoryPath;
	
	public WorkspaceBuilder(Workspace workspace, Path codeRepositoryPath) {
		this.workspace = workspace;
		this.codeRepositoryPath = codeRepositoryPath;
	}
	
	public void cleanWorkspace() {
		for (IProject workspaceProject : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (!containsProject(workspaceProject.getName(), workspaceProject.getLocation().toFile().toPath())) {
				ProjectUtil.removeProject(workspaceProject);
			}
		}
	}
	
	private boolean containsProject(String projectName, Path projectPath) {

		for (Project project : workspace.getProjects()) {
			if (project.getName().equals(projectName)) {
				if (getFullProjectPath(codeRepositoryPath, project).equals(projectPath)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Workspace createProjects(ProjectFilter projectFilter) {
		Workspace loadedWorkspace = new Workspace();
		
		for (Project project : workspace.getProjects()) {
			try {
				boolean isLoaded = loadProject(project, projectFilter);
				
				if (isLoaded) {
					loadedWorkspace.getProjects().add(project);
				} else {
					if (Activator.getLogger().isLoggable(Level.FINER)) {
						Activator.getLogger().log(Level.FINER, "Filtered project: " + project);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();

				if (Activator.getLogger().isLoggable(Level.WARNING)) {
					Activator.getLogger().log(Level.WARNING, "Could not load project: " + project);
				}
			}
		}
		
		return loadedWorkspace;
	}

	private boolean loadProject(Project project, ProjectFilter projectFilter) throws CoreException {
		
		if (!projectFilter.filter(project.getName(), project.getFolder())) {
			
			// Project exists in workspace?
			IProject workspaceProject = ProjectUtil.getProject(project.getName());
			
			// Load new project to the workspace?
			if (workspaceProject == null) {
				Path workspaceProjectFile = getFullProjectFilePath(codeRepositoryPath, project);
				workspaceProject = ProjectUtil.loadProject(workspaceProjectFile);
			}

			if (!projectFilter.filter(workspaceProject)) {
				return true;
			}
		}
		
		return false;
	}

	private Path getFullProjectFilePath(Path codeRepositoryPath, Project project) {
		Path workspaceProjectFile = Paths.get(
				codeRepositoryPath.toString(), 
				project.getFolder().toString(), 
				IProjectDescription.DESCRIPTION_FILE_NAME);
		return workspaceProjectFile;
	}
	
	private Path getFullProjectPath(Path codeRepositoryPath, Project project) {
		Path workspaceProjectFile = Paths.get(
				codeRepositoryPath.toString(), 
				project.getFolder().toString());
		return workspaceProjectFile;
	}

}
