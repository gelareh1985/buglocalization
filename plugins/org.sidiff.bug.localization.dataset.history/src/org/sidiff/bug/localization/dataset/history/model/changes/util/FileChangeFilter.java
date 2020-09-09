package org.sidiff.bug.localization.dataset.history.model.changes.util;

import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;

public interface FileChangeFilter {

	/**
	 * @param fileChange A file change to be tested.
	 * @return <code>true</code> if the file change should be filtered and ignored;
	 *         <code>false</code> otherwise.
	 */
	public boolean filter(FileChange fileChange);

}
