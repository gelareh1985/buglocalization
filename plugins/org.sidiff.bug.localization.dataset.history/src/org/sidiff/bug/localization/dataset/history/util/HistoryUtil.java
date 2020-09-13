package org.sidiff.bug.localization.dataset.history.util;

import java.util.ArrayList;
import java.util.List;

import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class HistoryUtil {
	
	public static boolean hasChanges(Project project, Version olderVersion, Version version, FileChangeFilter fileChangeFilter) {
		if (olderVersion == null) {
			return true; // initial version
		} else {
			return hasChanges(project, version.getFileChanges(), fileChangeFilter);
		}
	}

	public static boolean hasChanges(Project project, List<FileChange> fileChanges, FileChangeFilter fileChangeFilter) {
		
		for (FileChange fileChange : fileChanges) {
			if (!fileChangeFilter.filter(fileChange)) {
				if (fileChange.getLocation().startsWith(project.getFolder())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static List<FileChange> getChanges(Project project, List<FileChange> fileChanges, FileChangeFilter fileChangeFilter) {
		List<FileChange> projectFileChanges = new ArrayList<>();
		
		for (FileChange fileChange : fileChanges) {
			if (!fileChangeFilter.filter(fileChange)) {
				if (fileChange.getLocation().startsWith(project.getFolder())) {
					projectFileChanges.add(fileChange);
				}
			}
		}
		
		return projectFileChanges;
	}
	
	public static boolean hasPreviousVersion(Version oldVersion, Project project) {
		
		if (oldVersion != null) {
			for (Project oldProject : oldVersion.getWorkspace().getProjects()) {
				if (oldProject.getName().equals(project.getName())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}