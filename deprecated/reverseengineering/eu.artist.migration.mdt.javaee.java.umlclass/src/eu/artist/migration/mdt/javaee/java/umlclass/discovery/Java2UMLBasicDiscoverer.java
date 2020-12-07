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
import java.util.List;
import java.util.Map;

import org.eclipse.m2m.atl.engine.emfvm.ASM;

import eu.artist.migration.mdt.javaee.java.uml.discovery.AbstractJava2UMLDiscoverer;
import eu.artist.migration.mdt.javaee.java.uml.provider.TransformationProvider;
import eu.artist.migration.mdt.javaee.java.uml.util.ModuleRegisty;

public abstract class Java2UMLBasicDiscoverer<T> extends AbstractJava2UMLDiscoverer<T> {

	public Java2UMLBasicDiscoverer(TransformationProvider<T> transformationProvider) {
		super(transformationProvider);
	}
	
	@Override
	protected Map<String, ASM> loadLibraries() throws IOException {
		Map<String, ASM> libraries = super.loadLibraries();
		libraries.put("java2UMLHelpers", ModuleRegisty.getModule(Java2UMLBasicDiscoverer.class.getResource("/resources/java2UMLHelpers.asm")));
		return libraries;
	}

	@Override
	protected List<ASM> loadModules() throws IOException {
		List<ASM> modules = super.loadModules();
		ASM java2UML = ModuleRegisty.getModule(Java2UMLBasicDiscoverer.class.getResource("/resources/java2UML.asm"));
		modules.add(java2UML);
		return modules;
	}
	
}
