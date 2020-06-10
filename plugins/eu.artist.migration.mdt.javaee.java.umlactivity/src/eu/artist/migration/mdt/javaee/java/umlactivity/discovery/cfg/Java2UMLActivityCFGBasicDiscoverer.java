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
package eu.artist.migration.mdt.javaee.java.umlactivity.discovery.cfg;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;

import eu.artist.migration.mdt.javaee.java.uml.discovery.AbstractJava2UMLDiscoverer;
import eu.artist.migration.mdt.javaee.java.uml.provider.TransformationProvider;

public abstract class Java2UMLActivityCFGBasicDiscoverer<T> extends AbstractJava2UMLDiscoverer<T> {

	public Java2UMLActivityCFGBasicDiscoverer(TransformationProvider<T> transformationProvider) {
		super(transformationProvider);
	}

	@Override
	protected void transform(EMFModel javaModel, EMFModel umlModel, IProgressMonitor monitor) throws IOException {
		ILauncher transformationLauncher = new EMFVMLauncher();
		transformationLauncher.initialize(new HashMap<String, Object>());
		transformationLauncher.addInModel(javaModel, "IN", "JAVA");
		transformationLauncher.addOutModel(umlModel, "OUT", "UML");
		transformationLauncher.addLibrary("java2UMLActivityHelpers",
				Java2UMLActivityCFGBasicDiscoverer.class.getResource("/resources/java2UMLActivityHelpers.asm").openStream());
		transformationLauncher.launch(ILauncher.RUN_MODE, monitor, new HashMap<String, Object>(),
				Java2UMLActivityCFGBasicDiscoverer.class.getResource("/resources/JavaMethods2UMLActivityDiagram-OnlyCFG.asm").openStream());
	}

}
