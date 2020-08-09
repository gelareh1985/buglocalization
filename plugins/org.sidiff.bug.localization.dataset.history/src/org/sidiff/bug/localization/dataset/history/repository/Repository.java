package org.sidiff.bug.localization.dataset.history.repository;

import java.nio.file.Path;
import java.util.List;

import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;

public interface Repository {

	Path getWorkingDirectory();
	
	History getHistory(VersionFilter filter);
	
	List<FileChange> getChanges(Version version);
	
	boolean checkout(History history, Version version);
	
	String commit(String authorName, String authorEmail, String message, String username, String password);

}