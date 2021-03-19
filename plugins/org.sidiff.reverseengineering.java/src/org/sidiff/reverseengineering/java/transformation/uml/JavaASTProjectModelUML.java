package org.sidiff.reverseengineering.java.transformation.uml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.sidiff.reverseengineering.java.Activator;
import org.sidiff.reverseengineering.java.configuration.TransformationSettings;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingTranslator;
import org.sidiff.reverseengineering.java.transformation.JavaASTProjectModel;
import org.sidiff.reverseengineering.java.util.WorkspaceUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The UML model representing a Java project
 * 
 * @author Manuel Ohrndorf
 */
public class JavaASTProjectModelUML extends JavaASTProjectModel {
	
	protected UMLFactory umlFactory = UMLFactory.eINSTANCE;
	
	protected String defaultPackageName = "default" ;
	
	protected Model projectModelRoot;
	
	protected Package defaultPackage;
	
	protected TransformationSettings settings;

	/**
	 * @see {@link JavaASTProjectModel#JavaASTProjectModel(XMLResource, JavaASTBindingTranslator)}
	 */
	@Inject
	public JavaASTProjectModelUML(
			@Assisted XMLResource projectModel, 
			@Assisted IProject project,
			JavaASTBindingTranslator bindingTranslator) {
		super(projectModel, project, bindingTranslator);
	}
	
	@Override
	public void addPackagedElement(IPackageBinding binding, EObject modelElement) {
		
		// default package?
		if (binding == null) {
			if (modelElement instanceof PackageableElement) {
				getDefaultPackage().getPackagedElements().add((PackageableElement) modelElement);
				return;
			}
		}
		
		String[] packages = binding.getNameComponents();
		String bindingKey = null;
		Package parentPackage = getProjectPackage();
		Package childPackage = null;

		for (String packageName : packages) {
			if (bindingKey == null) {
				bindingKey = packageName;
			} else {
				bindingKey += "/" + packageName;
			}
			
			if (packageName == packages[packages.length - 1]) {
				// Bind Java key to inner most package:
				childPackage = getModelElement(getBindingKey(project, binding.getKey()));
			} else {
				childPackage = getModelElement(getBindingKey(project, bindingKey));
			}

			if (childPackage == null) {
				childPackage = umlFactory.createPackage();
				childPackage.setName(packageName);
				parentPackage.getNestedPackages().add(0, childPackage);
				
				if (packageName == packages[packages.length - 1]) {
					// Bind Java key to inner most package:
					bindModelElement(getBindingKey(project, binding.getKey()), childPackage);
				} else {
					bindModelElement(getBindingKey(project, bindingKey), childPackage);
				}
			}

			parentPackage = childPackage;
		}
		
		// add model element:
		if (modelElement instanceof PackageableElement) {
			if (childPackage != null) {
				childPackage.getPackagedElements().add((PackageableElement) modelElement);
			} else {
				getDefaultPackage().getPackagedElements().add((PackageableElement) modelElement);
			}
		}
	}
	
	@Override
	public void removePackagedElement(TransformationSettings settings, IResource removed) 
			throws NoSuchElementException, IOException {
		
		String[] pathFragments = settings.findModelPath(removed, settings.getBaseURI());
		
		if (pathFragments == null) {
			throw new NoSuchElementException(); // could not resolve type element
		}
		
		// Find containing package:
		Package modelPackage = getProjectPackage();
		String modelElementName = settings.getModelElementName(removed);
		PackageableElement typedElement = findPackagedElement(modelPackage, pathFragments, modelElementName);
		
		if (typedElement != null) {
			
			// Delete from model:
			EcoreUtil.remove(typedElement);
			
			// Delete from file system:
			Path typeModelPath = Path.of(settings.getBaseURI().devicePath(), pathFragments);
				
			if (!Files.deleteIfExists(typeModelPath)) {
				if (Activator.isLoggable(Level.SEVERE)) {
					Activator.getLogger().log(Level.SEVERE, "File not found for deletion:" + typeModelPath);
				}
			}
			
			// Garbage collect empty packages:
			garbageCollectPackages(modelPackage, typeModelPath);
				
		} else {
			throw new NoSuchElementException(); // could not resolve type element
		}
	}

	private PackageableElement findPackagedElement(Package modelPackage, String[] pathFragments, String modelElementName) {
		PackageableElement packageableElement = modelPackage.getPackagedElement(pathFragments[1]);
		
		for (int i = 2; i < pathFragments.length - 1; i++) {
			if (packageableElement instanceof Package) {
				packageableElement = ((Package) packageableElement).getPackagedElement(pathFragments[i]);
			}
		}
		
		// Try default package:
		if (packageableElement == null) {
			modelPackage = getDefaultPackage();
		}
		
		if (packageableElement instanceof Package) {
			packageableElement = ((Package) packageableElement).getPackagedElement(modelElementName);
		}
		
		return packageableElement;
	}

	private void garbageCollectPackages(Package modelPackage, Path typeModelPath) throws IOException {
		while (modelPackage != null) {
			if (modelPackage.getPackagedElements().isEmpty()) {
				Package emptyPackage = modelPackage;
				
				if (modelPackage.eContainer() instanceof Package) {
					modelPackage = (Package) modelPackage.eContainer();
				} else {
					modelPackage = null;
				}
				
				// Delete from file system:
				if (typeModelPath.getParent().getFileName().toString().equals(emptyPackage.getName())) {
					if (WorkspaceUtil.isEmptyFolder(typeModelPath.getParent())) {
						Files.deleteIfExists(typeModelPath.getParent());
					}
				}
				
				// Delete from model:
				EcoreUtil.remove(emptyPackage);
				
				if (emptyPackage == defaultPackage) {
					this.defaultPackage = null;
				}
				
				if (emptyPackage == projectModelRoot) {
					this.projectModelRoot = null;
				}
			} else {
				break;
			}
		}
	}

	protected Package getProjectPackage() {
		if (projectModelRoot == null) {
			if (getProjectModel().getContents().isEmpty() || !(getProjectModel().getContents().get(0) instanceof Model)) {
				this.projectModelRoot = umlFactory.createModel();
				projectModelRoot.setName(project.getName());
				getProjectModel().getContents().add(projectModelRoot);
				
				bindModelElement(getBindingKey(project), projectModelRoot);
			} else {
				this.projectModelRoot = (Model) getProjectModel().getContents().get(0);
			}
		}
		return projectModelRoot;
	}
	
	protected Package getDefaultPackage() {
		if (defaultPackage == null) {
			this.defaultPackage = (Package) getProjectPackage().getPackagedElement(defaultPackageName);
			
			if (defaultPackage == null) {
				this.defaultPackage = umlFactory.createPackage();
				this.defaultPackage.setName("default");
				getProjectPackage().getNestedPackages().add(0, defaultPackage);
				bindModelElement(getBindingKey(project, "default"), defaultPackage);
			}
		}
		return defaultPackage;
	}
}
