/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Change</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getType <em>Type</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getQuantification <em>Quantification</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getLocation <em>Location</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getOriginalResource <em>Original Resource</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getChange()
 * @model
 * @generated
 */
public interface Change extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The default value is <code>"Add"</code>.
	 * The literals are from the enumeration {@link org.sidiff.bug.localization.dataset.systemmodel.ChangeType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
	 * @see #setType(ChangeType)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getChange_Type()
	 * @model default="Add"
	 * @generated
	 */
	ChangeType getType();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
	 * @see #getType()
	 * @generated
	 */
	void setType(ChangeType value);

	/**
	 * Returns the value of the '<em><b>Quantification</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Quantification</em>' attribute.
	 * @see #setQuantification(int)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getChange_Quantification()
	 * @model
	 * @generated
	 */
	int getQuantification();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getQuantification <em>Quantification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Quantification</em>' attribute.
	 * @see #getQuantification()
	 * @generated
	 */
	void setQuantification(int value);

	/**
	 * Returns the value of the '<em><b>Location</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Location</em>' reference.
	 * @see #setLocation(EObject)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getChange_Location()
	 * @model
	 * @generated
	 */
	EObject getLocation();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getLocation <em>Location</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Location</em>' reference.
	 * @see #getLocation()
	 * @generated
	 */
	void setLocation(EObject value);

	/**
	 * Returns the value of the '<em><b>Original Resource</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Original Resource</em>' attribute.
	 * @see #setOriginalResource(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getChange_OriginalResource()
	 * @model
	 * @generated
	 */
	String getOriginalResource();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getOriginalResource <em>Original Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Original Resource</em>' attribute.
	 * @see #getOriginalResource()
	 * @generated
	 */
	void setOriginalResource(String value);

} // Change
