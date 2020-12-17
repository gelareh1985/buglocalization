/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.sidiff.bug.localization.dataset.systemmodel.FileChange;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;
import org.sidiff.bug.localization.dataset.systemmodel.TracedBugReport;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Traced Bug Report</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.TracedBugReportImpl#getCodeLocations <em>Code Locations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TracedBugReportImpl extends BugReportImpl implements TracedBugReport {
	/**
	 * The cached value of the '{@link #getCodeLocations() <em>Code Locations</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCodeLocations()
	 * @generated
	 * @ordered
	 */
	protected EList<FileChange> codeLocations;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TracedBugReportImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SystemModelPackage.Literals.TRACED_BUG_REPORT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FileChange> getCodeLocations() {
		if (codeLocations == null) {
			codeLocations = new EObjectContainmentEList<FileChange>(FileChange.class, this, SystemModelPackage.TRACED_BUG_REPORT__CODE_LOCATIONS);
		}
		return codeLocations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SystemModelPackage.TRACED_BUG_REPORT__CODE_LOCATIONS:
				return ((InternalEList<?>)getCodeLocations()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SystemModelPackage.TRACED_BUG_REPORT__CODE_LOCATIONS:
				return getCodeLocations();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SystemModelPackage.TRACED_BUG_REPORT__CODE_LOCATIONS:
				getCodeLocations().clear();
				getCodeLocations().addAll((Collection<? extends FileChange>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case SystemModelPackage.TRACED_BUG_REPORT__CODE_LOCATIONS:
				getCodeLocations().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case SystemModelPackage.TRACED_BUG_REPORT__CODE_LOCATIONS:
				return codeLocations != null && !codeLocations.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //TracedBugReportImpl
