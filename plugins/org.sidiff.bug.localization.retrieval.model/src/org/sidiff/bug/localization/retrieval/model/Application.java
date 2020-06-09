package org.sidiff.bug.localization.retrieval.model;

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.modisco.java.CompilationUnit;
import org.eclipse.modisco.java.Model;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromProject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Package;

import eu.artist.migration.mdt.javaee.java.umlclass.Java2UMLDiscoverer;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("org.sidiff.bug.localization.examples.musicplayer");
		
		// Discover Java:
		// https://help.eclipse.org/2019-12/index.jsp?topic=%2Forg.eclipse.modisco.java.doc%2Fmediawiki%2Fjava_discoverer%2Fplugin_dev.html
		DiscoverJavaModelFromProject javaDiscoverer = new DiscoverJavaModelFromProject ();
		javaDiscoverer.discoverElement(project, new NullProgressMonitor());
		
		Resource javaResource = javaDiscoverer.getTargetModel();
		Model javaModel = (Model) javaResource.getContents().get(0);
		
		// Save Java Model:
		javaResource.setURI(URI.createPlatformResourceURI(project.getName() + "/" + project.getName() + ".xmi", true));
		javaResource.save(Collections.emptyMap());
		
		// Print the list of Java classes in the model:
		for (CompilationUnit compilationUnit : javaModel.getCompilationUnits()) {
		    System.out.println(compilationUnit.getName());
		}
		
		// Java Model To UML Model:
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlclass/src/eu/artist/migration/mdt/javaee/java/umlclass/Java2UMLDiscoverer.java
		Java2UMLDiscoverer umlDiscoverer = new Java2UMLDiscoverer();
		umlDiscoverer.discoverElement(uriToIFile(javaResource.getURI()), new NullProgressMonitor());
		
		Resource umlResource = umlDiscoverer.getTargetModel();
		Package umlModel = (Package) umlResource.getContents().get(0);
		
		for (EObject umlElement : (Iterable<EObject>) () -> umlModel.eAllContents()) {
			if (umlElement instanceof Class) {
				System.out.println(umlElement);
			}
		}
		
		return IApplication.EXIT_OK;
	}
	
	/**
	 * Converts a {@link URI#isPlatformResource platform resource} {@link URI}
	 * (a platform resource with the first segment being "resource") to an {@link IFile}.
	 * @param uri the URI
	 * @return IFile for the given URI, <code>null</code> if the URI is not a platform resource URI
	 */
	public static IFile uriToIFile(URI uri) {
		if(uri.isPlatformResource() && uri.segmentCount() > 2) {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uri.toPlatformString(true)));
		}
		return null;
	}

	@Override
	public void stop() {
	}

}
