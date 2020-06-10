/**
 */
package org.sidiff.bug.localization.retrieval.model.multiview;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Describable Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.retrieval.model.multiview.DescribableElement#getName <em>Name</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.retrieval.model.multiview.DescribableElement#getDescription <em>Description</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.retrieval.model.multiview.MultiviewPackage#getDescribableElement()
 * @model
 * @generated
 */
public interface DescribableElement extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.sidiff.bug.localization.retrieval.model.multiview.MultiviewPackage#getDescribableElement_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.retrieval.model.multiview.DescribableElement#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.sidiff.bug.localization.retrieval.model.multiview.MultiviewPackage#getDescribableElement_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.retrieval.model.multiview.DescribableElement#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

} // DescribableElement
