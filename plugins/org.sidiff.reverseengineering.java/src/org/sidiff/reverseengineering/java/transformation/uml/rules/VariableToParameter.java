package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class VariableToParameter extends JavaToUML<SingleVariableDeclaration, Operation, Parameter> {

	@Override
	public void apply(SingleVariableDeclaration variableDeclaration) {
		Parameter umlParameter = createParameter(variableDeclaration.getName().getIdentifier());
		
		// For example fun(Foo... args)
		if (variableDeclaration.isVarargs()) {
			umlParameter.setLower(0);
			umlParameter.setUpper(-1);
		}
		
		trafo.createModelElement(variableDeclaration, umlParameter);
	}
	
	public Parameter createParameter(String name) {
		Parameter umlParameter = umlFactory.createParameter();
		umlParameter.setName(name);
		return umlParameter;
	}

	@Override
	public void apply(Operation modelContainer, Parameter modelNode) {
		modelContainer.getOwnedParameters().add(modelNode);
	}

	@Override
	public void link(SingleVariableDeclaration variableDeclaration, Parameter umlParameter) throws ClassNotFoundException {
		setParameterType(umlParameter, variableDeclaration.getType());
	}

	public void setParameterType(Parameter umlParameter, org.eclipse.jdt.core.dom.Type javaType) throws ClassNotFoundException {
		rules.javaToUMLHelper.setType(umlParameter, javaType);
		rules.javaToUMLHelper.encodeArrayType(umlParameter, javaType);
	}

}
