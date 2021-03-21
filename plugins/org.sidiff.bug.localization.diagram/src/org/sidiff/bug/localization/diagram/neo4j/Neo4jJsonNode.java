package org.sidiff.bug.localization.diagram.neo4j;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

public class Neo4jJsonNode {
	
	private Object[] nodeData;
	
	private EObject modelElement;

	public Neo4jJsonNode(Object[] nodeData) {
		this.nodeData = nodeData;
	}

	public int getId() {
		return ((Double) nodeData[0]).intValue();
	}

	@SuppressWarnings("unchecked")
	public String getType() {
		return ((List<String>) nodeData[1]).get(0);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getAttributes() {
		return (Map<String, Object>) nodeData[2];
	}

	public EObject getModelElement() {
		return modelElement;
	}

	public void setModelElement(EObject modelElement) {
		this.modelElement = modelElement;
	}
}
