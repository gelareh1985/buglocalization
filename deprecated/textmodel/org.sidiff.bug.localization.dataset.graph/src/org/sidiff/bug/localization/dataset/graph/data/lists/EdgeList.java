package org.sidiff.bug.localization.dataset.graph.data.lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.sidiff.bug.localization.dataset.graph.data.TableData;
import org.sidiff.bug.localization.dataset.graph.data.TableDataEntry;
import org.sidiff.bug.localization.dataset.graph.data.lists.converters.ModelReferenceConverter;
import org.sidiff.bug.localization.dataset.graph.data.lists.converters.ModelReferenceTest;

public class EdgeList extends TableData {

	private List<ModelReferenceConverter<?>> converter;
	
	private ModelReferenceTest test;
	
	public EdgeList(ModelReferenceTest test, List<ModelReferenceConverter<?>> converter) {
		super(converter.size());
		this.test = test;
		this.converter = converter;
	}
	
	public EdgeList(ModelReferenceTest test, ModelReferenceConverter<?>... converter) {
		this(test, Arrays.asList(converter));
	}
	
	public void addEdges(Iterable<EObject> modelElements, boolean allowParallelEdges) {
		for (EObject modelElement : modelElements) {
			if (modelElement != null) {
				addEdges(modelElement, allowParallelEdges);
			} else {
				System.err.println("Null-Element in Edge-List.");
			}
		}
	}

	public void addEdges(EObject modelElement, boolean allowParallelEdges) {
		
		// Filter parallel references of different types?
		Set<EObject> adjacent = Collections.emptySet();
		
		if (!allowParallelEdges) {
			adjacent = new HashSet<>();
		}
		
		// Process outgoing references:
		for (EReference reference : modelElement.eClass().getEAllReferences()) {
			if (reference.isMany()) {
				for (Object targetElement : (Iterable<?>) modelElement.eGet(reference)) {
					if ((targetElement instanceof EObject) && !adjacent.contains(targetElement)) {
						if (test.test(modelElement, reference, (EObject) targetElement)) {
							convert(modelElement, reference, (EObject) targetElement);

							if (!allowParallelEdges) {
								adjacent.add((EObject) targetElement);
							}
						}
					}
				}
			} else {
				Object targetElement = modelElement.eGet(reference);

				if ((targetElement instanceof EObject) && !adjacent.contains(targetElement)) {
					if (test.test(modelElement, reference, (EObject) targetElement)) {
						convert(modelElement, reference, (EObject) targetElement);

						if (!allowParallelEdges) {
							adjacent.add((EObject) targetElement);
						}
					}
				}
			}
		}
	}

	protected TableDataEntry convert(EObject source, EReference type, EObject target) {
		TableDataEntry edge = addEntry();
		Object[] attributes = edge.getValues();
		
		for (int i = 0; i < converter.size(); i++) {
			attributes[i] = converter.get(i).convert(source, type, target);
		}
		
		return edge;
	}
}
