package org.sidiff.bug.localization.dataset.history.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;

public class BugFixIterator implements Iterator<Version> {
	
	private HistoryIterator historyIterator;
	
	private Version fixedVersion;
	
	private Version buggyVersion;
	
	public BugFixIterator(History history) {
		this.historyIterator = new HistoryIterator(history);
		findNextBugReport();
	}

	@Override
	public boolean hasNext() {
		return (historyIterator != null) && (historyIterator.getCurrentVersion() != null);
	}

	@Override
	public Version next() {
		if (hasNext()) {
			this.fixedVersion = historyIterator.getCurrentVersion();
			this.buggyVersion = historyIterator.getOlderVersion();
			
			findNextBugReport();
			
			return fixedVersion;
		} else {
			throw new NoSuchElementException();
		}
	}

	private void findNextBugReport() {
		while (historyIterator.hasNext()) {
			historyIterator.next();
			
			if (historyIterator.getCurrentVersion().hasBugReport()) {
				return;
			}
		}
		this.historyIterator = null;
	}
	
	/**
	 * @return The newer fixed version.
	 */
	public Version getFixedVersion() {
		return fixedVersion;
	}

	/**
	 * @return The older buggy version.
	 */
	public Version getBuggyVersion() {
		return buggyVersion;
	}
	
	public int getNextIndex() {
		if (historyIterator != null) {
			return historyIterator.getRemaining() + 1; 
		} else {
			return -1;
		}
	}

}
