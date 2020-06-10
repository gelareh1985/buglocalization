package org.sidiff.bug.localization.retrieval.workspace.builder;

import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.isPlugInProject;
import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.isProjectFile;
import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.loadProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.sidiff.bug.localization.retrieval.workspace.Activator;
import org.sidiff.bug.localization.retrieval.workspace.model.Project;
import org.sidiff.bug.localization.retrieval.workspace.model.Workspace;

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
}
