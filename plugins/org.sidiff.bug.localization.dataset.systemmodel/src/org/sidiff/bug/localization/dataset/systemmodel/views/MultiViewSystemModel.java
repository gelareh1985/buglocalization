package org.sidiff.bug.localization.dataset.systemmodel.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiView;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.MultiviewFactory;
import org.sidiff.bug.localization.dataset.systemmodel.multiview.View;

public class MultiViewSystemModel {

	public static final String MULITVIEW_MODEL_FILE_EXTENSION = "multiview";
	
	private MultiView multiView;
	
	public MultiViewSystemModel() {
		this.multiView = MultiviewFactory.eINSTANCE.createMultiView();
		setURI(URI.createURI("")); // dummy
	}
	
	public MultiViewSystemModel(URI uri) {
		this.multiView = MultiviewFactory.eINSTANCE.createMultiView();
		setURI(uri);
	}
	
	public MultiViewSystemModel(Path file) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource multiViewResource = resourceSet.getResource(URI.createFileURI(file.toString()), true);
		this.multiView = (MultiView) multiViewResource.getContents().get(0);
	}
	
	public MultiViewSystemModel(MultiView multiView) {
		this.multiView = multiView;
		
		if ((multiView.eResource() == null) || (multiView.eResource().getResourceSet() == null)) {
			setURI(URI.createURI("")); // dummy
		}
	}
	
	public void setURI(URI uri) {
		if (multiView.eResource() == null) {
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource multiViewResource = resourceSet.createResource(uri);
			multiViewResource.getContents().add(multiView);
		} else {
			multiView.eResource().setURI(uri);
		}
	}
	
	public void addView(Resource resource, ViewDescription viewDescription) {
		multiView.eResource().getResourceSet().getResources().add(resource);
		
		for (EObject rootElement : resource.getContents()) {
			View view = MultiviewFactory.eINSTANCE.createView();
			view.setModel(rootElement);
			view.setName(viewDescription.getName());
			view.setDescription(viewDescription.getDescription());
			view.setKind(viewDescription.getViewKind());
			
			multiView.getViews().add(view);
		}
	}
	
	public Resource getViewByKind(ViewDescription viewDescription) {
		
		for (View view : multiView.getViews()) {
			if (view.getKind().equals(viewDescription.getViewKind())) {
				return view.getModel().eResource();
			}
		}
		
		return null;
	}
	
	public boolean containsViewKind(ViewDescription viewDescription) {
		
		for (View view : multiView.getViews()) {
			if (view.getKind().equals(viewDescription.getViewKind())) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean removeViewKind(ViewDescription viewDescription) {
		View toBeRemoved = null;
		
		for (View view : multiView.getViews()) {
			if (view.getKind().equals(viewDescription.getViewKind())) {
				toBeRemoved = view;
			}
		}
		
		return multiView.getViews().remove(toBeRemoved);
	}

	public MultiView getMultiViewModel() {
		return multiView;
	}
	
	public void saveAll(Map<?, ?> options) {
		saveAll(options, Collections.emptySet());
	}

	public void saveAll(Map<?, ?> options, Set<Resource> exclude) {
		URI baseURI = multiView.eResource().getURI().trimSegments(1);
		Set<Resource> resources = new HashSet<>();
		resources.add(multiView.eResource());
		
		for (View view : multiView.getViews()) {
			resources.add(view.getModel().eResource());
		}
		
		resources.removeAll(exclude);
		
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
