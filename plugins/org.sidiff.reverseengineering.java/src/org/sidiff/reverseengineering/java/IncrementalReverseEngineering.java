package org.sidiff.reverseengineering.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.sidiff.reverseengineering.java.configuration.TransformationModule.JavaASTBindingResolverFactory;
import org.sidiff.reverseengineering.java.configuration.TransformationModule.JavaASTLibraryModelFactory;
import org.sidiff.reverseengineering.java.configuration.TransformationModule.JavaASTProjectModelFactory;
import org.sidiff.reverseengineering.java.configuration.TransformationModule.JavaASTTransformationFactory;
import org.sidiff.reverseengineering.java.configuration.TransformationModule.JavaASTWorkspaceModelFactory;
import org.sidiff.reverseengineering.java.configuration.TransformationSettings;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingResolver;
import org.sidiff.reverseengineering.java.transformation.JavaASTLibraryModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTProjectModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTTransformation;
import org.sidiff.reverseengineering.java.transformation.JavaASTWorkspaceModel;
import org.sidiff.reverseengineering.java.util.EMFHelper;
import org.sidiff.reverseengineering.java.util.JavaParser;

import com.google.inject.Inject;

public class IncrementalReverseEngineering {

	private TransformationSettings settings;
	
	private JavaASTTransformationFactory transformationFactory;
	
	private JavaASTBindingResolverFactory bindingResolverFactory;
	
	private JavaASTProjectModelFactory projectModelFactory;
	
	private JavaParser javaParser;
	
	private EMFHelper emfHelper;
	
	private ResourceSet resourceSetNew;
	
	private JavaASTWorkspaceModel workspaceModel;

	private JavaASTLibraryModel libraryModel;

	private List<TransformationListener> listeners;
	
	@Inject
	public IncrementalReverseEngineering(
			TransformationSettings settings,
			JavaASTTransformationFactory transformationFactory,
			JavaASTBindingResolverFactory bindingResolverFactory,
			JavaASTWorkspaceModelFactory workspaceModelFactory,
			JavaASTLibraryModelFactory libraryModelFactory,
			JavaASTProjectModelFactory projectModelFactory, 
			JavaParser javaParser, 
			EMFHelper emfHelper,
			ResourceSet oldResourceSet) {
		this.settings = settings;
		this.transformationFactory = transformationFactory;
		this.bindingResolverFactory = bindingResolverFactory;
		this.projectModelFactory = projectModelFactory;
		this.javaParser = javaParser;
		this.emfHelper = emfHelper;
		
		this.listeners = new ArrayList<>();
		
		this.workspaceModel = workspaceModelFactory.create(settings.getWorkspaceModel(), settings.getName());
		this.libraryModel = libraryModelFactory.create(settings.getLibraryModel());
		
		workspaceModel.addToWorkspace(0, libraryModel.getLibraryModelRoot());
	}
	
	public void addTransformationListener(TransformationListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeTransformationListener(TransformationListener listener) {
		this.listeners.remove(listener);
	}
	
	public void performWorkspaceUpdate(List<WorkspaceUpdate> updates, Set<String> workspaceProjectScope) {
    	
		// Setup model resources:
    	this.resourceSetNew = settings.getWorkspaceModel().getResourceSet();
    	
		for (WorkspaceUpdate workspaceUpdate : updates) {
			try {
				/* Start Transformation */
				JavaASTProjectModel projectModel = process(workspaceUpdate, workspaceProjectScope, workspaceModel, libraryModel);
				projectModel.save();
				
				// New project model?
				if (projectModel.getOldRootModelElement() == null) {
					if (!projectModel.getProjectModel().getContents().isEmpty()) {
						workspaceModel.addToWorkspace(projectModel.getProjectModel().getContents().get(0));
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void saveWorkspaceModel() {
		try {
			settings.getWorkspaceModel().save(Collections.emptyMap());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveLibraryModel() {
		try {
			settings.getLibraryModel().save(Collections.emptyMap());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected JavaASTProjectModel process(
			WorkspaceUpdate workspaceUpdate, 
			Set<String> workspaceProjectScope,
			JavaASTWorkspaceModel workspaceModel,
			JavaASTLibraryModel libraryModel) 
					throws JavaModelException {
		
		// Parse Java source files to be updated:
		if (Activator.isLoggable(Level.FINE)) Activator.getLogger().log(Level.FINE, "Parsing...");
		
		Map<ICompilationUnit, CompilationUnit> parsedASTs = javaParser.parse(
				workspaceUpdate.getProject(), workspaceUpdate.needsUpdate(), settings.isIncludeMethodBodies());
		
		if (Activator.isLoggable(Level.FINE)) Activator.getLogger().log(Level.FINE, "Finished");

		// Setup project model resource:
		URI projectModelURI = settings.getBaseURI().appendSegment(workspaceUpdate.getProject().getName())
				.appendSegment(workspaceUpdate.getProject().getName()).appendFileExtension(settings.getModelFileExtension());
		XMLResource projectModelResource = EMFHelper.initializeResource(resourceSetNew, projectModelURI);
    	JavaASTProjectModel projectModel = projectModelFactory.create(projectModelResource, workspaceUpdate.getProject());
    	
    	// Remove elements from model:
    	for (IResource removed : workspaceUpdate.getRemoved()) {
    		removeFromProjectModel(projectModel, removed);
    	}
    	
    	// Log modified models:
    	List<Resource> oldResources = new ArrayList<>(); 
    	
    	// Process all compilation units:
    	for (Entry<ICompilationUnit, CompilationUnit> parsed : parsedASTs.entrySet()) {
    		try {
    			processCompilationUnit(
    					workspaceUpdate, 
    					workspaceProjectScope, 
    					workspaceModel, 
    					libraryModel, 
    					projectModel,
    					oldResources, 
    					parsed.getValue());
    		} catch(Throwable e) {
    			e.printStackTrace();
    			
    			if (Activator.isLoggable(Level.SEVERE)) {
    				Activator.getLogger().log(Level.SEVERE, 
    						"An exception occured during Java AST transformation: + " + e.toString() + 
    						"\n  Transformation of Java AST failed: " + parsed.getKey().getResource());
    			}
    		}
		}
    	
    	return projectModel;
    }

	protected void processCompilationUnit(
			WorkspaceUpdate workspaceUpdate, 
			Set<String> workspaceProjectScope,
			JavaASTWorkspaceModel workspaceModel, 
			JavaASTLibraryModel libraryModel, 
			JavaASTProjectModel projectModel,
			List<Resource> oldResources, 
			CompilationUnit javaAST) {
		
		IJavaElement javaElement = javaAST.getJavaElement();
		
		if (Activator.isLoggable(Level.FINE)) Activator.getLogger().log(Level.FINE, javaElement.getResource().getFullPath().toString());
 		
		JavaASTBindingResolver modelBindings = bindingResolverFactory.create(javaAST, workspaceProjectScope, libraryModel);
		JavaASTTransformation transformation = transformationFactory.create(javaAST, modelBindings);

		/* Start Model Transformation */
		transformation.apply();
		
		// Add to project model:
		if (workspaceUpdate.isCreated(javaElement.getResource())) {
			if (javaElement.getParent() instanceof IPackageFragment) {
				for (EObject rootModelElement : transformation.getRootModelElements()) {
					if ((javaAST.getPackage() != null) && (javaAST.getPackage().resolveBinding() != null)) {
						projectModel.addPackagedElement(javaAST.getPackage().resolveBinding(), rootModelElement);
					} else {
						projectModel.addPackagedElement(null, rootModelElement); // default package
					}
				}
			}
		}
		
		// Save model resource:
		URI modelURI = transformation.getModelURI(settings.getBaseURI());
		
		// Update model?
		XMLResource resourceOld = null; // for reuse of XMI object IDs
		
		if (workspaceUpdate.isModified(javaElement.getResource())) {
			ResourceSet resourceSetOld = new ResourceSetImpl();
			
			if (EMFHelper.resourceExists(resourceSetOld, modelURI)) {
				resourceOld = (XMLResource) resourceSetOld.getResource(modelURI, true);
   				oldResources.add(resourceOld);
			}
		}

		Resource typeModel = emfHelper.saveModelWithBindings(
				modelURI, resourceSetNew, modelBindings.getBindings(),
				transformation.getRootModelElements(), resourceOld);
		
		// Notify listeners:
		TransformationTrace trace = transformation.getTransformationTrace();
		trace.setWorkspaceModel(workspaceModel.getWorkspaceModel());
		trace.setLibraryModel(libraryModel.getLibraryModel());
		trace.setProjectModel(projectModel.getProjectModel());
		trace.setJavaResource(javaElement.getResource());
		trace.setTypeModel(typeModel);
		
		for (TransformationListener listener : listeners) {
			try {
				listener.typeModelCreated(trace.getJavaResource(), trace);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	protected void removeFromProjectModel(JavaASTProjectModel projectModel, IResource removed) {
		try {
			projectModel.removePackagedElement(
					settings.getBaseURI(),
					removed.getParent().getProjectRelativePath().segments(), 
					removed.getFullPath().removeFileExtension().lastSegment());
			
			// Remove resources:
			for (TransformationListener listener : listeners) {
				try {
					listener.typeModelRemoved(removed, projectModel.getProjectModel());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} catch (NoSuchElementException e) {
			if (Activator.getLogger().isLoggable(Level.FINE)) {
				Activator.getLogger().log(Level.FINE, "Element to be removed not found: " + removed);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public List<Path> removeProject(String projectName) {
		try {
			return workspaceModel.removeFromWorkspace(settings.getBaseURI(), projectName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
}
