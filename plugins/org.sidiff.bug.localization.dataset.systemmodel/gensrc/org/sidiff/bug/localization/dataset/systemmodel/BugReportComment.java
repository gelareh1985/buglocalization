/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bug Report Comment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment#getCreationTime <em>Creation Time</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment#getCreator <em>Creator</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment#getText <em>Text</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReportComment()
 * @model
 * @generated
 */
public interface BugReportComment extends EObject {
	/**
	 * Returns the value of the '<em><b>Creation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Creation Time</em>' attribute.
	 * @see #setCreationTime(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReportComment_CreationTime()
	 * @model
	 * @generated
	 */
	String getCreationTime();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment#getCreationTime <em>Creation Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Creation Time</em>' attribute.
	 * @see #getCreationTime()
	 * @generated
	 */
	void setCreationTime(String value);

	/**
	 * Returns the value of the '<em><b>Creator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Creator</em>' attribute.
	 * @see #setCreator(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReportComment_Creator()
	 * @model
	 * @generated
	 */
	String getCreator();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment#getCreator <em>Creator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Creator</em>' attribute.
	 * @see #getCreator()
	 * @generated
	 */
	void setCreator(String value);

	/**
	 * Returns the value of the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Text</em>' attribute.
	 * @see #setText(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReportComment_Text()
	 * @model
	 * @generated
	 */
	String getText();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment#getText <em>Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Text</em>' attribute.
	 * @see #getText()
	 * @generated
	 */
	void setText(String value);

} // BugReportComment
