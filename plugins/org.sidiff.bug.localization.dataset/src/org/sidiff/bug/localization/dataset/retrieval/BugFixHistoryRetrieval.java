package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.sidiff.bug.localization.common.utilities.json.JsonUtil;
import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixMessageIDMatcher;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugFixVersionFilter;
import org.sidiff.bug.localization.dataset.fixes.report.recovery.BugReportProductMatchingFilter;
import org.sidiff.bug.localization.dataset.fixes.report.request.BugReportRequestsExecutor;
import org.sidiff.bug.localization.dataset.fixes.report.request.filter.BugReportFilter;
import org.sidiff.bug.localization.dataset.fixes.report.request.placeholders.BugReportPlaceholder;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.repository.Repository;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.dataset.reports.bugtracker.EclipseBugzillaBugtracker;

public class BugFixHistoryRetrieval {

	private RetrievalConfiguration configuration; 

	private DataSet dataset;
	
	private Path datasetPath;
	
	private Repository repository;
	
	private String repositoryURL;
	
	private Path codeRepositoryPath;
	
	public BugFixHistoryRetrieval(RetrievalConfiguration configuration, DataSet dataset, Path datasetStorage) {
		this.configuration = configuration;
		this.dataset = dataset;
		this.datasetPath = datasetStorage;
	}
	
	public void retrieve() {
		retrieveHistory();
		retrieveBugReports();
		cleanUp(dataset);
		
		try {
			saveDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void retrieveHistory() {
		retrieveRepository();
		retrieveBugFixes();
	}

	private Repository retrieveRepository() {
		this.repositoryURL = dataset.getRepositoryHost() + dataset.getRepositoryPath();
		this.codeRepositoryPath = Paths.get(configuration.getLocalRepositoryPath().toFile() + "/" + dataset.getName());
		this.repository = new GitRepository(repositoryURL, codeRepositoryPath.toFile());
		return repository;
	}

	private void retrieveBugFixes() {
		
		// Retrieve commits with bug fixes in their comments:
		BugFixMessageIDMatcher bugFixMessageIDMatcher = new BugFixMessageIDMatcher();
		VersionFilter bugFixVersionFilter = new BugFixVersionFilter(bugFixMessageIDMatcher);
		
		History history = repository.getHistory(bugFixVersionFilter);
		dataset.setHistory(history);
	}

	public void retrieveBugReports() {
		BugFixMessageIDMatcher bugFixMessageIDMatcher = new BugFixMessageIDMatcher();
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();
		BugReportFilter productMatcher = new BugReportProductMatchingFilter(dataset.getBugtrackerProduct());
		
		BugReportRequestsExecutor bugReportRequestsExecutor = new BugReportRequestsExecutor(bugtracker, productMatcher, bugFixMessageIDMatcher);
		bugReportRequestsExecutor.request(dataset.getHistory().getVersions());
		bugReportRequestsExecutor.setPlaceholders();
	}

	public static void cleanUp(DataSet dataset) {
		for (Iterator<Version> iterator = dataset.getHistory().getVersions().iterator(); iterator.hasNext();) {
			Version version = iterator.next();
			
			if (version.getBugReport() instanceof BugReportPlaceholder) {
				iterator.remove();
			}
		}
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getDatasetPath() {
		return datasetPath;
	}
	
	public DataSet getDataset() {
		return dataset;
	}
	
	public void saveDataSet() throws IOException {
		JsonUtil.save(dataset, datasetPath);
	}
}
