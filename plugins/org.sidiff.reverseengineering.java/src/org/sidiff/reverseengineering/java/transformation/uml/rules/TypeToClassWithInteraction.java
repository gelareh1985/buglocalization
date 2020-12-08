package org.sidiff.reverseengineering.java.transformation.uml.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Operation;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class TypeToClassWithInteraction extends JavaToUML<TypeDeclaration, Class, FunctionBehavior> {

	@Override
	public void apply(TypeDeclaration typeDeclaration) {
		Class umlClass = trafo.getModelElement(typeDeclaration);

		// Create classifier behavior for operation behavior modeling:
		if (umlClass != null) {
			FunctionBehavior umlFunctionBehavior = umlFactory.createFunctionBehavior();
			umlFunctionBehavior.setName(umlClass.getName());
			umlClass.setClassifierBehavior(umlFunctionBehavior);
		}
	}

	@Override
	public void apply(Class umlClass, FunctionBehavior modelNode) {
	}

	@Override
	public void link(TypeDeclaration javaNode, FunctionBehavior modelNode) throws ClassNotFoundException {
	}

	@Override
	public void finalizing(List<EObject> rootElements) {
		if (trafo.getSettings().isIncludeMethodBodies()) {
			for (EObject element : rootElements) {
				finalizeClass(element);
			}
		}
	}

	protected void finalizeClass(EObject modelElement) {
		if (modelElement instanceof Class) {
			Class umlClass = (Class) modelElement;
			
			if (umlClass.getClassifierBehavior() != null) {
				for (Iterator<Behavior> behaviorIterator = umlClass.getClassifierBehavior().getOwnedBehaviors().iterator(); behaviorIterator.hasNext();) {
					Behavior operationBehavior = behaviorIterator.next();
					Interaction operationCalls = rules.blockToFunctionBehavior.getInteraction(operationBehavior);

					if (operationCalls.getActions().isEmpty()) {
						behaviorIterator.remove(); // clean up empty operation bodies
					} else {
						Set<Operation> calledOperations = new HashSet<>();

						for (Iterator<Action> actionIterator = operationCalls.getActions().iterator(); actionIterator.hasNext();) {
							Action operationCallAction = (Action) actionIterator.next();

							if (operationCallAction instanceof CallOperationAction) {
								Operation calledOperation = ((CallOperationAction) operationCallAction).getOperation();

								if (calledOperations.contains(calledOperation)) {
									actionIterator.remove(); // remove duplicated operation call
								} else {
									calledOperations.add(calledOperation);
								}
							}
						}
					}
				}
				
				if (umlClass.getClassifierBehavior().getOwnedBehaviors().isEmpty()) {
					// clean up classes without operation (calls)
					EcoreUtil.remove(umlClass.getClassifierBehavior());
				} 
			}

			// finalize nested classes:
			for (Classifier nestedClassifier : umlClass.getNestedClassifiers()) {
				finalizeClass(nestedClassifier);
			}
		}
	}
}
