/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.multiview;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewPackage
 * @generated
 */
public interface MultiviewFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MultiviewFactory eINSTANCE = org.sidiff.bug.localization.dataset.systemmodel.multiview.impl.MultiviewFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Multi View</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Multi View</em>'.
	 * @generated
	 */
	MultiView createMultiView();

	/**
	 * Returns a new object of class '<em>View</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>View</em>'.
	 * @generated
	 */
	View createView();

	/**
	 * Returns a new object of class '<em>Describable Element</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Describable Element</em>'.
	 * @generated
	 */
	DescribableElement createDescribableElement();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	MultiviewPackage getMultiviewPackage();

} //MultiviewFactory
