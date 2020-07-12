package org.sidiff.bug.localization.dataset.history.report.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.sidiff.bug.localization.dataset.history.Activator;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.report.util.placeholder.MissingBugReport;
import org.sidiff.bug.localization.dataset.history.report.util.placeholder.NoBugReportFound;
import org.sidiff.bug.localization.dataset.history.repository.util.BugFixMatcher;
import org.sidiff.bug.localization.dataset.reports.bugtracker.Bugtracker;

public class BugReportRequestsExecutor {

	private Bugtracker bugtracker;
	
	private BugFixMatcher bugFixMatcher;
	
	private ExecutorService executorService;
	
	private int retryCount = 0;
	
	private int requestCounter = 0;
	
	private List<Version> missingReports;
	
	private List<Version> noReports;
	
	public BugReportRequestsExecutor(Bugtracker bugtracker, BugFixMatcher bugFixMatcher) {
		this(bugtracker, bugFixMatcher, 5, 3);
	}

	/**
	 * NOTE: If too many requests are made at once, some of them will not be answered by the server!
	 */
	public BugReportRequestsExecutor(Bugtracker bugtracker, BugFixMatcher bugFixMatcher, int treadPoolSize, int retryCount) {
		this.bugtracker = bugtracker;
		this.bugFixMatcher = bugFixMatcher;
		this.executorService = Executors.newFixedThreadPool(treadPoolSize);
		this.retryCount = retryCount;
	}
	
	public void request(List<Version> versions) {
		List<BugReportResponse> requests = new ArrayList<>();
		
		this.missingReports = new LinkedList<>(versions);
		this.noReports = new ArrayList<>();
		
		this.retryCount++;
		
		while (!missingReports.isEmpty() && (retryCount > 0)) {
			--retryCount;
			++requestCounter;
			
			// start requests:
			for (Version version : missingReports) {
				int bugID = bugFixMatcher.matchBugID(version.getCommitMessage());
				BugReportRequest request = new BugReportRequest(bugtracker, version, bugID);
				Future<Boolean> response = executorService.submit(request);
				
				requests.add(new BugReportResponse(request, response));
			}
			
			// read responses:
			for (BugReportResponse response : requests) {
				try {
					if (response.getResponse().get()) {
						missingReports.remove(response.getRequest().getVersion());
					} else {
						noReports.add(response.getRequest().getVersion());
					}
				} catch (Throwable e) {
					
					// report:
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
			}
		}
		
		executorService.shutdown();
	}
	
	public void setPlaceholders() {
		
		for (Version version : missingReports) {
			version.setBugReport(MissingBugReport.getInstance());
		}
		
		for (Version version : noReports) {
			version.setBugReport(NoBugReportFound.getInstance());
		}
	}
	
	public List<Version> getMissingReports() {
		return missingReports;
	}

	public List<Version> getNoReports() {
		return noReports;
	}
	
	public int getRequestCounter() {
		return requestCounter;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
}
