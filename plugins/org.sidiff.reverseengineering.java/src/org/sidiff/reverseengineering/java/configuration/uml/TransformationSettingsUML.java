package org.sidiff.reverseengineering.java.configuration.uml;

import org.sidiff.reverseengineering.java.configuration.TransformationSettings;

public class TransformationSettingsUML extends TransformationSettings {

	/**
	 * List< String > -> String[0..*] {ordered=true, unique=false}<br>
	 * Set< String > -> String[0..*] {ordered=false, unique=true} LinkedHashSet< String >,<br>
	 * TreeSet< String > -> String[0..*] {ordered=true, unique=true}<br>
	 * Raw Type List, Set -> Object[0..*] ...<br>
	 */
	private boolean javaCollectionsToMultivaluedType = true;

	public boolean isJavaCollectionsToMultivaluedType() {
		return javaCollectionsToMultivaluedType;
	}

	public void setJavaCollectionsToMultivaluedType(boolean javaCollectionsToMultivaluedType) {
		this.javaCollectionsToMultivaluedType = javaCollectionsToMultivaluedType;
	}
}
