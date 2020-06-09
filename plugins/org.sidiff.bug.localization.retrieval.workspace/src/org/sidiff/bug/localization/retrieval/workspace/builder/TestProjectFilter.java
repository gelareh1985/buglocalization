package org.sidiff.bug.localization.retrieval.workspace.builder;

import java.nio.file.Path;

public class TestProjectFilter implements ProjectFilter {

	@Override
	public boolean filter(String name, Path path) {
		return name.contains(".test") || ((path.getParent() != null) && (path.getParent().toString().matches("(.*?test.*?)|.*?Test.*?")));
	}
}
