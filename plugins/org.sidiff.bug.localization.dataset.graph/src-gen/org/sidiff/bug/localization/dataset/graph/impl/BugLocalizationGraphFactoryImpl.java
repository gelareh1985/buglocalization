/**
 */
package org.sidiff.bug.localization.dataset.graph.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.sidiff.bug.localization.dataset.graph.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class BugLocalizationGraphFactoryImpl extends EFactoryImpl implements BugLocalizationGraphFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static BugLocalizationGraphFactory init() {
		try {
			BugLocalizationGraphFactory theBugLocalizationGraphFactory = (BugLocalizationGraphFactory) EPackage.Registry.INSTANCE
					.getEFactory(BugLocalizationGraphPackage.eNS_URI);
			if (theBugLocalizationGraphFactory != null) {
				return theBugLocalizationGraphFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new BugLocalizationGraphFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BugLocalizationGraphFactoryImpl() {
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
		case BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH:
			return createBugLocalizationGraph();
		case BugLocalizationGraphPackage.BUG_REPORT_NODE:
			return createBugReportNode();
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
	public BugLocalizationGraph createBugLocalizationGraph() {
		BugLocalizationGraphImpl bugLocalizationGraph = new BugLocalizationGraphImpl();
		return bugLocalizationGraph;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BugReportNode createBugReportNode() {
		BugReportNodeImpl bugReportNode = new BugReportNodeImpl();
		return bugReportNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BugLocalizationGraphPackage getBugLocalizationGraphPackage() {
		return (BugLocalizationGraphPackage) getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static BugLocalizationGraphPackage getPackage() {
		return BugLocalizationGraphPackage.eINSTANCE;
	}

} //BugLocalizationGraphFactoryImpl
