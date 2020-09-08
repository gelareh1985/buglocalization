package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.discoverer.JavaModel2UMLFlowSystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;

public class JavaModel2UMLFlowSystemModel {

	public SystemModel discover(SystemModel javaSystemModel, IProgressMonitor monitor) throws DiscoveryException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		
		Resource javaResource = javaSystemModel.getViewByKind(ViewDescriptions.JAVA_MODEL).getModel().eResource();
		
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlactivity/src/eu/artist/migration/mdt/javaee/java/umlactivity/Java2UMLActivityDiscoverer.java
		// - /eu.artist.migration.mdt.javaee.java.umlactivity/src/eu/artist/migration/mdt/javaee/java/umlactivity/Java2UMLActivityDiscovererFull.java
		// FIXME: Java2UMLActivityDiagramResourceDiscoverer
		
		JavaModel2UMLFlowSystemModelDiscoverer umlDiscoverer = new JavaModel2UMLFlowSystemModelDiscoverer(javaSystemModel.eResource());
		umlDiscoverer.discoverElement(javaResource, subMonitor.split(100));
		
		SystemModel umlSystemModel = umlDiscoverer.getUmlSystemModel();
		URI targetURI = javaSystemModel.eResource().getURI().trimFileExtension()
				.appendFileExtension(ViewDescriptions.UML_CLASS_OPERATION_CONTROL_FLOW.getViewKind() + "." + SystemModel.FILE_EXTENSION);
		umlSystemModel.setURI(targetURI);

		return umlSystemModel;
	}
}
