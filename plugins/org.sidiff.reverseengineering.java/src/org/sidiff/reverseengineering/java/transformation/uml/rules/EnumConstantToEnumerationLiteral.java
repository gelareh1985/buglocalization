package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class EnumConstantToEnumerationLiteral extends JavaToUML<EnumConstantDeclaration, Enumeration, EnumerationLiteral>{

	@Override
	public void apply(EnumConstantDeclaration javaNode) {
		EnumerationLiteral umlEnumerationLiteral = umlFactory.createEnumerationLiteral();
		umlEnumerationLiteral.setName(javaNode.getName().getIdentifier());
		trafo.createModelElement(javaNode, umlEnumerationLiteral);
	}

	@Override
	public void apply(Enumeration modelContainer, EnumerationLiteral modelNode) {
		modelContainer.getOwnedLiterals().add(modelNode);
	}

	@Override
	public void link(EnumConstantDeclaration javaNode, EnumerationLiteral modelNode) throws ClassNotFoundException {
	}

}
