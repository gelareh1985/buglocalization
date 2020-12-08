package org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromProject;
import org.eclipse.modisco.java.discoverer.internal.io.java.JavaReader;

@SuppressWarnings("restriction")
public class IncrementalDiscoverJavaModelFromProject extends DiscoverJavaModelFromProject {

	private IncrementalJavaParser javaParser;
	
	public IncrementalDiscoverJavaModelFromProject(IProject project, IncrementalJavaParser javaParser) {
		this.javaParser = javaParser;
	}

	@Override
	protected JavaReader getJavaReader(Map<String, Object> elementOptions) {
		return new IncrementalJavaReader(javaParser, getEFactory(), elementOptions, this);
	}
	
}
