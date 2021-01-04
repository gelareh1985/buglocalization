package org.sidiff.bug.localization.dataset.database.systemmodel;

import org.eclipse.emf.common.util.URI;
import org.eclipse.uml2.uml.internal.resource.UMLResourceImpl;

@SuppressWarnings("restriction")
public class UMLResourceIDPreserving extends UMLResourceImpl {

	public UMLResourceIDPreserving(URI uri) {
		super(uri);
	}

	@Override
	protected boolean assignIDsWhileLoading() {
		return false;
	}
	
}
