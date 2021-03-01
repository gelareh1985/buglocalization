/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bug Report</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getId <em>Id</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getProduct <em>Product</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getComponent <em>Component</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getCreationTime <em>Creation Time</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getCreator <em>Creator</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getAssignedTo <em>Assigned To</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getResolution <em>Resolution</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getStatus <em>Status</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getSummary <em>Summary</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getComments <em>Comments</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getModelLocations <em>Model Locations</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getBugfixTime <em>Bugfix Time</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getBugfixCommit <em>Bugfix Commit</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport()
 * @model
 * @generated
 */
public interface BugReport extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(int)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Id()
	 * @model
	 * @generated
	 */
	int getId();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(int value);

	/**
	 * Returns the value of the '<em><b>Product</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Product</em>' attribute.
	 * @see #setProduct(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Product()
	 * @model
	 * @generated
	 */
	String getProduct();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getProduct <em>Product</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Product</em>' attribute.
	 * @see #getProduct()
	 * @generated
	 */
	void setProduct(String value);

	/**
	 * Returns the value of the '<em><b>Component</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Component</em>' attribute.
	 * @see #setComponent(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Component()
	 * @model
	 * @generated
	 */
	String getComponent();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getComponent <em>Component</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Component</em>' attribute.
	 * @see #getComponent()
	 * @generated
	 */
	void setComponent(String value);

	/**
	 * Returns the value of the '<em><b>Creation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Creation Time</em>' attribute.
	 * @see #setCreationTime(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_CreationTime()
	 * @model
	 * @generated
	 */
	String getCreationTime();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getCreationTime <em>Creation Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Creation Time</em>' attribute.
	 * @see #getCreationTime()
	 * @generated
	 */
	void setCreationTime(String value);

	/**
	 * Returns the value of the '<em><b>Creator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Creator</em>' attribute.
	 * @see #setCreator(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Creator()
	 * @model
	 * @generated
	 */
	String getCreator();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getCreator <em>Creator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Creator</em>' attribute.
	 * @see #getCreator()
	 * @generated
	 */
	void setCreator(String value);

	/**
	 * Returns the value of the '<em><b>Assigned To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assigned To</em>' attribute.
	 * @see #setAssignedTo(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_AssignedTo()
	 * @model
	 * @generated
	 */
	String getAssignedTo();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getAssignedTo <em>Assigned To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Assigned To</em>' attribute.
	 * @see #getAssignedTo()
	 * @generated
	 */
	void setAssignedTo(String value);

	/**
	 * Returns the value of the '<em><b>Severity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Severity</em>' attribute.
	 * @see #setSeverity(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Severity()
	 * @model
	 * @generated
	 */
	String getSeverity();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getSeverity <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Severity</em>' attribute.
	 * @see #getSeverity()
	 * @generated
	 */
	void setSeverity(String value);

	/**
	 * Returns the value of the '<em><b>Resolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Resolution</em>' attribute.
	 * @see #setResolution(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Resolution()
	 * @model
	 * @generated
	 */
	String getResolution();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getResolution <em>Resolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Resolution</em>' attribute.
	 * @see #getResolution()
	 * @generated
	 */
	void setResolution(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see #setStatus(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Status()
	 * @model
	 * @generated
	 */
	String getStatus();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(String value);

	/**
	 * Returns the value of the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Summary</em>' attribute.
	 * @see #setSummary(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Summary()
	 * @model
	 * @generated
	 */
	String getSummary();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getSummary <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Summary</em>' attribute.
	 * @see #getSummary()
	 * @generated
	 */
	void setSummary(String value);

	/**
	 * Returns the value of the '<em><b>Comments</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.systemmodel.BugReportComment}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comments</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_Comments()
	 * @model containment="true"
	 * @generated
	 */
	EList<BugReportComment> getComments();

	/**
	 * Returns the value of the '<em><b>Model Locations</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.systemmodel.Change}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model Locations</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_ModelLocations()
	 * @model containment="true"
	 * @generated
	 */
	EList<Change> getModelLocations();

	/**
	 * Returns the value of the '<em><b>Bugfix Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bugfix Time</em>' attribute.
	 * @see #setBugfixTime(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_BugfixTime()
	 * @model
	 * @generated
	 */
	String getBugfixTime();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getBugfixTime <em>Bugfix Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bugfix Time</em>' attribute.
	 * @see #getBugfixTime()
	 * @generated
	 */
	void setBugfixTime(String value);

	/**
	 * Returns the value of the '<em><b>Bugfix Commit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bugfix Commit</em>' attribute.
	 * @see #setBugfixCommit(String)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getBugReport_BugfixCommit()
	 * @model
	 * @generated
	 */
	String getBugfixCommit();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.BugReport#getBugfixCommit <em>Bugfix Commit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bugfix Commit</em>' attribute.
	 * @see #getBugfixCommit()
	 * @generated
	 */
	void setBugfixCommit(String value);

} // BugReport
