package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphConstructor;
import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphStorage;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

/**
 * Converts the EMF model and bug report with its fix-locations to "simple" graph structure.
 */
public class BugLocalizationGraphApplication implements IApplication {

	/*
	 *  Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json" -repository "<Path to  Repository>"
	 */
	
	public static final String ARGUMENT_DATASET = "-dataset";

	public static final String ARGUMENT_SOURCE_REPOSITORY = "-repository";
	
	public static final int TEST_COUNT_OF_BUG_REPORTS = 10;
	
	private Path datasetPath;

	private DataSet dataset;

	private GitRepository repository;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		this.datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);

		Path sourceRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_SOURCE_REPOSITORY);
		this.repository = new GitRepository(sourceRepositoryPath.toFile()); 

		HistoryIterator historyIterator = new HistoryIterator(dataset.getHistory());
		int bugFixCounter = 0;
		
		while (historyIterator.hasNext()) {
			System.out.println("Remaining Versions: " + (historyIterator.nextIndex() + 1));
			
			Version fixedVersion = historyIterator.next();
			
			// NOTE: The bug report is stored for the fixed version.
			if (fixedVersion.hasBugReport()) {
				BugReport bugReport = fixedVersion.getBugReport();
				
				// NOTE: The buggy version is the version previous to the fixed version.
				Version buggyVersion = historyIterator.getOlderVersion();
				repository.checkout(dataset.getHistory(), buggyVersion);
				
				for (Project project : buggyVersion.getWorkspace().getProjects()) {
					Path systemModelPath = getRepositoryFile(project.getSystemModel());
					
					if (Files.exists(systemModelPath)) {
						SystemModel buggySystemModel = SystemModelFactory.eINSTANCE.createSystemModel(systemModelPath);
						BugLocalizationGraphConstructor graphConstructor = new BugLocalizationGraphConstructor(fixedVersion.getBugReport(), buggySystemModel);
						
						if (!graphConstructor.getBugLocations().isEmpty()) {
							Set<EObject> bugLocations = graphConstructor.getBugLocations();
							
							Iterable<EObject> bugLocalizationGraph = graphConstructor.createBugLocalizationGraph();
							save(bugLocalizationGraph, bugLocations, "evaluation", ++bugFixCounter, bugReport.getId(), buggyVersion.getIdentification());
							
							Iterable<EObject> localBugLocalizationGraph = graphConstructor.createLocalBugLocalizationGraph();
							save(localBugLocalizationGraph, bugLocations, "training", ++bugFixCounter, bugReport.getId(), buggyVersion.getIdentification());
							
							// TODO
							if (bugFixCounter >= TEST_COUNT_OF_BUG_REPORTS) {
								return IApplication.EXIT_OK;
							}
						} else {
							Activator.getLogger().log(Level.WARNING, "No locations found for bug: " 
									+ fixedVersion.getBugReport().getId() + " Bug report will be ignored!");
						}
					} else {
						Activator.getLogger().log(Level.SEVERE, "System model not found: " + systemModelPath);
					}
				}
			}
		}
		
		Activator.getLogger().log(Level.INFO, "Finished");
		return IApplication.EXIT_OK;
	}

	public void save(Iterable<EObject> bugLocalizationGraph, Set<EObject> bugLocations,
			String subFolder, int counter, int bugID, String version) throws FileNotFoundException, IOException {
		
		String folderName = datasetPath.getFileName().toString();
		folderName = folderName.substring(0, folderName.lastIndexOf("."));
		
		String fileNames = String.format("%05d", counter) + "_bug_" + bugID + "_version_" + version;
		
		Path folder = datasetPath.getParent().resolve(folderName).resolve(subFolder);
		Files.createDirectories(folder);
		
		BugLocalizationGraphStorage storage = new BugLocalizationGraphStorage(bugLocations, bugLocalizationGraph);
		storage.save(folder, fileNames);
	}

	public Path getRepositoryFile(Path localPath) {
		return repository.getWorkingDirectory().resolve(localPath);
	}

	@Override
	public void stop() {
	}

}
