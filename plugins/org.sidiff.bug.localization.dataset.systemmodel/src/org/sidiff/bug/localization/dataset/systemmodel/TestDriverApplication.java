package org.sidiff.bug.localization.dataset.systemmodel;

import java.util.Collections;
import java.util.logging.Level;

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
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Class;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2SystemModelDiscoverer;

public class TestDriverApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Activator.getLogger().setLevel(Level.FINE);

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("org.sidiff.bug.localization.examples.musicplayer");
		JavaProject2SystemModelDiscoverer multiViewModelDiscoverer = new JavaProject2SystemModelDiscoverer();
		multiViewModelDiscoverer.discoverElement(project, new NullProgressMonitor());
		
		Resource systemModelResource = multiViewModelDiscoverer.getTargetModel();
		SystemModel systemModel = (SystemModel) systemModelResource.getContents().get(0);
		
		for (View view : systemModel.getViews()) {
			for (EObject modelElement : (Iterable<EObject>) () -> view.getModel().eAllContents()) {
				if (modelElement instanceof Class) {
					System.out.println(modelElement);
				} else if (modelElement instanceof Activity) {
					System.out.println(modelElement);
				}
			}
		}

		systemModel.saveAll(Collections.emptyMap());
		
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
