package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.workspace.filter.PDEProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.ProjectFilter;
import org.sidiff.bug.localization.dataset.workspace.filter.TestProjectFilter;

public class JavaModelRetrievalFactory {

	/**
	 * Filters projects from the source code repository, e.g., test code projects
	 * and by default searches PDE projects.
	 */
	private Supplier<ProjectFilter> projectFilter;
	
	/**
	 * The repository containing the source code to be discovered.
	 */
	private Supplier<Repository> codeRepository;
	
	public JavaModelRetrievalFactory(
			Supplier<ProjectFilter> projectFilter, 
			Supplier<Repository> codeRepository) {
		
		this.projectFilter = projectFilter;
		this.codeRepository = codeRepository;
	}

	public JavaModelRetrievalFactory(Path codeRepositoryPath) {
		this.projectFilter =  () -> new TestProjectFilter(new PDEProjectFilter());
		this.codeRepository = () -> new GitRepository(codeRepositoryPath.toFile());
	}
	
	public Repository createCodeRepository() {
		return codeRepository.get();
	}
	
	public ProjectFilter createProjectFilter() {
		return projectFilter.get();
	}

}
