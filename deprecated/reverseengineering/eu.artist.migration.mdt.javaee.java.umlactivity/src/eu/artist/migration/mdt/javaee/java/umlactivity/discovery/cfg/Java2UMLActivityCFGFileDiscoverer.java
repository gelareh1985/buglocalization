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

import org.eclipse.core.resources.IFile;

import eu.artist.migration.mdt.javaee.java.uml.provider.Java2UMLByFileProvider;
import eu.artist.migration.mdt.javaee.java.uml.util.Java2UMLUtil;

public class Java2UMLActivityCFGFileDiscoverer extends Java2UMLActivityCFGBasicDiscoverer<IFile> {
	
	public static String ID = "eu.artist.migration.mdt.javaee.java.umlactivity.cfg.file";

	public Java2UMLActivityCFGFileDiscoverer() {
		super(new Java2UMLByFileProvider("activity.cfg"));
	}

	@Override
	public boolean isApplicableTo(IFile source) {
		return Java2UMLUtil.isJavaModel(source);
	}

}
