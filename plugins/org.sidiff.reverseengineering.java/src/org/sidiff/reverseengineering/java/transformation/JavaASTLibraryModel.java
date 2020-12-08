package org.sidiff.reverseengineering.java.transformation;

import java.io.IOException;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.dom.IBinding;
import org.sidiff.reverseengineering.java.util.BindingRecovery;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Manages the creation of library model elements.
 * 
 * @author Manuel Ohrndorf
 */
public abstract class JavaASTLibraryModel {

	/**
	 * The library model.
	 */
	private XMLResource libraryModel;
	
	/**
	 * The initial root element of the library model or <code>null</code> if the
	 * given resource is initially empty.
	 */
	protected EObject oldRootModelElement;
	
	/**
	 * Creates bindings for the model.
	 */
	private JavaASTBindingTranslator bindingTranslator;
	
	/**
	 * Project name for binding keys.
	 */
	protected String projectName = "library";
	
	/**
	 * @param libraryModel      The library model.
	 * @param bindingTranslator Helper to translate bindings.
	 */
	@Inject
	public JavaASTLibraryModel(
			@Assisted XMLResource libraryModel, 
			JavaASTBindingTranslator bindingTranslator) {
		this.libraryModel = libraryModel;
		this.bindingTranslator = bindingTranslator;
		
		if (!libraryModel.getContents().isEmpty()) {
			this.oldRootModelElement =  libraryModel.getContents().get(0);
		}
	}
	
	/**
	 * @return The root model element of the library.
	 */
	public abstract EObject getLibraryModelRoot();

	/**
	 * Returns or creates a library model element. This function is intended to be
	 * sub-classed by clients for specific modeling languages.
	 * 
	 * @param externalBinding     The Java AST binding.
	 * @param recoveredBindingKey A recovered binding key or <code>null</code>.
	 * @param isTypeOf            The minimal type of the library model element.
	 * @return The library model element.
	 */
	@SuppressWarnings("unchecked")
	public <E extends EObject> E getLibraryModelElement(IBinding externalBinding, EClass isTypeOf, BindingRecovery bindingRecovery) {
		String recoveredBinding = bindingRecovery.getRecoveredBinding(externalBinding);
		
		if (recoveredBinding != null) {
			return (E) libraryModel.getEObject(getBindingKey(recoveredBinding)); 
		} else {
			return (E) libraryModel.getEObject(getBindingKey(externalBinding));
		}
	}
	
	/**
	 * @param bindingKey A unique binding key.
	 * @return The corresponding model element.
	 */
	@SuppressWarnings("unchecked")
	public <E extends EObject> E getLibraryModelElement(String bindingKey) {
		return (E) libraryModel.getEObject(bindingKey);
	}
	
	/**
	 * @param binding             The Java AST binding.
	 * @param recoveredBindingKey A recovered binding key or <code>null</code>.
	 * @param modelElement        The corresponding library model element.
	 */
	public void bindModelElement(IBinding binding, EObject modelElement, BindingRecovery bindingRecovery) {
		String recoveredBinding = bindingRecovery.getRecoveredBinding(binding);
		
		if (recoveredBinding != null) {
			libraryModel.setID(modelElement, getBindingKey(bindingRecovery.getRecoveredBinding(binding)));
		} else {
			libraryModel.setID(modelElement, getBindingKey(binding));
		}
	}
	
	/**
	 * @param bindinKey      A unique binding key.
	 * @param modelElement The corresponding library model element.
	 */
	public void bindModelElement(String bindinKey, EObject modelElement) {
		libraryModel.setID(modelElement, bindinKey);
	}
	
	/**
	 * @param binding The Java AST binding.
	 * @return The library binding key.
	 */
	protected String getBindingKey(IBinding binding) {
		return bindingTranslator.getBindingKey(projectName, binding);
	}
	
	/**
	 * @param bindinKey A local binding key.
	 * @return The library binding key.
	 */
	protected String getBindingKey(String bindingKey) {
		return bindingTranslator.getBindingKey(projectName, bindingKey);
	}
	
	/**
	 * @param binding The Java primitive binding.
	 * @return The corresponding model element. 
	 */
	@SuppressWarnings("unchecked")
	public <E extends EObject> E getPrimitiveType(IBinding binding) {
		return (E) libraryModel.getEObject(getPrimitiveTypeBindingKey(binding));
	}
	
	/**
	 * @param binding      The Java AST binding.
	 * @param modelElement The corresponding library model element.
	 */
	public void bindPrimitiveType(IBinding binding, EObject modelElement) {
		libraryModel.setID(modelElement, getPrimitiveTypeBindingKey(binding));
	}
	
	/**
	 * @param bindinKey A local binding key.
	 * @return The library binding key.
	 */
	protected String getPrimitiveTypeBindingKey(IBinding binding) {
		return getBindingKey("datatypes/" + binding.getName());
	}
	
	/**
	 * @return The library model.
	 */
	public XMLResource getLibraryModel() {
		return libraryModel;
	}
	
	
	/**
	 * @return The initial root element of the library model or <code>null</code> if
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
			libraryModel.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
