package org.sidiff.bug.localization.dataset.graph.data.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.graph.data.TableData;
import org.sidiff.bug.localization.dataset.graph.data.TableDataEntry;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.ModelElementConverter;

public class Nodes extends TableData {

	private ModelElementConverter<String> commentConverter;
	
	private List<ModelElementConverter<?>> converters;
	
	public Nodes(ModelElementConverter<String> commentConverter, List<ModelElementConverter<?>> converters) {
		super(converters.size());
		this.commentConverter = commentConverter;
		this.converters = converters;
	}
	
	public Nodes(ModelElementConverter<String> commentConverter, ModelElementConverter<?>... converter) {
		this(commentConverter, Arrays.asList(converter));
	}
	
	public void addNodes(Iterable<EObject> modelElements) {
		for (EObject modelElement : modelElements) {
			addNode(modelElement);
		}
	}

	public void addNode(EObject modelElement) {
		TableDataEntry node = addEntry();
		Object[] attributes = node.getValues();
		
		for (int i = 0; i < converters.size(); i++) {
			attributes[i] = converters.get(i).convert(modelElement);
		}
		
		node.setComment(commentConverter.convert(modelElement));
	}
}