package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromProject;
import org.eclipse.modisco.kdm.source.extension.discovery.SourceVisitListener;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;

public class JavaProject2JavaSystemModel {

	public SystemModel discover(IProject project, SourceVisitListener discovererListener, IProgressMonitor monitor) throws DiscoveryException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, subMonitor.split(10));
		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
		}
		
		URI targetURI = URI.createPlatformResourceURI(project.getName(), true).appendSegment(project.getName()).appendFileExtension("java.systemmodel");
		SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel(targetURI);
		
		// https://help.eclipse.org/2019-12/index.jsp?topic=%2Forg.eclipse.modisco.java.doc%2Fmediawiki%2Fjava_discoverer%2Fplugin_dev.html
		DiscoverJavaModelFromProject javaDiscoverer = new DiscoverJavaModelFromProject();
				
		// Listen to change locations:
		if (discovererListener != null) {
			javaDiscoverer.addSourceVisitListener(discovererListener);
		}

		javaDiscoverer.discoverElement(project, subMonitor.split(90));
		Resource javaResource = javaDiscoverer.getTargetModel();
		javaResource.setURI(targetURI.trimFileExtension().trimFileExtension().appendFileExtension("java.xmi"));

		systemModel.setName(project.getName());
		systemModel.addView(javaResource, ViewDescriptions.JAVA_MODEL);
		
		return systemModel;
	}
}
