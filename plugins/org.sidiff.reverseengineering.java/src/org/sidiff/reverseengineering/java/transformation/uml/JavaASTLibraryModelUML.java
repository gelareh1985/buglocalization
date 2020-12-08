package org.sidiff.reverseengineering.java.transformation.uml;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.OperationOwner;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingTranslator;
import org.sidiff.reverseengineering.java.transformation.JavaASTLibraryModel;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUMLHelper;
import org.sidiff.reverseengineering.java.util.BindingRecovery;
import org.sidiff.reverseengineering.java.util.JavaASTUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Manages the creation of library UML model elements.
 * 
 * @author Manuel Ohrndorf
 */
public class JavaASTLibraryModelUML extends JavaASTLibraryModel {

	protected UMLFactory umlFactory = UMLFactory.eINSTANCE;
	
	protected UMLPackage umlPackage = UMLPackage.eINSTANCE;
	
	protected String libraryModelName = "Library";
	
	protected JavaToUMLHelper javaToUMLHelper;
	
	protected Model libraryModelRoot;
	
	protected Package primitiveTypeModel;
	
	protected Package defaultPackage;
	
	/**
	 * @see {@link JavaASTLibraryModel#JavaASTLibraryModel(XMLResource, JavaASTBindingTranslator)}
	 */
	@Inject
	public JavaASTLibraryModelUML(
			@Assisted XMLResource libraryModel,
			JavaASTBindingTranslator bindingTranslator, 
			JavaToUMLHelper javaToUMLHelper) {
		
		super(libraryModel, bindingTranslator);
		this.javaToUMLHelper = javaToUMLHelper;
		
		if (libraryModel.getContents().isEmpty()  || !(libraryModel.getContents().get(0) instanceof Model)) {
			this.libraryModelRoot = umlFactory.createModel();
			this.libraryModelRoot.setName(libraryModelName);
			libraryModel.getContents().add(libraryModelRoot);
			bindModelElement(getBindingKey("libraries"), libraryModelRoot);
		} else {
			this.libraryModelRoot = (Model) libraryModel.getContents().get(0);
		}
	}
	
	@Override
	public Model getLibraryModelRoot() {
		return libraryModelRoot;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DataType getPrimitiveType(IBinding binding) {
		EObject libraryModelElement =  super.getPrimitiveType(binding);

		if ((libraryModelElement == null) && JavaASTUtil.isPrimitiveType(binding)) {
			if (primitiveTypeModel == null) {
				createLibraryPrimitiveModel();
			}
			DataType newDataType = createLibraryPrimitiveDataType(binding);
			bindPrimitiveType(binding, newDataType);
			return newDataType;
		}
		
		return (DataType) libraryModelElement;
	}

	protected void createLibraryPrimitiveModel() {
		this.primitiveTypeModel = umlFactory.createPackage();
		this.primitiveTypeModel.setName("datatypes");
		this.libraryModelRoot.getPackagedElements().add(0, primitiveTypeModel);
		bindModelElement(getBindingKey("datatypes"), primitiveTypeModel);
	}

	protected DataType createLibraryPrimitiveDataType(IBinding binding) {
		DataType primitiveDataType = umlFactory.createDataType();
		primitiveDataType.setName(binding.getName());
		primitiveDataType.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		primitiveTypeModel.getPackagedElements().add(primitiveDataType);
		return primitiveDataType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends EObject> E getLibraryModelElement(IBinding externalBinding, EClass isTypeOf, BindingRecovery bindingRecovery) {
		externalBinding = JavaASTUtil.genericTypeErasure(externalBinding);
		externalBinding = JavaASTUtil.arrayTypeErasure(externalBinding);
		
		EObject libraryModelElement = super.getLibraryModelElement(externalBinding, isTypeOf, bindingRecovery);
		
		// Already exists?
		if (libraryModelElement != null) {
			return (E) libraryModelElement;
		}
		
		// Create new:
		switch (externalBinding.getKind()) {
		case IBinding.TYPE:
			ITypeBinding typeBinding = (ITypeBinding) externalBinding;
			
			if (typeBinding.isEnum()) {
				libraryModelElement = createLibraryEnumeration(typeBinding, bindingRecovery);
			} else if (typeBinding.isInterface()|| typeBinding.isRecovered()) {
				libraryModelElement = createLibraryInterface(typeBinding, bindingRecovery);
			} else if (typeBinding.isClass()) {
				libraryModelElement = createLibraryClass(typeBinding, bindingRecovery);
			}
			
			break;
		case IBinding.VARIABLE:
			IVariableBinding variableBinding = (IVariableBinding) externalBinding;
			
			if (variableBinding.isParameter()) {
				libraryModelElement = createLibraryParameter(variableBinding, bindingRecovery);
			} else if (variableBinding.isEnumConstant()) { // is also field
				libraryModelElement = createLibraryEnumLiteral(variableBinding, bindingRecovery);
			} else if (variableBinding.isField()) {
				libraryModelElement = createLibraryProperty(variableBinding, bindingRecovery);
			}
			
			break;
		case IBinding.METHOD:
			libraryModelElement = createLibraryOperation((IMethodBinding) externalBinding, bindingRecovery);
			break;
		}
		
		// Trace and return new model element:
		if (libraryModelElement != null) {
			bindModelElement(externalBinding, libraryModelElement, bindingRecovery);
			return (E) libraryModelElement;
		}
		
		return null;
	}

	protected Package getLibraryPackage(IPackageBinding packageBinding, ITypeBinding containedType, BindingRecovery bindingRecovery) {
		
		// Use the original package of a recovered binding from the imports:
		String[] packages = bindingRecovery.getRecoveredPackage(containedType);
		
		// Use original binding:
		if (packages == null) {
			packages = packageBinding.getNameComponents();
		}
		
		String bindingKey = null;
		Package parentPackage = libraryModelRoot;
		Package childPackage = null;

		for (String packageName : packages) {
			if (bindingKey == null) {
				bindingKey = packageName;
			} else {
				bindingKey += "/" + packageName;
			}
			childPackage = super.getLibraryModelElement(getBindingKey(bindingKey));

			if (childPackage == null) {
				childPackage = umlFactory.createPackage();
				childPackage.setName(packageName);
				parentPackage.getNestedPackages().add(0, childPackage);
				bindModelElement(getBindingKey(bindingKey), childPackage);
			}

			parentPackage = childPackage;
		}
		
		if (childPackage != null) {
			return childPackage;
		}
		
		// use default package:
		if (defaultPackage == null) {
			this.defaultPackage = umlFactory.createPackage();
			this.defaultPackage.setName("default");
			this.libraryModelRoot.getNestedPackages().add(0, defaultPackage);
			bindModelElement(getBindingKey("default"), defaultPackage);
		}
		return defaultPackage;
		
	}

	protected void addClassifierToModel(ITypeBinding typeBinding, Classifier newClassifier, BindingRecovery bindingRecovery) {
		if (typeBinding.getDeclaringClass() != null) {
			
			// nested in class or interface:
			Classifier parentClassifier = getLibraryModelElement(typeBinding.getDeclaringClass(), umlPackage.getClassifier(), bindingRecovery);
			
			if (parentClassifier instanceof Class) {
				((Class) parentClassifier).getNestedClassifiers().add(newClassifier);
			} else if (parentClassifier instanceof Interface) {
				((Interface) parentClassifier).getNestedClassifiers().add(newClassifier);
			}
		} else {
			
			// nested in package:
			Package libraryPackage = getLibraryPackage(typeBinding.getPackage(), typeBinding, bindingRecovery);
	
			if (libraryPackage != null) {
				libraryPackage.getOwnedTypes().add(newClassifier);
				return;
			}
		}
	}

	protected EObject createLibraryEnumeration(ITypeBinding typeBinding, BindingRecovery bindingRecovery) {
		Enumeration libraryEnum = umlFactory.createEnumeration();
		libraryEnum.setName(typeBinding.getName());
		
		// set container:
		addClassifierToModel(typeBinding, libraryEnum, bindingRecovery);

		// enumeration literal:
		for (IVariableBinding enumLiteral : typeBinding.getDeclaredFields()) {
			EnumerationLiteral libraryEnumLiteral = umlFactory.createEnumerationLiteral();
			libraryEnumLiteral.setName(enumLiteral.getName());
			libraryEnum.getOwnedLiterals().add(libraryEnumLiteral);
		}
		
		javaToUMLHelper.setModifiers(libraryEnum, typeBinding.getModifiers());
		
		// repair visibility:
		if (typeBinding.isRecovered()) {
			libraryEnum.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		}
		
		return libraryEnum;
	}

	protected EObject createLibraryEnumLiteral(IVariableBinding variableBinding, BindingRecovery bindingRecovery) {
		// create full enumeration:
		getLibraryModelElement(variableBinding.getDeclaringClass(), umlPackage.getEnumeration(), bindingRecovery);
		return super.getLibraryModelElement(variableBinding, umlPackage.getEnumerationLiteral(), bindingRecovery);
	}

	protected EObject createLibraryInterface(ITypeBinding typeBinding, BindingRecovery bindingRecovery) {
		Interface libraryIterface = umlFactory.createInterface();
		libraryIterface.setName(typeBinding.getName());
		
		// set container:
		addClassifierToModel(typeBinding, libraryIterface, bindingRecovery);
		
		// Operation visibility package->public depends on container...
		javaToUMLHelper.setModifiers(libraryIterface, typeBinding.getModifiers());
		
		// repair visibility:
		if (typeBinding.isRecovered()) {
			libraryIterface.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		}
		
		return libraryIterface;
	}

	protected EObject createLibraryClass(ITypeBinding typeBinding, BindingRecovery bindingRecovery) {
		Class libraryClass = umlFactory.createClass();
		libraryClass.setName(typeBinding.getName());
		
		// set container:
		addClassifierToModel(typeBinding, libraryClass, bindingRecovery);
		
		javaToUMLHelper.setModifiers(libraryClass, typeBinding.getModifiers());
		
		// repair visibility:
		if (typeBinding.isRecovered()) {
			libraryClass.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		}
		
		return libraryClass;
	}

	protected EObject createLibraryOperation(IMethodBinding methodBinding, BindingRecovery bindingRecovery) {
		Operation libraryOperation = umlFactory.createOperation();
		libraryOperation.setName(methodBinding.getName());
		
		OperationOwner libraryClassifier = getLibraryModelElement(methodBinding.getDeclaringClass(), umlPackage.getClassifier(), bindingRecovery);
		libraryClassifier.getOwnedOperations().add(libraryOperation);
		
		// Create parameter signature:
		for (ITypeBinding parameter : methodBinding.getParameterTypes()) {
			Parameter libraryParameter = umlFactory.createParameter();
			libraryParameter.setName(parameter.getName());
			libraryOperation.getOwnedParameters().add(libraryParameter);
		}
		
		javaToUMLHelper.setModifiers(libraryOperation, methodBinding.getModifiers());
		
		// repair visibility:
		if (methodBinding.isRecovered()) {
			libraryOperation.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		}
		
		return libraryOperation;
	}

	protected EObject createLibraryParameter(IVariableBinding variableBinding, BindingRecovery bindingRecovery) {
		getLibraryModelElement(variableBinding.getDeclaringMethod(), umlPackage.getOperation(), bindingRecovery);
		return super.getLibraryModelElement(variableBinding, umlPackage.getParameter(), bindingRecovery);
	}

	protected EObject createLibraryProperty(IVariableBinding variableBinding, BindingRecovery bindingRecovery) {
		Property libraryProperty = umlFactory.createProperty();
		libraryProperty.setName(variableBinding.getName());
		
		Classifier libraryClassifier = getLibraryModelElement(variableBinding.getDeclaringClass(), umlPackage.getClassifier(), bindingRecovery);
		
		if (libraryClassifier instanceof StructuredClassifier) {
			((StructuredClassifier) libraryClassifier).getOwnedAttributes().add(libraryProperty);
		} else if (libraryClassifier instanceof Interface) {
			((Interface) libraryClassifier).getOwnedAttributes().add(libraryProperty);
		}
		
		javaToUMLHelper.setModifiers(libraryProperty, variableBinding.getModifiers());
		
		// repair visibility:
		if (variableBinding.isRecovered()) {
			libraryProperty.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		}
		
		return libraryProperty;
	}
}
