package org.sidiff.bug.localization.dataset.retrieval.discoverer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.retrieval.util.SystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaModel2UMLClassSystemModel;

public class UMLClassSystemModelDiscoverer implements SystemModelDiscoverer {

	@Override
	public void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException {
		JavaModel2UMLClassSystemModel java2UmlClass = new JavaModel2UMLClassSystemModel();
		SystemModel umlSystemModel = java2UmlClass.discover(javaSystemModel, new NullProgressMonitor());
		SystemModelDiscoverer.moveViews(umlSystemModel, systemModel);
	}

}