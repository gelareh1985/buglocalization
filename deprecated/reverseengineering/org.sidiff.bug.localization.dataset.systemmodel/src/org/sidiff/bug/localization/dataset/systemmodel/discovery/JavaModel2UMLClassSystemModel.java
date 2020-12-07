package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.discoverer.JavaModel2UMLClassSystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;

public class JavaModel2UMLClassSystemModel {

	public SystemModel discover(SystemModel javaSystemModel, IProgressMonitor monitor) throws DiscoveryException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		
		Resource javaResource = javaSystemModel.getViewByKind(ViewDescriptions.JAVA_MODEL).getModel().eResource();
		
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlclass/src/eu/artist/migration/mdt/javaee/java/umlclass/Java2UMLDiscoverer.java

		JavaModel2UMLClassSystemModelDiscoverer umlDiscoverer = new JavaModel2UMLClassSystemModelDiscoverer(javaSystemModel.eResource());
		umlDiscoverer.discoverElement(javaResource, subMonitor.split(100));
		
		SystemModel umlSystemModel = umlDiscoverer.getUmlSystemModel();
		URI targetURI = javaSystemModel.eResource().getURI().trimFileExtension()
				.appendFileExtension(ViewDescriptions.UML_CLASS_DIAGRAM.getViewKind() + "." + SystemModel.FILE_EXTENSION);
		umlSystemModel.setURI(targetURI);
		
		return umlSystemModel;
	}
}
