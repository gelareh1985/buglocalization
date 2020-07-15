package org.sidiff.bug.localization.dataset.fixes.report.recovery;

import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;

/**
 * Verifies that a given bug report corresponds to a specific product.
 */
public class BugReportProductMatchingFilter implements BugReportFilter{
	
	private String product;

	public BugReportProductMatchingFilter(String product) {
		this.product = product;
	}
	
	@Override
	public boolean filter(BugReport bugReport) {
		return (bugReport.getProduct() == null) || !bugReport.getProduct().equals(product);
	}
}
