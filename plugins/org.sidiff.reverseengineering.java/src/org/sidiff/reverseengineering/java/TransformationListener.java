package org.sidiff.reverseengineering.java;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.resource.Resource;

public interface TransformationListener {

	/**
	 * @param resource The transformed Java type resource.
	 * @param trace    Tracing information of the transformation.
	 */
	void typeModelCreated(IResource resource, TransformationTrace trace);

	/**
	 * @param resource     The removed workspace resources.
	 * @param projectModel The project model which contained the corresponding type model.
	 */
	void typeModelRemoved(IResource resource, Resource projectModel);
}
