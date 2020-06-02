package org.sidiff.bug.localization.retrieval.history;

import java.io.File;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.retrieval.history.git.GitRepository;
import org.sidiff.bug.localization.retrieval.history.model.History;
import org.sidiff.bug.localization.retrieval.history.model.util.BugFixVersionFilter;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		String repositoryURL = "https://git.eclipse.org/r/jdt/eclipse.jdt.core.git";
		String repositoryName = repositoryURL.substring(repositoryURL.lastIndexOf("/") + 1, repositoryURL.lastIndexOf("."));
		File localRepository = new File(System.getProperty("user.home") + "/git/" + repositoryName); 
		
		GitRepository repository = new GitRepository(localRepository);
		
		if (!repository.exists()) {
			repository.clone(repositoryURL);
		}
		
		BugFixVersionFilter bugFixVersionFilter = new BugFixVersionFilter(); // VersionFilter.FILTER_NOTHING
		History history = repository.getHistory(bugFixVersionFilter);
		
		System.out.println(history);
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
