/**
 */
package org.sidiff.bug.localization.dataset.graph.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.emf.ecore.util.InternalEList;
import org.sidiff.bug.localization.dataset.graph.BugLocalizationGraphPackage;
import org.sidiff.bug.localization.dataset.graph.BugReportCommentNode;
import org.sidiff.bug.localization.dataset.graph.BugReportNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Bug Report Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl#getSummary <em>Summary</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl#getLocations <em>Locations</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.graph.impl.BugReportNodeImpl#getComments <em>Comments</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BugReportNodeImpl extends MinimalEObjectImpl.Container implements BugReportNode {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final int ID_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected int id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getSummary() <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSummary()
	 * @generated
	 * @ordered
	 */
	protected static final String SUMMARY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSummary() <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSummary()
	 * @generated
	 * @ordered
	 */
	protected String summary = SUMMARY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getLocations() <em>Locations</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocations()
	 * @generated
	 * @ordered
	 */
	protected EList<EObject> locations;

	/**
	 * The cached value of the '{@link #getComments() <em>Comments</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComments()
	 * @generated
	 * @ordered
	 */
	protected EList<BugReportCommentNode> comments;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BugReportNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BugLocalizationGraphPackage.Literals.BUG_REPORT_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(int newId) {
		int oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BugLocalizationGraphPackage.BUG_REPORT_NODE__ID,
					oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSummary() {
		return summary;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSummary(String newSummary) {
		String oldSummary = summary;
		summary = newSummary;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BugLocalizationGraphPackage.BUG_REPORT_NODE__SUMMARY,
					oldSummary, summary));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<BugReportCommentNode> getComments() {
		if (comments == null) {
			comments = new EObjectContainmentEList<BugReportCommentNode>(BugReportCommentNode.class, this,
					BugLocalizationGraphPackage.BUG_REPORT_NODE__COMMENTS);
		}
		return comments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__COMMENTS:
			return ((InternalEList<?>) getComments()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EObject> getLocations() {
		if (locations == null) {
			locations = new EObjectResolvingEList<EObject>(EObject.class, this,
					BugLocalizationGraphPackage.BUG_REPORT_NODE__LOCATIONS);
		}
		return locations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__ID:
			return getId();
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__SUMMARY:
			return getSummary();
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__LOCATIONS:
			return getLocations();
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__COMMENTS:
			return getComments();
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
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__ID:
			setId((Integer) newValue);
			return;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__SUMMARY:
			setSummary((String) newValue);
			return;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__LOCATIONS:
			getLocations().clear();
			getLocations().addAll((Collection<? extends EObject>) newValue);
			return;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__COMMENTS:
			getComments().clear();
			getComments().addAll((Collection<? extends BugReportCommentNode>) newValue);
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
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__ID:
			setId(ID_EDEFAULT);
			return;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__SUMMARY:
			setSummary(SUMMARY_EDEFAULT);
			return;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__LOCATIONS:
			getLocations().clear();
			return;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__COMMENTS:
			getComments().clear();
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
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__ID:
			return id != ID_EDEFAULT;
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__SUMMARY:
			return SUMMARY_EDEFAULT == null ? summary != null : !SUMMARY_EDEFAULT.equals(summary);
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__LOCATIONS:
			return locations != null && !locations.isEmpty();
		case BugLocalizationGraphPackage.BUG_REPORT_NODE__COMMENTS:
			return comments != null && !comments.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (id: ");
		result.append(id);
		result.append(", summary: ");
		result.append(summary);
		result.append(')');
		return result.toString();
	}

} //BugReportNodeImpl
