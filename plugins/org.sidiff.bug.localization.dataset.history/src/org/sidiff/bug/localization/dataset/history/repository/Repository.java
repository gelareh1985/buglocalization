package org.sidiff.bug.localization.dataset.history.repository;

import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;

public interface Repository {

	History getHistory(VersionFilter filter);
	
	boolean checkout(History history, Version version);
	
	String commit(String authorName, String authorEmail, String message, String username, String password);

}