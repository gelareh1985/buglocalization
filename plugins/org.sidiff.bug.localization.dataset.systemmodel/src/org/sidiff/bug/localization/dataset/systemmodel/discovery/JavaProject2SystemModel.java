package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.changes.model.LineChange;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.TracedVersion;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.util.UMLUtil;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.reverseengineering.java.IncrementalReverseEngineering;
import org.sidiff.reverseengineering.java.TransformationListener;
import org.sidiff.reverseengineering.java.TransformationTrace;
import org.sidiff.reverseengineering.java.WorkspaceUpdate;
import org.sidiff.reverseengineering.java.configuration.TransformationModule;
import org.sidiff.reverseengineering.java.configuration.uml.TransformationDomainUML;
import org.sidiff.reverseengineering.java.configuration.uml.TransformationModuleUML;
import org.sidiff.reverseengineering.java.configuration.uml.TransformationSettingsUML;
import org.sidiff.reverseengineering.java.util.CodeLinesToModelTrace;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class JavaProject2SystemModel {
	
	private Path modelRepository;
	
	private IncrementalReverseEngineering transformation;
	
	private CodeLinesToModelTrace codeToModel;
	
	private Map<IResource, Path> javaToModel;
	
	private TransformationSettingsUML settings;
	
	private SystemModel systemModel;
	
	private View umlClassDiagramView;
	
	private Set<Resource> modelResources;
	
	protected DataSet2SystemModel dataSet2SystemModel;
	
	public JavaProject2SystemModel(Path modelRepository, String name, boolean includeMethodBodies, SystemModel systemModel) {
		this.modelRepository = modelRepository;
		this.dataSet2SystemModel = new DataSet2SystemModel();
		
		// Remember created/modified resources:
		this.codeToModel = new CodeLinesToModelTrace();
		this.javaToModel = new HashMap<>();
		this.modelResources = new HashSet<>();
		
		// Application Settings:
		this.settings = new TransformationSettingsUML();
		this.settings.setBaseURI(URI.createFileURI(modelRepository.toString()));
		this.settings.setIncludeMethodBodies(includeMethodBodies);
		this.settings.setModelFileExtension(TransformationDomainUML.getModelFileExtension());
		this.settings.setName(name);
		
		// Add to UML model to system model:
		this.systemModel = systemModel;
		this.umlClassDiagramView = systemModel.getViewByKind(ViewDescriptions.UML_CLASS_DIAGRAM);
		
		if (umlClassDiagramView == null) {
			this.umlClassDiagramView = SystemModelFactory.eINSTANCE.createView();
			this.umlClassDiagramView.setName(ViewDescriptions.UML_CLASS_DIAGRAM.getName());
			this.umlClassDiagramView.setKind(ViewDescriptions.UML_CLASS_DIAGRAM.getViewKind());
			this.umlClassDiagramView.setDescription(ViewDescriptions.UML_CLASS_DIAGRAM.getDescription());
			systemModel.getViews().add(umlClassDiagramView);
		}

		// Engine:
		TransformationModule transformationModule = new TransformationModuleUML(settings);
		Injector injector = Guice.createInjector(transformationModule);
		this.transformation = injector.getInstance(IncrementalReverseEngineering.class);
		
		// Trace code line to model elements:
		transformation.addTransformationListener(new TransformationListenerUML());
	}
	
	private class TransformationListenerUML implements TransformationListener {
	
		@Override
		public void typeModelCreated(IResource resource, TransformationTrace trace) {
			
			// Store code to model line trace:
			codeToModel.addModel(resource, trace.getLineToModel());
			
			// Store Java resource to model file trace:
			Path typeModelFile = modelRepository.relativize(Paths.get(trace.getTypeModel().getURI().toFileString()));
			Path projectModelFile = modelRepository.relativize(Paths.get(trace.getProjectModel().getURI().toFileString()));
			javaToModel.put(resource, typeModelFile);
			javaToModel.put(resource.getProject(), projectModelFile);
			
			// Remember created/modified resources:
			modelResources.add(trace.getProjectModel());
			modelResources.add(trace.getTypeModel());
			
			// Unload models for garbage collection -> prevent resource leaks:
			UMLUtil.unloadUMLModels(Collections.singleton(trace.getTypeModel()));
		}
	
		@Override
		public void typeModelRemoved(IResource resource, Resource projectModel) {
			
			// Store code to model line trace:
			codeToModel.removeModel(resource);
			
			// Remove resource:
			javaToModel.remove(resource);
		}
	}

	public void saveModel() {
		this.umlClassDiagramView.setModel(settings.getWorkspaceModel().getContents().get(0));
		
		transformation.saveWorkspaceModel();
		transformation.saveLibraryModel();
		systemModel.save();
	}
	
	public List<Path> discover(
			IProject project, 
			Path projectRepositoryPath, 
			Set<String> workspaceProjectScope, 
			SystemModel systemModel, 
			List<FileChange> projectFileChanges,
			List<FileChange> projectBugLocations,
			Version version,
			Version newerVersion,
			boolean initialVersion) {
		
		List<Path> fileChanges = new ArrayList<>();
		fileChanges.add(modelRepository.relativize(Paths.get(settings.getWorkspaceModel().getURI().toFileString())));
		fileChanges.add(modelRepository.relativize(Paths.get(settings.getLibraryModel().getURI().toFileString())));
		
		for (FileChange fileChange : projectFileChanges) {
			if (!fileChange.getType().equals(FileChangeType.ADD)) {
				IResource codeResource = changeToResource(project, projectRepositoryPath, fileChange);
				Path modelFile = javaToModel.get(codeResource);
				
				if (modelFile != null) {
					fileChanges.add(modelFile);
				}
			}
		}
		
		// Create workspace update specification:
		WorkspaceUpdate projectWorkspaceUpdate = createWorkspaceUpdate(
				project, projectRepositoryPath, projectFileChanges, initialVersion);
		
		// Start transformation:
		transformation.performWorkspaceUpdate(Collections.singletonList(projectWorkspaceUpdate), workspaceProjectScope);
		
		// Add changes to system model:
		calculateSysteModel(systemModel, version, newerVersion, project, projectRepositoryPath, projectBugLocations);
		
		for (FileChange fileChange : projectFileChanges) {
			if (fileChange.getType().equals(FileChangeType.ADD)) {
				IResource codeResource = changeToResource(project, projectRepositoryPath, fileChange);
				Path modelFile = javaToModel.get(codeResource);
				
				if (modelFile != null) {
					fileChanges.add(modelFile);
				}
			}
		}
		
		Path projectModelFile = javaToModel.get(project);
		
		if (projectModelFile != null) {
			fileChanges.add(projectModelFile);
		}
		
		return fileChanges;
	}

	public List<Path> removeProject(String projectName) {
		List<Path> removed = transformation.removeProject(projectName);
		
		List<Path> fileChanges = new ArrayList<>();
		fileChanges.add(modelRepository.relativize(Paths.get(settings.getWorkspaceModel().getURI().toFileString())));
		
		for (Path removedFile : removed) {
			fileChanges.add(modelRepository.relativize(removedFile));
		}
		
		return fileChanges;
	}

	/**
	 * Changes to create the current revision.
	 * @param projectBugLocations 
	 */
	private WorkspaceUpdate createWorkspaceUpdate(
			IProject project, Path projectRepositoryPath, 
			List<FileChange> projectFileChanges, 
			boolean initialVersion) {
		
		WorkspaceUpdate projectWorkspaceUpdate;
		
		if (initialVersion) {
			projectWorkspaceUpdate = WorkspaceUpdate.getWorkspaceProject(project, false);
		} else {
			projectWorkspaceUpdate = new WorkspaceUpdate(project);
			projectWorkspaceUpdate.setCreated(new HashSet<>());
			projectWorkspaceUpdate.setModified(new HashSet<>());
			projectWorkspaceUpdate.setRemoved(new HashSet<>());
			
			for (FileChange fileChange : projectFileChanges) {
				IResource changedResource = changeToResource(project, projectRepositoryPath, fileChange);

				if (changedResource != null) {
					switch (fileChange.getType()) {
					case ADD:
						projectWorkspaceUpdate.getCreated().add(changedResource);
						break;
					case DELETE:
						projectWorkspaceUpdate.getRemoved().add(changedResource);
						break;
					case MODIFY:
						projectWorkspaceUpdate.getModified().add(changedResource);
						break;
					}
				}
			}
		}

		return projectWorkspaceUpdate;
	}

	private IResource changeToResource(IProject project, Path projectRepositoryPath, FileChange fileChange) {
		return project.getFile(projectRepositoryPath.relativize(fileChange.getLocation()).toString());
	}

	private Change createModelChange(IResource workspaceResource, LineChange lineChange, EObject modelElement) {
		if (modelElement != null) {
			int quantification = lineChange.getEndA() - lineChange.getBeginA();
			quantification += lineChange.getEndB() - lineChange.getBeginB();
			
			Change change = SystemModelFactory.eINSTANCE.createChange();
			change.setOriginalResource(workspaceResource.toString());
			change.setLocation(modelElement);
			change.setQuantification(quantification);
			change.setType(dataSet2SystemModel.convertChange(lineChange));
			return change;
		}
		return null;
	}
	
	public static void clearSysteModelVersion(SystemModel systemModel) {
		systemModel.setVersion(null);
	}
	
	private void calculateSysteModel(SystemModel systemModel, Version buggyVersion, Version fixedVersion, 
			IProject project, Path projectRepositoryPath, List<FileChange> projectBugLocations) {
		
		TracedVersion modelVersion = (TracedVersion) systemModel.getVersion(); 
				
		if (modelVersion == null) {
			BugReport bugReport = fixedVersion != null ? fixedVersion.getBugReport() : null;
			modelVersion = dataSet2SystemModel.convertVersion(buggyVersion, bugReport, fixedVersion);
			systemModel.setVersion(modelVersion);
		}
		
		if (!projectBugLocations.isEmpty()) {
			List<Change> changeLocations = calculateBugLocations(project, projectRepositoryPath, projectBugLocations);
			modelVersion.getBugreport().getModelLocations().addAll(changeLocations);
		}
		
		// Store document types:
		for (View view : systemModel.getViews()) {
			if (view.getModel() != null) {
				String docType = view.getModel().eClass().getEPackage().getNsURI();
				
				if (!view.getDocumentTypes().contains(docType)) {
					view.getDocumentTypes().add(docType);
				}
			}
		}
	}

	private List<Change> calculateBugLocations(
			IProject project, Path projectRepositoryPath, List<FileChange> projectBugLocations) {
		
		if (!projectBugLocations.isEmpty()) {
			Map<EObject, Change> changeLocations = new HashMap<>();
			
			for (FileChange fileChange : projectBugLocations) {
				IResource workspaceResource = changeToResource(project, projectRepositoryPath, fileChange);
				Change changeLocation = null;
				
				// Calculate change location:
				if (fileChange.getType().equals(FileChangeType.ADD)) {
					// Resource created in fixed version is not yet present in buggy version!
					Model workspaceRoot = (Model) settings.getWorkspaceModel().getContents().get(0);
					EObject container = findClosestPackage(fileChange, workspaceRoot);
					LineChange lineChange = fileChange.getLines().get(0);
					
					if (container != null) {
						addChange(changeLocations, createModelChange(workspaceResource, lineChange, container));
					} else {
						addChange(changeLocations, createModelChange(workspaceResource, lineChange, workspaceRoot));
					}
				} else {
					if ((fileChange.getLines() != null) && (!fileChange.getLines().isEmpty())) {
						for (LineChange lineChange : fileChange.getLines()) {
							EObject beginModelElementA = codeToModel.getModelElement(workspaceResource, lineChange.getBeginA());
							addChange(changeLocations, createModelChange(workspaceResource, lineChange, beginModelElementA));
							
							EObject endModelElementA = codeToModel.getModelElement(workspaceResource, lineChange.getEndA());
							addChange(changeLocations, createModelChange(workspaceResource, lineChange, endModelElementA));
						}
					} else {
						// Fallback solution...
						EObject modelElement = codeToModel.getModelElement(workspaceResource, 0);
						changeLocation = SystemModelFactory.eINSTANCE.createChange();
						changeLocation.setOriginalResource(workspaceResource.toString());
						changeLocation.setLocation(modelElement);
						changeLocation.setQuantification(1);
						changeLocation.setType(dataSet2SystemModel.convertChange(fileChange));
						
						addChange(changeLocations, changeLocation);
					}
				}
			}
			return new ArrayList<>(changeLocations.values());
		}
		
		return Collections.emptyList();
	}
	
	private EObject findClosestPackage(FileChange fileChange, Model workspaceRoot) {
		String[] filePath = URI.createFileURI(fileChange.getLocation().toString()).segments();
		
		// Find containing package:
		Package modelPackage = workspaceRoot;
		int segment = 0;
		
		while (segment < filePath.length) {
			PackageableElement projectResource = modelPackage.getPackagedElement(filePath[segment]);
			
			// Skip segments until project/source folder:
			if (projectResource != null) {
				// Step into packages:
				if (projectResource instanceof Package) {
					modelPackage = (Package) projectResource;
				}
			}
			
			++segment;
		}
		
		return modelPackage;
	}
	
	private void addChange(Map<EObject, Change> changeLocations, Change changeLocation) {
		// Insert/merge change in system model:
		if (changeLocation != null) {
			if (changeLocations.containsKey(changeLocation.getLocation())) {
				Change existingChange = changeLocations.get(changeLocation.getLocation());
				existingChange.setQuantification(existingChange.getQuantification() + changeLocation.getQuantification());
				
				// e.g. delete and create
				if (!existingChange.getType().equals(changeLocation.getType())) {
					existingChange.setType(ChangeType.MODIFY);
				}
			} else {
				changeLocations.put(changeLocation.getLocation(), changeLocation);
			}
		}
	}

}
