package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.uml2.common.util.CacheAdapter;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.sidiff.bug.localization.dataset.changes.model.FileChange;
import org.sidiff.bug.localization.dataset.changes.model.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.changes.model.LineChange;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.View;
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

public class JavaProject2JavaSystemModel {
	
	private Path modelRepository;
	
	private IncrementalReverseEngineering transformation;
	
	private CodeLinesToModelTrace codeToModel;
	
	private Map<IResource, Path> javaToModel;
	
	private TransformationSettingsUML settings;
	
	private SystemModel systemModel;
	
	private View umlClassDiagramView;
	
	private Set<Resource> modelResources;
	
	public JavaProject2JavaSystemModel(Path modelRepository, boolean includeMethodBodies, SystemModel systemModel) {
		this.modelRepository = modelRepository;
		
		// Remember created/modified resources:
		this.codeToModel = new CodeLinesToModelTrace();
		this.javaToModel = new HashMap<>();
		this.modelResources = new HashSet<>();
		
		// Application Settings:
		this.settings = new TransformationSettingsUML();
		this.settings.setBaseURI(URI.createFileURI(modelRepository.toString()));
		this.settings.setIncludeMethodBodies(includeMethodBodies);
		this.settings.setModelFileExtension(TransformationDomainUML.getModelFileExtension());
		
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
			unloadUMLModels(Collections.singleton(trace.getTypeModel()));
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
				project, projectRepositoryPath, projectFileChanges, projectBugLocations, initialVersion);
		
		// Start transformation:
		transformation.performWorkspaceUpdate(Collections.singletonList(projectWorkspaceUpdate), workspaceProjectScope);
		
		// Add changes to system model:
		calculateBugLocations(project, projectRepositoryPath, projectBugLocations);
		
		for (FileChange fileChange : projectFileChanges) {
			if (fileChange.getType().equals(FileChangeType.ADD)) {
				IResource codeResource = changeToResource(project, projectRepositoryPath, fileChange);
				Path modelFile = javaToModel.get(codeResource);
				
				if (modelFile != null) {
					fileChanges.add(modelFile);
				}
			}
		}
		
		fileChanges.add(javaToModel.get(project));
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
			List<FileChange> projectBugLocations, 
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
		int quantification = lineChange.getEndA() - lineChange.getBeginA();
		quantification += lineChange.getEndB() - lineChange.getBeginB();
		
		Change change = SystemModelFactory.eINSTANCE.createChange();
		change.setOriginalResource(workspaceResource.toString());
		change.setLocation(modelElement);
		change.setQuantification(quantification);
		change.setType(getChange(lineChange));
		return change;
	}

	private ChangeType getChange(LineChange lineChange) {
		switch (lineChange.getType()) {
		case DELETE:
			return ChangeType.DELETE;
		case EMPTY:
			return ChangeType.DELETE;
		case INSERT:
			return ChangeType.ADD;
		case REPLACE:
			return ChangeType.MODIFY;
		default:
			return ChangeType.MODIFY;
		}
	}
	
	private ChangeType getChange(FileChange fileChange) {
		switch (fileChange.getType()) {
		case DELETE:
			return ChangeType.DELETE;
		case ADD:
			return ChangeType.ADD;
		case MODIFY:
			return ChangeType.MODIFY;
		default:
			return ChangeType.MODIFY;
		}
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

	private void calculateBugLocations(IProject project, Path projectRepositoryPath, List<FileChange> projectBugLocations) {
		if (!projectBugLocations.isEmpty()) {
			Map<EObject, Change> bugLocations = new HashMap<>();
			
			for (FileChange fileChange : projectBugLocations) {
				IResource workspaceResource = changeToResource(project, projectRepositoryPath, fileChange);
				Change bugLocation = null;
				
				// Calculate change location:
				if (fileChange.getType().equals(FileChangeType.ADD)) {
					// Resource created in fixed version is not yet present in buggy version!
					Model workspaceRoot = (Model) settings.getWorkspaceModel().getContents().get(0);
					EObject container = findClosestPackage(fileChange, workspaceRoot);
					LineChange lineChange = fileChange.getLines().get(0);
					
					if (container != null) {
						bugLocation = createModelChange(workspaceResource, lineChange, container);
					} else {
						bugLocation = createModelChange(workspaceResource, lineChange, workspaceRoot);
					}
				} else {
					if ((fileChange.getLines() != null) && (!fileChange.getLines().isEmpty())) {
						for (LineChange lineChange : fileChange.getLines()) {
							EObject beginModelElement = codeToModel.getModelElement(workspaceResource, lineChange.getBeginA());
							EObject endModelElement = codeToModel.getModelElement(workspaceResource, lineChange.getEndA());
							
							if (beginModelElement != endModelElement) {
								bugLocation = createModelChange(workspaceResource, lineChange, beginModelElement);
								bugLocation = createModelChange(workspaceResource, lineChange, endModelElement);
							} else {
								bugLocation = createModelChange(workspaceResource, lineChange, beginModelElement);
							}
						}
					} else {
						// Fallback solution...
						EObject modelElement = codeToModel.getModelElement(workspaceResource, 0);
						bugLocation = SystemModelFactory.eINSTANCE.createChange();
						bugLocation.setOriginalResource(workspaceResource.toString());
						bugLocation.setLocation(modelElement);
						bugLocation.setQuantification(1);
						bugLocation.setType(getChange(fileChange));
					}
				}
				
				// Insert/merge change in system model:
				if (bugLocation != null) {
					if (bugLocations.containsKey(bugLocation.getLocation())) {
						Change existingChange = bugLocations.get(bugLocation.getLocation());
						existingChange.setQuantification(existingChange.getQuantification() + bugLocation.getQuantification());
						
						// e.g. delete and create
						if (!existingChange.getType().equals(bugLocation.getType())) {
							existingChange.setType(ChangeType.MODIFY);
						}
					} else {
						bugLocations.put(bugLocation.getLocation(), bugLocation);
						umlClassDiagramView.getChanges().add(bugLocation);
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void unloadUMLModels(Set<Resource> resources) {
		// Unload models for garbage collection -> prevent resource leaks:
		try {
			// CacheAdapter/ECrossReferenceAdapter resource leak: remove adapter, clear caches, unload all resources 
			for (Iterator<Resource> iterator = resources.iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
				ECrossReferenceAdapter crossReferenceAdapter = ECrossReferenceAdapter.getCrossReferenceAdapter(resource);

				if (crossReferenceAdapter != null) {
					crossReferenceAdapter.unsetTarget(resource);
				}

				if (CacheAdapter.getInstance() != null) {
					CacheAdapter.getInstance().clear(resource);
				}

				resource.unload();
			}
		} catch (Throwable e) {
			// e.printStackTrace(); // FIXME Index-Out-Of-Bounds on resource.unload();
		}

		// WORKAROUND: Clear resource leaks of UML CacheAdapter:
		try {
			CacheAdapter cacheAdapter = CacheAdapter.getInstance();
			cacheAdapter.clear();

			// Clear inverseCrossReferencer map:
			Field inverseCrossReferencerField = ECrossReferenceAdapter.class.getDeclaredField("inverseCrossReferencer");
			inverseCrossReferencerField.setAccessible(true);
			Object inverseCrossReferencerValue = inverseCrossReferencerField.get(cacheAdapter);

			if (inverseCrossReferencerValue instanceof Map) {
				((Map) inverseCrossReferencerValue).clear();
			}

			// Clear proxyMap map:
			Class<?> inverseCrossReferencerClass = Class.forName("org.eclipse.emf.ecore.util.ECrossReferenceAdapter$InverseCrossReferencer");
			Field proxyMapField = inverseCrossReferencerClass.getDeclaredField("proxyMap");
			proxyMapField.setAccessible(true);
			Object proxyMapValue = proxyMapField.get(inverseCrossReferencerValue);

			if (proxyMapValue instanceof Map) {
				((Map) proxyMapValue).clear();
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
