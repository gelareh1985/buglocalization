/**
 */
package org.sidiff.bug.localization.dataset.graph;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bug Report Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getId <em>Id</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getSummary <em>Summary</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getLocations <em>Locations</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getComments <em>Comments</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportNode()
 * @model
 * @generated
 */
public interface BugReportNode extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(int)
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportNode_Id()
	 * @model
	 * @generated
	 */
	int getId();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(int value);

	/**
	 * Returns the value of the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Summary</em>' attribute.
	 * @see #setSummary(String)
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportNode_Summary()
	 * @model
	 * @generated
	 */
	String getSummary();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.graph.BugReportNode#getSummary <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Summary</em>' attribute.
	 * @see #getSummary()
	 * @generated
	 */
	void setSummary(String value);

	/**
	 * Returns the value of the '<em><b>Comments</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.graph.BugReportCommentNode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comments</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportNode_Comments()
	 * @model containment="true"
	 * @generated
	 */
	EList<BugReportCommentNode> getComments();

	/**
	 * Returns the value of the '<em><b>Locations</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EObject}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Locations</em>' reference list.
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportNode_Locations()
	 * @model
	 * @generated
	 */
	EList<EObject> getLocations();

} // BugReportNode
