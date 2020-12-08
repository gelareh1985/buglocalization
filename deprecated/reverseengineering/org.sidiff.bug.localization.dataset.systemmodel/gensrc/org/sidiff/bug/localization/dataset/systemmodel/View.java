/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>View</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.View#getSystem <em>System</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.View#getDocumentType <em>Document Type</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.View#getKind <em>Kind</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.View#getModel <em>Model</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.View#getChanges <em>Changes</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getView()
 * @model
 * @generated
 */
public interface View extends DescribableElement {
	/**
	 * Returns the value of the '<em><b>System</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViews <em>Views</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System</em>' container reference.
	 * @see #setSystem(SystemModel)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getView_System()
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViews
	 * @model opposite="views" transient="false"
	 * @generated
	 */
	SystemModel getSystem();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getSystem <em>System</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System</em>' container reference.
	 * @see #getSystem()
	 * @generated
	 */
	void setSystem(SystemModel value);

	/**
	 * Returns the value of the '<em><b>Document Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Document Type</em>' attribute.
	 * @see #setDocumentType(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getView_DocumentType()
	 * @model
	 * @generated
	 */
	String getDocumentType();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getDocumentType <em>Document Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Document Type</em>' attribute.
	 * @see #getDocumentType()
	 * @generated
	 */
	void setDocumentType(String value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see #setKind(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getView_Kind()
	 * @model
	 * @generated
	 */
	String getKind();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see #getKind()
	 * @generated
	 */
	void setKind(String value);

	/**
	 * Returns the value of the '<em><b>Model</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' reference.
	 * @see #setModel(EObject)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getView_Model()
	 * @model
	 * @generated
	 */
	EObject getModel();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getModel <em>Model</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(EObject value);

	/**
	 * Returns the value of the '<em><b>Changes</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.systemmodel.Change}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Changes</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getView_Changes()
	 * @model containment="true"
	 * @generated
	 */
	EList<Change> getChanges();

} // View
