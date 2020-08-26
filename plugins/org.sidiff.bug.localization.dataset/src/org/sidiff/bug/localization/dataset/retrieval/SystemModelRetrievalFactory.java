package org.sidiff.bug.localization.dataset.retrieval;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2SystemModelDiscoverer;

public class SystemModelRetrievalFactory {

	/**
	 * The discovery transformations from the Java model to the system model.
	 */
	private SystemModelDiscoverer[] systemModelDiscoverer;
	
	@FunctionalInterface
	public interface SystemModelDiscoverer {
		void discover(SystemModel systemModel, Resource javaResource) throws DiscoveryException;
	}
	
	public SystemModelRetrievalFactory(SystemModelDiscoverer[] javaModelToSystemModel) {
		this.systemModelDiscoverer = javaModelToSystemModel;
	}
	
	public SystemModelRetrievalFactory() {
		SystemModelDiscoverer umlClasses = (systemModel, javaResource) -> {
			JavaProject2SystemModelDiscoverer multiViewModelDiscoverer = new JavaProject2SystemModelDiscoverer();
			multiViewModelDiscoverer.discoverUMLClassDiagram(systemModel, javaResource, new NullProgressMonitor());
		};
		
		// FIXME: discover UML Operation Control Flow
		@SuppressWarnings("unused")
		SystemModelDiscoverer umlOperationControlFlow = (systemModel, javaResource) -> {
			JavaProject2SystemModelDiscoverer multiViewModelDiscoverer = new JavaProject2SystemModelDiscoverer();
			multiViewModelDiscoverer.discoverUMLOperationControlFlow(systemModel, javaResource, new NullProgressMonitor());
		};
		
		systemModelDiscoverer = new SystemModelDiscoverer[] {umlClasses};
	}

	public void discover(SystemModel systemModel, Resource javaModel) throws DiscoveryException {
		for (SystemModelDiscoverer systemModelDiscovery : systemModelDiscoverer) {
			systemModelDiscovery.discover(systemModel, javaModel);
		}
	}
	
	public void setSystemModelDiscoverer(SystemModelDiscoverer[] systemModelDiscoverer) {
		this.systemModelDiscoverer = systemModelDiscoverer;
	}
}
