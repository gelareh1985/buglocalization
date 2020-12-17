package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import java.time.Instant;

import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.LineChange;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.reports.model.BugReportComment;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.View;

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
	
	public org.sidiff.bug.localization.dataset.systemmodel.TracedVersion convertVersion(Version version, Version newerVersion) {
		org.sidiff.bug.localization.dataset.systemmodel.TracedVersion eVersion = SystemModelFactory.eINSTANCE.createTracedVersion();
		eVersion.setCodeVersionID(version.getIdentificationTrace());
		eVersion.setModelVersionID(version.getIdentification());
		eVersion.setDate(convertDate(version.getDate()));
		eVersion.setAuthor(version.getAuthor());
		eVersion.setCommitMessage(version.getCommitMessage());
		eVersion.setFixedVersion(version.hasBugReport());
		eVersion.setBuggyVersion(newerVersion.hasBugReport());

		// Save bug report for 'buggy version':
		if (newerVersion.hasBugReport()) {
			BugReport bugReport = newerVersion.getBugReport();
			
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
			for (FileChange fileChange : newerVersion.getBugReport().getBugLocations()) {
				org.sidiff.bug.localization.dataset.systemmodel.FileChange eFileChange = SystemModelFactory.eINSTANCE.createFileChange();
				eFileChange.setLocation(fileChange.getLocation().toString().replace("\\", "/"));
				eFileChange.setType(convertChange(fileChange));
				
				eBugReport.getCodeLocations().add(eFileChange);
			}
		}
		
		return eVersion;
	}
	
	public void relocateModelChanges(SystemModel systemModel, org.sidiff.bug.localization.dataset.systemmodel.BugReport eBugReport) {
		for (View view : systemModel.getViews()) {
			eBugReport.getModelLocations().addAll(view.getChanges());
		}
	}
	
	public String convertDate(Instant date) {
		return date.toString();
	}
	
}
