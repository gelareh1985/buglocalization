package org.sidiff.bug.localization.retrieval.history.repository;

import org.sidiff.bug.localization.retrieval.history.model.History;
import org.sidiff.bug.localization.retrieval.history.repository.util.VersionFilter;

public interface Repository {

	History getHistory(VersionFilter filter);

}