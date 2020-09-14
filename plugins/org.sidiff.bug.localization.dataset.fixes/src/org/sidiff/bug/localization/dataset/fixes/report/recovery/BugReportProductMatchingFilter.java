package org.sidiff.bug.localization.dataset.fixes.report.recovery;

import java.util.List;

import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;

/**
 * Verifies that a given bug report corresponds to a specific product.
 */
public class BugReportProductMatchingFilter implements BugReportFilter {
	
	private List<String> products;

	public BugReportProductMatchingFilter(List<String> products) {
		this.products = products;
	}
	
	@Override
	public boolean filter(BugReport bugReport) {
		return (bugReport.getProduct() == null) || !products.contains(bugReport.getProduct());
	}
}
