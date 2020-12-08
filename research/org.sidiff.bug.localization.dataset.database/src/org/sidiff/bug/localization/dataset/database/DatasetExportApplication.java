package org.sidiff.bug.localization.dataset.database;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherGraphDelta;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;

public class DatasetExportApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		ResourceSet resourceSetV0 = new ResourceSetImpl();
		XMLResource umlResourceV0 = (XMLResource) resourceSetV0.getResource(URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/test/1/testdi.uml", true), true);
		
		Map<XMLResource, XMLResource> oldResourcesMatch = new HashMap<>();
		Map<XMLResource, XMLResource> newResourcesMatch = new HashMap<>();
		newResourcesMatch.put(umlResourceV0, null);
		
		ModelCypherGraphDelta graphDeltaV0 = new ModelCypherGraphDelta(-1, oldResourcesMatch, 0, newResourcesMatch);
		String graphV0 = graphDeltaV0.createGraphDelta();
		System.out.println(graphV0);
		
		try (Neo4jTransaction transaction = new Neo4jTransaction("bolt://localhost:7687", "neo4j", "password")) {
			transaction.execute(graphDeltaV0.clearEdges());
			transaction.execute(graphDeltaV0.clearNodes());
			transaction.execute(graphV0);
		}
		
		System.out.println("############################## NEW VERSION ##############################");
		
		ResourceSet resourceSetV1 = new ResourceSetImpl();
		XMLResource umlResourceV1 = (XMLResource) resourceSetV1.getResource(URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/test/2/testdi.uml", true), true);
		
		oldResourcesMatch = new HashMap<>();
		oldResourcesMatch.put(umlResourceV0, umlResourceV1);
		newResourcesMatch = new HashMap<>();
		newResourcesMatch.put(umlResourceV1, umlResourceV0);
		
		ModelCypherGraphDelta graphDeltaV1 = new ModelCypherGraphDelta(0, oldResourcesMatch, 1, newResourcesMatch);
		String graphV1 = graphDeltaV1.createGraphDelta();
		System.out.println(graphV1);
		
		try (Neo4jTransaction transaction = new Neo4jTransaction("bolt://localhost:7687", "neo4j", "password")) {
			transaction.execute(graphV1);
		}
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
