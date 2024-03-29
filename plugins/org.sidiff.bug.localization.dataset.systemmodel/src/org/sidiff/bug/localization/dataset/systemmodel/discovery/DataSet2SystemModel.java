package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import java.time.Instant;

import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.LineChange;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.reports.model.BugReportComment;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;

public class DataSet2SystemModel {
	
	public ChangeType convertChange(LineChange lineChange) {
		switch (lineChange.getType()) {
		case DELETE:
			return ChangeType.DELETE;
		case EMPTY:
			return ChangeType.DELETE;
		case INSERT:
			return ChangeType.ADD;
		case REPLACE:
			return ChangeType.MODIFY;
		default:
			return ChangeType.MODIFY;
		}
	}
	
	public ChangeType convertChange(FileChange fileChange) {
		switch (fileChange.getType()) {
		case DELETE:
			return ChangeType.DELETE;
		case ADD:
			return ChangeType.ADD;
		case MODIFY:
			return ChangeType.MODIFY;
		default:
			return ChangeType.MODIFY;
		}
	}
	
	public org.sidiff.bug.localization.dataset.systemmodel.TracedVersion convertVersion(Version version, BugReport bugReport, Version fixedVersion) {
		org.sidiff.bug.localization.dataset.systemmodel.TracedVersion eVersion = SystemModelFactory.eINSTANCE.createTracedVersion();
		eVersion.setCodeVersionID(version.getIdentification()); 
		eVersion.setDate(convertDate(version.getDate()));
		eVersion.setAuthor(version.getAuthor());
		eVersion.setCommitMessage(version.getCommitMessage());

		// Save bug report for 'buggy version':
		if (bugReport != null) {
			org.sidiff.bug.localization.dataset.systemmodel.TracedBugReport eBugReport = SystemModelFactory.eINSTANCE.createTracedBugReport();
			eBugReport.setId(bugReport.getId());
			eBugReport.setProduct(bugReport.getProduct());
			eBugReport.setComponent(bugReport.getComponent());
			eBugReport.setCreationTime(convertDate(bugReport.getCreationTime()));
			eBugReport.setCreator(bugReport.getCreator());
			eBugReport.setAssignedTo(bugReport.getAssignedTo());
			eBugReport.setSeverity(bugReport.getSeverity());
			eBugReport.setResolution(bugReport.getResolution());
			eBugReport.setStatus(bugReport.getStatus());
			eBugReport.setSummary(bugReport.getSummary());
			eBugReport.setBugfixTime(fixedVersion.getDate().toString());
			eBugReport.setBugfixCommit(fixedVersion.getCommitMessage());
			
			eVersion.setBugreport(eBugReport);
		
			// Bugtracker discussion:
			for (BugReportComment bugReportComment : bugReport.getComments()) {
				org.sidiff.bug.localization.dataset.systemmodel.BugReportComment eBugReportComment = SystemModelFactory.eINSTANCE.createBugReportComment();
				eBugReportComment.setCreationTime(convertDate(bugReportComment.getCreationTime()));
				eBugReportComment.setCreator(bugReportComment.getCreator());
				eBugReportComment.setText(bugReportComment.getText());
				
				eBugReport.getComments().add(eBugReportComment);
			}
			
			// Git changes of bug fix commit:
			for (FileChange fileChange : bugReport.getBugLocations()) {
				org.sidiff.bug.localization.dataset.systemmodel.FileChange eFileChange = SystemModelFactory.eINSTANCE.createFileChange();
				eFileChange.setLocation(fileChange.getLocation().toString().replace("\\", "/"));
				eFileChange.setType(convertChange(fileChange));
				
				eBugReport.getCodeLocations().add(eFileChange);
			}
		}
		
		return eVersion;
	}
	
	public String convertDate(Instant date) {
		return date.toString();
	}
	
}
