package org.sidiff.bug.localization.dataset.fixes.report.request;

import java.util.concurrent.Future;

public class BugReportResponse {

	private BugReportRequest request;
	
	private Future<Boolean> response;

	public BugReportResponse(BugReportRequest request, Future<Boolean> response) {
		this.request = request;
		this.response = response;
	}

	public BugReportRequest getRequest() {
		return request;
	}

	public void setRequest(BugReportRequest request) {
		this.request = request;
	}

	public Future<Boolean> getResponse() {
		return response;
	}

	public void setResponse(Future<Boolean> response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "BugReportResponse [version=" + request.getVersion() + ", bugID=" + request.getBugID() + "]";
	}
	
	
}
