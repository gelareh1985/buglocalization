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

public class BugFixHistoryRetrievalProvider {
	
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
	private Supplier<BugFixVersionFilter> versionFilter;

	/**
	 * The bug tracker that corresponds to the source code repository.
	 */
	private Supplier<Bugtracker> bugtracker;

	/**
	 * Validates the retrieved bug reports, e.g., by the product that corresponds to
	 * the source code repository.
	 */
	private Supplier<BugReportFilter> bugReportFilter;
	
	public BugFixHistoryRetrievalProvider(
			Supplier<Repository> repository,
			Supplier<BugFixMessageIDMatcher> bugFixMessageIDMatcher, 
			Supplier<BugFixVersionFilter> bugFixVersionFilter,
			Supplier<Bugtracker> bugtracker, 
			Supplier<BugReportFilter> bugReportFilter) {
		
		this.codeRepository = repository;
		this.bugFixMessageIDMatcher = bugFixMessageIDMatcher;
		this.versionFilter = bugFixVersionFilter;
		this.bugtracker = bugtracker;
		this.bugReportFilter = bugReportFilter;
	}

	public BugFixHistoryRetrievalProvider(
			String codeRepositoryURL, Path codeRepositoryPath, 
			Supplier<Bugtracker> bugtracker, String bugtrackerProduct) {
		
		this.codeRepository = () -> new GitRepository(codeRepositoryURL, codeRepositoryPath.toFile());
		this.bugFixMessageIDMatcher = () -> new BugFixMessageIDMatcher();
		this.versionFilter = () -> new BugFixVersionFilter(createBugFixMessageIDMatcher());
		this.bugtracker = bugtracker;
		this.bugReportFilter = () -> new BugReportProductMatchingFilter(bugtrackerProduct);
	}
	
	public Repository createCodeRepository() {
		return codeRepository.get();
	}
	
	public void setCodeRepository(Supplier<Repository> codeRepository) {
		this.codeRepository = codeRepository;
	}

	public BugFixMessageIDMatcher createBugFixMessageIDMatcher() {
		return bugFixMessageIDMatcher.get();
	}
	
	public void setBugFixMessageIDMatcher(Supplier<BugFixMessageIDMatcher> bugFixMessageIDMatcher) {
		this.bugFixMessageIDMatcher = bugFixMessageIDMatcher;
	}

	public VersionFilter createVersionFilter() {
		return versionFilter.get();
	}
	
	public void setVersionFilter(Supplier<BugFixVersionFilter> versionFilter) {
		this.versionFilter = versionFilter;
	}

	public Bugtracker createBugtracker() {
		return bugtracker.get();
	}
	
	public void setBugtracker(Supplier<Bugtracker> bugtracker) {
		this.bugtracker = bugtracker;
	}

	public BugReportFilter createBugReportFilter() {
		return bugReportFilter.get();
	}
	
	public void setBugReportFilter(Supplier<BugReportFilter> bugReportFilter) {
		this.bugReportFilter = bugReportFilter;
	}
}
