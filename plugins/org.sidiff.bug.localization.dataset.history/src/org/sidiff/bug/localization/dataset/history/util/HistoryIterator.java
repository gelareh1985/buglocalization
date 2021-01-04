package org.sidiff.bug.localization.dataset.history.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;

public class HistoryIterator implements Iterator<Version> {

	private ListIterator<Version> versionIterator;
	
	private int size;
	
	private Version newerVersion;
	
	private Version currentVersion;
	
	private Version olderVersion;
	
	/**
	 * Iterate from old to newer versions.
	 * 
	 * @param history A history with a list of versions, listed from newer to older versions.
	 */
	public HistoryIterator(History history) {
		this(history.getVersions());
	}
	
	/**
	 * Iterate from old to newer versions.
	 * 
	 * @param history A history with a list of versions, listed from newer to older versions.
	 */
	public HistoryIterator(List<Version> history) {
		this.size = history.size();
		this.versionIterator = history.listIterator(size);
		
		if (versionIterator.hasPrevious()) {
			this.newerVersion = versionIterator.previous();
		}
	}
	
	@Override
	public boolean hasNext() {
		return newerVersion != null;
	}

	@Override
	public Version next() {
		if (hasNext()) {
			this.olderVersion = currentVersion;
			this.currentVersion = newerVersion;
			
			if (versionIterator.hasPrevious()) {
				this.newerVersion = versionIterator.previous();
			} else {
				this.newerVersion = null;
			}
			
			return currentVersion;
		} else {
			throw new NoSuchElementException();
		}
	}

	public Version getNewerVersion() {
		return newerVersion;
	}

	public Version getCurrentVersion() {
		return currentVersion;
	}

	public Version getOlderVersion() {
		return olderVersion;
	}
	
	public int nextIndex() {
		return versionIterator.nextIndex(); 
	}
	
}
