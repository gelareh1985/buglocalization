package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.workspace.filter.PDEProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.TestProjectFilter;

public class JavaModelRetrievalProvider {

	/**
	 * Filters projects from the source code repository, e.g., test code projects
	 * and by default searches PDE projects.
	 */
	private Supplier<ProjectFilter> projectFilter;
	
	/**
	 * The repository containing the source code to be discovered.
	 */
	private Supplier<Repository> codeRepository;
	
	/**
	 * Files to be tested for changes, whether a model needs to be recalculated.
	 */
	private Supplier<Predicate<Path>> fileChangeFilter;
	
	public JavaModelRetrievalProvider(
			Supplier<ProjectFilter> projectFilter, 
			Supplier<Repository> codeRepository,
			Supplier<Predicate<Path>> fileChangeFilter) {
		this.projectFilter = projectFilter;
		this.codeRepository = codeRepository;
		this.fileChangeFilter = fileChangeFilter;
	}

	public JavaModelRetrievalProvider(Path codeRepositoryPath) {
		this.projectFilter =  () -> new TestProjectFilter(new PDEProjectFilter());
		this.codeRepository = () -> new GitRepository(codeRepositoryPath.toFile());
		this.fileChangeFilter = () -> (fileChangePath) -> fileChangePath.toString().endsWith(".java");
	}
	
	public Repository createCodeRepository() {
		return codeRepository.get();
	}
	
	public void setCodeRepository(Supplier<Repository> codeRepository) {
		this.codeRepository = codeRepository;
	}
	
	public ProjectFilter createProjectFilter() {
		return projectFilter.get();
	}

	public void setProjectFilter(Supplier<ProjectFilter> projectFilter) {
		this.projectFilter = projectFilter;
	}

	public Predicate<Path> createFileChangeFilter() {
		return fileChangeFilter.get();
	}

	public void setFileChangeFilter(Supplier<Predicate<Path>> fileChangeFilter) {
		this.fileChangeFilter = fileChangeFilter;
	}
}
