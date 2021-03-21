package org.sidiff.bug.localization.diagram.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jJsonDiagram {

	private List<Object[]> nodes;

	private List<Object[]> edges;

	public Map<Integer, Neo4jJsonNode> getNodesById() {
		Map<Integer, Neo4jJsonNode> nodeById = new HashMap<>();

		for (Object[] nodeData : nodes) {
			nodeById.put(((Double) nodeData[0]).intValue(), new Neo4jJsonNode(nodeData));
		}

		return nodeById;
	}

	public List<Neo4jJsonEdge> getEdges() {
		 List<Neo4jJsonEdge> outgoingEdges = new ArrayList<>();

		for (Object[] edgeData : this.edges) {
			outgoingEdges.add(new Neo4jJsonEdge(edgeData));
		}

		return outgoingEdges;
	}
}
