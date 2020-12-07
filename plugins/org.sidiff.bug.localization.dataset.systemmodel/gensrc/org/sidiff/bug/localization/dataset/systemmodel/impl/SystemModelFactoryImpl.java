/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.impl;

import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelPackage;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.ViewDescription;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SystemModelFactoryImpl extends EFactoryImpl implements SystemModelFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SystemModelFactory init() {
		try {
			SystemModelFactory theSystemModelFactory = (SystemModelFactory)EPackage.Registry.INSTANCE.getEFactory(SystemModelPackage.eNS_URI);
			if (theSystemModelFactory != null) {
				return theSystemModelFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SystemModelFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SystemModelFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case SystemModelPackage.SYSTEM_MODEL: return createSystemModel();
			case SystemModelPackage.VIEW: return createView();
			case SystemModelPackage.CHANGE: return createChange();
			case SystemModelPackage.VIEW_DESCRIPTION: return createViewDescription();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case SystemModelPackage.CHANGE_TYPE:
				return createChangeTypeFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case SystemModelPackage.CHANGE_TYPE:
				return convertChangeTypeToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public SystemModel createSystemModel() {
		SystemModelImpl systemModel = new SystemModelImpl();
		systemModel.setURI(URI.createURI("")); // dummy
		return systemModel;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public SystemModel createSystemModel(URI uri, boolean resolveResources) {
		ResourceSet resourceSet = new ResourceSetImpl();
		
		if (!resourceSet.getURIConverter().exists(uri, Collections.emptyMap())) {
			SystemModelImpl systemModel = new SystemModelImpl();
			Resource multiViewResource = resourceSet.createResource(uri);
			multiViewResource.getContents().add(systemModel);
			return systemModel;
		} else {
			Resource multiViewResource = resourceSet.getResource(uri, true);
			SystemModel systemModel = (SystemModel) multiViewResource.getContents().get(0);
			
			if (resolveResources) {
				for (View view : systemModel.getViews()) {
					if (view.getModel() != null) {
						view.getModel().eAllContents().forEachRemaining(e -> {});
					}
				}
			}
			
			return systemModel;
		}
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public SystemModel createSystemModel(Path file, boolean resolveResources) {
		return createSystemModel(URI.createFileURI(file.toString()), resolveResources);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public View createView() {
		ViewImpl view = new ViewImpl();
		return view;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Change createChange() {
		ChangeImpl change = new ChangeImpl();
		return change;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ViewDescription createViewDescription() {
		ViewDescriptionImpl viewDescription = new ViewDescriptionImpl();
		return viewDescription;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public ViewDescription createViewDescription(String name, String description, String viewKind) {
		ViewDescriptionImpl viewDescription = new ViewDescriptionImpl();
		viewDescription.setName(name);
		viewDescription.setDescription(description);
		viewDescription.setViewKind(viewKind);
		return viewDescription;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ChangeType createChangeTypeFromString(EDataType eDataType, String initialValue) {
		ChangeType result = ChangeType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertChangeTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemModelPackage getSystemModelPackage() {
		return (SystemModelPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SystemModelPackage getPackage() {
		return SystemModelPackage.eINSTANCE;
	}

} //SystemModelFactoryImpl
