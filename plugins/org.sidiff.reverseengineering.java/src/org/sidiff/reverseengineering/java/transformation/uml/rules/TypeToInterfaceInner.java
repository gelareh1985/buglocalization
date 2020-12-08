package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class TypeToInterfaceInner extends JavaToUML<TypeDeclaration, Classifier, Interface> {

	@Override
	public void apply(TypeDeclaration typeDeclaration) {
		Interface umlInterface = rules.typeToInterface.createInterface(typeDeclaration);
		trafo.createModelElement(typeDeclaration, umlInterface);
	}

	@Override
	public void apply(Classifier modelContainer, Interface modelNode) {
		if (modelContainer instanceof Class) {
			((Class) modelContainer).getNestedClassifiers().add(modelNode);
		} else if (modelContainer instanceof Interface) {
			((Interface) modelContainer).getNestedClassifiers().add(modelNode);
		}
	}

	@Override
	public void link(TypeDeclaration typeDeclaration, Interface umlInterface) throws ClassNotFoundException {

		// extends:
		for (Object javaInterface : typeDeclaration.superInterfaceTypes()) {
			if (javaInterface instanceof Type) {
				rules.typeToClass.createGeneralization(typeDeclaration, (Type) javaInterface, umlInterface);
			}
		}
	}

}
