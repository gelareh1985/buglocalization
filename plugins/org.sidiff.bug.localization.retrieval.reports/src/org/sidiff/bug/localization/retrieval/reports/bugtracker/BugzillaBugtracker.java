package org.sidiff.bug.localization.retrieval.reports.bugtracker;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.sidiff.bug.localization.retrieval.reports.Activator;
import org.sidiff.bug.localization.retrieval.reports.model.BugReport;
import org.sidiff.bug.localization.retrieval.reports.model.BugReportCommentList;
import org.sidiff.bug.localization.retrieval.reports.model.BugReportList;
import org.sidiff.bug.localization.retrieval.reports.util.JsonUtil;
import org.sidiff.bug.localization.retrieval.reports.util.WebUtil;

import com.google.gson.JsonElement;

/**
 * https://wiki.mozilla.org/Bugzilla:REST_API
 */
public class BugzillaBugtracker implements Bugtracker {

	protected String REST_API_URL;
	
	protected String COMMAND_BUG_BY_ID;
	
	protected String COMMAND_COMMENTS_FOR_BUG;

	public BugzillaBugtracker(String REST_API_URL) {
		this.REST_API_URL = REST_API_URL;
		this.COMMAND_BUG_BY_ID = REST_API_URL + "/bug/%s";
		this.COMMAND_COMMENTS_FOR_BUG = COMMAND_BUG_BY_ID + "/comment";
	}
	
	@Override
	public BugReport getBugReport(int bugID) throws NoSuchElementException {
		BugReport report = requestBugReport(bugID);
		requestBugReportComments(bugID, report);
		return report;
	}

	private BugReport requestBugReport(int bugID) {
		BugReport report = null;
		
		try {
			String responseBug = requestBug(bugID);
			BugReportList reports = JsonUtil.parse(responseBug, BugReportList.class);
			
			if (!reports.getBugs().isEmpty()) {
				report = reports.getBugs().get(0);
				
				if (reports.getBugs().size() > 1) {
					Activator.getLogger().log(Level.WARNING, "Multiple entries for bug " + bugID + "found");
				}
			} else {
				throw new NoSuchElementException("Could not find bug " + bugID);
			}
		} catch (IOException e) {
			Activator.getLogger().log(Level.WARNING, "Could not load bug " + bugID);
			throw new NoSuchElementException(e.getMessage());
		}
		
		return report;
	}

	private void requestBugReportComments(int bugID, BugReport report) {
		try {
			JsonElement commentsJson = getCommentsJSON(bugID);
			JsonElement bugsJson = commentsJson.getAsJsonObject().get("bugs");
			
			if (bugsJson != null) {
				JsonElement bugIdJson = bugsJson.getAsJsonObject().get(bugID + "");
				
				if (bugIdJson != null) {
					BugReportCommentList comments = JsonUtil.parse(bugIdJson, BugReportCommentList.class);
					
					if ((comments.getComments() != null) && !comments.getComments().isEmpty()) {
						report.setComments(comments.getComments());
					} else {
						report.setComments(Collections.emptyList());
					}
				}
			}
		} catch (IOException e) {
			Activator.getLogger().log(Level.WARNING, "Could not load comments for bug " + bugID);
		}
	}

	public JsonElement getBugJSON(int bugID) throws IOException {
		String responseBug = requestBug(bugID);
		return JsonUtil.parse(responseBug);
	}

	public JsonElement getCommentsJSON(int bugID) throws IOException {
		String responseComments = requestComments(bugID);
		return JsonUtil.parse(responseComments);
	}

	private String requestComments(int bugID) throws IOException {
		return extractJsonFromResponse(WebUtil.request(getCommandCommentsForBug(bugID)));
	}
	
	private String requestBug(int bugID) throws IOException {
		return extractJsonFromResponse(WebUtil.request(getCommandBugByID(bugID)));
	}
	
	protected String extractJsonFromResponse(String response) {
		return response;
	}
	
	private String getCommandBugByID(int bugID) {
		return String.format(COMMAND_BUG_BY_ID, bugID);
	}
	
	private String getCommandCommentsForBug(int bugID) {
		return String.format(COMMAND_COMMENTS_FOR_BUG, bugID);
	}
	
	public String getRestApiUrl() {
		return REST_API_URL;
	}

	@Override
	public String toString() {
		return "BugzillaBugtracker [REST_API_URL=" + REST_API_URL + "]";
	}
	
}
