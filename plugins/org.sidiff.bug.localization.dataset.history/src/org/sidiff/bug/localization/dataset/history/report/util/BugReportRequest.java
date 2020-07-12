package org.sidiff.bug.localization.dataset.history.report.util;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.Activator;
import org.sidiff.bug.localization.dataset.reports.bugtracker.Bugtracker;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;

public class BugReportRequest implements Callable<Boolean> {
	
	private Bugtracker bugtracker;
	
	private Version version;
	
	private int bugID;
	
	public BugReportRequest(Bugtracker bugtracker, Version version, int bugID) {
		this.bugtracker = bugtracker;
		this.version = version;
		this.bugID = bugID;
	}

	@Override
	public Boolean call() throws Exception {
		
		try {
			BugReport bugReport = bugtracker.getBugReport(bugID);
			
			if (bugReport != null) {
				version.setBugReport(bugReport);
				return true;
			} else {
				Activator.getLogger().log(Level.SEVERE, "Bug tracker returned <null> for bug ID: " + bugID);
			}
		} catch (NoSuchElementException e) {
			Activator.getLogger().log(Level.WARNING, "Bug ID not found: " + bugID);
		}
		
		return false;
	}

	public Bugtracker getBugtracker() {
		return bugtracker;
	}

	public Version getVersion() {
		return version;
	}

	public int getBugID() {
		return bugID;
	}
}
