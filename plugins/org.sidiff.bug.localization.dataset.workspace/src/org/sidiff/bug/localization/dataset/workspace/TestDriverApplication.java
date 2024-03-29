package org.sidiff.bug.localization.dataset.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.workspace.builder.WorkspaceDiscoverer;
import org.sidiff.bug.localization.dataset.workspace.model.Workspace;

public class TestDriverApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Activator.getLogger().setLevel(Level.FINE);
		
		String repositoryURL = "https://git.eclipse.org/r/jdt/eclipse.jdt.core.git";
		String repositoryName = repositoryURL.substring(repositoryURL.lastIndexOf("/") + 1,
				repositoryURL.lastIndexOf("."));
		Path localRepository = Paths.get(System.getProperty("user.home") + "/git/" + repositoryName);
		
		Workspace workspace = new Workspace();
		WorkspaceDiscoverer workspaceDiscoverer = new WorkspaceDiscoverer(workspace, localRepository);
		workspaceDiscoverer.findProjects(localRepository);
		
		System.out.println(workspace);
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
