package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.AbstractModelDiscoverer;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromProject;
import org.sidiff.bug.localization.common.utilities.workspace.ProjectUtil;
import org.sidiff.bug.localization.dataset.systemmodel.views.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescription;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;

import eu.artist.migration.mdt.javaee.java.umlactivity.discovery.cfg.Java2UMLActivityCFGResourceDiscoverer;
import eu.artist.migration.mdt.javaee.java.umlclass.discovery.Java2UMLResourceDiscoverer;

public class JavaProject2SystemModelDiscoverer extends AbstractModelDiscoverer<IProject> {
	
	private ViewDescription[] views = ViewDescriptions.ALL_VIEWS;
	
	public JavaProject2SystemModelDiscoverer() {
	}
	
	public JavaProject2SystemModelDiscoverer(URI targetURI) {
		setTargetURI(targetURI);
	}
	
	public JavaProject2SystemModelDiscoverer(ViewDescription... views) {
		this.views = views;
	}
	
	@Override
	public boolean isApplicableTo(IProject project) {
		try {
			return ProjectUtil.isJavaProject(project);
		} catch (CoreException e) {
			return false;
		}
	}
	
	public boolean containsView(ViewDescription viewDescription) {
		for (ViewDescription view : views) {
			if (view.equals(viewDescription)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void basicDiscoverElement(IProject project, IProgressMonitor monitor) throws DiscoveryException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		
		if (getTargetURI() == null) {
			this.setTargetURI(URI.createPlatformResourceURI(project.getName() + "/" + project.getName() + ".multiview.xmi", true));
		}
		
		SystemModel systemModel = new SystemModel(getTargetURI());
		
		Resource javaResource = discoverJavaAST(systemModel, project, subMonitor.split(30));
		
		if (containsView(ViewDescriptions.UML_CLASS_DIAGRAM)) {
			discoverUMLClassDiagram(systemModel, javaResource, subMonitor.split(30));
		}
		
		if (containsView(ViewDescriptions.UML_CLASS_OPERATION_CONTROL_FLOW)) {
			discoverUMLOperationControlFlow(systemModel, javaResource, subMonitor.split(30));
		}
		
		setTargetModel(systemModel.getMultiViewModel().eResource());
	}
	

	public Resource discoverJavaAST(SystemModel systemModel, IProject project, IProgressMonitor monitor) throws DiscoveryException {
		
		// https://help.eclipse.org/2019-12/index.jsp?topic=%2Forg.eclipse.modisco.java.doc%2Fmediawiki%2Fjava_discoverer%2Fplugin_dev.html
		
		DiscoverJavaModelFromProject javaDiscoverer = new DiscoverJavaModelFromProject();
		javaDiscoverer.discoverElement(project, monitor);
		
		Resource javaResource = javaDiscoverer.getTargetModel();
	
		if (getTargetURI().toString().endsWith(".multiview.xmi")) {
			javaResource.setURI(getTargetURI().trimFileExtension().trimFileExtension().appendFileExtension("java.xmi"));
		} else {
			javaResource.setURI(getTargetURI().trimFileExtension().appendFileExtension("java.xmi"));
		}
		
		systemModel.getMultiViewModel().setName(project.getName());
		systemModel.addView(javaResource, ViewDescriptions.JAVA_MODEL);
		
		return javaResource;
	}

	public Resource discoverUMLClassDiagram(SystemModel systemModel, Resource javaResource, IProgressMonitor monitor) throws DiscoveryException {
		
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlclass/src/eu/artist/migration/mdt/javaee/java/umlclass/Java2UMLDiscoverer.java
		
		Java2UMLResourceDiscoverer umlDiscoverer = new Java2UMLResourceDiscoverer();
		umlDiscoverer.discoverElement(javaResource, monitor);
		
		Resource umlClassResource = umlDiscoverer.getTargetModel();
		
		systemModel.addView(umlClassResource, ViewDescriptions.UML_CLASS_DIAGRAM);
		return umlClassResource;
	}

	public Resource discoverUMLOperationControlFlow(SystemModel systemModel, Resource javaResource, IProgressMonitor monitor) throws DiscoveryException {
		// https://github.com/artist-project/ARTIST.git
		// - ARTIST/source/Tooling/migration/application-discovery-understanding/MDT/
		// - /eu.artist.migration.mdt.javaee.java.umlactivity/src/eu/artist/migration/mdt/javaee/java/umlactivity/Java2UMLActivityDiscoverer.java
		// - /eu.artist.migration.mdt.javaee.java.umlactivity/src/eu/artist/migration/mdt/javaee/java/umlactivity/Java2UMLActivityDiscovererFull.java
//		Java2UMLActivityDiagramResourceDiscoverer umlActivityDiscoverer = new Java2UMLActivityDiagramResourceDiscoverer(); // FIXME
		Java2UMLActivityCFGResourceDiscoverer umlActivityDiscoverer = new Java2UMLActivityCFGResourceDiscoverer();
		umlActivityDiscoverer.discoverElement(javaResource, monitor);

		Resource umlActivityResource = umlActivityDiscoverer.getTargetModel();
		
		systemModel.addView(umlActivityResource, ViewDescriptions.UML_CLASS_OPERATION_CONTROL_FLOW);
		return umlActivityResource;
	}
}
