package org.sidiff.bug.localization.dataset.retrieval;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixMessageIDMatcher;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugReportProductMatchingFilter;
import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;
import org.sidiff.bug.localization.dataset.reports.bugtracker.Bugtracker;

public class BugFixHistoryRetrievalFactory {
	
	/**
	 * The repository containing the source code to be discovered.
	 */
	private Supplier<Repository> codeRepository;

	/**
	 * Matches a potential bug ID in a commit message.
	 */
	private Supplier<BugFixMessageIDMatcher> bugFixMessageIDMatcher;

	/**
	 * Matches version in of the source code repository that represent bug fixes.
	 */
	private Supplier<BugFixVersionFilter> bugFixVersionFilter;

	/**
	 * The bug tracker that corresponds to the source code repository.
	 */
	private Supplier<Bugtracker> bugtracker;

	/**
	 * Validates the retrieved bug reports, e.g., by the product that corresponds to
	 * the source code repository.
	 */
	private Supplier<BugReportFilter> bugReportFilter;
	
	public BugFixHistoryRetrievalFactory(
			Supplier<Repository> repository,
			Supplier<BugFixMessageIDMatcher> bugFixMessageIDMatcher, 
			Supplier<BugFixVersionFilter> bugFixVersionFilter,
			Supplier<Bugtracker> bugtracker, 
			Supplier<BugReportFilter> bugReportFilter) {
		
		this.codeRepository = repository;
		this.bugFixMessageIDMatcher = bugFixMessageIDMatcher;
		this.bugFixVersionFilter = bugFixVersionFilter;
		this.bugtracker = bugtracker;
		this.bugReportFilter = bugReportFilter;
	}

	public BugFixHistoryRetrievalFactory(
			String codeRepositoryURL, Path codeRepositoryPath, 
			Supplier<Bugtracker> bugtracker, String bugtrackerProduct) {
		
		this.codeRepository = () -> new GitRepository(codeRepositoryURL, codeRepositoryPath.toFile());
		this.bugFixMessageIDMatcher = () -> new BugFixMessageIDMatcher();
		this.bugFixVersionFilter = () -> new BugFixVersionFilter(createBugFixMessageIDMatcher());
		this.bugtracker = bugtracker;
		this.bugReportFilter = () -> new BugReportProductMatchingFilter(bugtrackerProduct);
	}
	
	public Repository createCodeRepository() {
		return codeRepository.get();
	}

	public BugFixMessageIDMatcher createBugFixMessageIDMatcher() {
		return bugFixMessageIDMatcher.get();
	}

	public VersionFilter createVersionFilter() {
		return bugFixVersionFilter.get();
	}

	public Bugtracker createBugtracker() {
		return bugtracker.get();
	}

	public BugReportFilter createBugReportFilter() {
		return bugReportFilter.get();
	}
}
