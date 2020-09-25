package org.sidiff.bug.localization.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class StatisticsApplication implements IApplication {
	
	public static final String ARGUMENT_DATASET = "-dataset";
	
	public static final String CSV_COLUMN_SEPERATOR = ";";
	
	public static final String CSV_ROW_SEPERATOR = "\n";
	
	private static class ProjectStatistic {
		String projectName; 		// Project Name	
		
		int bugFixes; 				// Bug Fixes	
		int javaBugFixes; 			// Java Bug Fixes
		
		int fixedFiles; 			// Fixed Files	
		int javaFixedFiles;			// Fixed Java Files
		
		int umlBugFixLocations;		// UML Bug Fix Locations
		
		int umlClasses;				// UML Classes
		int umlMethods;				// UML Methods
		int umlProperties;			// UML Properties
		
		int umlExternalClasses;		// External UML Classes
		int umlExternalMethods;		// External UML Methods
		int umlExternalProperties;	// External UML Properties
	}
	
	private static class ProductStatistic {
		String productName;			// Product Name
		int versions;				// Versions (reduced) 

		int bugFixedVersions;		// Versions with Bug Fixes
		int javaBugFixedVersions;	// Versions with Java Bug Fixes
		
		int fixedFiles;				// Fixed Files
		int javaFixedFiles;			// Fixed Java Files
		
		int projects;				// Projects
		int fixedProjects;			// Projects with Fixes
		int javaFixedProjects;		// Projects with Java Fixes
	}

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Path dataSetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		SystemModelRetrievalProvider provider = new SystemModelRetrievalProvider();
		
		ProductStatistic productStatistic = new ProductStatistic();
		Map<String, ProjectStatistic> projectStatistics = new LinkedHashMap<>();
		
		/*
		 *  Product and project related statistics:
		 */
		
		for (Version version : dataSet.getHistory().getVersions()) {
			if (version.hasBugReport()) {
				++productStatistic.bugFixedVersions;
			}
			
			boolean hasJavaBugFixes = false;
			
			for (Project project : version.getWorkspace().getProjects()) {
				ProjectStatistic projectStatistic = getProjectStatistic(projectStatistics, project);
				
				if (version.hasBugReport()) {
					
					// All changes:
					List<FileChange> projectFileChanges = HistoryUtil.getChanges(project, version.getBugReport().getBugLocations());

					if (!projectFileChanges.isEmpty()) {
						++projectStatistic.bugFixes;
						projectStatistic.fixedFiles += projectFileChanges.size();
						productStatistic.fixedFiles += projectFileChanges.size();
					}
					
					// Only changes on Java files:
					List<FileChange> projectJavaFileChanges = HistoryUtil.getChanges(project, version.getBugReport().getBugLocations(), provider.getFileChangeFilter());

					if (!projectJavaFileChanges.isEmpty()) {
						++projectStatistic.javaBugFixes;
						projectStatistic.javaFixedFiles += projectJavaFileChanges.size();
						productStatistic.javaFixedFiles += projectJavaFileChanges.size(); 
						
						hasJavaBugFixes = true;
					}
				}
			}
			
			if (hasJavaBugFixes) {
				++productStatistic.javaBugFixedVersions;
			}
		}
		
		/*
		 *  Product related statistics:
		 */
		
		productStatistic.productName = dataSet.getName();
		productStatistic.versions = dataSet.getHistory().getVersions().size();
		productStatistic.projects = projectStatistics.size();
		
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
		
		/*
		 *  Save statistics to file:
		 */
		
		saveProductStatistics(dataSetPath, Collections.singletonList(productStatistic));
		saveProjectStatistics(dataSetPath, new ArrayList<>(projectStatistics.values()));
		
		return IApplication.EXIT_OK;
	}
	
	private ProjectStatistic getProjectStatistic(Map<String, ProjectStatistic> projectStatistics, Project project) {
		ProjectStatistic data = projectStatistics.get(project.getName());
		
		if (data == null) {
			data = new ProjectStatistic();
			data.projectName = project.getName();
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
		
		csv.append(CSV_COLUMN_SEPERATOR + "Versions with Bug Fixes");
		csv.append(CSV_COLUMN_SEPERATOR + "Versions with Java Bug Fixes");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Fixed Files");
		csv.append(CSV_COLUMN_SEPERATOR + "Fixed Java Files");
		
		csv.append(CSV_COLUMN_SEPERATOR + "Projects");
		csv.append(CSV_COLUMN_SEPERATOR + "Projects with Fixes");
		csv.append(CSV_COLUMN_SEPERATOR + "Projects with Java Fixes");
		
		csv.append(CSV_ROW_SEPERATOR);
		
		// Table:
		for (ProductStatistic productStatistic : productStatistics) {
			csv.append(productStatistic.productName);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.versions);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.bugFixedVersions);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.javaBugFixedVersions);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.fixedFiles);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.javaFixedFiles);
			
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.projects);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.fixedProjects);
			csv.append(CSV_COLUMN_SEPERATOR + productStatistic.javaFixedProjects);
			
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
		
		csv.append(CSV_COLUMN_SEPERATOR + "UML Bug Fix Locations");
		
		csv.append(CSV_COLUMN_SEPERATOR + "UML Classes");
		csv.append(CSV_COLUMN_SEPERATOR + "UML Methods");
		csv.append(CSV_COLUMN_SEPERATOR + "UML Properties");
		
		csv.append(CSV_COLUMN_SEPERATOR + "External UML Classes");
		csv.append(CSV_COLUMN_SEPERATOR + "External UML Methods");
		csv.append(CSV_COLUMN_SEPERATOR + "External UML Properties");
		
		csv.append(CSV_ROW_SEPERATOR);
		
		// Table:
		for (ProjectStatistic projectStatistic : projectStatistics) {
			csv.append(projectStatistic.projectName);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.bugFixes);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.javaBugFixes);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.fixedFiles);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.javaFixedFiles);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlBugFixLocations);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlClasses);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlMethods);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlProperties);
			
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlExternalClasses);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlExternalMethods);
			csv.append(CSV_COLUMN_SEPERATOR + projectStatistic.umlExternalProperties);
			
			csv.append(CSV_ROW_SEPERATOR);
		}
		
		try {
			Files.write(path, csv.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
	}

}
