/**
 */
package org.sidiff.bug.localization.dataset.graph;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage
 * @generated
 */
public interface BugLocalizationGraphFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	BugLocalizationGraphFactory eINSTANCE = org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphFactoryImpl
			.init();

	/**
	 * Returns a new object of class '<em>Bug Localization Graph</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Bug Localization Graph</em>'.
	 * @generated
	 */
	BugLocalizationGraph createBugLocalizationGraph();

	/**
	 * Returns a new object of class '<em>Bug Report Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Bug Report Node</em>'.
	 * @generated
	 */
	BugReportNode createBugReportNode();

	/**
	 * Returns a new object of class '<em>Bug Report Comment Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Bug Report Comment Node</em>'.
	 * @generated
	 */
	BugReportCommentNode createBugReportCommentNode();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	BugLocalizationGraphPackage getBugLocalizationGraphPackage();

} //BugLocalizationGraphFactory
