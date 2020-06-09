package org.sidiff.bug.localization.retrieval.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.retrieval.workspace.builder.TestProjectFilter;
import org.sidiff.bug.localization.retrieval.workspace.builder.WorkspaceBuilder;
import org.sidiff.bug.localization.retrieval.workspace.model.Workspace;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		String repositoryURL = "https://git.eclipse.org/r/jdt/eclipse.jdt.core.git";
		String repositoryName = repositoryURL.substring(repositoryURL.lastIndexOf("/") + 1,
				repositoryURL.lastIndexOf("."));
		Path localRepository = Paths.get(System.getProperty("user.home") + "/git/" + repositoryName);
		
		Workspace workspace = new Workspace(localRepository);
		WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder(workspace);
		workspaceBuilder.findProjects(localRepository, new TestProjectFilter());
		
		System.out.println(workspace);
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}