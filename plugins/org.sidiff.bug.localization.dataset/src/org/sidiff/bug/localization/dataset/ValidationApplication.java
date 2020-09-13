package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class ValidationApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";
	
	private DataSet dataset;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Path datasetPath = getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);
		
		validate();
		
		Activator.getLogger().log(Level.INFO, "Validation Finished");
		return IApplication.EXIT_OK;
	}
	
	private Path getPathFromProgramArguments(IApplicationContext context, String argumentName) throws FileNotFoundException {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(argumentName)) {
				Path dataSetPath = Paths.get(args[i + 1]);
				
				if (!Files.exists(dataSetPath)) {
					throw new FileNotFoundException(args[i + 1]);
				}
				
				return dataSetPath;
			}
		}
		
		throw new FileNotFoundException("Program argument '" + argumentName + "' not specified.");
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
