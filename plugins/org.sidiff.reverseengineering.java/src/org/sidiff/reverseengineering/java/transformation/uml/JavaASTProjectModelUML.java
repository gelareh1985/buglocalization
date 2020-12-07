package org.sidiff.reverseengineering.java.transformation.uml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
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
	public void removePackagedElement(URI baseURI, String[] projectPath, String typeName) 
			throws NoSuchElementException, IOException {
		
		// Find containing package:
		Package modelPackage = getProjectPackage();
		int segment = 0;
		boolean sourceFolderFound = false;
		
		while (segment < projectPath.length) {
			PackageableElement projectResource = modelPackage.getPackagedElement(projectPath[segment]);
			
			// Skip segments until source folder:
			if (projectResource != null) {
				// Step into packages:
				if (projectResource instanceof Package) {
					modelPackage = (Package) projectResource;
					sourceFolderFound = true;
				} else {
					throw new NoSuchElementException(); // could not resolve package
				}
			} else {
				if (sourceFolderFound) {
					throw new NoSuchElementException(); // could not resolve package
				}
			}
			
			++segment;
		}
		
		if (modelPackage == null) {
			modelPackage = getDefaultPackage();
		}
		
		// Remove typed element:
		PackageableElement typedElement = modelPackage.getPackagedElement(typeName);

		if (typedElement != null) {
			
			// Delete from file system:
			Path typeModelPath = Paths.get(typedElement.eResource().getURI().resolve(baseURI).toFileString());
			Files.deleteIfExists(typeModelPath);
			
			// Delete from model:
			EcoreUtil.remove(typedElement);

			// Garbage collect empty packages:
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
		} else {
			throw new NoSuchElementException(); // could not resolve type element
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
