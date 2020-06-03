package org.sidiff.bug.localization.retrieval.reports.bugtracker;

import java.util.NoSuchElementException;

import org.sidiff.bug.localization.retrieval.reports.model.BugReport;

public interface Bugtracker {

	BugReport getReport(int bugID) throws NoSuchElementException;

}