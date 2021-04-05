/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Change</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl#getQuantification <em>Quantification</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl#getLocation <em>Location</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl#getOriginalResource <em>Original Resource</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.ChangeImpl#getModelElementURI <em>Model Element URI</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ChangeImpl extends MinimalEObjectImpl.Container implements Change {
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final ChangeType TYPE_EDEFAULT = ChangeType.ADD;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected ChangeType type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getQuantification() <em>Quantification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQuantification()
	 * @generated
	 * @ordered
	 */
	protected static final int QUANTIFICATION_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getQuantification() <em>Quantification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQuantification()
	 * @generated
	 * @ordered
	 */
	protected int quantification = QUANTIFICATION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getLocation() <em>Location</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected EObject location;

	/**
	 * The default value of the '{@link #getOriginalResource() <em>Original Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOriginalResource()
	 * @generated
	 * @ordered
	 */
	protected static final String ORIGINAL_RESOURCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOriginalResource() <em>Original Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOriginalResource()
	 * @generated
	 * @ordered
	 */
	protected String originalResource = ORIGINAL_RESOURCE_EDEFAULT;

	/**
	 * The default value of the '{@link #getModelElementURI() <em>Model Element URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelElementURI()
	 * @generated
	 * @ordered
	 */
	protected static final String MODEL_ELEMENT_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getModelElementURI() <em>Model Element URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelElementURI()
	 * @generated
	 * @ordered
	 */
	protected String modelElementURI = MODEL_ELEMENT_URI_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ChangeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SystemModelPackage.Literals.CHANGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ChangeType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(ChangeType newType) {
		ChangeType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.CHANGE__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getQuantification() {
		return quantification;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setQuantification(int newQuantification) {
		int oldQuantification = quantification;
		quantification = newQuantification;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.CHANGE__QUANTIFICATION, oldQuantification, quantification));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject getLocation() {
		if (location != null && location.eIsProxy()) {
			InternalEObject oldLocation = (InternalEObject)location;
			location = eResolveProxy(oldLocation);
			if (location != oldLocation) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SystemModelPackage.CHANGE__LOCATION, oldLocation, location));
			}
		}
		return location;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EObject basicGetLocation() {
		return location;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLocation(EObject newLocation) {
		EObject oldLocation = location;
		location = newLocation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.CHANGE__LOCATION, oldLocation, location));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOriginalResource() {
		return originalResource;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOriginalResource(String newOriginalResource) {
		String oldOriginalResource = originalResource;
		originalResource = newOriginalResource;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.CHANGE__ORIGINAL_RESOURCE, oldOriginalResource, originalResource));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getModelElementURI() {
		return modelElementURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setModelElementURI(String newModelElementURI) {
		String oldModelElementURI = modelElementURI;
		modelElementURI = newModelElementURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.CHANGE__MODEL_ELEMENT_URI, oldModelElementURI, modelElementURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SystemModelPackage.CHANGE__TYPE:
				return getType();
			case SystemModelPackage.CHANGE__QUANTIFICATION:
				return getQuantification();
			case SystemModelPackage.CHANGE__LOCATION:
				if (resolve) return getLocation();
				return basicGetLocation();
			case SystemModelPackage.CHANGE__ORIGINAL_RESOURCE:
				return getOriginalResource();
			case SystemModelPackage.CHANGE__MODEL_ELEMENT_URI:
				return getModelElementURI();
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
			case SystemModelPackage.CHANGE__TYPE:
				setType((ChangeType)newValue);
				return;
			case SystemModelPackage.CHANGE__QUANTIFICATION:
				setQuantification((Integer)newValue);
				return;
			case SystemModelPackage.CHANGE__LOCATION:
				setLocation((EObject)newValue);
				return;
			case SystemModelPackage.CHANGE__ORIGINAL_RESOURCE:
				setOriginalResource((String)newValue);
				return;
			case SystemModelPackage.CHANGE__MODEL_ELEMENT_URI:
				setModelElementURI((String)newValue);
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
			case SystemModelPackage.CHANGE__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case SystemModelPackage.CHANGE__QUANTIFICATION:
				setQuantification(QUANTIFICATION_EDEFAULT);
				return;
			case SystemModelPackage.CHANGE__LOCATION:
				setLocation((EObject)null);
				return;
			case SystemModelPackage.CHANGE__ORIGINAL_RESOURCE:
				setOriginalResource(ORIGINAL_RESOURCE_EDEFAULT);
				return;
			case SystemModelPackage.CHANGE__MODEL_ELEMENT_URI:
				setModelElementURI(MODEL_ELEMENT_URI_EDEFAULT);
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
			case SystemModelPackage.CHANGE__TYPE:
				return type != TYPE_EDEFAULT;
			case SystemModelPackage.CHANGE__QUANTIFICATION:
				return quantification != QUANTIFICATION_EDEFAULT;
			case SystemModelPackage.CHANGE__LOCATION:
				return location != null;
			case SystemModelPackage.CHANGE__ORIGINAL_RESOURCE:
				return ORIGINAL_RESOURCE_EDEFAULT == null ? originalResource != null : !ORIGINAL_RESOURCE_EDEFAULT.equals(originalResource);
			case SystemModelPackage.CHANGE__MODEL_ELEMENT_URI:
				return MODEL_ELEMENT_URI_EDEFAULT == null ? modelElementURI != null : !MODEL_ELEMENT_URI_EDEFAULT.equals(modelElementURI);
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
		result.append(" (type: ");
		result.append(type);
		result.append(", quantification: ");
		result.append(quantification);
		result.append(", originalResource: ");
		result.append(originalResource);
		result.append(", modelElementURI: ");
		result.append(modelElementURI);
		result.append(')');
		return result.toString();
	}

} //ChangeImpl
