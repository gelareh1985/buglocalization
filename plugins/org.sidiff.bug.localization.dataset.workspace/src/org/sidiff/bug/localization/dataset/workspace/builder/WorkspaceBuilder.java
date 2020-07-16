package org.sidiff.bug.localization.dataset.workspace.builder;

import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.isProjectFile;
import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.loadProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.sidiff.bug.localization.dataset.workspace.Activator;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class WorkspaceBuilder {

	private Workspace workspace;
	
	private Path localRepository;
	
	public WorkspaceBuilder(Workspace workspace, Path localRepository) {
		this.workspace = workspace;
		this.localRepository = localRepository;
	}

	public void findProjects(Path folder, ProjectFilter projectFilter) {

		// Search project file:
		try (Stream<Path> paths = Files.list(folder)) {
			for (Path containedFile : (Iterable<Path>) () -> paths.iterator()) {
				if (!Files.isDirectory(containedFile) && isProjectFile(containedFile)) {
					try {
						IProject project = loadProject(containedFile);
						foundProject(containedFile, project, projectFilter);
						return; // not searching for nested projects
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Search subfolders:
		try (Stream<Path> paths = Files.list(folder)) {
			for (Path containedFile : (Iterable<Path>) () -> paths.iterator()) {
				if (Files.isDirectory(containedFile) && !Files.isHidden(containedFile)) {
					findProjects(containedFile, projectFilter);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void foundProject(Path projectFile, IProject project, ProjectFilter projectFilter) throws CoreException {
		if (!projectFilter.filter(project)) {
			Project pluginProject = new Project();
			pluginProject.setName(project.getName());
			pluginProject.setFolder(localRepository, projectFile.getParent());

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
