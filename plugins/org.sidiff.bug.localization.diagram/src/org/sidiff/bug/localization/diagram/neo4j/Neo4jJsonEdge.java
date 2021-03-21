package org.sidiff.bug.localization.diagram.neo4j;

public class Neo4jJsonEdge {

	private Object[] edgeData;

	public Neo4jJsonEdge(Object[] edgeData) {
		this.edgeData = edgeData;
	}

	public String getType() {
		return (String) edgeData[0];
	}

	public int getFrom() {
		return ((Double) edgeData[1]).intValue();
	}

	public int getTo() {
		return ((Double) edgeData[2]).intValue();
	}
}
