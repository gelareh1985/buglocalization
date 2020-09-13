package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;

public class WorkspaceHistoryRetrievalProvider {

	/**
	 * The repository containing the source code to be discovered.
	 */
	private Supplier<Repository> codeRepository;

	public WorkspaceHistoryRetrievalProvider(
			Supplier<Repository> codeRepository,
			FileChangeFilter fileChangeFilter) {
		this.codeRepository = codeRepository;
	}

	public WorkspaceHistoryRetrievalProvider(Path codeRepositoryPath) {
		this.codeRepository = () -> new GitRepository(codeRepositoryPath.toFile());
	}
	
	public Repository createCodeRepository() {
		return codeRepository.get();
	}
	
	public void setCodeRepository(Supplier<Repository> codeRepository) {
		this.codeRepository = codeRepository;
	}
}
