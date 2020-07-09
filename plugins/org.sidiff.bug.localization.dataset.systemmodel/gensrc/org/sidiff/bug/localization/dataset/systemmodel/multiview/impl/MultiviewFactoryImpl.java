/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.multiview.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MultiviewFactoryImpl extends EFactoryImpl implements MultiviewFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static MultiviewFactory init() {
		try {
			MultiviewFactory theMultiviewFactory = (MultiviewFactory)EPackage.Registry.INSTANCE.getEFactory(MultiviewPackage.eNS_URI);
			if (theMultiviewFactory != null) {
				return theMultiviewFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new MultiviewFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MultiviewFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case MultiviewPackage.MULTI_VIEW: return createMultiView();
			case MultiviewPackage.VIEW: return createView();
			case MultiviewPackage.DESCRIBABLE_ELEMENT: return createDescribableElement();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MultiView createMultiView() {
		MultiViewImpl multiView = new MultiViewImpl();
		return multiView;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public View createView() {
		ViewImpl view = new ViewImpl();
		return view;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DescribableElement createDescribableElement() {
		DescribableElementImpl describableElement = new DescribableElementImpl();
		return describableElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MultiviewPackage getMultiviewPackage() {
		return (MultiviewPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static MultiviewPackage getPackage() {
		return MultiviewPackage.eINSTANCE;
	}

} //MultiviewFactoryImpl
