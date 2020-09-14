package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.workspace.filter.JavaProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;

public class JavaModelRetrievalProvider extends WorkspaceHistoryRetrievalProvider {

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
	 * Ignore method bodies during Java AST parsing and Java model discovery.
	 */
	private boolean ignoreMethodBodies;
	
	/**
	 * Cyclic save of the data set.
	 */
	private int intermediateSave = -1;
	
	public JavaModelRetrievalProvider(
			Supplier<ProjectFilter> projectFilter, 
			Supplier<Repository> codeRepository,
			FileChangeFilter fileChangeFilter,
			boolean ignoreMethodBodies) {
		
		super(codeRepository, fileChangeFilter);
		this.projectFilter = projectFilter;
		this.fileChangeFilter = fileChangeFilter;
		this.ignoreMethodBodies = ignoreMethodBodies;
	}
	
	public JavaModelRetrievalProvider(Path codeRepositoryPath) {
		super(codeRepositoryPath);
		this.projectFilter =  () -> new JavaProjectFilter(); // new PDEProjectFilter();
		this.fileChangeFilter = (fileChange) -> !fileChange.getLocation().toString().endsWith(".java");
		this.ignoreMethodBodies = true;
		this.intermediateSave = 100;
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

	public boolean isIgnoreMethodBodies() {
		return ignoreMethodBodies;
	}

	public void setIgnoreMethodBodies(boolean ignoreMethodBodies) {
		this.ignoreMethodBodies = ignoreMethodBodies;
	}

	public int getIntermediateSave() {
		return intermediateSave;
	}

	public void setIntermediateSave(int intermediateSave) {
		this.intermediateSave = intermediateSave;
	}
}
