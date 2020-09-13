package org.sidiff.bug.localization.dataset.changes.util;

import org.sidiff.bug.localization.dataset.changes.model.FileChange;

public interface FileChangeFilter {

	/**
	 * @param fileChange A file change to be tested.
	 * @return <code>true</code> if the file change should be filtered and ignored;
	 *         <code>false</code> otherwise.
	 */
	public boolean filter(FileChange fileChange);

}
