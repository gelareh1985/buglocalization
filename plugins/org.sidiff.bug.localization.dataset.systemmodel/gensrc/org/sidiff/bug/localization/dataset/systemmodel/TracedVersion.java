/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Traced Version</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.TracedVersion#getCodeVersionID <em>Code Version ID</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getTracedVersion()
 * @model
 * @generated
 */
public interface TracedVersion extends Version {
	/**
	 * Returns the value of the '<em><b>Code Version ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Code Version ID</em>' attribute.
	 * @see #setCodeVersionID(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getTracedVersion_CodeVersionID()
	 * @model
	 * @generated
	 */
	String getCodeVersionID();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.TracedVersion#getCodeVersionID <em>Code Version ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code Version ID</em>' attribute.
	 * @see #getCodeVersionID()
	 * @generated
	 */
	void setCodeVersionID(String value);

} // TracedVersion
