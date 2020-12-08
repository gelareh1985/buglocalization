package org.sidiff.bug.localization.dataset.retrieval.util;

import java.nio.file.Path;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class WorkspaceUtil {

	public static void refreshWorkspace(Version version, Workspace workspace, boolean byDelta) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		// Make checked out resource changes visible:
		if (!byDelta) {
			for (Project project : workspace.getProjects()) {
				try {
					workspaceRoot.getProject(project.getName()).refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (Throwable e) {
					if (Activator.getLogger().isLoggable(Level.WARNING)) {
						Activator.getLogger().log(Level.WARNING, "Refresh failed: " + e.toString());
					}
				}
			}
		} else {
			
			// Delta update:
			for (Project project : workspace.getProjects()) {
				try {
					IProject workspaceProject = workspaceRoot.getProject(project.getName());

					for (FileChange fileChange : version.getFileChanges()) {
						if (fileChange.getLocation().startsWith(project.getFolder())) {
							Path fileToBeRefreshed = null;

							if (fileChange.getType().equals(FileChangeType.DELETE)) {
								fileToBeRefreshed = project.getFolder().relativize(fileChange.getLocation().getParent());

									if ((workspaceProject != null) && (fileToBeRefreshed != null)) {
										if (fileToBeRefreshed.toString().isEmpty()) {
											workspaceRoot.getProject(project.getName()).refreshLocal(IResource.DEPTH_ONE, null);
										} else {
											workspaceProject.getFile(fileToBeRefreshed.toString()).refreshLocal(IResource.DEPTH_ONE, null);
										}
									}
							} else if (fileChange.getType().equals(FileChangeType.ADD)) {
								fileToBeRefreshed = project.getFolder().relativize(fileChange.getLocation());

								if ((workspaceProject != null) && (fileToBeRefreshed != null)) {
									workspaceProject.getFile(fileToBeRefreshed.toString()).refreshLocal(IResource.DEPTH_ZERO, null);
								}
							}

						}
					}
				} catch (Throwable e) {
					try {
						workspaceRoot.getProject(project.getName()).refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (Throwable e1) {
						if (Activator.getLogger().isLoggable(Level.WARNING)) {
							Activator.getLogger().log(Level.WARNING, "Refresh failed: " + e.toString());
						}
					}
				}
			}
		}
	}
}
