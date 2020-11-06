package org.sidiff.bug.localization.dataset.graph.data.lists.converters.signature;

import org.sidiff.bug.localization.dataset.graph.BugReportCommentNode;

public class ModelElement2SignatureBugComment implements ModelElement2SignatureTyped<BugReportCommentNode> {

	@Override
	public String convert(BugReportCommentNode bugReportCommentNode) {
		return bugReportCommentNode.getComment();
	}

}
