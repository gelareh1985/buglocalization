package org.sidiff.bug.localization.dataset.systemmodel.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.View;

public class MultiViewModelStorage {
	
	public static final String MULITVIEW_MODEL_FILE_EXTENSION = "multiview";

	public static void saveAll(MultiView systemModel, Map<?, ?> options) {
		URI baseURI = systemModel.eResource().getURI().trimSegments(1);
		Set<Resource> resources = new HashSet<>();
		resources.add(systemModel.eResource());
		
		for (View view : systemModel.getViews()) {
			resources.add(view.getModel().eResource());
		}
		
		for (Resource resource : resources) {
			try {
				String fileName = resource.getURI().segment(resource.getURI().segmentCount() - 1);
				URI fileURI = baseURI.appendSegment(fileName);
				resource.setURI(fileURI);
				
				resource.save(options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
