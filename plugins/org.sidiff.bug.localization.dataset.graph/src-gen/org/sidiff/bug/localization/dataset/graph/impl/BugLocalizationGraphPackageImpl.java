/**
 */
package org.sidiff.bug.localization.dataset.graph.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph;
import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphFactory;
import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage;
import org.sidiff.bug.localization.dataset.graph.BugReportNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class BugLocalizationGraphPackageImpl extends EPackageImpl implements BugLocalizationGraphPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bugLocalizationGraphEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bugReportNodeEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private BugLocalizationGraphPackageImpl() {
		super(eNS_URI, BugLocalizationGraphFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link BugLocalizationGraphPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static BugLocalizationGraphPackage init() {
		if (isInited)
			return (BugLocalizationGraphPackage) EPackage.Registry.INSTANCE
					.getEPackage(BugLocalizationGraphPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredBugLocalizationGraphPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		BugLocalizationGraphPackageImpl theBugLocalizationGraphPackage = registeredBugLocalizationGraphPackage instanceof BugLocalizationGraphPackageImpl
				? (BugLocalizationGraphPackageImpl) registeredBugLocalizationGraphPackage
				: new BugLocalizationGraphPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theBugLocalizationGraphPackage.createPackageContents();

		// Initialize created meta-data
		theBugLocalizationGraphPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theBugLocalizationGraphPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(BugLocalizationGraphPackage.eNS_URI, theBugLocalizationGraphPackage);
		return theBugLocalizationGraphPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBugLocalizationGraph() {
		return bugLocalizationGraphEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBugLocalizationGraph_Reports() {
		return (EReference) bugLocalizationGraphEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBugReportNode() {
		return bugReportNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReportNode_Id() {
		return (EAttribute) bugReportNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReportNode_Summary() {
		return (EAttribute) bugReportNodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReportNode_Comments() {
		return (EAttribute) bugReportNodeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBugReportNode_Locations() {
		return (EReference) bugReportNodeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BugLocalizationGraphFactory getBugLocalizationGraphFactory() {
		return (BugLocalizationGraphFactory) getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated)
			return;
		isCreated = true;

		// Create classes and their features
		bugLocalizationGraphEClass = createEClass(BUG_LOCALIZATION_GRAPH);
		createEReference(bugLocalizationGraphEClass, BUG_LOCALIZATION_GRAPH__REPORTS);

		bugReportNodeEClass = createEClass(BUG_REPORT_NODE);
		createEAttribute(bugReportNodeEClass, BUG_REPORT_NODE__ID);
		createEAttribute(bugReportNodeEClass, BUG_REPORT_NODE__SUMMARY);
		createEAttribute(bugReportNodeEClass, BUG_REPORT_NODE__COMMENTS);
		createEReference(bugReportNodeEClass, BUG_REPORT_NODE__LOCATIONS);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized)
			return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		EcorePackage theEcorePackage = (EcorePackage) EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(bugLocalizationGraphEClass, BugLocalizationGraph.class, "BugLocalizationGraph", !IS_ABSTRACT,
				!IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBugLocalizationGraph_Reports(), this.getBugReportNode(), null, "reports", null, 0, -1,
				BugLocalizationGraph.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE,
				!IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(bugReportNodeEClass, BugReportNode.class, "BugReportNode", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBugReportNode_Id(), ecorePackage.getEInt(), "id", null, 0, 1, BugReportNode.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReportNode_Summary(), ecorePackage.getEString(), "summary", null, 0, 1,
				BugReportNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReportNode_Comments(), theEcorePackage.getEString(), "comments", null, 0, -1,
				BugReportNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEReference(getBugReportNode_Locations(), theEcorePackage.getEObject(), null, "locations", null, 0, -1,
				BugReportNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //BugLocalizationGraphPackageImpl
