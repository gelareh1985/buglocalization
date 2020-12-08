package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class EnumToEnumerationInner extends JavaToUML<EnumDeclaration, Classifier, Enumeration> {

	@Override
	public void apply(EnumDeclaration enumDeclaration) {
		Enumeration umlEnumeration = rules.enumToEnumeration.createEnumeration(enumDeclaration);
		trafo.createModelElement(enumDeclaration, umlEnumeration);
	}

	@Override
	public void apply(Classifier modelContainer, Enumeration modelNode) {
		if (modelContainer instanceof Class) {
			((Class) modelContainer).getNestedClassifiers().add(modelNode);
		} else if (modelContainer instanceof Interface) {
			((Interface) modelContainer).getNestedClassifiers().add(modelNode);
		}
	}

	@Override
	public void link(EnumDeclaration javaNode, Enumeration modelNode) throws ClassNotFoundException {
	}
}
