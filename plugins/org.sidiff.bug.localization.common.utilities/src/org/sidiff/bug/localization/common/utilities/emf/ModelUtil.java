package org.sidiff.bug.localization.common.utilities.emf;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;

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
	 * @param models           The root elements of the model to be considered.
	 * @param modelElements    All initial elements serving as target elements.
	 * @param incomingDistance The number of references in term of the distance from
	 *                         the target element.
	 * @param edgeTest         Return <code>true</code> for all incoming references
	 *                         that should considered.
	 */
	public static Set<EObject> collectIncomingReferences(List<EObject> models, Set<EObject> modelElements, Function<EClass, Integer> incomingDistance, Predicate<EReference> edgeTest) {
		
		// Collect all model elements within the maximum distance from the full model:
		int maxIncomingDistance = 0;
		
		for (EObject modelElement : modelElements) {
			Integer modelElementIncomingDistance = incomingDistance.apply(modelElement.eClass());
			
			if (modelElementIncomingDistance != null) {
				if (modelElementIncomingDistance > maxIncomingDistance) {
					maxIncomingDistance = modelElementIncomingDistance;
				}
			}
		}

		Set<EObject> maxDistanceCollected  = new HashSet<>(); 

		for (EObject model : models) {
			maxDistanceCollected.addAll(
					collectIncomingReferences((Iterable<EObject>) () -> model.eAllContents(), 
					modelElements, maxIncomingDistance, edgeTest));
		}
		
		// Collect all model elements within the type specific distance from the model elements within maximum distance:
		Set<EObject> collected  = new HashSet<>(modelElements);
		
		for (EObject modelElement : modelElements) {
			int currentDistance = incomingDistance.apply(modelElement.eClass());
			
			if (currentDistance > 0) {
				collected.addAll(collectIncomingReferences(
						maxDistanceCollected, Collections.singleton(modelElement), currentDistance, edgeTest));
			}
		}
		
		return collected;
	}
	
	private static Set<EObject> collectIncomingReferences(Iterable<EObject> model, Set<EObject> modelElements, int incomingDistance, Predicate<EReference> edgeTest) {
		Set<EObject> collected  = new HashSet<>();
		Set<EObject> currentModelElements = new HashSet<>();
		
		for (int distance = 1; distance <= incomingDistance; distance++) {
			Set<EObject> nextModelElements = new HashSet<>();
			
			for (EObject modelElement : model) {
				if ((modelElement != null) && !collected.contains(modelElement)) { //no loops
					if (ModelUtil.isAdjacent(modelElement, currentModelElements, edgeTest)) {
						collected.add(modelElement);
						nextModelElements.add(modelElement);
					}
				}
			}
			
			currentModelElements = nextModelElements;
		}
		
		return collected;
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
					
					if ((targetElement != null) && adjacentElements.contains(targetElement)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * @param modelElement A model element.
	 * @return The labeling text; typically the name or a string attribute.
	 */
	public static String getLabel(EObject modelElement) {
		if (modelElement != null) {
			EStructuralFeature lableFeature = getLabelFeature(modelElement.eClass());
			Object value = modelElement.eGet(lableFeature);
			
			if (value != null) {
				return value.toString();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	/**
	 * @param eClass A model element type.
	 * @return The labeling feature; typically the name or a string attribute.
	 */
	public static EStructuralFeature getLabelFeature(EClass eClass) {
		EAttribute result = null;
		
		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			if (!eAttribute.isMany() && eAttribute.getEType().getInstanceClass() != FeatureMap.Entry.class) {
				if ("name".equalsIgnoreCase(eAttribute.getName())) {
					result = eAttribute;
					break;
				} else if (result == null) {
					result = eAttribute;
				} else if (eAttribute.getEAttributeType().getInstanceClass() == String.class
						&& result.getEAttributeType().getInstanceClass() != String.class) {
					result = eAttribute;
				}
			}
		}
		return result;
	}
	
}
