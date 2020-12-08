package org.sidiff.bug.localization.dataset.database.query;

import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherGraphDelta extends ModelCypherEdgeDelta {

	public ModelCypherGraphDelta(
			int oldVersion, Map<XMLResource, XMLResource> oldResourcesMatch, 
			int newVersion, Map<XMLResource, XMLResource> newResourcesMatch) {
		super(oldVersion, oldResourcesMatch, newVersion, newResourcesMatch);
	}

	public String createGraphDelta() {
		internal_createNodesDeltas();
		internal_createEdgesDeltas();
		return compileQuery();
	}

}
