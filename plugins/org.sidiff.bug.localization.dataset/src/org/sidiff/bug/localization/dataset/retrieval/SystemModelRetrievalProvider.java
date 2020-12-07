package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.JavaProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;

public class SystemModelRetrievalProvider extends WorkspaceHistoryRetrievalProvider {
	
	/**
	 * Filters projects from the source code repository, e.g., test code projects
	 * and by default searches PDE projects.
	 */
	private Supplier<ProjectFilter> projectFilter;
	
	/**
	 * Files to be tested for changes, whether a model needs to be recalculated.
	 */
	private FileChangeFilter fileChangeFilter;
	
	/**
	 * Cyclic save of the data set.
	 */
	private int intermediateSave = -1;
	
	/**
	 * Transform method bodies in system model.
	 */
	private boolean includeMethodBodies = false;
	
	public SystemModelRetrievalProvider(Path codeRepositoryPath) {
		super(codeRepositoryPath);
		this.projectFilter =  () -> new JavaProjectFilter(); // new PDEProjectFilter();
		this.fileChangeFilter = (fileChange) -> !fileChange.getLocation().toString().toLowerCase().endsWith(".java");
		this.intermediateSave = 200;
	}
	
	public ProjectFilter createProjectFilter() {
		return projectFilter.get();
	}

	public void setProjectFilter(Supplier<ProjectFilter> projectFilter) {
		this.projectFilter = projectFilter;
	}

	public FileChangeFilter getFileChangeFilter() {
		return fileChangeFilter;
	}

	public void setFileChangeFilter(FileChangeFilter fileChangeFilter) {
		this.fileChangeFilter = fileChangeFilter;
	}

	public int getIntermediateSave() {
		return intermediateSave;
	}

	public void setIntermediateSave(int intermediateSave) {
		this.intermediateSave = intermediateSave;
	}

	public boolean isIncludeMethodBodies() {
		return includeMethodBodies;
	}

	public void setIncludeMethodBodies(boolean includeMethodBodies) {
		this.includeMethodBodies = includeMethodBodies;
	}
}
