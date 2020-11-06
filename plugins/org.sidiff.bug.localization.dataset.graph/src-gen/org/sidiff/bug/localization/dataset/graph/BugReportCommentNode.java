/**
 */
package org.sidiff.bug.localization.dataset.graph;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bug Report Comment Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.BugReportCommentNode#getComment <em>Comment</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportCommentNode()
 * @model
 * @generated
 */
public interface BugReportCommentNode extends EObject {
	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage#getBugReportCommentNode_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.graph.BugReportCommentNode#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

} // BugReportCommentNode
