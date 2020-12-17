/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.sidiff.bug.localization.dataset.systemmodel.BugReport;
import org.sidiff.bug.localization.dataset.systemmodel.BugReportComment;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Bug Report</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getProduct <em>Product</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getComponent <em>Component</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getCreationTime <em>Creation Time</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getCreator <em>Creator</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getAssignedTo <em>Assigned To</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getResolution <em>Resolution</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getSummary <em>Summary</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getComments <em>Comments</em>}</li>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.BugReportImpl#getModelLocations <em>Model Locations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BugReportImpl extends MinimalEObjectImpl.Container implements BugReport {
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
	 * The default value of the '{@link #getProduct() <em>Product</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProduct()
	 * @generated
	 * @ordered
	 */
	protected static final String PRODUCT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProduct() <em>Product</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProduct()
	 * @generated
	 * @ordered
	 */
	protected String product = PRODUCT_EDEFAULT;

	/**
	 * The default value of the '{@link #getComponent() <em>Component</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponent()
	 * @generated
	 * @ordered
	 */
	protected static final String COMPONENT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComponent() <em>Component</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponent()
	 * @generated
	 * @ordered
	 */
	protected String component = COMPONENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getCreationTime() <em>Creation Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreationTime()
	 * @generated
	 * @ordered
	 */
	protected static final String CREATION_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCreationTime() <em>Creation Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreationTime()
	 * @generated
	 * @ordered
	 */
	protected String creationTime = CREATION_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getCreator() <em>Creator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreator()
	 * @generated
	 * @ordered
	 */
	protected static final String CREATOR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCreator() <em>Creator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreator()
	 * @generated
	 * @ordered
	 */
	protected String creator = CREATOR_EDEFAULT;

	/**
	 * The default value of the '{@link #getAssignedTo() <em>Assigned To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssignedTo()
	 * @generated
	 * @ordered
	 */
	protected static final String ASSIGNED_TO_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAssignedTo() <em>Assigned To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssignedTo()
	 * @generated
	 * @ordered
	 */
	protected String assignedTo = ASSIGNED_TO_EDEFAULT;

	/**
	 * The default value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final String SEVERITY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected String severity = SEVERITY_EDEFAULT;

	/**
	 * The default value of the '{@link #getResolution() <em>Resolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResolution()
	 * @generated
	 * @ordered
	 */
	protected static final String RESOLUTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getResolution() <em>Resolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResolution()
	 * @generated
	 * @ordered
	 */
	protected String resolution = RESOLUTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final String STATUS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected String status = STATUS_EDEFAULT;

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
	 * The cached value of the '{@link #getComments() <em>Comments</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComments()
	 * @generated
	 * @ordered
	 */
	protected EList<BugReportComment> comments;

	/**
	 * The cached value of the '{@link #getModelLocations() <em>Model Locations</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelLocations()
	 * @generated
	 * @ordered
	 */
	protected EList<Change> modelLocations;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BugReportImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SystemModelPackage.Literals.BUG_REPORT;
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
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getProduct() {
		return product;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProduct(String newProduct) {
		String oldProduct = product;
		product = newProduct;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__PRODUCT, oldProduct, product));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getComponent() {
		return component;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComponent(String newComponent) {
		String oldComponent = component;
		component = newComponent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__COMPONENT, oldComponent, component));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCreationTime() {
		return creationTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCreationTime(String newCreationTime) {
		String oldCreationTime = creationTime;
		creationTime = newCreationTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__CREATION_TIME, oldCreationTime, creationTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCreator() {
		return creator;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCreator(String newCreator) {
		String oldCreator = creator;
		creator = newCreator;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__CREATOR, oldCreator, creator));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getAssignedTo() {
		return assignedTo;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAssignedTo(String newAssignedTo) {
		String oldAssignedTo = assignedTo;
		assignedTo = newAssignedTo;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__ASSIGNED_TO, oldAssignedTo, assignedTo));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSeverity() {
		return severity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSeverity(String newSeverity) {
		String oldSeverity = severity;
		severity = newSeverity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__SEVERITY, oldSeverity, severity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getResolution() {
		return resolution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResolution(String newResolution) {
		String oldResolution = resolution;
		resolution = newResolution;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__RESOLUTION, oldResolution, resolution));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(String newStatus) {
		String oldStatus = status;
		status = newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__STATUS, oldStatus, status));
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
			eNotify(new ENotificationImpl(this, Notification.SET, SystemModelPackage.BUG_REPORT__SUMMARY, oldSummary, summary));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<BugReportComment> getComments() {
		if (comments == null) {
			comments = new EObjectContainmentEList<BugReportComment>(BugReportComment.class, this, SystemModelPackage.BUG_REPORT__COMMENTS);
		}
		return comments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Change> getModelLocations() {
		if (modelLocations == null) {
			modelLocations = new EObjectContainmentEList<Change>(Change.class, this, SystemModelPackage.BUG_REPORT__MODEL_LOCATIONS);
		}
		return modelLocations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SystemModelPackage.BUG_REPORT__COMMENTS:
				return ((InternalEList<?>)getComments()).basicRemove(otherEnd, msgs);
			case SystemModelPackage.BUG_REPORT__MODEL_LOCATIONS:
				return ((InternalEList<?>)getModelLocations()).basicRemove(otherEnd, msgs);
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
			case SystemModelPackage.BUG_REPORT__ID:
				return getId();
			case SystemModelPackage.BUG_REPORT__PRODUCT:
				return getProduct();
			case SystemModelPackage.BUG_REPORT__COMPONENT:
				return getComponent();
			case SystemModelPackage.BUG_REPORT__CREATION_TIME:
				return getCreationTime();
			case SystemModelPackage.BUG_REPORT__CREATOR:
				return getCreator();
			case SystemModelPackage.BUG_REPORT__ASSIGNED_TO:
				return getAssignedTo();
			case SystemModelPackage.BUG_REPORT__SEVERITY:
				return getSeverity();
			case SystemModelPackage.BUG_REPORT__RESOLUTION:
				return getResolution();
			case SystemModelPackage.BUG_REPORT__STATUS:
				return getStatus();
			case SystemModelPackage.BUG_REPORT__SUMMARY:
				return getSummary();
			case SystemModelPackage.BUG_REPORT__COMMENTS:
				return getComments();
			case SystemModelPackage.BUG_REPORT__MODEL_LOCATIONS:
				return getModelLocations();
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
			case SystemModelPackage.BUG_REPORT__ID:
				setId((Integer)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__PRODUCT:
				setProduct((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__COMPONENT:
				setComponent((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__CREATION_TIME:
				setCreationTime((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__CREATOR:
				setCreator((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__ASSIGNED_TO:
				setAssignedTo((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__SEVERITY:
				setSeverity((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__RESOLUTION:
				setResolution((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__STATUS:
				setStatus((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__SUMMARY:
				setSummary((String)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__COMMENTS:
				getComments().clear();
				getComments().addAll((Collection<? extends BugReportComment>)newValue);
				return;
			case SystemModelPackage.BUG_REPORT__MODEL_LOCATIONS:
				getModelLocations().clear();
				getModelLocations().addAll((Collection<? extends Change>)newValue);
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
			case SystemModelPackage.BUG_REPORT__ID:
				setId(ID_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__PRODUCT:
				setProduct(PRODUCT_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__COMPONENT:
				setComponent(COMPONENT_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__CREATION_TIME:
				setCreationTime(CREATION_TIME_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__CREATOR:
				setCreator(CREATOR_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__ASSIGNED_TO:
				setAssignedTo(ASSIGNED_TO_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__SEVERITY:
				setSeverity(SEVERITY_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__RESOLUTION:
				setResolution(RESOLUTION_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__SUMMARY:
				setSummary(SUMMARY_EDEFAULT);
				return;
			case SystemModelPackage.BUG_REPORT__COMMENTS:
				getComments().clear();
				return;
			case SystemModelPackage.BUG_REPORT__MODEL_LOCATIONS:
				getModelLocations().clear();
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
			case SystemModelPackage.BUG_REPORT__ID:
				return id != ID_EDEFAULT;
			case SystemModelPackage.BUG_REPORT__PRODUCT:
				return PRODUCT_EDEFAULT == null ? product != null : !PRODUCT_EDEFAULT.equals(product);
			case SystemModelPackage.BUG_REPORT__COMPONENT:
				return COMPONENT_EDEFAULT == null ? component != null : !COMPONENT_EDEFAULT.equals(component);
			case SystemModelPackage.BUG_REPORT__CREATION_TIME:
				return CREATION_TIME_EDEFAULT == null ? creationTime != null : !CREATION_TIME_EDEFAULT.equals(creationTime);
			case SystemModelPackage.BUG_REPORT__CREATOR:
				return CREATOR_EDEFAULT == null ? creator != null : !CREATOR_EDEFAULT.equals(creator);
			case SystemModelPackage.BUG_REPORT__ASSIGNED_TO:
				return ASSIGNED_TO_EDEFAULT == null ? assignedTo != null : !ASSIGNED_TO_EDEFAULT.equals(assignedTo);
			case SystemModelPackage.BUG_REPORT__SEVERITY:
				return SEVERITY_EDEFAULT == null ? severity != null : !SEVERITY_EDEFAULT.equals(severity);
			case SystemModelPackage.BUG_REPORT__RESOLUTION:
				return RESOLUTION_EDEFAULT == null ? resolution != null : !RESOLUTION_EDEFAULT.equals(resolution);
			case SystemModelPackage.BUG_REPORT__STATUS:
				return STATUS_EDEFAULT == null ? status != null : !STATUS_EDEFAULT.equals(status);
			case SystemModelPackage.BUG_REPORT__SUMMARY:
				return SUMMARY_EDEFAULT == null ? summary != null : !SUMMARY_EDEFAULT.equals(summary);
			case SystemModelPackage.BUG_REPORT__COMMENTS:
				return comments != null && !comments.isEmpty();
			case SystemModelPackage.BUG_REPORT__MODEL_LOCATIONS:
				return modelLocations != null && !modelLocations.isEmpty();
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
		result.append(" (id: ");
		result.append(id);
		result.append(", product: ");
		result.append(product);
		result.append(", component: ");
		result.append(component);
		result.append(", creationTime: ");
		result.append(creationTime);
		result.append(", creator: ");
		result.append(creator);
		result.append(", assignedTo: ");
		result.append(assignedTo);
		result.append(", severity: ");
		result.append(severity);
		result.append(", resolution: ");
		result.append(resolution);
		result.append(", status: ");
		result.append(status);
		result.append(", summary: ");
		result.append(summary);
		result.append(')');
		return result.toString();
	}

} //BugReportImpl
