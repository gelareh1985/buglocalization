package org.sidiff.bug.localization.dataset.fixes.report.request;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.sidiff.bug.localization.dataset.fixes.Activator;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixMessageIDMatcher;
import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.bugtracker.Bugtracker;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;

public class BugReportRequestsExecutor {

	private Bugtracker bugtracker;
	
	private BugReportFilter bugReportFilter;
	
	private BugFixMessageIDMatcher bugFixMessageIDMatcher;
	
	private ExecutorService executorService;
	
	private int retryCount = 0;
	
	private int requestCounter = 0;
	
	private DiscardedBugReports discardedBugReports;

	public BugReportRequestsExecutor(Bugtracker bugtracker, BugReportFilter bugReportFilter, BugFixMessageIDMatcher bugFixMessageIDMatcher) {
		this(bugtracker, bugReportFilter, bugFixMessageIDMatcher, 5, 3);
	}

	/**
	 * NOTE: If too many requests are made at once, some of them will not be answered by the server!
	 */
	public BugReportRequestsExecutor(Bugtracker bugtracker, BugReportFilter bugReportFilter,
			BugFixMessageIDMatcher bugFixMessageIDMatcher, int treadPoolSize, int retryCount) {
		
		this.bugReportFilter = bugReportFilter;
		this.bugtracker = bugtracker;
		this.bugFixMessageIDMatcher = bugFixMessageIDMatcher;
		this.executorService = Executors.newFixedThreadPool(treadPoolSize);
		this.retryCount = retryCount;
	}
	
	public void request(Iterator<Version> versions) {
		List<BugReportResponse> requests = new ArrayList<>();
		this.discardedBugReports = new DiscardedBugReports(versions);

		this.retryCount++;
		
		while (!discardedBugReports.getMissingReports().isEmpty() && (retryCount > 0)) {
			--retryCount;
			++requestCounter;
			
			// start requests:
			for (Version version : discardedBugReports.getMissingReports()) {
				int bugID = bugFixMessageIDMatcher.matchBugID(version.getCommitMessage());
				BugReportRequest request = new BugReportRequest(bugtracker, version, bugID);
				Future<Boolean> response = executorService.submit(request);
				
				requests.add(new BugReportResponse(request, response));
			}
			
			// read responses:
			for (BugReportResponse response : requests) {
				try {
					if (response.getResponse().get()) {
						BugReport bugReport = response.getRequest().getVersion().getBugReport();
						
						if (bugReportFilter.filter(bugReport)) {
							discardedBugReports.getFilteredReports().add(response.getRequest().getVersion());
							
							if (Activator.getLogger().isLoggable(Level.FINE)) {
								Activator.getLogger().log(Level.FINE, "Bug report filtered for bug ID: " 
										+ response.getRequest().getBugID() + " Product: " + bugReport.getProduct());
							}
						}
						discardedBugReports.getMissingReports().remove(response.getRequest().getVersion());
					} else {
						discardedBugReports.getNoReports().add(response.getRequest().getVersion());
					}
				} catch (Throwable e) {
					
					// logging:
					if (retryCount == 0) {
						
						if (Activator.getLogger().isLoggable(Level.SEVERE)) {
							Activator.getLogger().log(Level.SEVERE, "Bug ID request failed: " + response, e);
						}
						
						e.printStackTrace();
					} else {
						if (Activator.getLogger().isLoggable(Level.FINE)) {
							Activator.getLogger().log(Level.FINE, "Bug ID request failed: " + response, e);
						}
					}
				}
				
				// logging:
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "Remaining Reports: " + discardedBugReports.getMissingReports().size());
				}
			}
		}
		
		executorService.shutdown();
	}
	
	public DiscardedBugReports getDiscardedBugReports() {
		return discardedBugReports;
	}
	
	public int getRequestCounter() {
		return requestCounter;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
}
