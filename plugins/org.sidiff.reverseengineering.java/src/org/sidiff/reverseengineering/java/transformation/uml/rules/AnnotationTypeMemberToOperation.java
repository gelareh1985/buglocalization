package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.ValueSpecification;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;
import org.sidiff.reverseengineering.java.util.JavaASTUtil;

public class AnnotationTypeMemberToOperation extends JavaToUML<AnnotationTypeMemberDeclaration, Interface, Operation> {

	@Override
	public void apply(AnnotationTypeMemberDeclaration memberDeclaration) {
		Operation umlOperation = rules.methodToOperation.createOperation(memberDeclaration, memberDeclaration.getName().getIdentifier());
		trafo.createModelElement(memberDeclaration, umlOperation);
	}

	@Override
	public void apply(Interface annotationInterface, Operation annotationOperation) {
		annotationInterface.getOwnedOperations().add(annotationOperation);
	}
	
	@Override
	public void link(AnnotationTypeMemberDeclaration memberDeclaration, Operation operation) throws ClassNotFoundException {
		rules.javaToUMLHelper.setModifiers(operation, memberDeclaration); // interface visibility depends on container
		
		Type returnType = memberDeclaration.getType();
		
		// constructor return type = null or void unexpected on annotation:
		if ((returnType != null) && !JavaASTUtil.isPrimitiveType(returnType, PrimitiveType.VOID)) {
			Parameter umlReturnParameter = rules.variableToParameter.createParameter(null);
			umlReturnParameter.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			umlReturnParameter.setName(rules.methodToOperation.returnParameterName);
			
			rules.variableToParameter.setParameterType(umlReturnParameter, returnType);
			
			operation.getOwnedParameters().add(umlReturnParameter);
			if (memberDeclaration.getDefault() != null) {
				ValueSpecification defaultValue = rules.javaToUMLHelper.createValueSpecification(memberDeclaration.getDefault(), false);
				umlReturnParameter.setDefaultValue(defaultValue);
			}
		}
	}

}
