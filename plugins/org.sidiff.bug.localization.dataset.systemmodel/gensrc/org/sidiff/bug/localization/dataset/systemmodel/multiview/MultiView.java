/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.multiview;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Multi View</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView#getViews <em>Views</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getMultiView()
 * @model
 * @generated
 */
public interface MultiView extends DescribableElement {
	/**
	 * Returns the value of the '<em><b>Views</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View}.
	 * It is bidirectional and its opposite is '{@link org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Views</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage#getMultiView_Views()
	 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.View#getSystem
	 * @model opposite="system" containment="true"
	 * @generated
	 */
	EList<View> getViews();

} // MultiView
