package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
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
	
	private ExecutorService cleanUpSystemModelResourcesThread;
	
	private IncrementalReverseEngineering transformation;
	
	private CodeLinesToModelTrace codeToModel;
	
	private TransformationSettingsUML settings;
	
	private View umlClassDiagramView; 
	
	public JavaProject2JavaSystemModel(Path modelRepository, boolean includeMethodBodies, SystemModel systemModel) {
		this.cleanUpSystemModelResourcesThread = Executors.newSingleThreadExecutor();
		
		this.codeToModel = new CodeLinesToModelTrace();
		
		// Application Settings:
		this.settings = new TransformationSettingsUML();
		this.settings.setBaseURI(URI.createFileURI(modelRepository.toString()));
		this.settings.setIncludeMethodBodies(includeMethodBodies);
		this.settings.setModelFileExtension(TransformationDomainUML.getModelFileExtension());
		
		// Add to UML model to system model:
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
	}
	
	private class TransformationListenerUML implements TransformationListener {
	
		@Override
		public void typeModelCreated(IResource resource, TransformationTrace trace) {
			codeToModel.addModel(resource, trace.getLineToModel());
			
			// Unload models for garbage collection -> prevent resource leaks:
			unloadUMLModels(Collections.singleton(trace.getTypeModel()));
		}
	
		@Override
		public void typeModelRemoved(IResource resource) {
			codeToModel.removeModel(resource);
		}
		
	}

	public void saveModel() {
		transformation.saveWorkspaceModel();
		transformation.saveLibraryModel();
	}
	
	public void discover(
			IProject project, 
			Path projectRepositoryPath, 
			Set<String> workspaceProjectScope, 
			SystemModel systemModel, 
			List<FileChange> projectFileChanges,
			List<FileChange> projectBugLocations,
			boolean initialVersion) {
		
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
		}
		
		// Create workspace update specification:
		WorkspaceUpdate projectWorkspaceUpdate = createWorkspaceUpdate(
				project, projectRepositoryPath, projectFileChanges, projectBugLocations, initialVersion);
		
		// Trace code line to model elements:
		transformation.addTransformationListener(new TransformationListenerUML());
		
		// Start transformation:
		transformation.performWorkspaceUpdate(Collections.singletonList(projectWorkspaceUpdate), workspaceProjectScope);
		
		// Add changes to system model:
		if (!projectBugLocations.isEmpty()) {
			for (FileChange fileChange : projectBugLocations) {
				IResource workspaceResource = changeToResource(project, projectRepositoryPath, fileChange);
				
				if (fileChange.getType().equals(FileChangeType.ADD)) {
					// Resource created in fixed version is not yet present in buggy version!
					Model workspaceRoot = (Model) settings.getWorkspaceModel().getContents().get(0);
					EObject container = findClosestPackage(fileChange, workspaceRoot);
					LineChange lineChange = fileChange.getLines().get(0);
					
					if (container != null) {
						umlClassDiagramView.getChanges().add(createModelChange(workspaceResource, lineChange, container));
					} else {
						umlClassDiagramView.getChanges().add(createModelChange(workspaceResource, lineChange, workspaceRoot));
					}
				} else {
					for (LineChange lineChange : fileChange.getLines()) {
						EObject beginModelElement = codeToModel.getModelElement(workspaceResource, lineChange.getBeginA());
						EObject endModelElement = codeToModel.getModelElement(workspaceResource, lineChange.getEndA());
						
						if (beginModelElement != endModelElement) {
							umlClassDiagramView.getChanges().add(createModelChange(workspaceResource, lineChange, beginModelElement));
							umlClassDiagramView.getChanges().add(createModelChange(workspaceResource, lineChange, endModelElement));
						} else {
							umlClassDiagramView.getChanges().add(createModelChange(workspaceResource, lineChange, beginModelElement));
						}
					}
				}
			}
		}
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
		}
		
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

	public void shutdown() {
		cleanUpSystemModelResourcesThread.shutdown();
	}
	
	@SuppressWarnings("rawtypes")
	private void unloadUMLModels(Set<Resource> resources) {
		// Unload models for garbage collection -> prevent resource leaks:
		
		// Clear resource from UML CacheAdapter:
		cleanUpSystemModelResourcesThread.execute(() -> {
			
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
//				e.printStackTrace(); // FIXME Index-Out-Of-Bounds on resource.unload();
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
		});
	}
}
