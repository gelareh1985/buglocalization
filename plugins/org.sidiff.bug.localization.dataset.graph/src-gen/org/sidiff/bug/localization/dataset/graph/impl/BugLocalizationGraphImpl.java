/**
 */
package org.sidiff.bug.localization.dataset.graph.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraph;
import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage;
import org.sidiff.bug.localization.dataset.graph.BugReportNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Bug Localization Graph</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.impl.BugLocalizationGraphImpl#getReports <em>Reports</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BugLocalizationGraphImpl extends MinimalEObjectImpl.Container implements BugLocalizationGraph {
	/**
	 * The cached value of the '{@link #getReports() <em>Reports</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReports()
	 * @generated
	 * @ordered
	 */
	protected EList<BugReportNode> reports;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BugLocalizationGraphImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BugLocalizationGraphPackage.Literals.BUG_LOCALIZATION_GRAPH;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<BugReportNode> getReports() {
		if (reports == null) {
			reports = new EObjectContainmentEList<BugReportNode>(BugReportNode.class, this,
					BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH__REPORTS);
		}
		return reports;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH__REPORTS:
			return ((InternalEList<?>) getReports()).basicRemove(otherEnd, msgs);
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
		case BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH__REPORTS:
			return getReports();
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
		case BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH__REPORTS:
			getReports().clear();
			getReports().addAll((Collection<? extends BugReportNode>) newValue);
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
		case BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH__REPORTS:
			getReports().clear();
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
		case BugLocalizationGraphPackage.BUG_LOCALIZATION_GRAPH__REPORTS:
			return reports != null && !reports.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //BugLocalizationGraphImpl
