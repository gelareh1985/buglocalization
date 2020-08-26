/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory
 * @model kind="package"
 * @generated
 */
public interface SystemModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "systemmodel";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.sidiff.org/systemmodel/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "SystemModel";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SystemModelPackage eINSTANCE = org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.DescribableElementImpl <em>Describable Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.DescribableElementImpl
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getDescribableElement()
	 * @generated
	 */
	int DESCRIBABLE_ELEMENT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIBABLE_ELEMENT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIBABLE_ELEMENT__DESCRIPTION = 1;

	/**
	 * The number of structural features of the '<em>Describable Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIBABLE_ELEMENT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Describable Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIBABLE_ELEMENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelImpl <em>System Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelImpl
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getSystemModel()
	 * @generated
	 */
	int SYSTEM_MODEL = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL__NAME = DESCRIBABLE_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL__DESCRIPTION = DESCRIBABLE_ELEMENT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Views</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL__VIEWS = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>System Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL_FEATURE_COUNT = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Add View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL___ADD_VIEW__RESOURCE_VIEWDESCRIPTION = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get View By Kind</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL___GET_VIEW_BY_KIND__VIEWDESCRIPTION = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Contains View Kind</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL___CONTAINS_VIEW_KIND__VIEWDESCRIPTION = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>Remove View Kind</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL___REMOVE_VIEW_KIND__VIEWDESCRIPTION = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 3;

	/**
	 * The number of operations of the '<em>System Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_MODEL_OPERATION_COUNT = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ViewImpl <em>View</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.ViewImpl
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getView()
	 * @generated
	 */
	int VIEW = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__NAME = DESCRIBABLE_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__DESCRIPTION = DESCRIBABLE_ELEMENT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>System</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__SYSTEM = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Document Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__DOCUMENT_TYPE = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__KIND = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Model</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__MODEL = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Changes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW__CHANGES = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_FEATURE_COUNT = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_OPERATION_COUNT = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl <em>Change</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getChange()
	 * @generated
	 */
	int CHANGE = 3;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHANGE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Quantification</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHANGE__QUANTIFICATION = 1;

	/**
	 * The feature id for the '<em><b>Location</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHANGE__LOCATION = 2;

	/**
	 * The number of structural features of the '<em>Change</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHANGE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Change</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHANGE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ViewDescriptionImpl <em>View Description</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.ViewDescriptionImpl
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getViewDescription()
	 * @generated
	 */
	int VIEW_DESCRIPTION = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_DESCRIPTION__NAME = DESCRIBABLE_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_DESCRIPTION__DESCRIPTION = DESCRIBABLE_ELEMENT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>View Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_DESCRIPTION__VIEW_KIND = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>View Description</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_DESCRIPTION_FEATURE_COUNT = DESCRIBABLE_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>View Description</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIEW_DESCRIPTION_OPERATION_COUNT = DESCRIBABLE_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.systemmodel.ChangeType <em>Change Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
	 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getChangeType()
	 * @generated
	 */
	int CHANGE_TYPE = 5;


	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel <em>System Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>System Model</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel
	 * @generated
	 */
	EClass getSystemModel();

	/**
	 * Returns the meta object for the containment reference list '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViews <em>Views</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Views</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViews()
	 * @see #getSystemModel()
	 * @generated
	 */
	EReference getSystemModel_Views();

	/**
	 * Returns the meta object for the '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#addView(org.eclipse.emf.ecore.resource.Resource, org.sidiff.bug.localization.dataset.systemmodel.ViewDescription) <em>Add View</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Add View</em>' operation.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel#addView(org.eclipse.emf.ecore.resource.Resource, org.sidiff.bug.localization.dataset.systemmodel.ViewDescription)
	 * @generated
	 */
	EOperation getSystemModel__AddView__Resource_ViewDescription();

	/**
	 * Returns the meta object for the '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViewByKind(org.sidiff.bug.localization.dataset.systemmodel.ViewDescription) <em>Get View By Kind</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get View By Kind</em>' operation.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViewByKind(org.sidiff.bug.localization.dataset.systemmodel.ViewDescription)
	 * @generated
	 */
	EOperation getSystemModel__GetViewByKind__ViewDescription();

	/**
	 * Returns the meta object for the '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#containsViewKind(org.sidiff.bug.localization.dataset.systemmodel.ViewDescription) <em>Contains View Kind</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Contains View Kind</em>' operation.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel#containsViewKind(org.sidiff.bug.localization.dataset.systemmodel.ViewDescription)
	 * @generated
	 */
	EOperation getSystemModel__ContainsViewKind__ViewDescription();

	/**
	 * Returns the meta object for the '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#removeViewKind(org.sidiff.bug.localization.dataset.systemmodel.ViewDescription) <em>Remove View Kind</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Remove View Kind</em>' operation.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModel#removeViewKind(org.sidiff.bug.localization.dataset.systemmodel.ViewDescription)
	 * @generated
	 */
	EOperation getSystemModel__RemoveViewKind__ViewDescription();

	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.systemmodel.View <em>View</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>View</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View
	 * @generated
	 */
	EClass getView();

	/**
	 * Returns the meta object for the container reference '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>System</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View#getSystem()
	 * @see #getView()
	 * @generated
	 */
	EReference getView_System();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getDocumentType <em>Document Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Document Type</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View#getDocumentType()
	 * @see #getView()
	 * @generated
	 */
	EAttribute getView_DocumentType();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View#getKind()
	 * @see #getView()
	 * @generated
	 */
	EAttribute getView_Kind();

	/**
	 * Returns the meta object for the reference '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Model</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View#getModel()
	 * @see #getView()
	 * @generated
	 */
	EReference getView_Model();

	/**
	 * Returns the meta object for the containment reference list '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getChanges <em>Changes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Changes</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View#getChanges()
	 * @see #getView()
	 * @generated
	 */
	EReference getView_Changes();

	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.systemmodel.DescribableElement <em>Describable Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Describable Element</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.DescribableElement
	 * @generated
	 */
	EClass getDescribableElement();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.DescribableElement#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.DescribableElement#getName()
	 * @see #getDescribableElement()
	 * @generated
	 */
	EAttribute getDescribableElement_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.DescribableElement#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.DescribableElement#getDescription()
	 * @see #getDescribableElement()
	 * @generated
	 */
	EAttribute getDescribableElement_Description();

	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.systemmodel.Change <em>Change</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Change</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.Change
	 * @generated
	 */
	EClass getChange();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.Change#getType()
	 * @see #getChange()
	 * @generated
	 */
	EAttribute getChange_Type();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getQuantification <em>Quantification</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Quantification</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.Change#getQuantification()
	 * @see #getChange()
	 * @generated
	 */
	EAttribute getChange_Quantification();

	/**
	 * Returns the meta object for the reference '{@link org.sidiff.bug.localization.dataset.systemmodel.Change#getLocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Location</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.Change#getLocation()
	 * @see #getChange()
	 * @generated
	 */
	EReference getChange_Location();

	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.systemmodel.ViewDescription <em>View Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>View Description</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ViewDescription
	 * @generated
	 */
	EClass getViewDescription();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.systemmodel.ViewDescription#getViewKind <em>View Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>View Kind</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ViewDescription#getViewKind()
	 * @see #getViewDescription()
	 * @generated
	 */
	EAttribute getViewDescription_ViewKind();

	/**
	 * Returns the meta object for enum '{@link org.sidiff.bug.localization.dataset.systemmodel.ChangeType <em>Change Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Change Type</em>'.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
	 * @generated
	 */
	EEnum getChangeType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SystemModelFactory getSystemModelFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelImpl <em>System Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelImpl
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getSystemModel()
		 * @generated
		 */
		EClass SYSTEM_MODEL = eINSTANCE.getSystemModel();

		/**
		 * The meta object literal for the '<em><b>Views</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SYSTEM_MODEL__VIEWS = eINSTANCE.getSystemModel_Views();

		/**
		 * The meta object literal for the '<em><b>Add View</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_MODEL___ADD_VIEW__RESOURCE_VIEWDESCRIPTION = eINSTANCE.getSystemModel__AddView__Resource_ViewDescription();

		/**
		 * The meta object literal for the '<em><b>Get View By Kind</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_MODEL___GET_VIEW_BY_KIND__VIEWDESCRIPTION = eINSTANCE.getSystemModel__GetViewByKind__ViewDescription();

		/**
		 * The meta object literal for the '<em><b>Contains View Kind</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_MODEL___CONTAINS_VIEW_KIND__VIEWDESCRIPTION = eINSTANCE.getSystemModel__ContainsViewKind__ViewDescription();

		/**
		 * The meta object literal for the '<em><b>Remove View Kind</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_MODEL___REMOVE_VIEW_KIND__VIEWDESCRIPTION = eINSTANCE.getSystemModel__RemoveViewKind__ViewDescription();

		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ViewImpl <em>View</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.ViewImpl
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getView()
		 * @generated
		 */
		EClass VIEW = eINSTANCE.getView();

		/**
		 * The meta object literal for the '<em><b>System</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIEW__SYSTEM = eINSTANCE.getView_System();

		/**
		 * The meta object literal for the '<em><b>Document Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VIEW__DOCUMENT_TYPE = eINSTANCE.getView_DocumentType();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VIEW__KIND = eINSTANCE.getView_Kind();

		/**
		 * The meta object literal for the '<em><b>Model</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIEW__MODEL = eINSTANCE.getView_Model();

		/**
		 * The meta object literal for the '<em><b>Changes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIEW__CHANGES = eINSTANCE.getView_Changes();

		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.DescribableElementImpl <em>Describable Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.DescribableElementImpl
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getDescribableElement()
		 * @generated
		 */
		EClass DESCRIBABLE_ELEMENT = eINSTANCE.getDescribableElement();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DESCRIBABLE_ELEMENT__NAME = eINSTANCE.getDescribableElement_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DESCRIBABLE_ELEMENT__DESCRIPTION = eINSTANCE.getDescribableElement_Description();

		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl <em>Change</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getChange()
		 * @generated
		 */
		EClass CHANGE = eINSTANCE.getChange();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CHANGE__TYPE = eINSTANCE.getChange_Type();

		/**
		 * The meta object literal for the '<em><b>Quantification</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CHANGE__QUANTIFICATION = eINSTANCE.getChange_Quantification();

		/**
		 * The meta object literal for the '<em><b>Location</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CHANGE__LOCATION = eINSTANCE.getChange_Location();

		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ViewDescriptionImpl <em>View Description</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.ViewDescriptionImpl
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getViewDescription()
		 * @generated
		 */
		EClass VIEW_DESCRIPTION = eINSTANCE.getViewDescription();

		/**
		 * The meta object literal for the '<em><b>View Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VIEW_DESCRIPTION__VIEW_KIND = eINSTANCE.getViewDescription_ViewKind();

		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.systemmodel.ChangeType <em>Change Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.systemmodel.ChangeType
		 * @see org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelPackageImpl#getChangeType()
		 * @generated
		 */
		EEnum CHANGE_TYPE = eINSTANCE.getChangeType();

	}

} //SystemModelPackage
