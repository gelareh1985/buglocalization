package org.sidiff.bug.localization.dataset.history.repository;

import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.repository.util.VersionFilter;

public interface Repository {

	History getHistory(VersionFilter filter);

}