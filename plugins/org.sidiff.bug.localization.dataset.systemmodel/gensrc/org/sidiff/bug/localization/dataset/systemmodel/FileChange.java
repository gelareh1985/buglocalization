/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>File Change</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.FileChange#getType <em>Type</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.FileChange#getLocation <em>Location</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getFileChange()
 * @model
 * @generated
 */
public interface FileChange extends EObject {

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.sidiff.bug.localization.dataset.systemmodel.ChangeType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
	 * @see #setType(ChangeType)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getFileChange_Type()
	 * @model
	 * @generated
	 */
	ChangeType getType();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.FileChange#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
	 * @see #getType()
	 * @generated
	 */
	void setType(ChangeType value);

	/**
	 * Returns the value of the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Location</em>' attribute.
	 * @see #setLocation(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getFileChange_Location()
	 * @model
	 * @generated
	 */
	String getLocation();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.FileChange#getLocation <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Location</em>' attribute.
	 * @see #getLocation()
	 * @generated
	 */
	void setLocation(String value);
} // FileChange
