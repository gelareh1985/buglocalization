package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.ValueSpecification;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class FieldToProperty extends JavaToUML<VariableDeclarationFragment, Classifier, Property> {

	@Override
	public void apply(VariableDeclarationFragment fieldDeclaration) {
		Property umlProperty = createProperty(fieldDeclaration, fieldDeclaration);
		trafo.createModelElement(fieldDeclaration, umlProperty);
	}
	
	public Property createProperty(VariableDeclarationFragment fieldDeclarationFragment, VariableDeclarationFragment declarationFragment) {
		if (fieldDeclarationFragment.getParent() instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) fieldDeclarationFragment.getParent();
			
			Property umlProperty = umlFactory.createProperty();
			umlProperty.setName(declarationFragment.getName().getIdentifier());
			
			// Encode initialization with mulitplicities:
			// NOTE: 1 is default value...
//			if (declarationFragment.getInitializer() != null) {
//				umlProperty.setLower(1);
//			}
			
			// Encode arrays with multiplicities:
			if (fieldDeclaration.getType().isArrayType()) {
				umlProperty.setUpper(-1);
			}
			
			if (fieldDeclaration.getJavadoc() != null) {
				rules.javaToUMLHelper.createJavaDocComment(umlProperty, fieldDeclaration.getJavadoc());
			}
			
			return umlProperty;
		}
		
		return null;
	}

	@Override
	public void apply(Classifier modelContainer, Property modelNode) {
		if (modelContainer instanceof StructuredClassifier) {
			((StructuredClassifier) modelContainer).getOwnedAttributes().add(modelNode);
		} else if (modelContainer instanceof Interface) {
			((Interface) modelContainer).getOwnedAttributes().add(modelNode);
		} else if (modelContainer instanceof DataType) {
			((DataType) modelContainer).getOwnedAttributes().add(modelNode);
		}
	}

	@Override
	public void link(VariableDeclarationFragment declarationFragment, Property umlProperty) throws ClassNotFoundException {
		if (declarationFragment.getParent() instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) declarationFragment.getParent();
			
			setPropertyType(umlProperty, fieldDeclaration.getType());
			setPropertyDefaultValue(declarationFragment, umlProperty);
			rules.javaToUMLHelper.setModifiers(umlProperty, fieldDeclaration); // interface visibility depends on container
		}
	}

	public void setPropertyType(Property umlProperty, org.eclipse.jdt.core.dom.Type javaType) throws ClassNotFoundException {
		rules.javaToUMLHelper.setType(umlProperty, javaType);
		rules.javaToUMLHelper.encodeArrayType(umlProperty, javaType);
	}

	public void setPropertyDefaultValue(VariableDeclarationFragment declarationFragment, Property umlProperty) {
		ValueSpecification defaultValue = rules.javaToUMLHelper.createValueSpecification(declarationFragment.getInitializer(), false);
		
		if (defaultValue != null) {
			// Primitive value:
			umlProperty.setDefaultValue(defaultValue);
		} else {
			if (rules.createPropertyAssignmentComment) {
				if (declarationFragment.getInitializer() != null) {
					String[] sortedWorts = rules.javaToUMLHelper.createBagOfWords(declarationFragment.getInitializer().toString());

					if (sortedWorts.length > 0) {
						String operationBodyBOW = String.join(" ", sortedWorts);
						Comment umlComment = umlFactory.createComment();
						umlComment.setBody(operationBodyBOW);
						umlProperty.getOwnedComments().add(umlComment);
					}
				}
			}
		}
	}

}
