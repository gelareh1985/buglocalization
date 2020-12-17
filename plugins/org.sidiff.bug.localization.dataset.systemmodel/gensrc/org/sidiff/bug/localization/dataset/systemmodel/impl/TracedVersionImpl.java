/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;
import org.sidiff.bug.localization.dataset.systemmodel.TracedVersion;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Traced Version</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.TracedVersionImpl#getCodeVersionID <em>Code Version ID</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TracedVersionImpl extends VersionImpl implements TracedVersion {
	/**
	 * The default value of the '{@link #getCodeVersionID() <em>Code Version ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCodeVersionID()
	 * @generated
	 * @ordered
	 */
	protected static final String CODE_VERSION_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCodeVersionID() <em>Code Version ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCodeVersionID()
	 * @generated
	 * @ordered
	 */
	protected String codeVersionID = CODE_VERSION_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TracedVersionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SystemModelPackage.Literals.TRACED_VERSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCodeVersionID() {
		return codeVersionID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCodeVersionID(String newCodeVersionID) {
		String oldCodeVersionID = codeVersionID;
		codeVersionID = newCodeVersionID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.TRACED_VERSION__CODE_VERSION_ID, oldCodeVersionID, codeVersionID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SystemModelPackage.TRACED_VERSION__CODE_VERSION_ID:
				return getCodeVersionID();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SystemModelPackage.TRACED_VERSION__CODE_VERSION_ID:
				setCodeVersionID((String)newValue);
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
			case SystemModelPackage.TRACED_VERSION__CODE_VERSION_ID:
				setCodeVersionID(CODE_VERSION_ID_EDEFAULT);
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
			case SystemModelPackage.TRACED_VERSION__CODE_VERSION_ID:
				return CODE_VERSION_ID_EDEFAULT == null ? codeVersionID != null : !CODE_VERSION_ID_EDEFAULT.equals(codeVersionID);
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
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (codeVersionID: ");
		result.append(codeVersionID);
		result.append(')');
		return result.toString();
	}

} //TracedVersionImpl
