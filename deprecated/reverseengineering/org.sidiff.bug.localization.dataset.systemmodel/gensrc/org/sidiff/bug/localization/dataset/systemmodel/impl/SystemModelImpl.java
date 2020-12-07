/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.ViewDescription;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>System Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.sidiff.bug.localization.dataset.systemmodel.impl.SystemModelImpl#getViews <em>Views</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SystemModelImpl extends DescribableElementImpl implements SystemModel {
	/**
	 * The cached value of the '{@link #getViews() <em>Views</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getViews()
	 * @generated
	 * @ordered
	 */
	protected EList<View> views;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SystemModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SystemModelPackage.Literals.SYSTEM_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<View> getViews() {
		if (views == null) {
			views = new EObjectContainmentWithInverseEList<View>(View.class, this, SystemModelPackage.SYSTEM_MODEL__VIEWS, SystemModelPackage.VIEW__SYSTEM);
		}
		return views;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public void addView(Resource resource, ViewDescription viewDescription) {
		eResource().getResourceSet().getResources().add(resource);
		
		for (EObject rootElement : resource.getContents()) {
			View view = SystemModelFactory.eINSTANCE.createView();
			view.setModel(rootElement);
			view.setName(viewDescription.getName());
			view.setDescription(viewDescription.getDescription());
			view.setKind(viewDescription.getViewKind());
			
			getViews().add(view);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public View getViewByKind(ViewDescription viewDescription) {
		return getViewByKind(viewDescription.getViewKind());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public View getViewByKind(String viewDescription) {

		for (View view : getViews()) {
			if (view.getKind().equals(viewDescription)) {
				return view;
			}
		}
		
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public boolean containsViewKind(ViewDescription viewDescription) {
		
		for (View view : getViews()) {
			if (view.getKind().equals(viewDescription.getViewKind())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public boolean removeViewKind(ViewDescription viewDescription) {
		View toBeRemoved = null;
		
		for (View view : getViews()) {
			if (view.getKind().equals(viewDescription.getViewKind())) {
				toBeRemoved = view;
			}
		}
		
		return getViews().remove(toBeRemoved);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SystemModelPackage.SYSTEM_MODEL__VIEWS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getViews()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SystemModelPackage.SYSTEM_MODEL__VIEWS:
				return ((InternalEList<?>)getViews()).basicRemove(otherEnd, msgs);
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
			case SystemModelPackage.SYSTEM_MODEL__VIEWS:
				return getViews();
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
			case SystemModelPackage.SYSTEM_MODEL__VIEWS:
				getViews().clear();
				getViews().addAll((Collection<? extends View>)newValue);
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
			case SystemModelPackage.SYSTEM_MODEL__VIEWS:
				getViews().clear();
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
			case SystemModelPackage.SYSTEM_MODEL__VIEWS:
				return views != null && !views.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case SystemModelPackage.SYSTEM_MODEL___ADD_VIEW__RESOURCE_VIEWDESCRIPTION:
				addView((Resource)arguments.get(0), (ViewDescription)arguments.get(1));
				return null;
			case SystemModelPackage.SYSTEM_MODEL___GET_VIEW_BY_KIND__VIEWDESCRIPTION:
				return getViewByKind((ViewDescription)arguments.get(0));
			case SystemModelPackage.SYSTEM_MODEL___GET_VIEW_BY_KIND__STRING:
				return getViewByKind((String)arguments.get(0));
			case SystemModelPackage.SYSTEM_MODEL___CONTAINS_VIEW_KIND__VIEWDESCRIPTION:
				return containsViewKind((ViewDescription)arguments.get(0));
			case SystemModelPackage.SYSTEM_MODEL___REMOVE_VIEW_KIND__VIEWDESCRIPTION:
				return removeViewKind((ViewDescription)arguments.get(0));
		}
		return super.eInvoke(operationID, arguments);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public void setURI(URI uri) {
		if (eResource() == null) {
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource multiViewResource = resourceSet.createResource(uri);
			multiViewResource.getContents().add(this);
		} else {
			eResource().setURI(uri);
		}
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public void saveAll(Map<Object, Object> options) {
		saveAll(options, Collections.emptySet());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public void saveAll(Map<Object, Object> options, Set<Resource> exclude) {
		
		// allow control characters, e.g., invalid XML character Unicode 0xc
		options = new HashMap<>(options);
		options.put(XMLResource.OPTION_XML_VERSION, "1.1");
		
		URI baseURI = eResource().getURI().trimSegments(1);
		Set<Resource> viewResources = new HashSet<>();
		
		// Save views:
		for (View view : getViews()) {
			viewResources.add(view.getModel().eResource());
		}
		
		viewResources.removeAll(exclude);
		
		for (Resource resource : viewResources) {
			String fileName = resource.getURI().segment(resource.getURI().segmentCount() - 1);
			URI fileURI = baseURI.appendSegment(fileName);
			resource.setURI(fileURI);
			
			try {
				resource.save(options);
			} catch (Throwable e) {
				System.err.println(fileURI + ": " + e.getCause());
			}
		}
		
		// Save system model (views must be saved first for relative paths):
		try {
			eResource().save(options);
		} catch (Throwable e) {
			System.err.println(eResource().getURI() + ": " + e.getCause());
		}
	}

} //SystemModelImpl
