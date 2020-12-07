package org.sidiff.reverseengineering.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.sidiff.reverseengineering.java.util.WorkspaceUtil;

public class WorkspaceUpdate {
	
	private IProject project;
	
	private Set<IResource> removed;
	
	private Set<IResource> modified;
	
	private Set<IResource> created;
	
	public WorkspaceUpdate(IProject project) {
		this.project = project;
	}
	
	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	public boolean hasRemoved() {
		return (removed != null) && !removed.isEmpty();
	}
	
	public boolean isRemoved(IResource resource) {
		return getRemoved().contains(resource);
	}

	public Set<IResource> getRemoved() {
		
		if (removed == null) {
			removed = Collections.emptySet();
		}
		
		return removed;
	}

	public void setRemoved(Set<IResource> removed) {
		this.removed = removed;
	}
	
	public boolean hasModified() {
		return (modified != null) && !modified.isEmpty();
	}
	
	public boolean isModified(IResource resource) {
		return getModified().contains(resource);
	}

	public Set<IResource> getModified() {
		
		if (modified == null) {
			modified = Collections.emptySet();
		}
		
		return modified;
	}

	public void setModified(Set<IResource> modified) {
		this.modified = modified;
	}
	
	public boolean hasCreated() {
		return (created != null) && !created.isEmpty();
	}
	
	public boolean isCreated(IResource resource) {
		return getCreated().contains(resource);
	}

	public Set<IResource> getCreated() {
		
		if (created == null) {
			created = Collections.emptySet();
		}
		
		return created;
	}

	public void setCreated(Set<IResource> created) {
		this.created = created;
	}
	
	public Predicate<IResource> needsUpdate() {
		return (resource) -> getCreated().contains(resource) || getModified().contains(resource);
	}

	public static WorkspaceUpdate getWorkspaceProject(String name, boolean modified) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		return getWorkspaceProject(project, modified);
	}
	
	public static WorkspaceUpdate getWorkspaceProject(IProject project, boolean modified) {
		if (WorkspaceUtil.isJavaProject(project)) {
			WorkspaceUpdate projectWorkspaceUpdate = new WorkspaceUpdate(project);
			
			try {
				if (modified) {
					projectWorkspaceUpdate.setModified(getAllWorkspaceJavaSources(project));
				} else {
					projectWorkspaceUpdate.setCreated(getAllWorkspaceJavaSources(project));
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
			return projectWorkspaceUpdate;
		}
		return null;
	}
	
	public static List<WorkspaceUpdate> getAllWorkspaceProjects(boolean modified) {
		return getAllWorkspaceProjects(Collections.emptySet(), modified);
	}

	public static List<WorkspaceUpdate> getAllWorkspaceProjects(Set<String> workspaceProjectsFilter, boolean modified) {
		List<WorkspaceUpdate> workspaceUpdate = new ArrayList<>();
		
		// Get all projects in the workspace
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (IProject project : projects) {
			if (!workspaceProjectsFilter.contains(project.getName())) {
				if (WorkspaceUtil.isJavaProject(project)) {
					WorkspaceUpdate projectWorkspaceUpdate = new WorkspaceUpdate(project);
					
					try {
						if (modified) {
							projectWorkspaceUpdate.setModified(getAllWorkspaceJavaSources(project));
						} else {
							projectWorkspaceUpdate.setCreated(getAllWorkspaceJavaSources(project));
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
					
					workspaceUpdate.add(projectWorkspaceUpdate);
				}
			}
		}
		
		return workspaceUpdate;
	}
	
	public static Set<IResource> getAllWorkspaceJavaSources(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		Set<IResource> sources = new HashSet<>();

		for (IPackageFragment javaPackage : packages) {
				try {
					if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit compilationUnit : javaPackage.getCompilationUnits()) {
							sources.add(compilationUnit.getResource());
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}
		
		return sources;
	}
	
	public static Set<String> getProjectScope(List<WorkspaceUpdate> updates) {
		return updates.stream()
				.map(WorkspaceUpdate::getProject)
				.map(IProject::getName)
				.collect(Collectors.toSet());
	}
}
