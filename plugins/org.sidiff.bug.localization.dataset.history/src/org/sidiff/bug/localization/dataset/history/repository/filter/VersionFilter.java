package org.sidiff.bug.localization.dataset.history.repository.filter;

import java.time.Instant;

public interface VersionFilter {

	public static final VersionFilter FILTER_NOTHING = new VersionFilter() {
		
		@Override
		public boolean filter(String url, Instant date, String author, String commitMessage) {
			return false;
		}
		
		@Override
		public boolean retainRevisions() {
			return true; // don't care
		}
	};
	
	/**
	 * @return <code>true</code> if the version should be filtered;
	 *         <code>false</code> otherwise.
	 */
	boolean filter(String url, Instant date, String author, String commitMessage);
	
	/**
	 * @return <code>true</code> if the previous version of none filtered version
	 *         will always be retained (even it would be filtered);
	 *         <code>false</code> otherwise.
	 */
	boolean retainRevisions();

}
