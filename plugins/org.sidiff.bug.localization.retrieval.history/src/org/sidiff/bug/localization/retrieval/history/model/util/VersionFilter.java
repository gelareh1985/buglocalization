package org.sidiff.bug.localization.retrieval.history.model.util;

import java.util.Calendar;

public interface VersionFilter {

	public static final VersionFilter FILTER_NOTHING = new VersionFilter() {
		
		@Override
		public boolean filter(String url, Calendar date, String author, String commitMessage) {
			return false;
		}
	};
	
	/**
	 * @return <code>true</code> if the version should be filtered;
	 *         <code>false</code> otherwise.
	 */
	boolean filter(String url, Calendar date, String author, String commitMessage);

}
