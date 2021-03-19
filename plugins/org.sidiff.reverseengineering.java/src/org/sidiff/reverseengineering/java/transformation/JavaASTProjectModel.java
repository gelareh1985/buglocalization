package org.sidiff.reverseengineering.java.transformation;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.sidiff.reverseengineering.java.configuration.TransformationSettings;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The model representing a Java project
 * 
 * @author Manuel Ohrndorf
 */
public abstract class JavaASTProjectModel {

	/**
	 * The model representing a Java project.
	 */
	protected XMLResource projectModel;
	
	/**
	 * The initial root element of the project model or <code>null</code> if the
	 * given resource is initially empty.
	 */
	protected EObject oldRootModelElement;
	
	/**
	 * The corresponding workspace project.
	 */
	protected IProject project;
	
	/**
	 * Creates bindings for the model.
	 */
	protected JavaASTBindingTranslator bindingTranslator;
	
	/**
	 * @param projectModel      The model representing a Java project
	 * @param project           The corresponding workspace project.
	 * @param bindingTranslator Helper to translate bindings.
	 */
	@Inject
	public JavaASTProjectModel(
			@Assisted XMLResource projectModel, 
			@Assisted IProject project,
			JavaASTBindingTranslator bindingTranslator) {
		this.projectModel = projectModel;
		this.project = project;
		this.bindingTranslator = bindingTranslator;
		
		if (!projectModel.getContents().isEmpty()) {
			this.oldRootModelElement =  projectModel.getContents().get(0);
		}
	}

	/**
	 * @param binding A binding of a Java package.
	 * @param modelElement The contained model element.
	 */
	public abstract void addPackagedElement(IPackageBinding binding, EObject modelElement);
	
	/**
	 * Matches the given project path to the qualified type names and removes the
	 * given type from the project model.
	 * 
	 * @param settings The main transformation settings.
	 * @param removed  The java resource that was removed.
	 * @return All correspondingly removed workspace resources.
	 */
	public abstract void removePackagedElement(TransformationSettings settings, IResource removed) 
			throws NoSuchElementException, IOException;

	/**
	 * @param bindingKey A binding in of this project.
	 * @return The corresponding model element.
	 */
	@SuppressWarnings("unchecked")
	public <E extends EObject> E getModelElement(String bindingKey) {
		return (E) projectModel.getEObject(bindingKey);
	}

	/**
	 * @param bindingKey The unique binding key.
	 * @param element    A new model element.
	 */
	public void bindModelElement(String bindingKey, EObject element) {
		projectModel.setID(element, bindingKey);
	}
	
	/**
	 * @param project The presented project.
	 * @return The binding key.
	 */
	protected String getBindingKey(IProject project) {
		return bindingTranslator.getBindingKey("project", project.toString());
	}

	/**
	 * @param bindinKey A local binding key.
	 * @return The library binding key.
	 */
	protected String getBindingKey(IProject project, String bindingKey) {
		return bindingTranslator.getBindingKey(project.getName(), bindingKey);
	}
	
	/**
	 * @return The model representing a Java project
	 */
	public XMLResource getProjectModel() {
		return projectModel;
	}
	
	/**
	 * @return The corresponding workspace project.
	 */
	public IProject getProject() {
		return project;
	}
	
	/**
	 * @return The initial root element of the project model or <code>null</code> if
	 *         the given resource is initially empty.
	 */
	public EObject getOldRootModelElement() {
		return oldRootModelElement;
	}
	
	/**
	 * Save with default options.
	 */
	public void save() {
		try {
			projectModel.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
