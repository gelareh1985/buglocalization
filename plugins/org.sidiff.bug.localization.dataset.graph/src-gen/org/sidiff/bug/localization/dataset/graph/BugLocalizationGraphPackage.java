/**
 */
package org.sidiff.bug.localization.dataset.graph;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
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
 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphFactory
 * @model kind="package"
 * @generated
 */
public interface BugLocalizationGraphPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "graph";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.sidiff.org/buglocalizationgraph/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "BugLocalizationGraph";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	BugLocalizationGraphPackage eINSTANCE = org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphPackageImpl
			.init();

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphImpl <em>Bug Localization Graph</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphImpl
	 * @see org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphPackageImpl#getBugLocalizationGraph()
	 * @generated
	 */
	int BUG_LOCALIZATION_GRAPH = 0;

	/**
	 * The feature id for the '<em><b>Reports</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_LOCALIZATION_GRAPH__REPORTS = 0;

	/**
	 * The number of structural features of the '<em>Bug Localization Graph</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_LOCALIZATION_GRAPH_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Bug Localization Graph</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_LOCALIZATION_GRAPH_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl <em>Bug Report Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl
	 * @see org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphPackageImpl#getBugReportNode()
	 * @generated
	 */
	int BUG_REPORT_NODE = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_REPORT_NODE__ID = 0;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_REPORT_NODE__SUMMARY = 1;

	/**
	 * The feature id for the '<em><b>Comments</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_REPORT_NODE__COMMENTS = 2;

	/**
	 * The feature id for the '<em><b>Locations</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_REPORT_NODE__LOCATIONS = 3;

	/**
	 * The number of structural features of the '<em>Bug Report Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_REPORT_NODE_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Bug Report Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUG_REPORT_NODE_OPERATION_COUNT = 0;

	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph <em>Bug Localization Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bug Localization Graph</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph
	 * @generated
	 */
	EClass getBugLocalizationGraph();

	/**
	 * Returns the meta object for the containment reference list '{@link org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph#getReports <em>Reports</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Reports</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph#getReports()
	 * @see #getBugLocalizationGraph()
	 * @generated
	 */
	EReference getBugLocalizationGraph_Reports();

	/**
	 * Returns the meta object for class '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode <em>Bug Report Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bug Report Node</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugReportNode
	 * @generated
	 */
	EClass getBugReportNode();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugReportNode#getId()
	 * @see #getBugReportNode()
	 * @generated
	 */
	EAttribute getBugReportNode_Id();

	/**
	 * Returns the meta object for the attribute '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getSummary <em>Summary</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Summary</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugReportNode#getSummary()
	 * @see #getBugReportNode()
	 * @generated
	 */
	EAttribute getBugReportNode_Summary();

	/**
	 * Returns the meta object for the attribute list '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getComments <em>Comments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Comments</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugReportNode#getComments()
	 * @see #getBugReportNode()
	 * @generated
	 */
	EAttribute getBugReportNode_Comments();

	/**
	 * Returns the meta object for the reference list '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getLocations <em>Locations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Locations</em>'.
	 * @see org.sidiff.bug.localization.dataset.graph.BugReportNode#getLocations()
	 * @see #getBugReportNode()
	 * @generated
	 */
	EReference getBugReportNode_Locations();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	BugLocalizationGraphFactory getBugLocalizationGraphFactory();

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
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphImpl <em>Bug Localization Graph</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphImpl
		 * @see org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphPackageImpl#getBugLocalizationGraph()
		 * @generated
		 */
		EClass BUG_LOCALIZATION_GRAPH = eINSTANCE.getBugLocalizationGraph();

		/**
		 * The meta object literal for the '<em><b>Reports</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUG_LOCALIZATION_GRAPH__REPORTS = eINSTANCE.getBugLocalizationGraph_Reports();

		/**
		 * The meta object literal for the '{@link org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl <em>Bug Report Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl
		 * @see org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphPackageImpl#getBugReportNode()
		 * @generated
		 */
		EClass BUG_REPORT_NODE = eINSTANCE.getBugReportNode();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BUG_REPORT_NODE__ID = eINSTANCE.getBugReportNode_Id();

		/**
		 * The meta object literal for the '<em><b>Summary</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BUG_REPORT_NODE__SUMMARY = eINSTANCE.getBugReportNode_Summary();

		/**
		 * The meta object literal for the '<em><b>Comments</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BUG_REPORT_NODE__COMMENTS = eINSTANCE.getBugReportNode_Comments();

		/**
		 * The meta object literal for the '<em><b>Locations</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUG_REPORT_NODE__LOCATIONS = eINSTANCE.getBugReportNode_Locations();

	}

} //BugLocalizationGraphPackage
