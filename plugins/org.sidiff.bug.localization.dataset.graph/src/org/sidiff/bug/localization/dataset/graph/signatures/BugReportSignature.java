package org.sidiff.bug.localization.dataset.graph.signatures;

import org.sidiff.bug.localization.dataset.graph.BugReportNode;

public class BugReportSignature implements NodeSignature<BugReportNode> {

	@Override
	public String createSignature(BugReportNode bugReport) {
		return bugReport.getSummary(); // TODO
	}

}
