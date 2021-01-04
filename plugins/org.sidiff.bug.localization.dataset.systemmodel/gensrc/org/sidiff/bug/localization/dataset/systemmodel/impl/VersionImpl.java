/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.sidiff.bug.localization.dataset.systemmodel.BugReport;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;
import org.sidiff.bug.localization.dataset.systemmodel.Version;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Version</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.VersionImpl#getModelVersionID <em>Model Version ID</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.VersionImpl#getDate <em>Date</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.VersionImpl#getAuthor <em>Author</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.VersionImpl#getCommitMessage <em>Commit Message</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.VersionImpl#getBugreport <em>Bugreport</em>}</li>
 * </ul>
 *
 * @generated
 */
public class VersionImpl extends MinimalEObjectImpl.Container implements Version {
	/**
	 * The default value of the '{@link #getModelVersionID() <em>Model Version ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelVersionID()
	 * @generated
	 * @ordered
	 */
	protected static final String MODEL_VERSION_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getModelVersionID() <em>Model Version ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelVersionID()
	 * @generated
	 * @ordered
	 */
	protected String modelVersionID = MODEL_VERSION_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getDate() <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDate()
	 * @generated
	 * @ordered
	 */
	protected static final String DATE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDate() <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDate()
	 * @generated
	 * @ordered
	 */
	protected String date = DATE_EDEFAULT;

	/**
	 * The default value of the '{@link #getAuthor() <em>Author</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAuthor()
	 * @generated
	 * @ordered
	 */
	protected static final String AUTHOR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAuthor() <em>Author</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAuthor()
	 * @generated
	 * @ordered
	 */
	protected String author = AUTHOR_EDEFAULT;

	/**
	 * The default value of the '{@link #getCommitMessage() <em>Commit Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommitMessage()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMIT_MESSAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCommitMessage() <em>Commit Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommitMessage()
	 * @generated
	 * @ordered
	 */
	protected String commitMessage = COMMIT_MESSAGE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getBugreport() <em>Bugreport</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBugreport()
	 * @generated
	 * @ordered
	 */
	protected BugReport bugreport;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VersionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SystemModelPackage.Literals.VERSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getModelVersionID() {
		return modelVersionID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setModelVersionID(String newModelVersionID) {
		String oldModelVersionID = modelVersionID;
		modelVersionID = newModelVersionID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.VERSION__MODEL_VERSION_ID, oldModelVersionID, modelVersionID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDate() {
		return date;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDate(String newDate) {
		String oldDate = date;
		date = newDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.VERSION__DATE, oldDate, date));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getAuthor() {
		return author;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAuthor(String newAuthor) {
		String oldAuthor = author;
		author = newAuthor;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.VERSION__AUTHOR, oldAuthor, author));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCommitMessage() {
		return commitMessage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCommitMessage(String newCommitMessage) {
		String oldCommitMessage = commitMessage;
		commitMessage = newCommitMessage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.VERSION__COMMIT_MESSAGE, oldCommitMessage, commitMessage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BugReport getBugreport() {
		return bugreport;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBugreport(BugReport newBugreport, NotificationChain msgs) {
		BugReport oldBugreport = bugreport;
		bugreport = newBugreport;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SystemModelPackage.VERSION__BUGREPORT, oldBugreport, newBugreport);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBugreport(BugReport newBugreport) {
		if (newBugreport != bugreport) {
			NotificationChain msgs = null;
			if (bugreport != null)
				msgs = ((InternalEObject)bugreport).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SystemModelPackage.VERSION__BUGREPORT, null, msgs);
			if (newBugreport != null)
				msgs = ((InternalEObject)newBugreport).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SystemModelPackage.VERSION__BUGREPORT, null, msgs);
			msgs = basicSetBugreport(newBugreport, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.VERSION__BUGREPORT, newBugreport, newBugreport));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SystemModelPackage.VERSION__BUGREPORT:
				return basicSetBugreport(null, msgs);
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
			case SystemModelPackage.VERSION__MODEL_VERSION_ID:
				return getModelVersionID();
			case SystemModelPackage.VERSION__DATE:
				return getDate();
			case SystemModelPackage.VERSION__AUTHOR:
				return getAuthor();
			case SystemModelPackage.VERSION__COMMIT_MESSAGE:
				return getCommitMessage();
			case SystemModelPackage.VERSION__BUGREPORT:
				return getBugreport();
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
			case SystemModelPackage.VERSION__MODEL_VERSION_ID:
				setModelVersionID((String)newValue);
				return;
			case SystemModelPackage.VERSION__DATE:
				setDate((String)newValue);
				return;
			case SystemModelPackage.VERSION__AUTHOR:
				setAuthor((String)newValue);
				return;
			case SystemModelPackage.VERSION__COMMIT_MESSAGE:
				setCommitMessage((String)newValue);
				return;
			case SystemModelPackage.VERSION__BUGREPORT:
				setBugreport((BugReport)newValue);
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
			case SystemModelPackage.VERSION__MODEL_VERSION_ID:
				setModelVersionID(MODEL_VERSION_ID_EDEFAULT);
				return;
			case SystemModelPackage.VERSION__DATE:
				setDate(DATE_EDEFAULT);
				return;
			case SystemModelPackage.VERSION__AUTHOR:
				setAuthor(AUTHOR_EDEFAULT);
				return;
			case SystemModelPackage.VERSION__COMMIT_MESSAGE:
				setCommitMessage(COMMIT_MESSAGE_EDEFAULT);
				return;
			case SystemModelPackage.VERSION__BUGREPORT:
				setBugreport((BugReport)null);
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
			case SystemModelPackage.VERSION__MODEL_VERSION_ID:
				return MODEL_VERSION_ID_EDEFAULT == null ? modelVersionID != null : !MODEL_VERSION_ID_EDEFAULT.equals(modelVersionID);
			case SystemModelPackage.VERSION__DATE:
				return DATE_EDEFAULT == null ? date != null : !DATE_EDEFAULT.equals(date);
			case SystemModelPackage.VERSION__AUTHOR:
				return AUTHOR_EDEFAULT == null ? author != null : !AUTHOR_EDEFAULT.equals(author);
			case SystemModelPackage.VERSION__COMMIT_MESSAGE:
				return COMMIT_MESSAGE_EDEFAULT == null ? commitMessage != null : !COMMIT_MESSAGE_EDEFAULT.equals(commitMessage);
			case SystemModelPackage.VERSION__BUGREPORT:
				return bugreport != null;
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
		result.append(" (modelVersionID: ");
		result.append(modelVersionID);
		result.append(", date: ");
		result.append(date);
		result.append(", author: ");
		result.append(author);
		result.append(", commitMessage: ");
		result.append(commitMessage);
		result.append(')');
		return result.toString();
	}

} //VersionImpl
