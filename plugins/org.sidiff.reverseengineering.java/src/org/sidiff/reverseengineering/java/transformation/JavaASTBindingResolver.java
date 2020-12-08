package org.sidiff.reverseengineering.java.transformation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.sidiff.reverseengineering.java.Activator;
import org.sidiff.reverseengineering.java.util.BindingRecovery;
import org.sidiff.reverseengineering.java.util.JavaASTUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Manages the Java AST to model bindings.
 * 
 * @author Manuel Ohrndorf
 */
public class JavaASTBindingResolver {
	
	/**
	 * The local projects in the workspace that will be transformed to corresponding
	 * models. Otherwise, a project will be considered as external projects.
	 * External projects might be generated as common fragments.
	 */
	private Set<String> workspaceProjectScope;
	
	/**
	 * Creates bindings for the model.
	 */
	private JavaASTBindingTranslator bindingTranslator;

	/**
	 * Binding key -> Model element
	 */
	private Map<String, EObject> bindings;
	
	/**
	 * The common model manager.
	 */
	private JavaASTLibraryModel libraryModel;
	
	/**
	 * Helper to handle unresolved, recovered binding.
	 */
	private BindingRecovery bindingRecovery;
	
	/**
	 * @param compilationUnit       The corresponding compilation unit.
	 * @param workspaceProjectScope The local projects in the workspace that will be
	 *                              transformed to corresponding models. Otherwise,
	 *                              a project will be considered as external
	 *                              projects. External projects might be generated
	 *                              as common fragments.
	 * @param modelFileExtension    The file extension of the modeling domain.
	 * @param bindings              The initial bindings, e.g., common model
	 *                              elements.
	 * @param libraryModel          The common model manager.
	 */
	@Inject
	public JavaASTBindingResolver(
			@Assisted CompilationUnit compilationUnit, 
			@Assisted Set<String> workspaceProjectScope,
			@Assisted JavaASTLibraryModel libraryModel, 
			JavaASTBindingTranslator bindingTranslator) {
		
		this.libraryModel = libraryModel;
		this.workspaceProjectScope = workspaceProjectScope;
		this.bindingTranslator = bindingTranslator;
		this.bindings = new HashMap<>();
		this.bindingRecovery = new BindingRecovery(compilationUnit);
	}
	
	/**
	 * Traces a model element binding in this transformation.
	 * 
	 * @param projectName  The name of the containing project
	 * @param binding      The Java AST binding of the corresponding model element.
	 * @param modelElement A model element (or main element of model fragment).
	 */
	public void bind(String projectName, IBinding binding, EObject modelElement) {
		bindings.put(bindingTranslator.getBindingKey(projectName, binding), modelElement);
	}

	/**
	 * @param astBindingKey  The binding of the AST root in which the given node is
	 *                       contained.
	 * @param nodeBindingKey An AST node binding.
	 * @param isTypeOfThe    The minimal type of the proxy
	 * @return The corresponding model element or <code>null</code>; possibly a
	 *         proxy that can not be resolved/loaded yet! So do NOT access
	 *         informations of this object!
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public <E extends EObject> E resolveBindingProxy(String[] localPath, IBinding binding, EClass isTypeOf) 
			throws ClassNotFoundException {
		
   		if (JavaASTUtil.isPrimitiveType(binding)) {
			return libraryModel.getPrimitiveType(binding);
		} else {
			
			// Add recovered bindings to library model:
			if (binding.isRecovered()) {
				return libraryModel.getLibraryModelElement(binding, isTypeOf, bindingRecovery);
			} 
			
			// Try to resolve existing Java resource:
			IJavaElement javaElement = null;
			
			try {
				javaElement = binding.getJavaElement();
			} catch (Throwable e) {
			}
			
			if (javaElement != null) {
				String projectName = javaElement.getJavaProject().getProject().getName();
				String uniqueBindingKey = bindingTranslator.getBindingKey(projectName, binding);
				
				// Check for internal bindings and already existing common external bindings:
				EObject existingBinding = bindings.get(uniqueBindingKey); 
				
				if (existingBinding != null) {
					return (E) existingBinding;
				} else {
					if (isInScope(projectName, binding)) {
						
						// Create new workspace proxy binding or common external binding:
						String[] externalPath = bindingTranslator.getModelPath(projectName, javaElement);
						URI externalBindingURI = bindingTranslator.getExternalURI(projectName, binding, externalPath, localPath);
						uniqueBindingKey = externalBindingURI.fragment();
						EObject newExternalModelElement = createExternalProxy(externalBindingURI, binding, isTypeOf);

						// Trace the new binding:
						if ((newExternalModelElement != null) && (newExternalModelElement != null)) {
							bindings.put(uniqueBindingKey, newExternalModelElement);
						}
						
						return (E) newExternalModelElement;
					} else {
						
						//Library model element:
						return libraryModel.getLibraryModelElement(binding, isTypeOf, bindingRecovery);
					}
				}
			} else {
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "Can not resolve: " + binding.getKey());
				}
			}
		}
		
		return null;
	}

	public boolean isInScope(String projectName, IBinding binding) {
		return workspaceProjectScope.contains(projectName) && isInWorkspace(binding);
	}

	/**
	 * @param externalBinding A Java binding.
	 * @return <code>true</code> if it is a source file in the workspace;
	 *         <code>false</code> if it is, e.g., a Java source of the JDT.
	 */
	public boolean isInWorkspace(IBinding externalBinding) {
		IResource resource = externalBinding.getJavaElement().getResource();
		
		if (resource != null) {
			if (resource.getFileExtension().equalsIgnoreCase("java")) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @param externalBindingURI The model element URI based on the Java AST binding.
	 * @param externalBinding    The Java AST binding.
	 * @param isTypeOfThe        minimal type of the proxy.
	 * @return A proxy model element for the given configuration.
	 * @throws ClassNotFoundException
	 */
	protected EObject createExternalProxy(URI externalBindingURI, IBinding externalBinding, EClass isTypeOf) 
			throws ClassNotFoundException {

		BasicEObjectImpl proxyElement = (BasicEObjectImpl) EcoreUtil.create(getBindingProxyType(externalBinding, isTypeOf));
		proxyElement.eSetProxyURI(externalBindingURI);
		
		return proxyElement;
	}
	
	/**
	 * @param node An node of the AST.
	 * @return The binding of this node or <code>null</code>.
	 */
	public IBinding getBinding(ASTNode node) {

		// see org.eclipse.jdt.core.dom.IBinding
		// see org.eclipse.jdt.core.dom.DefaultBindingResolver

		if (node instanceof ModuleDeclaration) {
			return ((ModuleDeclaration) node).resolveBinding();
		} else if (node instanceof AbstractTypeDeclaration) {
			return ((AbstractTypeDeclaration) node).resolveBinding();
		} else if (node instanceof VariableDeclaration) {
			return ((VariableDeclaration) node).resolveBinding();
		} else if (node instanceof VariableDeclarationFragment) {
			return((VariableDeclarationFragment) node).resolveBinding();
		} else if (node instanceof MethodDeclaration) {
			return ((MethodDeclaration) node).resolveBinding();
		} else if (node instanceof MemberValuePair) {
			return ((MemberValuePair) node).resolveMemberValuePairBinding();
		} else if (node instanceof EnumConstantDeclaration) {
			return ((EnumConstantDeclaration) node).resolveVariable();
		} else if (node instanceof AnnotationTypeMemberDeclaration) {
			return ((AnnotationTypeMemberDeclaration) node).resolveBinding();
		} else if (node instanceof Annotation) {
			return ((Annotation) node).resolveAnnotationBinding();
		}

		return null;
	}
	
	/**
	 * Returns the type of a proxy element based on a Java AST binding. The type is
	 * also stored in the xmi:type attribute of the model file. Please note that,
	 * for example, a UML Class might be converted into an Interface. This would
	 * require to load and save all resources with references pointing at the new
	 * interface, to change the type stored in the xmi:type attribute. However, the
	 * xmi:type only determines the type of the proxy objects and do not lead to
	 * failures during loading. The xmi:type have to be a concrete class, i.e., not
	 * abstract or an interface. For example, a UML Class could even be represented
	 * by an Activity as the Type of a Parameter.
	 * 
	 * @param binding  The Java AST binding.
	 * @param isTypeOf The minimal type required type;
	 * @return The type of the proxy model element.
	 */
	protected EClass getBindingProxyType(IBinding binding, EClass isTypeOf) throws ClassNotFoundException {

		if (!isTypeOf.isInterface() && !isTypeOf.isAbstract()) {
			return isTypeOf;
		}
		
		// Generic strategy:
		EClass concreteSubClass = getConcreteSubClass(isTypeOf, isTypeOf.eClass().getEPackage());
		
		if (concreteSubClass != null) {
			return concreteSubClass;
		}

		throw new ClassNotFoundException("No concrete subtype found: " + isTypeOf.toString());
	}

	private EClass getConcreteSubClass(EClass eSuperClass, EPackage ePackage) {
		for (EClassifier eClassifier : ePackage.getEClassifiers()) {
			if (eClassifier instanceof EClass) {
				EClass eClass = (EClass) eClassifier;
				
				if (!eClass.isInterface() && !eClass.isAbstract()) {
					if (eClass.getEAllSuperTypes().contains(eSuperClass)) {
						return eClass;
					}
				}
			}
		}
		
		for (EPackage sub : ePackage.getESubpackages()) {
			return getConcreteSubClass(eSuperClass, sub);
		}
		
		return null;
	}
	
	/**
	 * @return The local projects in the workspace that will be transformed to
	 *         corresponding models. Otherwise, a project will be considered as
	 *         external projects. External projects might be generated as common
	 *         fragments.
	 */
	public Set<String> getWorkspaceProjectScope() {
		return workspaceProjectScope;
	}
	
	/**
	 * @return Binding key -> Model element
	 */
	public Map<String, EObject> getBindings() {
		return bindings;
	}
	
	/**
	 * @param bindings Binding key -> Model element
	 */
	public void setBindings(Map<String, EObject> bindings) {
		this.bindings = bindings;
	}
	
	/**
	 * @return Maps Java AST bindings to EMF XMI object ID bindings.
	 */
	public JavaASTBindingTranslator getBindingTranslator() {
		return bindingTranslator;
	}
}
