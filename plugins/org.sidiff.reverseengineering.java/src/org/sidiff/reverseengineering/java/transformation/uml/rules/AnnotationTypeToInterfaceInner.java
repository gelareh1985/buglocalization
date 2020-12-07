package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class AnnotationTypeToInterfaceInner extends JavaToUML<AnnotationTypeDeclaration, Classifier, Interface> {

	@Override
	public void apply(AnnotationTypeDeclaration typeDeclaration) {
		Interface annotationInterface = rules.typeToInterface.createInterface(typeDeclaration);
		trafo.createModelElement(typeDeclaration, annotationInterface);
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
	public void link(AnnotationTypeDeclaration javaNode, Interface modelNode) throws ClassNotFoundException {
	}

}
