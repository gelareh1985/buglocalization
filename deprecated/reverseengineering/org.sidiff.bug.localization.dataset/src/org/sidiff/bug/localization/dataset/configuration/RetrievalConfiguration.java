package org.sidiff.bug.localization.dataset.configuration;

import java.nio.file.Path;

public class RetrievalConfiguration {
	
	private Path localRepositoryPath;

	public Path getLocalRepositoryPath() {
		return localRepositoryPath;
	}

	public void setLocalRepositoryPath(Path localRepositoryPath) {
		this.localRepositoryPath = localRepositoryPath;
	}

	@Override
	public String toString() {
		return "RetrievalConfiguration [localRepositoryPath=" + localRepositoryPath + "]";
	}
}
