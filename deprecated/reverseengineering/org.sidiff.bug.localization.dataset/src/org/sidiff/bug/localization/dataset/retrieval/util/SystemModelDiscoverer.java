package org.sidiff.bug.localization.dataset.retrieval.util;

import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;

@FunctionalInterface
public interface SystemModelDiscoverer {
	
	void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException;
	
	static void moveViews(SystemModel source, SystemModel target) {
		for (View sourceView : source.getViews().toArray(new View[0])) {
			target.eResource().getResourceSet().getResources().add(sourceView.getModel().eResource());
			target.getViews().add(sourceView);
		}
	}
}
