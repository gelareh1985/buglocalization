package org.sidiff.bug.localization.dataset.workspace.builder;

import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.isProjectFile;
import static org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil.loadProjectDescription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class WorkspaceDiscoverer {

	private Workspace workspace;
	
	private Path localRepository;
	
	public WorkspaceDiscoverer(Workspace workspace, Path localRepository) {
		this.workspace = workspace;
		this.localRepository = localRepository;
	}

	public void findProjects(Path folder) {
		
		// Search project file:
		try (Stream<Path> paths = Files.list(folder)) {
			for (Path containedFile : (Iterable<Path>) () -> paths.iterator()) {
				if (!Files.isDirectory(containedFile) && isProjectFile(containedFile)) {
					try {
						IProjectDescription project = loadProjectDescription(containedFile);
						foundProject(containedFile, project);
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
					findProjects(containedFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void foundProject(Path projectFile, IProjectDescription project) throws CoreException {
		Project pluginProject = new Project();
		pluginProject.setName(project.getName());
		pluginProject.setFolder(localRepository, projectFile.getParent());

		this.workspace.getProjects().add(pluginProject);
	}
}
