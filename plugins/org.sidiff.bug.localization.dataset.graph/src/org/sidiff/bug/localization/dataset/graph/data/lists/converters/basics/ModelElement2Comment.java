package org.sidiff.bug.localization.dataset.graph.data.lists.converters.basics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.graph.data.lists.converters.ModelElementConverter;

public class ModelElement2Comment implements ModelElementConverter<String> {

	private Map<EObject, String> comments;
	
	public ModelElement2Comment() {
		this.comments = new HashMap<>();
	}
	
	public void addComment(EObject modelElement, String comment) {
		comments.put(modelElement, comment);
	}
	
	@Override
	public String convert(EObject modelElement) {
		return comments.get(modelElement);
	}

}
