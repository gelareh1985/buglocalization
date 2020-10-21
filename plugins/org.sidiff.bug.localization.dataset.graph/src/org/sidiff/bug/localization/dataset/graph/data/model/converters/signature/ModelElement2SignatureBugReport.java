package org.sidiff.bug.localization.dataset.graph.data.model.converters.signature;

import org.sidiff.bug.localization.dataset.graph.BugReportNode;

public class ModelElement2SignatureBugReport implements ModelElement2SignatureTyped<BugReportNode> {

	@Override
	public String convert(BugReportNode bugReport) {
		return bugReport.getSummary(); // TODO: Comments!?
	}
}
