package org.sidiff.reverseengineering.java.transformation.uml.rules;

import java.util.logging.Level;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Operation;
import org.sidiff.reverseengineering.java.Activator;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class MethodInvocationToCallOperationAction extends JavaToUML<MethodInvocation, FunctionBehavior, CallOperationAction> {

	@Override
	public void apply(MethodInvocation methodInvocation) {
		CallOperationAction callOperation = umlFactory.createCallOperationAction();
		trafo.createModelElementFragment(methodInvocation, callOperation);
	}

	@Override
	public void apply(FunctionBehavior operationBehavior, CallOperationAction callOperation) {
		Interaction operationCalls = rules.blockToFunctionBehavior.getInteraction(operationBehavior); 
		
		if (operationCalls != null) {
			operationCalls.getActions().add(callOperation);
		}
	}

	@Override
	public void link(MethodInvocation methodInvocation, CallOperationAction callOperation) throws ClassNotFoundException {
		IMethodBinding invokedMethodBinding = methodInvocation.resolveMethodBinding();
		
		if (invokedMethodBinding != null) {
			Operation umlOperationProxy = trafo.resolveBindingProxy(invokedMethodBinding, umlPackage.getOperation());
			
			if (umlOperationProxy != null) {
				callOperation.setOperation(umlOperationProxy);
				callOperation.setName(invokedMethodBinding.getName());
			}
		} else {
			if (Activator.getLogger().isLoggable(Level.FINE)) {
				Activator.getLogger().log(Level.FINE, "No method binding for invocation: " + methodInvocation);
			}
		}
	}

}
