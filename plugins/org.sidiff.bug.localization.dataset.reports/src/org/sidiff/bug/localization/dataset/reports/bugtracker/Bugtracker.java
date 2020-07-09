package org.sidiff.bug.localization.dataset.reports.bugtracker;

import java.util.NoSuchElementException;

import org.sidiff.bug.localization.dataset.reports.model.BugReport;

public interface Bugtracker {

	/**
	 * @param bugID The unique ID of the bug in the bugtracking system.
	 * @return The bug report and additional information, e.g., comments on the bug.
	 * @throws NoSuchElementException If the report for the bug ID can't be found.
	 */
	BugReport getBugReport(int bugID) throws NoSuchElementException;

}