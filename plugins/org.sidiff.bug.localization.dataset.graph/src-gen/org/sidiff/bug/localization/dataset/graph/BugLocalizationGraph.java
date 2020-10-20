/**
 */
package org.sidiff.bug.localization.dataset.graph;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bug Localization Graph</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph#getReports <em>Reports</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugLocalizationGraph()
 * @model
 * @generated
 */
public interface BugLocalizationGraph extends EObject {
	/**
	 * Returns the value of the '<em><b>Reports</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.graph.BugReportNode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reports</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugLocalizationGraph_Reports()
	 * @model containment="true"
	 * @generated
	 */
	EList<BugReportNode> getReports();

} // BugLocalizationGraph
