package org.sidiff.bug.localization.diagram.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;

public class CreateAssociations {
	
	/**
	 * Source -> Target
	 */
	private Map<Classifier, Map<Classifier, List<OppositeProperties>>> opposites = new HashMap<>();
	
	private static class OppositeProperties {
		Property propertyA;
		Property propertyB;
	}
	
	public void apply(Model model) {
		createOppositeProperties(model);
		createAssociations(model);
	}

	private void createOppositeProperties(Model model) {
		
		// Search opposite properties for bidirectional associations:
		for (EObject element : (Iterable<EObject>) () -> model.eAllContents()) {
			if (element instanceof Property) {
				if (element.eContainer() instanceof Classifier) {
					addProperty((Classifier) element.eContainer(), (Property) element);
				}
			}
		}
	}
	
	private void addProperty(Classifier containing, Property property) {
		if (!(property.getType() instanceof Classifier)) {
			return;
		}
		
		// Is opposite (already existing)?
		if (opposites.containsKey(property.getType())) {
			List<OppositeProperties> opposite = opposites.get(property.getType()).get(containing);
			
			if (opposite != null) {
				for (OppositeProperties oppositeProperties : opposite) {
					if (oppositeProperties.propertyB == null) {
						oppositeProperties.propertyB = property;
						return;
					}
				}
			}
		}
		
		// Create new association:
		OppositeProperties opposite = new OppositeProperties();
		opposite.propertyA = property;
		
		if (!opposites.containsKey(containing)) {
			Map<Classifier, List<OppositeProperties>> properties = new HashMap<>();
			opposites.put(containing, properties);
		} 
		
		if (!opposites.get(containing).containsKey(property.getType())) {
			opposites.get(containing).put((Classifier) property.getType(), new ArrayList<>());
		}
		
		opposites.get(containing).get(property.getType()).add(opposite);
	}
	
	private void createAssociations(Model model) {
		for (Entry<Classifier, Map<Classifier, List<OppositeProperties>>> properties : opposites.entrySet()) {
			Classifier containing = properties.getKey();
			
			for (List<OppositeProperties> property : properties.getValue().values()) {
				for (OppositeProperties oppositeProperties : property) {
					Association association = UMLFactory.eINSTANCE.createAssociation();
					model.getPackagedElements().add(association);
					
					// Remote end:
					association.getMemberEnds().add(oppositeProperties.propertyA);
					
					// Local end:
					if (oppositeProperties.propertyB != null) {
						association.getMemberEnds().add(oppositeProperties.propertyB);
					} else {
						Property ownedProperty = UMLFactory.eINSTANCE.createProperty();
						ownedProperty.setType(containing);
						ownedProperty.setName(StringUtils.uncapitalize(containing.getName()));
						
						association.getOwnedEnds().add(ownedProperty);
						association.getMemberEnds().add(ownedProperty);
					}
				}
			}
		}
	}
}
