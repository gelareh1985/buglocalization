package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Operation;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class BodyBlockToFunctionBehavior extends JavaToUML<Block, Behavior, FunctionBehavior> {

	public String OPERATION_CALLS = "calling";
	
	@Override
	public void apply(Block block) {
		FunctionBehavior umlFunctionBehavior = umlFactory.createFunctionBehavior();
		
		Interaction umlInteraction = umlFactory.createInteraction();
		umlInteraction.setName(OPERATION_CALLS);
		umlFunctionBehavior.getOwnedBehaviors().add(umlInteraction);
		
		trafo.createModelElementFragment(block, umlFunctionBehavior);
	}
	
	public Interaction getInteraction(Behavior operationBehavior) {
		Behavior operationCalls = operationBehavior.getOwnedBehavior(OPERATION_CALLS);
		
		if (operationCalls instanceof Interaction) {
			return (Interaction) operationCalls;
		}
		
		return null;
	}

	@Override
	public void apply(Behavior classifierBehavior, FunctionBehavior operationBehavior) {
		classifierBehavior.getOwnedBehaviors().add(operationBehavior);
	}

	@Override
	public void link(Block block, FunctionBehavior operationBehavior) throws ClassNotFoundException {
		ASTNode parentNode = block.getParent();
		
		if (parentNode instanceof MethodDeclaration) {
			Operation umlOperation = trafo.getModelElement(block.getParent());
			
			if (umlOperation != null) {
				operationBehavior.setName(umlOperation.getLabel());
			}
		}
	}

}
