/**
 */
package org.sidiff.bug.localization.dataset.systemmodel;

import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage
 * @generated
 */
public interface SystemModelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SystemModelFactory eINSTANCE = org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>System Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>System Model</em>'.
	 * @generated
	 */
	SystemModel createSystemModel();

	/**
	 * <!-- begin-user-doc -->
	 * 
	 * @param uri              The system model file. If the file exists it will be
	 *                         loaded; otherwise a new resource will be created.
	 * @param resolveResources <code>true</code> if all contained resources should
	 *                         be loaded; <code>false</code> if not. This is
	 *                         important if a system model consist of multiple
	 *                         models that are stored by cross-resource
	 *                         containments. For example, change locations might
	 *                         point into a resource but its parent resource will
	 *                         not be loaded automatically.
	 * @return <!-- end-user-doc -->
	 * @generated NOT
	 */
	SystemModel createSystemModel(URI uri, boolean resolveResources);
	
	/**
	 * <!-- begin-user-doc -->
	 * 
	 * @param resourceSet      The resource set used for loading all resources.
	 * @param uri              The system model file. If the file exists it will be
	 *                         loaded; otherwise a new resource will be created.
	 * @param resolveResources <code>true</code> if all contained resources should
	 *                         be loaded; <code>false</code> if not. This is
	 *                         important if a system model consist of multiple
	 *                         models that are stored by cross-resource
	 *                         containments. For example, change locations might
	 *                         point into a resource but its parent resource will
	 *                         not be loaded automatically.
	 * @return <!-- end-user-doc -->
	 * @generated NOT
	 */
	SystemModel createSystemModel(ResourceSet resourceSet, URI uri, boolean resolveResources);

	/**
	 * <!-- begin-user-doc -->
	 * 
	 * @param file             The system model file. If the file exists it will be
	 *                         loaded; otherwise a new resource will be created.
	 * @param resolveResources <code>true</code> if all contained resources should
	 *                         be loaded; <code>false</code> if not. This is
	 *                         important if a system model consist of multiple
	 *                         models that are stored by cross-resource
	 *                         containments. For example, change locations might
	 *                         point into a resource but its parent resource will
	 *                         not be loaded automatically.
	 * @return <!-- end-user-doc -->
	 * @generated NOT
	 */
	SystemModel createSystemModel(Path file, boolean resolveResources);

	/**
	 * Returns a new object of class '<em>View</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>View</em>'.
	 * @generated
	 */
	View createView();

	/**
	 * Returns a new object of class '<em>Change</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Change</em>'.
	 * @generated
	 */
	Change createChange();

	/**
	 * Returns a new object of class '<em>View Description</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>View Description</em>'.
	 * @generated
	 */
	ViewDescription createViewDescription();
	
	/**
	 * Returns a new object of class '<em>Version</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Version</em>'.
	 * @generated
	 */
	Version createVersion();

	/**
	 * Returns a new object of class '<em>Bug Report</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Bug Report</em>'.
	 * @generated
	 */
	BugReport createBugReport();

	/**
	 * Returns a new object of class '<em>Traced Version</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Traced Version</em>'.
	 * @generated
	 */
	TracedVersion createTracedVersion();

	/**
	 * Returns a new object of class '<em>Bug Report Comment</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Bug Report Comment</em>'.
	 * @generated
	 */
	BugReportComment createBugReportComment();

	/**
	 * Returns a new object of class '<em>File Change</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>File Change</em>'.
	 * @generated
	 */
	FileChange createFileChange();

	/**
	 * Returns a new object of class '<em>Traced Bug Report</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Traced Bug Report</em>'.
	 * @generated
	 */
	TracedBugReport createTracedBugReport();

	/**
	 * Returns a new object of class '<em>View Description</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>View Description</em>'.
	 * @generated NOT
	 */
	ViewDescription createViewDescription(String name, String description, String viewKind);

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	SystemModelPackage getSystemModelPackage();

} //SystemModelFactory
