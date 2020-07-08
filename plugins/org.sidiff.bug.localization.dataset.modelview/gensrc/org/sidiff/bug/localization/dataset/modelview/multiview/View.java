/**
 */
package org.sidiff.bug.localization.dataset.modelview.multiview;

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
 *   <li>{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getSystem <em>System</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getDocumentType <em>Document Type</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getKind <em>Kind</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getModel <em>Model</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.modelview.multiview.MultiviewPackage#getView()
 * @model
 * @generated
 */
public interface View extends DescribableElement {
	/**
	 * Returns the value of the '<em><b>System</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.sidiff.bug.localization.dataset.modelview.multiview.SystemModel#getViews <em>Views</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System</em>' container reference.
	 * @see #setSystem(SystemModel)
	 * @see org.sidiff.bug.localization.dataset.modelview.multiview.MultiviewPackage#getView_System()
	 * @see org.sidiff.bug.localization.dataset.modelview.multiview.SystemModel#getViews
	 * @model opposite="views" transient="false"
	 * @generated
	 */
	SystemModel getSystem();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getSystem <em>System</em>}' container reference.
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
	 * @see org.sidiff.bug.localization.dataset.modelview.multiview.MultiviewPackage#getView_DocumentType()
	 * @model
	 * @generated
	 */
	String getDocumentType();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getDocumentType <em>Document Type</em>}' attribute.
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
	 * @see org.sidiff.bug.localization.dataset.modelview.multiview.MultiviewPackage#getView_Kind()
	 * @model
	 * @generated
	 */
	String getKind();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getKind <em>Kind</em>}' attribute.
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
	 * @see org.sidiff.bug.localization.dataset.modelview.multiview.MultiviewPackage#getView_Model()
	 * @model
	 * @generated
	 */
	EObject getModel();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.modelview.multiview.View#getModel <em>Model</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(EObject value);

} // View
