package org.sidiff.bug.localization.dataset.history;

import java.io.File;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.report.util.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixMatcher;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;

public class TestDriverApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {

		String repositoryURL = "https://git.eclipse.org/r/jdt/eclipse.jdt.core.git";
		String repositoryName = repositoryURL.substring(repositoryURL.lastIndexOf("/") + 1,
				repositoryURL.lastIndexOf("."));
		File localRepository = new File(System.getProperty("user.home") + "/git/" + repositoryName);

		GitRepository repository = new GitRepository(localRepository);

		if (!repository.exists()) {
			repository.clone(repositoryURL);
		}

		// Retrieve commits with bug fixes:
		BugFixMatcher bugFixMatcher = new BugFixMatcher();
		BugFixVersionFilter bugFixVersionFilter = new BugFixVersionFilter(bugFixMatcher); // VersionFilter.FILTER_NOTHING
		History history = repository.getHistory(bugFixVersionFilter);
		
		// Retrieve bug reports:
		// > TEST ON SUB LIST OF VERSIONS <
		List<Version> retrieveReportsForVersions = history.getVersions().subList(0, 50);
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(bugtracker, bugFixMatcher);
		
		System.out.println("Start bug report requests:");
		bugReportRequestsExecutor.request(retrieveReportsForVersions);
		
		System.out.println("No reports found for " + bugReportRequestsExecutor.getNoReports().size() + " bugs");
		System.out.println("Bug report request failed for " + bugReportRequestsExecutor.getMissingReports().size() + " bugs");
		System.out.println("Request count: " + bugReportRequestsExecutor.getRequestCounter());

		System.out.println(history);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
