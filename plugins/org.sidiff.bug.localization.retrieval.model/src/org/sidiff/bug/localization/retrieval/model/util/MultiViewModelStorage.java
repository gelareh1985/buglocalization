package org.sidiff.bug.localization.retrieval.model.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.bug.localization.retrieval.model.multiview.SystemModel;
import org.sidiff.bug.localization.retrieval.model.multiview.View;

public class MultiViewModelStorage {

	public static void saveAll(SystemModel systemModel, Map<?, ?> options) {
		Set<Resource> resources = new HashSet<>();
		resources.add(systemModel.eResource());
		
		for (View view : systemModel.getViews()) {
			resources.add(view.getModel().eResource());
		}
		
		for (Resource resource : resources) {
			try {
				resource.save(options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
