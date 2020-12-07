package org.sidiff.reverseengineering.java.transformation.uml.rulebase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.sidiff.reverseengineering.java.transformation.uml.JavaASTTransformationUML;

public abstract class JavaToUML<JN, UC, UN> {

	protected static UMLFactory umlFactory = UMLFactory.eINSTANCE;

	protected static UMLPackage umlPackage = UMLPackage.eINSTANCE;

	protected JavaASTTransformationUML trafo;
	
	protected JavaToUMLRules rules;
	
	protected Map<EObject, Object> trace;
	
	public JavaToUMLRules getRules() {
		return rules;
	}

	public void init(JavaASTTransformationUML trafo, JavaToUMLRules rules) {
		this.trafo = trafo;
		this.rules = rules;
	}
	
	public void cleanUp() {
		// avoids resource leaks
		this.trafo = null;
		this.rules = null;
		this.trace = null;
	}
	
	public JavaASTTransformationUML getTransformation() {
		return trafo;
	}
	
	@SuppressWarnings("unchecked")
	public <E> E getFragment(EObject modelElement) {
		return (E) trace.get(modelElement);
	}
	
	public void addFragment(EObject main, Object child) {
		
		if (trace == null) {
			this.trace = new HashMap<>();
		}
		
		trace.put(main, child);
	}
	
	/*
	 * 1. Create model graph nodes (+ local attributes)
	 * 2. Create model AST containments
	 *    Create model graph edges (+ none local attribute value)
	 */
	
	/**
	 * Transforms the Java AST node to a corresponding model element (fragment).
	 * 
	 * @param javaNode The Java AST node.
	 */
	public abstract void apply(JN javaNode);

	/**
	 * Creates the containment of the model node.
	 * 
	 * @param modelContainer The model container.
	 * @param modelNode      The contained model element.
	 */
	public abstract void apply(UC modelContainer, UN modelNode);

	/**
	 * Creates connections (references or attributes) to other model elements in the
	 * transformation.
	 * 
	 * @param javaNode  The Java AST node.
	 * @param modelNode The corresponding model element.
	 * @throws ClassNotFoundException A type binding that could not be resolved.
	 */
	public abstract void link(JN javaNode, UN modelNode) throws ClassNotFoundException;
	
	/**
	 * Optional final processing step.
	 * 
	 * @param rootElements All root elements of the transformation.
	 */
	public void finalizing(List<EObject> rootElements) {}
}
