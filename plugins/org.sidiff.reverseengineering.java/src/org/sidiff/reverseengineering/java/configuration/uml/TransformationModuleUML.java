package org.sidiff.reverseengineering.java.configuration.uml;

import org.sidiff.reverseengineering.java.configuration.TransformationModule;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUMLHelper;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUMLRules;

public class TransformationModuleUML extends TransformationModule {

	private TransformationSettingsUML settingsUML;
	
	public TransformationModuleUML(TransformationSettingsUML settings) {
		super(settings, new TransformationDomainUML());
		this.settingsUML = settings;
	}
	
	@Override
	protected void configure() {
		super.configure();
		configureSettingsUML();
		bindJavaToUMLRules();
		bindJavaToUMLHelper();
	}
	
	/*
	 * Instances:
	 */
	
	protected void configureSettingsUML() {
		bind(TransformationSettingsUML.class).toInstance(settingsUML);
	}
	
	/*
	 * Type Bindings:
	 */
	
	protected void bindJavaToUMLRules() {
		bind(JavaToUMLRules.class);
	}
	
	protected void bindJavaToUMLHelper() {
		bind(JavaToUMLHelper.class);
	}
	
}
