/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.multiview;

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
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getSystem <em>System</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getDocumentType <em>Document Type</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getKind <em>Kind</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getModel <em>Model</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getView()
 * @model
 * @generated
 */
public interface View extends DescribableElement {
	/**
	 * Returns the value of the '<em><b>System</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView#getViews <em>Views</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System</em>' container reference.
	 * @see #setSystem(MultiView)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getView_System()
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView#getViews
	 * @model opposite="views" transient="false"
	 * @generated
	 */
	MultiView getSystem();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getSystem <em>System</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System</em>' container reference.
	 * @see #getSystem()
	 * @generated
	 */
	void setSystem(MultiView value);

	/**
	 * Returns the value of the '<em><b>Document Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Document Type</em>' attribute.
	 * @see #setDocumentType(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getView_DocumentType()
	 * @model
	 * @generated
	 */
	String getDocumentType();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getDocumentType <em>Document Type</em>}' attribute.
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
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getView_Kind()
	 * @model
	 * @generated
	 */
	String getKind();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getKind <em>Kind</em>}' attribute.
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
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getView_Model()
	 * @model
	 * @generated
	 */
	EObject getModel();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getModel <em>Model</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(EObject value);

} // View
