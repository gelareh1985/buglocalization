package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.RetrievalProcess;

public class RetrievalApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_ARQUISITION = "-acquisition";
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Path dataSetPath = getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = JsonUtil.parse(dataSetPath, DataSet.class);
		
		Path acquisitionConfigurationPath = getPathFromProgramArguments(context, ARGUMENT_ARQUISITION);
		RetrievalConfiguration retrievalConfiguration = JsonUtil.parse(acquisitionConfigurationPath, RetrievalConfiguration.class);
		
		RetrievalProcess retrievalProcess = new RetrievalProcess(retrievalConfiguration, dataSet);
		retrievalProcess.retrieve();
		
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

}
