package org.sidiff.reverseengineering.java.transformation.uml;

import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.uml2.uml.UMLPackage;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingResolver;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingTranslator;
import org.sidiff.reverseengineering.java.transformation.JavaASTLibraryModel;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Maps UML types to Java AST bindings.
 * 
 * @author Manuel Ohrndorf
 */
public class JavaASTBindingResolverUML extends JavaASTBindingResolver {
	
	/**
	 * @see {@link JavaASTBindingResolver#JavaASTBindingResolver}
	 */
	@Inject
	public JavaASTBindingResolverUML(
			@Assisted CompilationUnit compilationUnit, 
			@Assisted Set<String> workspaceProjectScope,
			@Assisted JavaASTLibraryModel libraryModel, 
			JavaASTBindingTranslator bindingTranslator) {
		super(compilationUnit, workspaceProjectScope, libraryModel, bindingTranslator);
	}

	@Override
	public EClass getBindingProxyType(IBinding binding, EClass isTypeOf) throws ClassNotFoundException {
		
		/*
		 * NOTE: In general, for example, a UML Class might be converted into an
		 * Interface. This would require to load and save all resources with references
		 * pointing at the new interface, to change the type stored in the xmi:type
		 * attribute. However, the xmi:type only determines the type of the proxy
		 * objects and do not lead to failures during loading. The xmi:type have to be a
		 * concrete class, i.e., not abstract or an interface. For example, a UML Class
		 * could even be represented by an Activity as the Type of a Parameter.
		 */
		
		// Map UML types to bindings:
		EClass concreteSubClass = null;
		
		switch (binding.getKind()) {
		case IBinding.PACKAGE:
			concreteSubClass = UMLPackage.eINSTANCE.getPackage();
			break;
		case IBinding.TYPE:
			ITypeBinding typeBinding = (ITypeBinding) binding;
			
			if (typeBinding.isEnum()) {
				concreteSubClass = UMLPackage.eINSTANCE.getEnumeration();
			} else if (typeBinding.isInterface()) {
				concreteSubClass = UMLPackage.eINSTANCE.getInterface();
			} else {
				concreteSubClass = UMLPackage.eINSTANCE.getClass_();
			}
			
			break;
		case IBinding.VARIABLE:
			IVariableBinding variableBinding = (IVariableBinding) binding;
			
			if (variableBinding.isParameter()) {
				concreteSubClass = UMLPackage.eINSTANCE.getParameter();
			} else if (variableBinding.isEnumConstant()) { // is also field
				concreteSubClass = UMLPackage.eINSTANCE.getEnumerationLiteral();
			} else {
				concreteSubClass = UMLPackage.eINSTANCE.getProperty();
			}
			
			break;
		case IBinding.METHOD:
			concreteSubClass = UMLPackage.eINSTANCE.getOperation();
			break;
		case IBinding.ANNOTATION:
			concreteSubClass = UMLPackage.eINSTANCE.getComment();
			break;
		case IBinding.MEMBER_VALUE_PAIR:
			concreteSubClass = UMLPackage.eINSTANCE.getComment();
			break;
		case IBinding.MODULE:
			concreteSubClass = UMLPackage.eINSTANCE.getModel();
			break;
		}
		
		if (concreteSubClass != null) {
			return concreteSubClass;
		}
		
		// Fallback to generic implementation:
		return super.getBindingProxyType(binding, isTypeOf);
	}
}
