package org.sidiff.bug.localization.dataset.retrieval.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.ChangeProvider;

public class ProjectChangeProvider implements ChangeProvider {

	private List<FileChange> projectFileChanges;
	
	public ProjectChangeProvider(List<FileChange> projectFileChanges) {
		this.projectFileChanges = projectFileChanges;
	}

	@Override
	public Set<IPath> getChanges(IProject project) {
		Set<IPath> projectChangeLocations = new LinkedHashSet<>();
		
		for (FileChange projectFileChange : projectFileChanges) {
			IPath projectLocation = project.getFile(projectFileChange.getLocation().toString()).getProjectRelativePath();
			
			if (projectLocation != null) {
				projectChangeLocations.add(projectLocation);
			}
		}
		
		return projectChangeLocations;
	}
}
