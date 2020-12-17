/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getViews <em>Views</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getVersion <em>Version</em>}</li>
 * </ul>
 *
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getSystemModel()
 * @model
 * @generated
 */
public interface SystemModel extends DescribableElement {
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String FILE_EXTENSION = "systemmodel";
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String NS_URI =  "http://www.sidiff.org/systemmodel/1.0";

	/**
	 * Returns the value of the '<em><b>Views</b></em>' containment reference list.
	 * The list contents are of type {@link org.sidiff.bug.localization.dataset.systemmodel.View}.
	 * It is bidirectional and its opposite is '{@link org.sidiff.bug.localization.dataset.systemmodel.View#getSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Views</em>' containment reference list.
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getSystemModel_Views()
	 * @see org.sidiff.bug.localization.dataset.systemmodel.View#getSystem
	 * @model opposite="system" containment="true"
	 * @generated
	 */
	EList<View> getViews();

	/**
	 * Returns the value of the '<em><b>Version</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Version</em>' containment reference.
	 * @see #setVersion(Version)
	 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage#getSystemModel_Version()
	 * @model containment="true"
	 * @generated
	 */
	Version getVersion();

	/**
	 * Sets the value of the '{@link org.sidiff.bug.localization.dataset.systemmodel.SystemModel#getVersion <em>Version</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' containment reference.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(Version value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addView(Resource resource, ViewDescription viewDescription);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	View getViewByKind(ViewDescription viewDescription);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	View getViewByKind(String viewDescription);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean containsViewKind(ViewDescription viewDescription);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean removeViewKind(ViewDescription viewDescription);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	void setURI(URI uri);
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	void save();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	void saveAll(Map<Object, Object> options);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	void saveAll(Map<Object, Object> options, Set<Resource> exclude);

} // SystemModel
