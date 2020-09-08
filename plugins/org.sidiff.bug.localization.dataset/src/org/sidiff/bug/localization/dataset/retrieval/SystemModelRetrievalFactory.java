package org.sidiff.bug.localization.dataset.retrieval;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaModel2UMLClassSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaModel2UMLFlowSystemModel;

public class SystemModelRetrievalFactory {

	/**
	 * The discovery transformations from the Java model to the system model.
	 */
	private SystemModelDiscoverer[] systemModelDiscoverer;
	
	@FunctionalInterface
	public interface SystemModelDiscoverer {
		void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException;
	}
	
	public SystemModelRetrievalFactory(SystemModelDiscoverer[] javaModelToSystemModel) {
		this.systemModelDiscoverer = javaModelToSystemModel;
	}
	
	public SystemModelRetrievalFactory() {
		SystemModelDiscoverer umlClasses = (SystemModel systemModel, SystemModel javaSystemModel) -> {
			JavaModel2UMLClassSystemModel java2UmlClass = new JavaModel2UMLClassSystemModel();
			SystemModel umlSystemModel = java2UmlClass.discover(javaSystemModel, new NullProgressMonitor());
			moveViews(umlSystemModel, systemModel);
		};
		
		// FIXME: discover UML Operation Control Flow
		@SuppressWarnings("unused")
		SystemModelDiscoverer umlOperationControlFlow = (SystemModel systemModel, SystemModel javaSystemModel) -> {
			JavaModel2UMLFlowSystemModel java2UmlClass = new JavaModel2UMLFlowSystemModel();
			SystemModel umlSystemModel = java2UmlClass.discover(javaSystemModel, new NullProgressMonitor());
			moveViews(umlSystemModel, systemModel);
		};
		
		systemModelDiscoverer = new SystemModelDiscoverer[] {umlClasses};
	}

	private void moveViews(SystemModel source, SystemModel target) {
		for (View sourceView : source.getViews().toArray(new View[0])) {
			target.eResource().getResourceSet().getResources().add(sourceView.eResource());
			target.getViews().add(sourceView);
		}
	}

	public void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException {
		for (SystemModelDiscoverer systemModelDiscovery : systemModelDiscoverer) {
			systemModelDiscovery.discover(systemModel, javaSystemModel);
		}
	}
	
	public void setSystemModelDiscoverer(SystemModelDiscoverer[] systemModelDiscoverer) {
		this.systemModelDiscoverer = systemModelDiscoverer;
	}
}
