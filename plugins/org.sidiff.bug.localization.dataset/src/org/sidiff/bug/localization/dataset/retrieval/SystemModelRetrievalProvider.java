package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.JavaProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectNameFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.TestProjectFilter;

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
		// new JavaProjectFilter()
		// new PDEProjectFilter()
		// new TestProjectFilter()
		// new ProjectNameFilter()
		Set<String> filteredProjectNames = new HashSet<>(Arrays.asList(new String[] {
				"converterJclMin", "converterJclMin1.5", "converterJclMin1.7",
				"converterJclMin1.8", "converterJclMin9", "converterJclMin10",
				"converterJclMin11", "converterJclMin12", "converterJclMin12",
				"converterJclMin13", "converterJclMin14", "converterJclMin15"})); 
		this.projectFilter =  () -> new ProjectNameFilter(new TestProjectFilter(new JavaProjectFilter()), filteredProjectNames);
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
