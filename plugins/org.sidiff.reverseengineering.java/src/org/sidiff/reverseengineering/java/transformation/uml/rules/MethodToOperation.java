package org.sidiff.reverseengineering.java.transformation.uml.rules;


import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.OperationOwner;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;
import org.sidiff.reverseengineering.java.util.JavaASTUtil;

public class MethodToOperation extends JavaToUML<MethodDeclaration, OperationOwner, Operation> {
	
	public String returnParameterName = "return";
	
	@Override
	public void apply(MethodDeclaration methodDeclaration) {
		Operation umlOperation = createOperation(methodDeclaration, methodDeclaration.getName().getIdentifier());
		trafo.createModelElement(methodDeclaration, umlOperation);
	}

	public Operation createOperation(BodyDeclaration methodDeclaration, String name) {
		Operation umlOperation = umlFactory.createOperation();
		umlOperation.setName(name);
		
		if (methodDeclaration.getJavadoc() != null) {
			rules.javaToUMLHelper.createJavaDocComment(umlOperation, methodDeclaration.getJavadoc());
		}
		return umlOperation;
	}

	@Override
	public void apply(OperationOwner modelContainer, Operation modelNode) {
		modelContainer.getOwnedOperations().add(modelNode);
	}

	@Override
	public void link(MethodDeclaration methodDeclaration, Operation operation) throws ClassNotFoundException {
		rules.javaToUMLHelper.setModifiers(operation, methodDeclaration); // interface visibility depends on container
		
		Type returnType = methodDeclaration.getReturnType2();
		
		// constructor return type = null
		if ((returnType != null) && !JavaASTUtil.isPrimitiveType(returnType, PrimitiveType.VOID)) {
			Parameter umlReturnParameter = rules.variableToParameter.createParameter(null);
			umlReturnParameter.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			umlReturnParameter.setName(returnParameterName);
			
			rules.variableToParameter.setParameterType(umlReturnParameter, returnType);
			operation.getOwnedParameters().add(umlReturnParameter);
		}
		
		// exceptions as parameters:
		for (Object exceptionType : methodDeclaration.thrownExceptionTypes()) {
			if (exceptionType instanceof Type) {
				Parameter umlExceptionParameter = rules.variableToParameter.createParameter(null);
				umlExceptionParameter.setDirection(ParameterDirectionKind.OUT_LITERAL);
				umlExceptionParameter.setIsException(true);
				umlExceptionParameter.setLower(0);
				
				rules.variableToParameter.setParameterType(umlExceptionParameter, (Type) exceptionType);
				operation.getOwnedParameters().add(umlExceptionParameter);
				
				if (umlExceptionParameter.getType() != null) {
					umlExceptionParameter.setName("throws" + umlExceptionParameter.getType().getName());
				}
			}
		}
	}
}
