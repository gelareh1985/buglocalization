package org.sidiff.bug.localization.dataset.systemmodel.discovery;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.modisco.java.CompilationUnit;
import org.eclipse.modisco.java.Model;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromProject;
import org.eclipse.modisco.kdm.source.extension.discovery.SourceVisitListener;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.IncrementalDiscoverJavaModelFromProject;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental.IncrementalJavaParser;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;

public class JavaProject2JavaSystemModel {
	
	private IncrementalJavaParser javaParser;
	
	public JavaProject2JavaSystemModel() {
	}
	
	public JavaProject2JavaSystemModel(IncrementalJavaParser javaParser) {
		this.javaParser = javaParser;
	}

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
		DiscoverJavaModelFromProject javaDiscoverer;
		
		// Use incremental Java parser:
		if (javaParser != null) {
			javaDiscoverer = new IncrementalDiscoverJavaModelFromProject(project, javaParser);
			javaDiscoverer.setDeepAnalysis(!javaParser.isIgnoreMethodBodies());
			javaDiscoverer.setRefreshSourceBeforeDiscovery(true);
		} else {
			javaDiscoverer = new DiscoverJavaModelFromProject();
		}
		
		// Check for new/removed files in the project:
		javaDiscoverer.setRefreshSourceBeforeDiscovery(true);
		
		// Listen to change locations:
		if (discovererListener != null) {
			javaDiscoverer.addSourceVisitListener(discovererListener);
		}

		// START
		javaDiscoverer.discoverElement(project, subMonitor.split(90));
		
		// Read result:
		Resource javaResource = javaDiscoverer.getTargetModel();
		javaResource.setURI(targetURI.trimFileExtension().trimFileExtension().appendFileExtension("java.xmi"));
		makeProjectRelativePaths(javaResource, project);

		systemModel.setName(project.getName());
		systemModel.addView(javaResource, ViewDescriptions.JAVA_MODEL);
		
		return systemModel;
	}

	private void makeProjectRelativePaths(Resource javaResource, IProject project) {
		Path projectContainerPath = Paths.get(project.getLocation().toFile().toString()).getParent();
		Model javaModel = (Model) javaResource.getContents().get(0);
		
		for (CompilationUnit compilationUnit : javaModel.getCompilationUnits()) {
			Path compilationUnitPath = Paths.get(compilationUnit.getOriginalFilePath());
			Path relativeCompilationUnitPath = projectContainerPath.relativize(compilationUnitPath);
			compilationUnit.setOriginalFilePath(relativeCompilationUnitPath.toString().replace("\\", "/"));
		}
	}
}
