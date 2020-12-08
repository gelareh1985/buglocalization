package org.sidiff.reverseengineering.java.transformation.uml.rulebase;

import java.util.ArrayList;
import java.util.List;

import org.sidiff.reverseengineering.java.transformation.uml.JavaASTTransformationUML;
import org.sidiff.reverseengineering.java.transformation.uml.rules.AnnotationTypeMemberToOperation;
import org.sidiff.reverseengineering.java.transformation.uml.rules.AnnotationTypeToInterface;
import org.sidiff.reverseengineering.java.transformation.uml.rules.AnnotationTypeToInterfaceInner;
import org.sidiff.reverseengineering.java.transformation.uml.rules.BodyBlockToFunctionBehavior;
import org.sidiff.reverseengineering.java.transformation.uml.rules.EnumConstantToEnumerationLiteral;
import org.sidiff.reverseengineering.java.transformation.uml.rules.EnumToEnumeration;
import org.sidiff.reverseengineering.java.transformation.uml.rules.EnumToEnumerationInner;
import org.sidiff.reverseengineering.java.transformation.uml.rules.FieldToProperty;
import org.sidiff.reverseengineering.java.transformation.uml.rules.MethodInvocationToCallOperationAction;
import org.sidiff.reverseengineering.java.transformation.uml.rules.MethodToOperation;
import org.sidiff.reverseengineering.java.transformation.uml.rules.TypeToClass;
import org.sidiff.reverseengineering.java.transformation.uml.rules.TypeToClassInner;
import org.sidiff.reverseengineering.java.transformation.uml.rules.TypeToClassWithInteraction;
import org.sidiff.reverseengineering.java.transformation.uml.rules.TypeToInterface;
import org.sidiff.reverseengineering.java.transformation.uml.rules.TypeToInterfaceInner;
import org.sidiff.reverseengineering.java.transformation.uml.rules.VariableToParameter;

import com.google.inject.Inject;

public class JavaToUMLRules {
	
	private List<JavaToUML<?,?,?>> transformations;
	
	public JavaToUMLHelper javaToUMLHelper;
	
	// Class Diagram:
	
	public EnumToEnumeration enumToEnumeration;
	
	public EnumToEnumerationInner enumToEnumerationInner;
	
	public EnumConstantToEnumerationLiteral enumConstantToEnumerationLiteral;
	
	public TypeToClass typeToClass;
	
	public TypeToClassInner typeToClassInner;
	
	public TypeToInterface typeToInterface;
	
	public TypeToInterfaceInner typeToInterfaceInner;
	
	public FieldToProperty fieldToProperty;
	
	public MethodToOperation methodToOperation;
	
	public VariableToParameter variableToParameter;
	
	public AnnotationTypeToInterface annotationTypeToInterface;
	
	public AnnotationTypeToInterfaceInner annotationTypeToInterfaceInner;
	
	public AnnotationTypeMemberToOperation annotationTypeMemberToOperation;
	
	// Method body -> Activity Diagram:
	
	public TypeToClassWithInteraction typeToClassWithInteraction;
	
	public BodyBlockToFunctionBehavior blockToFunctionBehavior;
	
	public MethodInvocationToCallOperationAction methodInvocationToCallOperationAction;
	
	@Inject
	public JavaToUMLRules(JavaToUMLHelper javaToUMLHelper) {
		this.javaToUMLHelper = javaToUMLHelper;
		
		this.transformations = new ArrayList<>();
		
		this.enumToEnumeration = add(new EnumToEnumeration());
		this.enumToEnumerationInner = add(new EnumToEnumerationInner());
		this.enumConstantToEnumerationLiteral = add(new EnumConstantToEnumerationLiteral());
		this.typeToClass = add(new TypeToClass());
		this.typeToClassInner = add(new TypeToClassInner());
		this.typeToInterface = add(new TypeToInterface());
		this.typeToInterfaceInner = add(new TypeToInterfaceInner());
		this.fieldToProperty = add(new FieldToProperty());
		this.methodToOperation = add(new MethodToOperation());
		this.variableToParameter = add(new VariableToParameter());
		this.annotationTypeToInterface = add(new AnnotationTypeToInterface());
		this.typeToClassWithInteraction = add(new TypeToClassWithInteraction());
		this.annotationTypeToInterfaceInner = add(new AnnotationTypeToInterfaceInner());
		this.annotationTypeMemberToOperation = add(new AnnotationTypeMemberToOperation());
		
		this.blockToFunctionBehavior = add(new BodyBlockToFunctionBehavior());
		this.methodInvocationToCallOperationAction = add(new MethodInvocationToCallOperationAction());
	}
	
	protected <T extends JavaToUML<?, ?, ?>> T add(T transformation) {
		this.transformations.add(transformation);
		return transformation;
	}

	public void init(JavaASTTransformationUML trafo) {
		javaToUMLHelper.init(trafo, this);
		
		for (JavaToUML<?, ?, ?> javaToUML : transformations) {
			javaToUML.init(trafo, this);
		}
	}
	
	public void cleanUp() {
		javaToUMLHelper.cleanUp();
		
		for (JavaToUML<?, ?, ?> javaToUML : transformations) {
			javaToUML.cleanUp();
		}
	}
	
	public List<JavaToUML<?,?,?>> getTransformations() {
		return transformations;
	}
}
