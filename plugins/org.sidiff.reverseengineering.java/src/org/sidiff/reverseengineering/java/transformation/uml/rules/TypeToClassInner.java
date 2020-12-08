package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class TypeToClassInner extends JavaToUML<TypeDeclaration, Classifier, Class> {

	@Override
	public void apply(TypeDeclaration typeDeclaration) {
		Class umlClass = rules.typeToClass.createClass(typeDeclaration);
		trafo.createModelElement(typeDeclaration, umlClass);
	}

	@Override
	public void apply(Classifier modelContainer, Class modelNode) {
		if (modelContainer instanceof Class) {
			((Class) modelContainer).getNestedClassifiers().add(modelNode);
		} else if (modelContainer instanceof Interface) {
			((Interface) modelContainer).getNestedClassifiers().add(modelNode);
		}
	}

	@Override
	public void link(TypeDeclaration typeDeclaration, Class umlClass) throws ClassNotFoundException {
		
		// extends:
		rules.typeToClass.createGeneralization(typeDeclaration, typeDeclaration.getSuperclassType(), umlClass);
		
		// implements:
		rules.typeToClass.createInterfaces(typeDeclaration, umlClass);
	}

}
