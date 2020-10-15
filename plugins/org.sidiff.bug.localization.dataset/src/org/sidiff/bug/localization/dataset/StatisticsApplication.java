package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.util.BugFixIterator;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
import org.sidiff.bug.localization.dataset.retrieval.util.BugReportUtil;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

import eu.artist.migration.mdt.javaee.java.uml.traces.Model2CodeTrace;

public class StatisticsApplication implements IApplication {
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String ARGUMENT_SYSTEM_MODEL_REPOSITORY = "-systemmodelrepository";
	
	public static final String CSV_COLUMN_SEPERATOR = ";";
	
	public static final String CSV_ROW_SEPERATOR = "\n";
	
	private static class ProductStatistic {
		String productName;				// Product Name
		int versions;					// Versions (reduced) 
		int yearOldestVersion;			// Oldest Version (Year)
		int yearNewestVersion;			// Newest Version (Year)
	
		int bugFixedVersions;			// Versions with Bug Fixes
		int javaBugFixedVersions;		// Versions with Java Bug Fixes
		
		int fixedFiles;					// Fixed Files
		int javaFixedFiles;				// Fixed Java Files
		
		int bugReportWords;				// Words in Bug Report
		int bugReportUniqueWords;		// Unique Words in Bug Report (Dictionary)
		
		int projects;					// Projects
		int fixedProjects;				// Projects with Fixes
		int javaFixedProjects;			// Projects with Java Fixes
		
		int umlBugFixLocations;			// UML Bug Fix Locations
		int umlBugFixQuantification;	// UML Bug Fix Quantification
	}

	private static class ProjectStatistic {
		String projectName; 			// Project Name
		
		int bugFixes; 					// Bug Fixes	
		int javaBugFixes; 				// Java Bug Fixes
		
		int fixedFiles; 				// Fixed Files	
		int javaFixedFiles;				// Fixed Java Files
		
		int bugReportWords;				// Words in Bug Report
		int bugReportUniqueWords;		// Unique Words in Bug Report (Dictionary)
		
		int umlBugFixLocations;			// UML Bug Fix Locations
		int umlBugFixQuantification;	// UML Bug Fix Quantification
		
		int umlClassifiers;				// UML Classifiers
		int umlExternalClassifiers;		// + External UML Classifiers
		int umlOperations;				// UML Operations
		int umlExternalOperations;		// + External UML Operations
		int umlProperties;				// UML Properties
		int umlExternalProperties;		// + External UML Properties
	}
	
	private Path dataSetPath;
	
	private DataSet dataSet;
	
	private SystemModelRetrievalProvider provider;
	
	private SystemModelRepository systemModelRepository;
	
	private ProductStatistic productStatistic;
	
	private Map<String, ProjectStatistic> projectStatistics;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		this.dataSetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataSet = DataSetStorage.load(dataSetPath);
		
		Path systemModelRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_SYSTEM_MODEL_REPOSITORY);
		this.systemModelRepository = new SystemModelRepository(systemModelRepositoryPath, dataSet);
		this.provider = new SystemModelRetrievalProvider();
		
		this.productStatistic = new ProductStatistic();
		this.projectStatistics = new LinkedHashMap<>();
		
		// Product and project fix related statistics:
		countDataSetFixes();
		
		// Product related statistics:
		calculateProductStatistics();
		
		// Product and project bug report related statistics:
		measureBugReports();
		
		//  Product and project UML related statistics:
		countUMLChangeLocations();
		measureUMLModels();
		
		// Save statistics to file:
		saveProductStatistics(dataSetPath, Collections.singletonList(productStatistic));
		saveProjectStatistics(dataSetPath, getSortedProjectStatistics());
		
		return IApplication.EXIT_OK;
	}

	private void countDataSetFixes() {
		BugFixIterator bugFixIterator = new BugFixIterator(dataSet.getHistory());
		
		while (bugFixIterator.hasNext()) {
			Version fixedVersion = bugFixIterator.next();
			assert fixedVersion.hasBugReport();
			
			boolean hasBugFixes = false;
			boolean hasJavaBugFixes = false;
			
			for (Project project : fixedVersion.getWorkspace().getProjects()) {
				ProjectStatistic projectStatistic = getProjectStatistic(project);

				// All changes:
				List<FileChange> projectFileChanges = HistoryUtil.getChanges(project, fixedVersion.getBugReport().getBugLocations());

				if (!projectFileChanges.isEmpty()) {
					++projectStatistic.bugFixes;
					projectStatistic.fixedFiles += projectFileChanges.size(); // project
					productStatistic.fixedFiles += projectFileChanges.size(); // product

					hasBugFixes = true;
				}

				// Only changes on Java files:
				List<FileChange> projectJavaFileChanges = HistoryUtil.getChanges(project, fixedVersion.getBugReport().getBugLocations(), provider.getFileChangeFilter());

				if (!projectJavaFileChanges.isEmpty()) {
					++projectStatistic.javaBugFixes;
					projectStatistic.javaFixedFiles += projectJavaFileChanges.size(); // project
					productStatistic.javaFixedFiles += projectJavaFileChanges.size(); // product 

					hasJavaBugFixes = true;
				}
			}
			
			/*
			 *  Product related statistics:
			 */
			if (hasBugFixes) {
				++productStatistic.bugFixedVersions;
			}
			
			if (hasJavaBugFixes) {
				++productStatistic.javaBugFixedVersions;
			}
		}
	}

	private void calculateProductStatistics() {
		productStatistic.productName = dataSet.getName();
		productStatistic.versions = dataSet.getHistory().getVersions().size();
		productStatistic.projects = projectStatistics.size();
		
		List<Version> versions = dataSet.getHistory().getVersions();
		SimpleDateFormat df = new SimpleDateFormat("yyyy");
		
		productStatistic.yearOldestVersion = Integer.valueOf(df.format(Date.from(versions.get(versions.size() - 1).getDate())));
		productStatistic.yearNewestVersion = Integer.valueOf(df.format(Date.from(versions.get(0).getDate())));
		
		projectStatistics.forEach((name, projectStatistic) -> {
			if (projectStatistic.fixedFiles > 0) {
				++productStatistic.fixedProjects;
			}
		});
		
		projectStatistics.forEach((name, projectStatistic) -> {
			if (projectStatistic.javaFixedFiles > 0) {
				++productStatistic.javaFixedProjects;
			}
		});
	}
	
	private void measureBugReports() {
		System.out.println("Start measuring bug reports...");
		
		Set<String> productDictionary = new HashSet<>();
		Map<String, Set<String>> projectDictionaries = new HashMap<>();
		
		for (Version version : dataSet.getHistory().getVersions()) {
			if (version.hasBugReport()) {
				List<String> bugReportWords = getBugReportWords(version);
				
				productStatistic.bugReportWords += bugReportWords.size();
				productDictionary.addAll(bugReportWords);
				
				for (Project project : version.getWorkspace().getProjects()) {
					if (HistoryUtil.hasChanges(project, version.getBugReport().getBugLocations(), provider.getFileChangeFilter())) {
						ProjectStatistic projectStatistic = getProjectStatistic(project);
						projectStatistic.bugReportWords += bugReportWords.size();
						
						Set<String> projectDictionary = projectDictionaries.getOrDefault(project.getName(), new HashSet<>());
						projectDictionaries.put(project.getName(), projectDictionary);
						projectDictionary.addAll(bugReportWords);
					}
				}
			}
		}
		
		productStatistic.bugReportUniqueWords = productDictionary.size();
		
		for (Entry<String, Set<String>> projectDictionary : projectDictionaries.entrySet()) {
			ProjectStatistic projectStatistic = getProjectStatistic(projectDictionary.getKey());
			projectStatistic.bugReportUniqueWords = projectDictionary.getValue().size();
		}
		
		System.out.println("Finished measuring bug reports");
	}
	
	private List<String> getBugReportWords(Version version) {
		String text = BugReportUtil.getFullPlainText(version, BugReportUtil.DEFAULT_BUG_REPORT_COMMENT_FILTER);
		return BugReportUtil.getWords(text);
	}

	private void countUMLChangeLocations() {
		BugFixIterator bugFixIterator = new BugFixIterator(dataSet.getHistory());
		
		while (bugFixIterator.hasNext()) {
			System.out.println("UML Change Locations - Current Version: " + bugFixIterator.nextIndex());
			
			bugFixIterator.next();
			Version fixedVersion = bugFixIterator.getFixedVersion();
			Version buggyVersion = bugFixIterator.getBuggyVersion();
			
			assert fixedVersion.hasBugReport();
			
			if (buggyVersion != null) {
				systemModelRepository.checkout(buggyVersion);
			}
			
			for (Project project : fixedVersion.getWorkspace().getProjects()) {
				if (HistoryUtil.hasChanges(project, fixedVersion.getBugReport().getBugLocations(), provider.getFileChangeFilter())) {
					Project buggyProject = HistoryUtil.getCorrespondingVersion(buggyVersion, project);
					
					if ((buggyProject != null) && (buggyProject.hasSystemModel())) {
						try {
							ProjectStatistic projectStatistic = getProjectStatistic(buggyProject);
							
							SystemModel umlSystemModel = systemModelRepository.getSystemModel(buggyProject);
							View classDiagramView = umlSystemModel.getViewByKind(ViewDescriptions.UML_CLASS_DIAGRAM);
							
							projectStatistic.umlBugFixLocations += classDiagramView.getChanges().size(); // project
							productStatistic.umlBugFixLocations += classDiagramView.getChanges().size(); // product
							
							for (Change bugFixLocation : classDiagramView.getChanges()) {
								projectStatistic.umlBugFixQuantification += bugFixLocation.getQuantification(); // project
								productStatistic.umlBugFixQuantification += bugFixLocation.getQuantification(); // product
							}
						} catch (Throwable e) {
							System.err.println("System model not loaded: ");
							System.err.println(project);
							System.err.println(buggyProject);
						}
					}
				}
			}
		}
	}

	private void measureUMLModels() {
		Set<String> measuredUMLModels = new HashSet<>();
		int counter = 0;
		
		for (Version version : dataSet.getHistory().getVersions()) {
			System.out.println("UML Measure Model - Current Version: " + ++counter);
			
			for (Project project : version .getWorkspace().getProjects()) {
				
				// Measure only size of latest version:
				if (!measuredUMLModels.contains(project.getName()) && project.hasSystemModel()) {
					try {
						systemModelRepository.checkout(version);
						SystemModel umlSystemModel = systemModelRepository.getSystemModel(project);

						ProjectStatistic projectStatistic = getProjectStatistic(project);
						measureUMLModel(umlSystemModel, projectStatistic);

						measuredUMLModels.add(project.getName());
					} catch (Throwable e) {
						System.err.println("System model not loaded: ");
						System.err.println(project);
						System.err.println(version);
					}
				}
			}
		}
	}
	
	private void measureUMLModel(SystemModel umlSystemModel, ProjectStatistic projectStatistic) {
		View classDiagramView = umlSystemModel.getViewByKind(ViewDescriptions.UML_CLASS_DIAGRAM);
		Model2CodeTrace trace = new Model2CodeTrace(classDiagramView.getModel().eResource());
		
		classDiagramView.getModel().eAllContents().forEachRemaining(element -> {
			if (element instanceof Classifier) {
				if (isTraceableElement(element, trace)) {
					if (element instanceof Class) {
						++projectStatistic.umlClassifiers;
						projectStatistic.umlOperations += ((Class) element).getOwnedOperations().size();
						projectStatistic.umlProperties += ((Class) element).getAttributes().size();
					} else if (element instanceof Interface) {
						++projectStatistic.umlClassifiers;
						projectStatistic.umlOperations += ((Interface) element).getOwnedOperations().size();
						projectStatistic.umlProperties += ((Interface) element).getAttributes().size();
					}
				} else {
					if (element instanceof Class) {
						++projectStatistic.umlExternalClassifiers;
						projectStatistic.umlExternalOperations += ((Class) element).getOwnedOperations().size();
						projectStatistic.umlExternalProperties += ((Class) element).getAttributes().size();
					} else if (element instanceof Interface) {
						++projectStatistic.umlExternalClassifiers;
						projectStatistic.umlExternalOperations += ((Interface) element).getOwnedOperations().size();
						projectStatistic.umlExternalProperties += ((Interface) element).getAttributes().size();
					}
				}
			}
		});
	}

	private boolean isTraceableElement(EObject element, Model2CodeTrace trace) {
		return trace.getJavaFile(element, false) != null;
	}

	private ProjectStatistic getProjectStatistic(Project project) {
		return getProjectStatistic(project.getName());
	}
	
	private ProjectStatistic getProjectStatistic(String projectName) {
		ProjectStatistic data = projectStatistics.get(projectName);
		
		if (data == null) {
			data = new ProjectStatistic();
			data.projectName = projectName;
			projectStatistics.put(data.projectName, data);
		}
		
		return data;
	}
	
	private void saveProductStatistics(Path dataSetPath, List<ProductStatistic> productStatistics) {
		String fileName = dataSetPath.getFileName().toString();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		fileName = fileName + "_product_statistics.csv"; 
		
		Path path = dataSetPath.getParent().resolve(fileName);
		
		StringBuilder csv = new StringBuilder();
		
		// Header:
		csv.append("Product Name");
		csv.append(CSV_COLUMN_SEPERATOR + "Versions (reduced)");
		csv.append(CSV_COLUMN_SEPERATOR + "Oldest Version (Year)");
		csv.append(CSV_COLUMN_SEPERATOR + "Newest Version (Year)");		
		
		csv.append(CSV_COLUMN_SEPERATOR + "Versions with Bug Fixes");
		csv.append(CSV_COLUMN_SEPERATOR + "Versions with Java Bug Fixes");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Fixed Files");
		csv.append(CSV_COLUMN_SEPERATOR + "Fixed Java Files");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Words in Bug Report");
		csv.append(CSV_COLUMN_SEPERATOR + "Unique Words in Bug Report (Dictionary)");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Projects");
		csv.append(CSV_COLUMN_SEPERATOR + "Projects with Fixes");
		csv.append(CSV_COLUMN_SEPERATOR + "Projects with Java Fixes");
		
		csv.append(CSV_COLUMN_SEPERATOR + "UML Bug Fix Locations");
		csv.append(CSV_COLUMN_SEPERATOR + "UML Bug Fix Quantification");
		
		csv.append(CSV_ROW_SEPERATOR);
		
		// Table:
		for (ProductStatistic productStatistic : productStatistics) {
			csv.append(productStatistic.productName);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.versions);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.yearOldestVersion);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.yearNewestVersion);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.bugFixedVersions);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.javaBugFixedVersions);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.fixedFiles);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.javaFixedFiles);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.bugReportWords);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.bugReportUniqueWords);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.projects);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.fixedProjects);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.javaFixedProjects);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.umlBugFixLocations);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.umlBugFixQuantification);
			
			csv.append(CSV_ROW_SEPERATOR);
		}
		
		try {
			Files.write(path, csv.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveProjectStatistics(Path dataSetPath, List<ProjectStatistic> projectStatistics) {
		String fileName = dataSetPath.getFileName().toString();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		fileName = fileName + "_project_statistics.csv"; 
		
		Path path = dataSetPath.getParent().resolve(fileName);
		
		StringBuilder csv = new StringBuilder();
		
		// Header:
		csv.append("Project Name");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Bug Fixes");
		csv.append(CSV_COLUMN_SEPERATOR + "Java Bug Fixes");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Fixed Files");
		csv.append(CSV_COLUMN_SEPERATOR + "Fixed Java Files");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Words in Bug Report");
		csv.append(CSV_COLUMN_SEPERATOR + "Unique Words in Bug Report (Dictionary)");
		
		csv.append(CSV_COLUMN_SEPERATOR + "UML Bug Fix Locations");
		csv.append(CSV_COLUMN_SEPERATOR + "UML Bug Fix Quantification");
		
		csv.append(CSV_COLUMN_SEPERATOR + "UML Classes/Interfaces");
		csv.append(CSV_COLUMN_SEPERATOR + " + External UML Classes/Interfaces");
		csv.append(CSV_COLUMN_SEPERATOR + "UML Operations");
		csv.append(CSV_COLUMN_SEPERATOR + " + External UML Operations");
		csv.append(CSV_COLUMN_SEPERATOR + "UML Properties");
		csv.append(CSV_COLUMN_SEPERATOR + " + External UML Properties");
		
		csv.append(CSV_ROW_SEPERATOR);
		
		// Table:
		for (ProjectStatistic projectStatistic : projectStatistics) {
			csv.append(projectStatistic.projectName);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.bugFixes);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.javaBugFixes);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.fixedFiles);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.javaFixedFiles);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.bugReportWords);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.bugReportUniqueWords);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlBugFixLocations);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlBugFixQuantification);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlClassifiers);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlExternalClassifiers);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlOperations);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlExternalOperations);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlProperties);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlExternalProperties);
			
			csv.append(CSV_ROW_SEPERATOR);
		}
		
		try {
			Files.write(path, csv.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<ProjectStatistic> getSortedProjectStatistics() {
		List<ProjectStatistic> sortedProjectStatistics = new ArrayList<>(projectStatistics.values());
		sortedProjectStatistics.sort(new Comparator<ProjectStatistic>() {
	
			@Override
			public int compare(ProjectStatistic s1, ProjectStatistic s2) {
				return s1.projectName.compareTo(s2.projectName);
			}
		});
		return sortedProjectStatistics;
	}

	@Override
	public void stop() {
	}

}
