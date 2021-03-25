package org.sidiff.bug.localization.diagram.neo4j;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.sidiff.bug.localization.common.utilities.json.JsonUtil;

public class Neo4jJsonToEcoreUML {
	
	public Resource convert(Path diagramJsonPath) throws FileNotFoundException {
		Neo4jJsonDiagram diagram = JsonUtil.parse(diagramJsonPath, Neo4jJsonDiagram.class);

		UMLPackage umlPackage = UMLPackage.eINSTANCE;
		UMLFactory umlFactory = UMLFactory.eINSTANCE;
		
		ResourceSet resourceSet = new ResourceSetImpl();
		XMLResource sliceResource = (XMLResource) resourceSet
				.createResource(URI.createFileURI(diagramJsonPath.toString()).appendFileExtension("uml"));

		// Convert nodes:
		Map<Integer, Neo4jJsonNode> nodesById = diagram.getNodesById();

		for (Neo4jJsonNode node : nodesById.values()) {
			EClass modelType = (EClass) umlPackage.getEClassifier(node.getType());
			EObject modelElement = umlFactory.create(modelType);
			node.setModelElement(modelElement);

			for (Entry<String, Object> attribute : node.getAttributes().entrySet()) {
				if (!attribute.getKey().startsWith("__") && !attribute.getKey().endsWith("__")) {
					EAttribute attributeType = modelType.getEAllAttributes().stream()
							.filter(a -> a.getName().equals(attribute.getKey())).findFirst().get();
					String stringValue = attribute.getValue().toString();

					if (attribute.getValue() instanceof Double) {
						Double attributeDoubleValue = (Double) attribute.getValue();

						if ((attributeDoubleValue == Math.floor(attributeDoubleValue))
								&& !Double.isInfinite(attributeDoubleValue)) {
							stringValue = ((Double) attribute.getValue()).intValue() + "";
						}
					}

					Object attributeValue = attributeType.getEAttributeType().getEPackage().getEFactoryInstance()
							.createFromString(attributeType.getEAttributeType(), stringValue);
					modelElement.eSet(attributeType, attributeValue);
				} else if (attribute.getKey().equals("__model__element__id__")) {
					sliceResource.setID(modelElement, attribute.getValue().toString());
				}
			}
		}
		
		// Convert edges:
		List<Neo4jJsonEdge> outgoingEdges = diagram.getEdges();
		
		for (Neo4jJsonEdge edge : outgoingEdges) {
			EObject sourceModelElement = nodesById.get(edge.getFrom()).getModelElement();
			EObject targetModelElement = nodesById.get(edge.getTo()).getModelElement();
			EReference referenceType = sourceModelElement.eClass().getEAllReferences().stream()
					.filter(r -> r.getName().equals(edge.getType())).findFirst().get();
			
			if (referenceType.isMany()) {
				@SuppressWarnings("unchecked")
				Collection<EObject> references = (Collection<EObject>) sourceModelElement.eGet(referenceType);
				references.add(targetModelElement);
			} else {
				sourceModelElement.eSet(referenceType, targetModelElement);
			}
		}
		
		// Create UML slice:
		Model umlModel = umlFactory.createModel();
		umlModel.setName("Slice");
		
		for (Neo4jJsonNode node : nodesById.values()) {
			if (node.getModelElement() instanceof PackageableElement) {
				if (node.getModelElement().eContainer() == null) {
					umlModel.getPackagedElements().add((PackageableElement) node.getModelElement());
				}
			}
		}
		
		sliceResource.getContents().add(umlModel);
		
		return sliceResource;
	}
	
}
