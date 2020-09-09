package org.sidiff.bug.localization.dataset.fixes.report.request;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sidiff.bug.localization.dataset.history.model.Version;

public class DiscardedBugReports {
	
	/**
	 * Versions for which the bug report request faild (after n retries).
	 */
	private List<Version> missingReports;
	
	/**
	 * Versions for which no bug report could be determined from the bug tracker.
	 */
	private Set<Version> noReports;
	
	/**
	 * Versions for which a bug report was found which was filtered, e.g., mismatch of the product.
	 */
	private Set<Version> filteredReports;

	public DiscardedBugReports(Iterator<Version> versions) {
		this.missingReports = new LinkedList<>();
		versions.forEachRemaining(missingReports::add);
		
		this.noReports = new HashSet<>();
		this.filteredReports = new HashSet<>();
	}
	
	public List<Version> getMissingReports() {
		return missingReports;
	}

	public void setMissingReports(List<Version> missingReports) {
		this.missingReports = missingReports;
	}

	public Set<Version> getNoReports() {
		return noReports;
	}

	public void setNoReports(Set<Version> noReports) {
		this.noReports = noReports;
	}

	public Set<Version> getFilteredReports() {
		return filteredReports;
	}

	public void setFilteredReports(Set<Version> filteredReports) {
		this.filteredReports = filteredReports;
	}
	
}