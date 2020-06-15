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
package eu.artist.migration.mdt.javaee.java.umlclass.discovery;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;

import eu.artist.migration.mdt.javaee.java.uml.discovery.AbstractJava2UMLDiscoverer;
import eu.artist.migration.mdt.javaee.java.uml.provider.TransformationProvider;

public abstract class Java2UMLBasicDiscoverer<T> extends AbstractJava2UMLDiscoverer<T> {

	public Java2UMLBasicDiscoverer(TransformationProvider<T> transformationProvider) {
		super(transformationProvider);
	}
	
	@Override
	protected void transform(EMFModel javaModel, EMFModel umlModel, IProgressMonitor monitor) throws IOException {
		ILauncher transformationLauncher = new EMFVMLauncher();
		transformationLauncher.initialize(new HashMap<String, Object>());
		transformationLauncher.addInModel(javaModel, "IN", "JAVA");
		transformationLauncher.addOutModel(umlModel, "OUT", "UML");
		transformationLauncher.addLibrary("java2UMLHelpers",
				Java2UMLBasicDiscoverer.class.getResource("/resources/java2UMLHelpers.asm").openStream());

		HashMap<String, Object> options = new HashMap<String, Object>();
//		options.put("allowInterModelReferences", true);
		
		transformationLauncher.launch(ILauncher.RUN_MODE, monitor, options,
				Java2UMLBasicDiscoverer.class.getResource("/resources/java2UML.asm").openStream());
	}

}
