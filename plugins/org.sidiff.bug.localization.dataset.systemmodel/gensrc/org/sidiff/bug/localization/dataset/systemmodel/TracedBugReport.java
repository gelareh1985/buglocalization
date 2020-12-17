/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Traced Bug Report</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.TracedBugReport#getCodeLocations <em>Code Locations</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getTracedBugReport()
 * @model
 * @generated
 */
public interface TracedBugReport extends BugReport {
	/**
	 * Returns the value of the '<em><b>Code Locations</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.systemmodel.FileChange}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Code Locations</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getTracedBugReport_CodeLocations()
	 * @model containment="true"
	 * @generated
	 */
	EList<FileChange> getCodeLocations();

} // TracedBugReport
