package org.sidiff.bug.localization.dataset;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

/**
 * Helper application to run some validation rules on a data set.
 */
public class ValidationApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";
	
	private DataSet dataset;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Path datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);
		
		validate();
		
		Activator.getLogger().log(Level.INFO, "Validation Finished");
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}
	
	private void validate() {
		validate(() -> noDuplicatedProjectInWorkspace());
	}
	
	private void validate(Runnable validation) {
		try {
			validation.run();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void noDuplicatedProjectInWorkspace() {
		for (Version version : dataset.getHistory().getVersions()) {
			Set<String> names = new HashSet<>();
			
			for (Project project : version.getWorkspace().getProjects()) {
				if (!names.add(project.getName())) {
					Activator.getLogger().log(Level.SEVERE, 
							"A project with the name '" + project.getName() 
							+ "' is already contained in version " + version.getIdentification());
				}
			}
		}
	}

}
