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
package eu.artist.migration.mdt.javaee.java.umlactivity;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IInjector;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.eclipse.modisco.infra.discovery.core.AbstractModelDiscoverer;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.uml2.uml.resource.UMLResource;

public class Java2UMLActivityDiscoverer extends AbstractModelDiscoverer<IFile>{

	public static String ID="eu.artist.migration.mdt.javaee.java.umlactivity.cfg";

	public static final String UML_MM_URI = "http://www.eclipse.org/uml2/4.0.0/UML"; //$NON-NLS-1$
	public static final String JAVA_MM_URI = "http://www.eclipse.org/MoDisco/Java/0.2.incubation/java"; //$NON-NLS-1$

	@Override
	public boolean isApplicableTo(IFile source) {
		if (!source.exists()) {
			return false;
		}
		return source.getName().endsWith("xmi");
	}

	@Override
	protected void basicDiscoverElement(IFile source, IProgressMonitor monitor)
			throws DiscoveryException {
		String uri = source.getLocationURI().toString();
		String outputUri = uri.replaceFirst(".xmi", ".activity.cfg.uml");
		if (this.getTargetURI()==null){
			this.setTargetURI(URI.createURI(outputUri));
		}
		try {
			ILauncher transformationLauncher = new EMFVMLauncher();
			ModelFactory modelFactory = new EMFModelFactory();
			IInjector injector = new EMFInjector();
			IReferenceModel umlMetamodel = modelFactory.newReferenceModel();
			injector.inject(umlMetamodel, Java2UMLActivityDiscoverer.UML_MM_URI);
			IReferenceModel javaMetamodel = modelFactory.newReferenceModel();
			injector.inject(javaMetamodel, Java2UMLActivityDiscoverer.JAVA_MM_URI);
			
			IModel javaModel = modelFactory.newModel(javaMetamodel);
			injector.inject(javaModel, uri);
			IModel umlModel = modelFactory.newModel(umlMetamodel);
			
			transformationLauncher.initialize(new HashMap<String,Object>());
			transformationLauncher.addInModel(javaModel, "IN", "JAVA");
			transformationLauncher.addOutModel(umlModel, "OUT", "UML");
			transformationLauncher.addLibrary("java2UMLActivityHelpers", Java2UMLActivityDiscoverer.class.getResource("/resources/java2UMLActivityHelpers.asm").openStream());
			transformationLauncher.launch(ILauncher.RUN_MODE, monitor, new HashMap<String,Object>(),
				Java2UMLActivityDiscoverer.class.getResource("/resources/JavaMethods2UMLActivityDiagram-OnlyCFG.asm").openStream());
			
			Resource umlOutput = UMLResource.Factory.INSTANCE.createResource(getTargetURI());
			EMFModel umlEMFModel = (EMFModel) umlModel;
			umlOutput.getContents().addAll(umlEMFModel.getResource().getContents());
			this.setTargetModel(umlOutput);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ATLCoreException e) {
			e.printStackTrace();
		}


	}

}
