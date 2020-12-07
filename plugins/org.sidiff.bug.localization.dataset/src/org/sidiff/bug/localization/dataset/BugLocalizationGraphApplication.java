package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
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

/**
 * Converts the EMF model and bug report with its fix-locations to "simple" graph structure.
 */
public class BugLocalizationGraphApplication implements IApplication {

	/*
	 *  Program Arguments: -dataset "<Path to>/DataSet.json" -retrieval "<Path to>/RetrievalConfiguration.json" -repository "<Path to  Repository>"
	 */
	
	public static final String ARGUMENT_DATASET = "-dataset";

	public static final String ARGUMENT_SOURCE_REPOSITORY = "-repository";
	
	public static final String SETTINGS_START_AFTER_VERSION_ID = null; // e.g. "f722ea23e3caf3bc3d51d10558e3e3fea80cbcc5" or null
	
	public static final int SETTINGS_START_AFTER_VERSION_NO = 0;
	
	public static final int SETTINGS_COUNT_OF_BUG_REPORTS = -1; // or -1
	
	public static final boolean SETTINGS_FULL_VERSION = false;
	
	public static final boolean SETTINGS_POSITIVE_SAMPLES = false;
	
	public static final boolean SETTINGS_NEGATIVE_SAMPLES = true;
	
	public static final boolean SETTINGS_ADD_BUG_REPORT_COMMENTS = true;
	
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
		
		if (SETTINGS_START_AFTER_VERSION_ID != null) {
			while (historyIterator.hasNext()) {
				Version version = historyIterator.next();
				
				if (version.getIdentification().equals(SETTINGS_START_AFTER_VERSION_ID)) {
					if (historyIterator.hasNext()) {
						historyIterator.next();
					}
					break;
				}
			}
			bugFixCounter = SETTINGS_START_AFTER_VERSION_NO;
		}
		
		while (historyIterator.hasNext()) {
			System.out.println("Remaining Versions: " + (historyIterator.nextIndex() + 1));
			
			Version fixedVersion = historyIterator.next();
			
			// NOTE: The bug report is stored for the fixed version.
			if (fixedVersion.hasBugReport()) {
				BugReport bugReport = fixedVersion.getBugReport();
				
				// NOTE: The buggy version is the version previous to the fixed version.
				Version buggyVersion = historyIterator.getOlderVersion();
				repository.checkout(dataset.getHistory(), buggyVersion);

				Path systemModelPath = getRepositoryFile(dataset.getSystemModel());

				if (Files.exists(systemModelPath)) {
					SystemModel buggySystemModel = SystemModelFactory.eINSTANCE.createSystemModel(systemModelPath, true);
					BugLocalizationGraphConstructor graphConstructor = new BugLocalizationGraphConstructor(fixedVersion.getBugReport(), buggySystemModel);

					if (!graphConstructor.getBugLocations().isEmpty()) {
						Set<EObject> bugLocations = graphConstructor.getBugLocations();
						++bugFixCounter;

						if (SETTINGS_FULL_VERSION) {
							Iterable<EObject> fullBugLocalizationGraph = graphConstructor.createFullBugLocalizationGraph(SETTINGS_ADD_BUG_REPORT_COMMENTS);
							save(fullBugLocalizationGraph, bugLocations, "full", bugFixCounter, bugReport.getId(), buggyVersion.getIdentification());
						}

						if (SETTINGS_POSITIVE_SAMPLES) {
							Iterable<EObject> positiveSampleBugLocalizationGraph = graphConstructor.createPositiveSampleBugLocalizationGraph(SETTINGS_ADD_BUG_REPORT_COMMENTS);
							save(positiveSampleBugLocalizationGraph, bugLocations, "buglocations", bugFixCounter, bugReport.getId(), buggyVersion.getIdentification());
						}
						
						if (SETTINGS_NEGATIVE_SAMPLES) {
							Set<EObject> negativeSampleBugLocations = graphConstructor.selectNegativeSamples(bugLocations, 10, 100, 50, 1, 1000);
							Iterable<EObject> negativeSampleBugLocalizationGraph = graphConstructor.createNegativeSampleBugLocalizationGraph(SETTINGS_ADD_BUG_REPORT_COMMENTS, negativeSampleBugLocations);
							save(negativeSampleBugLocalizationGraph, negativeSampleBugLocations, "negativesamples", bugFixCounter, bugReport.getId(), buggyVersion.getIdentification());
						}

						if ((bugFixCounter > 0) && ((bugFixCounter >= SETTINGS_COUNT_OF_BUG_REPORTS) && (SETTINGS_COUNT_OF_BUG_REPORTS != -1))) {
							return IApplication.EXIT_OK;
						}
					}
					
					try {
						for (Resource systemModelResource : buggySystemModel.eResource().getResourceSet().getResources()) {
							systemModelResource.unload();
						}
					} catch (Throwable e) {
						System.err.println("Unload exception: " + e.toString());
					}
				} else {
					Activator.getLogger().log(Level.SEVERE, "System model not found: " + systemModelPath);
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
