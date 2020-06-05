package org.sidiff.bug.localization.retrieval.history;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.retrieval.history.model.History;
import org.sidiff.bug.localization.retrieval.history.model.Version;
import org.sidiff.bug.localization.retrieval.history.repository.GitRepository;
import org.sidiff.bug.localization.retrieval.history.repository.util.BugFixMatcher;
import org.sidiff.bug.localization.retrieval.history.repository.util.BugFixVersionFilter;
import org.sidiff.bug.localization.retrieval.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.retrieval.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.retrieval.reports.model.BugReport;

public class Application implements IApplication {

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

		List<Callable<Object>> requestReportTasks = new ArrayList<>();

		for (Version version : retrieveReportsForVersions) {
			if (version.getCommitMessage() != null) {
				int bugID = bugFixMatcher.matchBugID(version.getCommitMessage());

				if (bugID != -1) {
					requestReportTasks.add(() -> {
						try {
							BugReport bugReport = bugtracker.getBugReport(bugID);
							
							if (bugReport != null) {
								version.setBugReport(bugReport);
							} else {
								Activator.getLogger().log(Level.SEVERE, "Bug tracker returned <null> for bug ID: " + bugID);
							}
						} catch (NoSuchElementException e) {
							Activator.getLogger().log(Level.WARNING, "Bug ID not found: " + bugID);
						} catch (Throwable e) {
							Activator.getLogger().log(Level.SEVERE, "Bug ID request failed: " + bugID, e);
							e.printStackTrace();
						}
						return null;
					});
				}
			}
		}
		
		// NOTE: If too many requests are made at once, some of them will not be answered by the server!
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(requestReportTasks);
		executorService.shutdown();

		System.out.println(history);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
