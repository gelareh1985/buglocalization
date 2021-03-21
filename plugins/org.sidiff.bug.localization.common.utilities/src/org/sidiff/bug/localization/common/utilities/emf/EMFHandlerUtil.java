package org.sidiff.bug.localization.common.utilities.emf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class EMFHandlerUtil {
	
	public static List<URI> getSelectedURI(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		List<URI> selectedURIs = new ArrayList<>();

		if (selection instanceof IStructuredSelection) {
			for (Object selected : ((IStructuredSelection) selection).toList()) {
				if ((selected != null) && (selected instanceof IResource)) {
					URI uri = EMFHandlerUtil.getURI((IResource) selected);
					selectedURIs.add(uri);
				}
			}
		}
		
		return selectedURIs;
	}
	
	public static Resource getSelection(ExecutionEvent event, ResourceSet rss, int selectionIndex) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			if (selectionIndex < ((IStructuredSelection) selection).size()) {
				Object selected = ((IStructuredSelection) selection).toArray()[selectionIndex];
				
				if ((selected != null) && (selected instanceof IResource)) {
					URI uri = getURI((IResource) selected);
					return rss.getResource(uri, true);
				}
			}
		}
		
		return null;
	}

	public static Resource getSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			
			if ((selected != null) && (selected instanceof IResource)) {
				ResourceSet rss = new ResourceSetImpl();
				URI uri = getURI((IResource) selected);
				return rss.getResource(uri, true);
			}
		}
		
		return null;
	}
	
	public static URI getSelectionURI(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			
			if ((selected != null) && (selected instanceof IResource)) {
				return getURI((IResource) selected);
			}
		}
		
		return null;
	}
	
	public static <E extends EObject> E getSelection(ExecutionEvent event, Class<E> type, ResourceSet rss, int selectionIndex) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			if (selectionIndex < ((IStructuredSelection) selection).size()) {
				Object selected = ((IStructuredSelection) selection).toArray()[selectionIndex];

				if ((selected != null) && (selected instanceof IResource)) {
					return loadResource((IResource) selected, type, rss);
				}
			}
		}

		return null;
	}
	
	public static <E extends EObject> E getSelection(ExecutionEvent event, Class<E> type) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection).getFirstElement();
			
			if ((selected != null) && (selected instanceof IResource)) {
				return loadResource((IResource) selected, type, new ResourceSetImpl());
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends EObject> E loadResource(IResource resource, Class<E> type, ResourceSet rss) {
		URI uri = getURI(resource);
		Resource eResource = rss.getResource(uri, true);
		
		if ((eResource != null) && !eResource.getContents().isEmpty() 
				&& (type.isInstance(eResource.getContents().get(0)))) {
			
			return (E) eResource.getContents().get(0);
		}
		
		return null;
	}
	
	public static URI getURI(IResource workbenchResource) {

		String projectName = workbenchResource.getProject().getName();
		String filePath = workbenchResource.getProjectRelativePath().toOSString();
		String platformPath = projectName + "/" + filePath;

		return URI.createPlatformResourceURI(platformPath, true);
	}
}
