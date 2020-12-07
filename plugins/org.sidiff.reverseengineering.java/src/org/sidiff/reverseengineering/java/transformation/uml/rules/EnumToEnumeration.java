package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.uml2.uml.Enumeration;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class EnumToEnumeration extends JavaToUML<EnumDeclaration, Package, Enumeration> {

	@Override
	public void apply(EnumDeclaration enumDeclaration) {
		Enumeration umlEnumeration = createEnumeration(enumDeclaration);
		trafo.createRootModelElement(enumDeclaration, umlEnumeration);
	}
	
	public Enumeration createEnumeration(EnumDeclaration enumDeclaration) {
		Enumeration umlEnumeration = umlFactory.createEnumeration();
		umlEnumeration.setName(enumDeclaration.getName().getIdentifier());
		rules.javaToUMLHelper.setModifiers(umlEnumeration, enumDeclaration);
		
		if (enumDeclaration.getJavadoc() != null) {
			rules.javaToUMLHelper.createJavaDocComment(umlEnumeration, enumDeclaration.getJavadoc());
		}
		
		return umlEnumeration;
	}

	@Override
	public void apply(Package modelContainer, Enumeration modelNode) {
		// Containment will be created by JavaASTProjectModelUML
	}

	@Override
	public void link(EnumDeclaration javaNode, Enumeration modelNode) throws ClassNotFoundException {
	}

}
