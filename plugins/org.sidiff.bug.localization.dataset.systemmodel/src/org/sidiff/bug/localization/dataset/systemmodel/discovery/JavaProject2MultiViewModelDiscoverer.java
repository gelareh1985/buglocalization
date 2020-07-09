package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.modisco.infra.discovery.core.AbstractModelDiscoverer;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromProject;
import org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewFactory;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.View;

import eu.artist.migration.mdt.javaee.java.umlactivity.discovery.cfg.Java2UMLActivityCFGResourceDiscoverer;
import eu.artist.migration.mdt.javaee.java.umlclass.discovery.Java2UMLResourceDiscoverer;

public class JavaProject2MultiViewModelDiscoverer extends AbstractModelDiscoverer<IProject> {

	@Override
	public boolean isApplicableTo(IProject project) {
		try {
			return ProjectUtil.isJavaProject(project);
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	protected void basicDiscoverElement(IProject project, IProgressMonitor monitor) throws DiscoveryException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		
		if (getTargetURI() == null) {
			this.setTargetURI(URI.createPlatformResourceURI(project.getName() + "/" + project.getName() + ".multiview.xmi", true));
		}
		
		// Create Multi-View-Model:
		MultiView multiView = MultiviewFactory.eINSTANCE.createMultiView();
		
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource multiViewResource = resourceSet.createResource(getTargetURI());
		multiViewResource.getContents().add(multiView);
		
		// Discover Java:
		Resource javaResource = javaDiscovery(project, subMonitor.split(30));
		resourceSet.getResources().add(javaResource);
		addView(multiView, javaResource, "Java AST", "Java EMF-based AST", "java");
		
		// Java Model To UML class diagram:
		Resource umlClassResource = umlClassDiscovery(javaResource, subMonitor.split(30));
		resourceSet.getResources().add(umlClassResource);
		addView(multiView, umlClassResource, "UML Class Diagram", "Classes", "class");
		
		// Java Model To UML activity diagram (control flow graph):
		Resource umlActivityResource = umlActivityControlFlowGraphDiscovery(javaResource, subMonitor.split(30));
		resourceSet.getResources().add(umlActivityResource);
		addView(multiView, umlActivityResource, "UML Activity Diagram", "Operation Control Flow Graph", "cfg.activity");
		
		setTargetModel(multiViewResource);
	}

	private Resource javaDiscovery(IProject project, IProgressMonitor monitor) throws DiscoveryException {
		
		// https://help.eclipse.org/2019-12/index.jsp?topic=%2Forg.eclipse.modisco.java.doc%2Fmediawiki%2Fjava_discoverer%2Fplugin_dev.html
		
		DiscoverJavaModelFromProject javaDiscoverer = new DiscoverJavaModelFromProject();
		javaDiscoverer.discoverElement(project, monitor);
		
		Resource javaResource = javaDiscoverer.getTargetModel();
	
		if (getTargetURI().toString().endsWith(".multiview.xmi")) {
			javaResource.setURI(getTargetURI().trimFileExtension().trimFileExtension().appendFileExtension("java.xmi"));
		} else {
			javaResource.setURI(getTargetURI().trimFileExtension().appendFileExtension("java.xmi"));
		}
		return javaResource;
	}

	private Resource umlClassDiscovery(Resource javaResource, IProgressMonitor monitor) throws DiscoveryException {
		
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlclass/src/eu/artist/migration/mdt/javaee/java/umlclass/Java2UMLDiscoverer.java
		
		Java2UMLResourceDiscoverer umlDiscoverer = new Java2UMLResourceDiscoverer();
		umlDiscoverer.discoverElement(javaResource, monitor);
		
		Resource umlResource = umlDiscoverer.getTargetModel();
		return umlResource;
	}

	private Resource umlActivityControlFlowGraphDiscovery(Resource javaResource, IProgressMonitor monitor) throws DiscoveryException {
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlactivity/src/eu/artist/migration/mdt/javaee/java/umlactivity/Java2UMLActivityDiscoverer.java
		// - /eu.artist.migration.mdt.javaee.java.umlactivity/src/eu/artist/migration/mdt/javaee/java/umlactivity/Java2UMLActivityDiscovererFull.java
//		Java2UMLActivityDiagramResourceDiscoverer umlActivityDiscoverer = new Java2UMLActivityDiagramResourceDiscoverer(); // FIXME
		Java2UMLActivityCFGResourceDiscoverer umlActivityDiscoverer = new Java2UMLActivityCFGResourceDiscoverer();
		umlActivityDiscoverer.discoverElement(javaResource, monitor);

		Resource umlActivityResource = umlActivityDiscoverer.getTargetModel();
		return umlActivityResource;
	}

	private void addView(MultiView multiView, Resource umlClassResource, String name, String description, String viewKind) {
		
		for (EObject rootElement : umlClassResource.getContents()) {
			View view = MultiviewFactory.eINSTANCE.createView();
			view.setModel(rootElement);
			view.setName(name);
			view.setDescription(description);
			view.setKind(viewKind);
			
			multiView.getViews().add(view);
		}
		
	}

}
