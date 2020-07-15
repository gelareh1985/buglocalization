package org.sidiff.bug.localization.dataset.history.repository;

import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;

public interface Repository {

	History getHistory(VersionFilter filter);

}