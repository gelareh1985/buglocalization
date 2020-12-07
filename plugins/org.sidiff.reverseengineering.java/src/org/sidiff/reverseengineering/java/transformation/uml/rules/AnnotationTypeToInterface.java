package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.uml2.uml.Interface;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class AnnotationTypeToInterface extends JavaToUML<AnnotationTypeDeclaration, Package, Interface> {

	@Override
	public void apply(AnnotationTypeDeclaration typeDeclaration) {
		Interface annotationInterface = rules.typeToInterface.createInterface(typeDeclaration);
		
		// annotations do not allow extends:
		annotationInterface.setIsFinalSpecialization(true);
		annotationInterface.setIsLeaf(true);
		
		trafo.createRootModelElement(typeDeclaration, annotationInterface);
	}

	@Override
	public void apply(Package modelContainer, Interface modelNode) {
		// Containment will be created by JavaASTProjectModelUML
	}

	@Override
	public void link(AnnotationTypeDeclaration javaNode, Interface modelNode) throws ClassNotFoundException {
	}

}
