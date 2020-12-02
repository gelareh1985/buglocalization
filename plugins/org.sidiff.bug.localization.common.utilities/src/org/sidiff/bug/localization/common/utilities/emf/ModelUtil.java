package org.sidiff.bug.localization.common.utilities.emf;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class ModelUtil {

	/**
	 * Collects all parent/container of the given model element.
	 * 
	 * @param collected Returns all collected parent/container elements.
	 * @param element   The starting child element.
	 * @param distance  the level of parent elements to be collected.
	 */
	public static void collectParents(Set<EObject> collected, EObject element, int distance) {
		if (element == null) {
			return;
		}
		
		EObject currentElement = element;
		
		for (int i = 0; i < distance; i++) {
			if (currentElement != null) {
				collected.add(currentElement);
				currentElement = currentElement.eContainer();
			}
		}
	}

	/**
	 * Collects all element on outgoing references of the given model element.
	 * 
	 * @param collected Returns all collected elements.
	 * @param element   The starting element.
	 * @param distance  The number of references in term of the distance from the
	 *                  starting element.
	 * @param edgeTest  Return <code>true</code> for all outgoing references that
	 *                  should considered.
	 */
	public static void collectOutgoingReferences(Set<EObject> collected, EObject element, int distance, Predicate<EReference> edgeTest) {
		if (element == null) {
			return;
		}
		
		collected.add(element);
		
		if (distance <= 0) {
			return;
		}
		
		for (EReference reference : element.eClass().getEAllReferences()) {
			if (edgeTest.test(reference)) {
				if (reference.isMany()) {
					for (Object targetElement : (Iterable<?>) element.eGet(reference)) {
						if (targetElement instanceof EObject) {
							collectOutgoingReferences(collected, (EObject) targetElement, distance - 1, edgeTest);
						}
					}
				} else {
					Object targetElement = element.eGet(reference);
					
					if (targetElement instanceof EObject) {
						collectOutgoingReferences(collected, (EObject) targetElement, distance - 1, edgeTest);
					}
				}
			}
		}
	}
	
	/**
	 * @param collected        Returns all collected elements.
	 * @param model            The root element of the model to be considered.
	 * @param modelElements    All initial elements serving as target elements.
	 * @param incomingDistance The number of references in term of the distance from
	 *                         the target element.
	 * @param edgeTest         Return <code>true</code> for all incoming references
	 *                         that should considered.
	 */
	public static void collectIncomingReferences(Set<EObject> collected, EObject model, Set<EObject> modelElements, int incomingDistance, Predicate<EReference> edgeTest) {
		Set<EObject> currentModelElements = new HashSet<>(modelElements);
		
		for (int i = 0; i < incomingDistance; i++) {
			
			for (EObject modelElement : (Iterable<EObject>) () -> model.eAllContents()) {
				if ((modelElement != null) && !collected.contains(modelElement)) {
					if (ModelUtil.isAdjacent(modelElement, currentModelElements, edgeTest)) {
						collected.add(modelElement);
					}
				}
			}
			
			currentModelElements.addAll(collected);
		}
	}
	
	/**
	 * Check if a given model element is adjacent to at least one element in a given
	 * set of model elements.
	 * 
	 * @param modelElement     A model element to be tested.
	 * @param adjacentElements A set of model element to be tested.
	 * @param edgeTest         Return <code>true</code> for all incoming references
	 * @return <code>true</code> if the given model element is adjacent to at least
	 *         one element in a given set of model elements; <code>false</code> otherwise.
	 */
	public static boolean isAdjacent(EObject modelElement, Set<EObject> adjacentElements, Predicate<EReference> edgeTest) {
		
		for (EReference reference : modelElement.eClass().getEAllReferences()) {
			if (edgeTest.test(reference)) {
				if (reference.isMany()) {
					for (Object targetElement : (Iterable<?>) modelElement.eGet(reference)) {
						if (adjacentElements.contains(targetElement)) {
							return true;
						}
					}
				} else {
					Object targetElement = modelElement.eGet(reference);
					
					if (adjacentElements.contains(targetElement)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
}
