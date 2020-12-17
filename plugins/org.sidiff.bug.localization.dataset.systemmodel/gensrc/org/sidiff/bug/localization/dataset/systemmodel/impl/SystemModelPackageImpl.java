/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.sidiff.bug.localization.dataset.systemmodel.BugReport;
import org.sidiff.bug.localization.dataset.systemmodel.BugReportComment;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.DescribableElement;
import org.sidiff.bug.localization.dataset.systemmodel.FileChange;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;
import org.sidiff.bug.localization.dataset.systemmodel.TracedBugReport;
import org.sidiff.bug.localization.dataset.systemmodel.TracedVersion;
import org.sidiff.bug.localization.dataset.systemmodel.Version;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.ViewDescription;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SystemModelPackageImpl extends EPackageImpl implements SystemModelPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass systemModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass viewEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass describableElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass changeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass viewDescriptionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass versionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bugReportEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tracedVersionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bugReportCommentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fileChangeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tracedBugReportEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum changeTypeEEnum = null;

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
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SystemModelPackageImpl() {
		super(eNS_URI, SystemModelFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link SystemModelPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SystemModelPackage init() {
		if (isInited) return (SystemModelPackage)EPackage.Registry.INSTANCE.getEPackage(SystemModelPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredSystemModelPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		SystemModelPackageImpl theSystemModelPackage = registeredSystemModelPackage instanceof SystemModelPackageImpl ? (SystemModelPackageImpl)registeredSystemModelPackage : new SystemModelPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theSystemModelPackage.createPackageContents();

		// Initialize created meta-data
		theSystemModelPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theSystemModelPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SystemModelPackage.eNS_URI, theSystemModelPackage);
		return theSystemModelPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSystemModel() {
		return systemModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemModel_Views() {
		return (EReference)systemModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemModel_Version() {
		return (EReference)systemModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemModel__AddView__Resource_ViewDescription() {
		return systemModelEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemModel__GetViewByKind__ViewDescription() {
		return systemModelEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemModel__GetViewByKind__String() {
		return systemModelEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemModel__ContainsViewKind__ViewDescription() {
		return systemModelEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemModel__RemoveViewKind__ViewDescription() {
		return systemModelEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getView() {
		return viewEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getView_System() {
		return (EReference)viewEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getView_DocumentType() {
		return (EAttribute)viewEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getView_Kind() {
		return (EAttribute)viewEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getView_Model() {
		return (EReference)viewEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getView_Changes() {
		return (EReference)viewEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDescribableElement() {
		return describableElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDescribableElement_Name() {
		return (EAttribute)describableElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDescribableElement_Description() {
		return (EAttribute)describableElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getChange() {
		return changeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getChange_Type() {
		return (EAttribute)changeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getChange_Quantification() {
		return (EAttribute)changeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getChange_Location() {
		return (EReference)changeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getChange_OriginalResource() {
		return (EAttribute)changeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getViewDescription() {
		return viewDescriptionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getViewDescription_ViewKind() {
		return (EAttribute)viewDescriptionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVersion() {
		return versionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_ModelVersionID() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_Date() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_Author() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_CommitMessage() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_FixedVersion() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_BuggyVersion() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVersion_Bugreport() {
		return (EReference)versionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBugReport() {
		return bugReportEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Id() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Product() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Component() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_CreationTime() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Creator() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_AssignedTo() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Severity() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Resolution() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Status() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReport_Summary() {
		return (EAttribute)bugReportEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBugReport_Comments() {
		return (EReference)bugReportEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBugReport_ModelLocations() {
		return (EReference)bugReportEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTracedVersion() {
		return tracedVersionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTracedVersion_CodeVersionID() {
		return (EAttribute)tracedVersionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBugReportComment() {
		return bugReportCommentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReportComment_CreationTime() {
		return (EAttribute)bugReportCommentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReportComment_Creator() {
		return (EAttribute)bugReportCommentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBugReportComment_Text() {
		return (EAttribute)bugReportCommentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFileChange() {
		return fileChangeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFileChange_Type() {
		return (EAttribute)fileChangeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFileChange_Location() {
		return (EAttribute)fileChangeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTracedBugReport() {
		return tracedBugReportEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTracedBugReport_CodeLocations() {
		return (EReference)tracedBugReportEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getChangeType() {
		return changeTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemModelFactory getSystemModelFactory() {
		return (SystemModelFactory)getEFactoryInstance();
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
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		systemModelEClass = createEClass(SYSTEM_MODEL);
		createEReference(systemModelEClass, SYSTEM_MODEL__VIEWS);
		createEReference(systemModelEClass, SYSTEM_MODEL__VERSION);
		createEOperation(systemModelEClass, SYSTEM_MODEL___ADD_VIEW__RESOURCE_VIEWDESCRIPTION);
		createEOperation(systemModelEClass, SYSTEM_MODEL___GET_VIEW_BY_KIND__VIEWDESCRIPTION);
		createEOperation(systemModelEClass, SYSTEM_MODEL___GET_VIEW_BY_KIND__STRING);
		createEOperation(systemModelEClass, SYSTEM_MODEL___CONTAINS_VIEW_KIND__VIEWDESCRIPTION);
		createEOperation(systemModelEClass, SYSTEM_MODEL___REMOVE_VIEW_KIND__VIEWDESCRIPTION);

		viewEClass = createEClass(VIEW);
		createEReference(viewEClass, VIEW__SYSTEM);
		createEAttribute(viewEClass, VIEW__DOCUMENT_TYPE);
		createEAttribute(viewEClass, VIEW__KIND);
		createEReference(viewEClass, VIEW__MODEL);
		createEReference(viewEClass, VIEW__CHANGES);

		describableElementEClass = createEClass(DESCRIBABLE_ELEMENT);
		createEAttribute(describableElementEClass, DESCRIBABLE_ELEMENT__NAME);
		createEAttribute(describableElementEClass, DESCRIBABLE_ELEMENT__DESCRIPTION);

		changeEClass = createEClass(CHANGE);
		createEAttribute(changeEClass, CHANGE__TYPE);
		createEAttribute(changeEClass, CHANGE__QUANTIFICATION);
		createEReference(changeEClass, CHANGE__LOCATION);
		createEAttribute(changeEClass, CHANGE__ORIGINAL_RESOURCE);

		viewDescriptionEClass = createEClass(VIEW_DESCRIPTION);
		createEAttribute(viewDescriptionEClass, VIEW_DESCRIPTION__VIEW_KIND);

		versionEClass = createEClass(VERSION);
		createEAttribute(versionEClass, VERSION__MODEL_VERSION_ID);
		createEAttribute(versionEClass, VERSION__DATE);
		createEAttribute(versionEClass, VERSION__AUTHOR);
		createEAttribute(versionEClass, VERSION__COMMIT_MESSAGE);
		createEAttribute(versionEClass, VERSION__FIXED_VERSION);
		createEAttribute(versionEClass, VERSION__BUGGY_VERSION);
		createEReference(versionEClass, VERSION__BUGREPORT);

		bugReportEClass = createEClass(BUG_REPORT);
		createEAttribute(bugReportEClass, BUG_REPORT__ID);
		createEAttribute(bugReportEClass, BUG_REPORT__PRODUCT);
		createEAttribute(bugReportEClass, BUG_REPORT__COMPONENT);
		createEAttribute(bugReportEClass, BUG_REPORT__CREATION_TIME);
		createEAttribute(bugReportEClass, BUG_REPORT__CREATOR);
		createEAttribute(bugReportEClass, BUG_REPORT__ASSIGNED_TO);
		createEAttribute(bugReportEClass, BUG_REPORT__SEVERITY);
		createEAttribute(bugReportEClass, BUG_REPORT__RESOLUTION);
		createEAttribute(bugReportEClass, BUG_REPORT__STATUS);
		createEAttribute(bugReportEClass, BUG_REPORT__SUMMARY);
		createEReference(bugReportEClass, BUG_REPORT__COMMENTS);
		createEReference(bugReportEClass, BUG_REPORT__MODEL_LOCATIONS);

		tracedVersionEClass = createEClass(TRACED_VERSION);
		createEAttribute(tracedVersionEClass, TRACED_VERSION__CODE_VERSION_ID);

		bugReportCommentEClass = createEClass(BUG_REPORT_COMMENT);
		createEAttribute(bugReportCommentEClass, BUG_REPORT_COMMENT__CREATION_TIME);
		createEAttribute(bugReportCommentEClass, BUG_REPORT_COMMENT__CREATOR);
		createEAttribute(bugReportCommentEClass, BUG_REPORT_COMMENT__TEXT);

		fileChangeEClass = createEClass(FILE_CHANGE);
		createEAttribute(fileChangeEClass, FILE_CHANGE__TYPE);
		createEAttribute(fileChangeEClass, FILE_CHANGE__LOCATION);

		tracedBugReportEClass = createEClass(TRACED_BUG_REPORT);
		createEReference(tracedBugReportEClass, TRACED_BUG_REPORT__CODE_LOCATIONS);

		// Create enums
		changeTypeEEnum = createEEnum(CHANGE_TYPE);
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
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		systemModelEClass.getESuperTypes().add(this.getDescribableElement());
		viewEClass.getESuperTypes().add(this.getDescribableElement());
		viewDescriptionEClass.getESuperTypes().add(this.getDescribableElement());
		tracedVersionEClass.getESuperTypes().add(this.getVersion());
		tracedBugReportEClass.getESuperTypes().add(this.getBugReport());

		// Initialize classes, features, and operations; add parameters
		initEClass(systemModelEClass, SystemModel.class, "SystemModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSystemModel_Views(), this.getView(), this.getView_System(), "views", null, 0, -1, SystemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSystemModel_Version(), this.getVersion(), null, "version", null, 0, 1, SystemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = initEOperation(getSystemModel__AddView__Resource_ViewDescription(), null, "addView", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theEcorePackage.getEResource(), "resource", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getViewDescription(), "viewDescription", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getSystemModel__GetViewByKind__ViewDescription(), this.getView(), "getViewByKind", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getViewDescription(), "viewDescription", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getSystemModel__GetViewByKind__String(), this.getView(), "getViewByKind", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theEcorePackage.getEString(), "viewDescription", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getSystemModel__ContainsViewKind__ViewDescription(), theEcorePackage.getEBoolean(), "containsViewKind", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getViewDescription(), "viewDescription", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getSystemModel__RemoveViewKind__ViewDescription(), theEcorePackage.getEBoolean(), "removeViewKind", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getViewDescription(), "viewDescription", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(viewEClass, View.class, "View", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getView_System(), this.getSystemModel(), this.getSystemModel_Views(), "system", null, 0, 1, View.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getView_DocumentType(), ecorePackage.getEString(), "documentType", null, 0, 1, View.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getView_Kind(), ecorePackage.getEString(), "kind", null, 0, 1, View.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getView_Model(), theEcorePackage.getEObject(), null, "model", null, 0, 1, View.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getView_Changes(), this.getChange(), null, "changes", null, 0, -1, View.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(describableElementEClass, DescribableElement.class, "DescribableElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDescribableElement_Name(), ecorePackage.getEString(), "name", null, 0, 1, DescribableElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDescribableElement_Description(), ecorePackage.getEString(), "description", null, 0, 1, DescribableElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(changeEClass, Change.class, "Change", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getChange_Type(), this.getChangeType(), "type", "Add", 0, 1, Change.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getChange_Quantification(), theEcorePackage.getEInt(), "quantification", null, 0, 1, Change.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getChange_Location(), theEcorePackage.getEObject(), null, "location", null, 0, 1, Change.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getChange_OriginalResource(), theEcorePackage.getEString(), "originalResource", null, 0, 1, Change.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(viewDescriptionEClass, ViewDescription.class, "ViewDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getViewDescription_ViewKind(), theEcorePackage.getEString(), "viewKind", null, 0, 1, ViewDescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(versionEClass, Version.class, "Version", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getVersion_ModelVersionID(), theEcorePackage.getEString(), "modelVersionID", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_Date(), theEcorePackage.getEString(), "date", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_Author(), theEcorePackage.getEString(), "author", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_CommitMessage(), theEcorePackage.getEString(), "commitMessage", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_FixedVersion(), theEcorePackage.getEBoolean(), "fixedVersion", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_BuggyVersion(), theEcorePackage.getEBoolean(), "buggyVersion", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getVersion_Bugreport(), this.getBugReport(), null, "bugreport", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(bugReportEClass, BugReport.class, "BugReport", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBugReport_Id(), theEcorePackage.getEInt(), "id", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Product(), theEcorePackage.getEString(), "product", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Component(), theEcorePackage.getEString(), "component", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_CreationTime(), theEcorePackage.getEString(), "creationTime", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Creator(), theEcorePackage.getEString(), "creator", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_AssignedTo(), theEcorePackage.getEString(), "assignedTo", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Severity(), theEcorePackage.getEString(), "severity", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Resolution(), theEcorePackage.getEString(), "resolution", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Status(), theEcorePackage.getEString(), "status", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReport_Summary(), theEcorePackage.getEString(), "summary", null, 0, 1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBugReport_Comments(), this.getBugReportComment(), null, "comments", null, 0, -1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBugReport_ModelLocations(), this.getChange(), null, "modelLocations", null, 0, -1, BugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tracedVersionEClass, TracedVersion.class, "TracedVersion", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTracedVersion_CodeVersionID(), theEcorePackage.getEString(), "codeVersionID", null, 0, 1, TracedVersion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(bugReportCommentEClass, BugReportComment.class, "BugReportComment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBugReportComment_CreationTime(), theEcorePackage.getEString(), "creationTime", null, 0, 1, BugReportComment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReportComment_Creator(), theEcorePackage.getEString(), "creator", null, 0, 1, BugReportComment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBugReportComment_Text(), theEcorePackage.getEString(), "text", null, 0, 1, BugReportComment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fileChangeEClass, FileChange.class, "FileChange", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFileChange_Type(), this.getChangeType(), "type", null, 0, 1, FileChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFileChange_Location(), theEcorePackage.getEString(), "location", null, 0, 1, FileChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tracedBugReportEClass, TracedBugReport.class, "TracedBugReport", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTracedBugReport_CodeLocations(), this.getFileChange(), null, "codeLocations", null, 0, -1, TracedBugReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(changeTypeEEnum, ChangeType.class, "ChangeType");
		addEEnumLiteral(changeTypeEEnum, ChangeType.ADD);
		addEEnumLiteral(changeTypeEEnum, ChangeType.DELETE);
		addEEnumLiteral(changeTypeEEnum, ChangeType.MODIFY);

		// Create resource
		createResource(eNS_URI);
	}

} //SystemModelPackageImpl
