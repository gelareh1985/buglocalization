package org.sidiff.bug.localization.dataset.systemmodel.views;

public class ViewDescriptions {

	public static ViewDescription JAVA_AST = new ViewDescription("Java AST", "Java EMF-based AST", "javamodel");
	
	public static ViewDescription UML_CLASS_DIAGRAM = new ViewDescription("UML Class Diagram", "Classes", "uml.class");
	
	public static ViewDescription UML_CLASS_OPERATION_CONTROL_FLOW = new ViewDescription("UML Activity Diagram", "Operation Control Flow Graph", "cfg.uml.activity");

	public static ViewDescription[] ALL_VIEWS = {JAVA_AST, UML_CLASS_DIAGRAM, UML_CLASS_OPERATION_CONTROL_FLOW};
	
}
