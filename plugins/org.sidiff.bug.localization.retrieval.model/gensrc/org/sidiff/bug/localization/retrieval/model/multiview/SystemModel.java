/**
 */
package org.sidiff.bug.localization.retrieval.model.multiview;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.retrieval.model.multiview.SystemModel#getViews <em>Views</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.retrieval.model.multiview.MultiviewPackage#getSystemModel()
 * @model
 * @generated
 */
public interface SystemModel extends DescribableElement {
	/**
	 * Returns the value of the '<em><b>Views</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.retrieval.model.multiview.View}.
	 * It is bidirectional and its opposite is '{@link org.sidiff.bug.localization.retrieval.model.multiview.View#getSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Views</em>' containment reference list.
	 * @see org.sidiff.bug.localization.retrieval.model.multiview.MultiviewPackage#getSystemModel_Views()
	 * @see org.sidiff.bug.localization.retrieval.model.multiview.View#getSystem
	 * @model opposite="system" containment="true"
	 * @generated
	 */
	EList<View> getViews();

} // SystemModel
