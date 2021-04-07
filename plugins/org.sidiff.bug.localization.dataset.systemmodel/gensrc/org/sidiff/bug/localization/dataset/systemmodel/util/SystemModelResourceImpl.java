/**
 */
package org.sidiff.bug.localization.dataset.systemmodel.util;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

/**
 * <!-- begin-user-doc --> The <b>Resource </b> associated with the package.
 * <!-- end-user-doc -->
 * 
 * @see org.sidiff.bug.localization.dataset.systemmodel.util.SystemModelResourceFactoryImpl
 * @generated
 */
public class SystemModelResourceImpl extends XMIResourceImpl {

	/**
	 * Creates an instance of the resource. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param uri the URI of the new resource.
	 * @generated
	 */
	public SystemModelResourceImpl(URI uri) {
		super(uri);
	}

	protected void init() {
		super.init();
		setXMLVersion("1.1");  //  Allow control characters XML
	}

	@Override
	protected boolean useUUIDs() {
		return true;
	}

} // SystemModelResourceImpl
