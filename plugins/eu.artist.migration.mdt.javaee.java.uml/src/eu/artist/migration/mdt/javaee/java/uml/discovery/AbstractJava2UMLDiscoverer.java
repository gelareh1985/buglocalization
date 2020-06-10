/********************************************************************************
* Copyright (c) 2013 INRIA. 
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*	INRIA - Initial implementation
*	authors: Guillaume Doux (guillaume.doux at inria.fr) 
*		 	 Matthieu Allon (matthieu.allon at gmail.com)
* Initially developed in the context of ARTIST EU project www.artist-project.eu
*********************************************************************************/
package eu.artist.migration.mdt.javaee.java.uml.discovery;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.modisco.infra.discovery.core.AbstractModelDiscoverer;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;

import eu.artist.migration.mdt.javaee.java.uml.provider.TransformationProvider;

public abstract class AbstractJava2UMLDiscoverer<T> extends AbstractModelDiscoverer<T> {

	private TransformationProvider<T> transformationProvider;

	public AbstractJava2UMLDiscoverer(TransformationProvider<T> transformationProvider) {
		this.transformationProvider = transformationProvider;
	}
	
	@Override
	protected void basicDiscoverElement(T source, IProgressMonitor monitor) throws DiscoveryException {

		// Generate default URI:
		if (getTargetURI() == null) {
			URI outputUri = transformationProvider.getOutputURI(source);

			if (outputUri != null) {
				this.setTargetURI(URI.createURI(outputUri.toString()));
			}
		}

		// Start ATL transformation:
		try {
			EMFModel javaModel = transformationProvider.getInputModel(source);
			EMFModel umlModel = transformationProvider.getOutputModel();

			transform(javaModel, umlModel, monitor);

			umlModel.getResource().setURI(getTargetURI());
			this.setTargetModel(umlModel.getResource());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ATLCoreException e) {
			e.printStackTrace();
		}

	}

	protected abstract void transform(EMFModel javaModel, EMFModel umlModel, IProgressMonitor monitor) throws IOException;

}
