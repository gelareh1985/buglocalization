package org.sidiff.bug.localization.dataset.fixes;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixMessageIDMatcher;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugReportProductMatchingFilter;
import org.sidiff.bug.localization.dataset.fixes.report.request.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;

public class TestDriverApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Activator.getLogger().setLevel(Level.FINE);
		org.sidiff.bug.localization.dataset.reports.Activator.getLogger().setLevel(Level.FINE);
		org.sidiff.bug.localization.dataset.history.Activator.getLogger().setLevel(Level.FINE);
		
		List<String> products = Arrays.asList(new String[] {"JDT", "Platform"}); // for validation and filtering of bug reports
		
		String repositoryURL = "https://git.eclipse.org/r/jdt/eclipse.jdt.core.git";
		String repositoryName = repositoryURL.substring(repositoryURL.lastIndexOf("/") + 1,
				repositoryURL.lastIndexOf("."));
		File localRepository = new File(System.getProperty("user.home") + "/git/" + repositoryName);

		GitRepository repository = new GitRepository(repositoryURL, localRepository);

		// Retrieve commits with bug fixes:
		BugFixMessageIDMatcher bugFixMatcher = new BugFixMessageIDMatcher();
		BugFixVersionFilter bugFixVersionFilter = new BugFixVersionFilter(bugFixMatcher);
		bugFixVersionFilter.setRetainVersion(false); // bug fixes only
//		VersionFilter bugFixVersionFilter =  VersionFilter.FILTER_NOTHING;
		History history = repository.getHistory(bugFixVersionFilter);
		
		// Retrieve bug reports:
		// > TEST ON SUB LIST OF VERSIONS <
		List<Version> retrieveReportsForVersions = history.getVersions().subList(0, 50);
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();
		BugReportFilter bugReportFilter = new BugReportProductMatchingFilter(products);
//		BugReportFilter bugReportFilter = BugReportFilter.FILTER_NOTHING;
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(bugtracker, bugReportFilter, bugFixMatcher);
		
		System.out.println("Start bug report requests:");
		bugReportRequestsExecutor.request(retrieveReportsForVersions.iterator());
		
		System.out.println("Bug reports filtered for " + bugReportRequestsExecutor.getDiscardedBugReports().getFilteredReports().size() + " bug fixes");
		System.out.println("No bug reports found for " + bugReportRequestsExecutor.getDiscardedBugReports().getNoReports().size() + " bug fixes");
		System.out.println("Bug report request failed for " + bugReportRequestsExecutor.getDiscardedBugReports().getMissingReports().size() + " bug fixes");
		System.out.println("Request count: " + bugReportRequestsExecutor.getRequestCounter());

		System.out.println(history);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
