package org.sidiff.bug.localization.dataset.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.neo4j.driver.Record;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherEdgeDelta;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherNodeDelta;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.history.util.HistoryIterator;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;

public class DatasetExportApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";

	public static final String ARGUMENT_MODEL_REPOSITORY = "-modelrepository";
	
	public static final String ARGUMENT_DATABASE_CONNECTION = "-databaseconnection";
	
	public static final String ARGUMENT_DATABASE_NAMES = "-databasename";
	
	public static final String ARGUMENT_DATABASE_PASSWORD = "-databasepassword";
	
	public static final String ARGUMENT_DATABASE_CLEAR = "-clear";

	private Path datasetPath;

	private DataSet dataset;

	private GitRepository modelRepository;
	
	// TODO: Save version number to Git identification mapping
	// TODO: Incremental Update
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		// bolt://localhost:7687 , neo4j , password
		String databaseConnection = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_CONNECTION);
		String databaseName = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_NAMES);
		String databasePassword = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_PASSWORD);
		
		if (ApplicationUtil.containsProgramArgument(context, ARGUMENT_DATABASE_CLEAR)) {
			clear(databaseConnection, databaseName, databasePassword);
//			test();
//			return IApplication.EXIT_OK;
		}
		
		this.datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);

		Path modelRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_MODEL_REPOSITORY);
		this.modelRepository = new GitRepository(modelRepositoryPath.toFile()); 
		URI repositoryBaseURI = URI.createFileURI(modelRepositoryPath.toString());

		HistoryIterator historyIterator = new HistoryIterator(dataset.getHistory());
		int historyVersion = -1;
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseConnection, databaseName, databasePassword)) {
			SystemModel systemModelOld = null;
			
			while (historyIterator.hasNext()) {
				++historyVersion;
				
				System.out.println("Remaining Versions: " + (historyIterator.nextIndex() + 1));
				Version currentVersion = historyIterator.next();

				long time = System.currentTimeMillis();
				modelRepository.checkout(dataset.getHistory(), currentVersion);
				Path systemModelPath = getRepositoryFile(dataset.getSystemModel());
				
				time = stopTime(time, "Checkout");

				if (Files.exists(systemModelPath)) {
					try {
						
						time = System.currentTimeMillis();
						SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel(systemModelPath, true);
						
						time = stopTime(time, "Loading");
						
						time = System.currentTimeMillis();
						Map<XMLResource, XMLResource> oldResourcesMatch = matchResources(systemModelOld, systemModel);
						Map<XMLResource, XMLResource> newResourcesMatch = matchResources(systemModel, systemModelOld);
						
						ModelCypherNodeDelta nodeDeltaDerivation = new ModelCypherNodeDelta(
								historyVersion - 1, oldResourcesMatch,
								historyVersion, newResourcesMatch,
								repositoryBaseURI);
						
						Map<String, Map<String, Object>> removedNodeQueries = nodeDeltaDerivation.removedNodesBatchQueries();
						Map<String, Map<String, Object>> createdNodeQueries = nodeDeltaDerivation.createdNodesBatchQueries();
						
						ModelCypherEdgeDelta edgeDeltaDerivation = new ModelCypherEdgeDelta(
								historyVersion - 1, oldResourcesMatch,
								historyVersion, newResourcesMatch,
								repositoryBaseURI);
						
						Map<String, Map<String, Object>> removedEdgeQueries = edgeDeltaDerivation.removedEdgesDelta();
						Map<String, Map<String, Object>> createdEdgeQueries = edgeDeltaDerivation.createdEdgesDelta();
						
						time = stopTime(time, "Compute Queries");
						
						/*
						 * Make sure all nodes have an index for creation of edges.
						 */
						createNodeKeyAttributes(transaction, nodeDeltaDerivation);
						
						/*
						 *  Increase all latest version IDs and remove nodes in the next step.
						 */
						transaction.execute(nodeDeltaDerivation.increaseVersion());
						
						/*
						 * NOTE: Remove edges before removing nodes, otherwise source and target nodes
						 * might have different version number.
						 */
						transaction.execute(removedEdgeQueries);
						
						/*
						 * NOTE: Remove old nodes before creating new nodes, e.g., to correctly match
						 * node with same ID but changed attribute values.
						 */
						transaction.execute(removedNodeQueries);
						transaction.execute(createdNodeQueries);
						transaction.execute(createdEdgeQueries);
						time = stopTime(time, "Write Transaction");
						
						/*
						 * Atomic commit of new model version.
						 */
						transaction.commit();
						time = stopTime(time, "Commit Transaction");
						
						systemModelOld = systemModel;
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}

		return IApplication.EXIT_OK;
	}

	private void createNodeKeyAttributes(Neo4jTransaction transaction, ModelCypherNodeDelta nodeDeltaDerivation) {
		transaction.commit();
		
		List<String> nodeKeyAttributes = nodeDeltaDerivation.createKeyAttributes();
		
		for (String string : nodeKeyAttributes) {
			transaction.execute(string);
		}
		
		transaction.commit();
	}
	
	private Map<XMLResource, XMLResource> matchResources(SystemModel systemModelA, SystemModel systemModelB) {
		Map<XMLResource, XMLResource> resourcesMatch = new HashMap<>();
		
		List<Resource> resourceSetA = Collections.emptyList();
		
		if (systemModelA != null) {
			resourceSetA = systemModelA.eResource().getResourceSet().getResources();
		}
		
		List<Resource> resourceSetB = Collections.emptyList();
		
		if (systemModelB != null) {
			resourceSetB = systemModelB.eResource().getResourceSet().getResources();
		}
		
		for (Resource resourceA : resourceSetA) {
			resourcesMatch.put((XMLResource) resourceA, (XMLResource) matchResource(resourceA, resourceSetB));
		}
		
		return resourcesMatch;
	}

	private Resource matchResource(Resource resourceA, List<Resource> resourceSetB) {
		URI uriA = resourceA.getURI();
		
		for (Resource resourceB : resourceSetB) {
			if (uriA.equals(resourceB.getURI())) {
				return resourceB;
			}
		}
		
		return null;
	}

	public Path getRepositoryFile(Path localPath) {
		return modelRepository.getWorkingDirectory().resolve(localPath);
	}
	
	private void clear(String databaseConnection, String databaseName, String databasePassword) throws Exception {
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseConnection, databaseName, databasePassword)) {
			transaction.execute(ModelCypherEdgeDelta.clearEdges());
			transaction.execute(ModelCypherNodeDelta.clearNodes());
			transaction.commit();
			
			for (Record record : transaction.execute("SHOW CONSTRAINTS").list()) {
				transaction.execute("DROP CONSTRAINT " + record.get("name").asString());
				transaction.commit();
			}
			
			for (Record record : transaction.execute("SHOW INDEXES").list()) {
				transaction.execute("DROP INDEX " + record.get("name").asString());
				transaction.commit();
			}
		}
	}
	
	private long stopTime(long startTime, String text) {
		System.out.println(text + ": " + (System.currentTimeMillis() - startTime) + "ms");
		return System.currentTimeMillis();
	}

	@SuppressWarnings("unused")
	private void test() throws Exception {
		ResourceSet resourceSetV0 = new ResourceSetImpl();
		XMLResource umlResourceV0 = (XMLResource) resourceSetV0.getResource(URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/test/1/testdi.uml", true), true);
		
		Map<XMLResource, XMLResource> oldResourcesMatch = new HashMap<>();
		Map<XMLResource, XMLResource> newResourcesMatch = new HashMap<>();
		newResourcesMatch.put(umlResourceV0, null);
		
		ModelCypherNodeDelta nodeDeltaV0 = new ModelCypherNodeDelta(-1, oldResourcesMatch, 0, newResourcesMatch, null);
		ModelCypherEdgeDelta edgeDeltaV0 = new ModelCypherEdgeDelta(-1, oldResourcesMatch, 0, newResourcesMatch, null);
		
		Map<String, Map<String, Object>> removedNodeQueriesV0 = nodeDeltaV0.removedNodesBatchQueries();
		Map<String, Map<String, Object>> createdNodeQueriesV0 = nodeDeltaV0.createdNodesBatchQueries();
		Map<String, Map<String, Object>> removedEdgeQueriesV0 = edgeDeltaV0.removedEdgesDelta();
		Map<String, Map<String, Object>> createdEdgeQueriesV0 = edgeDeltaV0.createdEdgesDelta();
		
		try (Neo4jTransaction transaction = new Neo4jTransaction("bolt://localhost:7687", "neo4j", "password")) {
			createNodeKeyAttributes(transaction, nodeDeltaV0);
			
			transaction.execute(nodeDeltaV0.increaseVersion());
			transaction.execute(edgeDeltaV0.increaseVersion());
			
			transaction.execute(removedEdgeQueriesV0);
			transaction.execute(removedNodeQueriesV0);
			transaction.execute(createdNodeQueriesV0);
			transaction.execute(createdEdgeQueriesV0);
			
			transaction.commit();
		}
		
		System.out.println("######################################## NEW VERSION ########################################");
		
		ResourceSet resourceSetV1 = new ResourceSetImpl();
		XMLResource umlResourceV1 = (XMLResource) resourceSetV1.getResource(URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/test/2/testdi.uml", true), true);
		
		oldResourcesMatch = new HashMap<>();
		newResourcesMatch = new HashMap<>();
		oldResourcesMatch.put(umlResourceV0, umlResourceV1);
		newResourcesMatch.put(umlResourceV1, umlResourceV0);
		
		ModelCypherNodeDelta nodeDeltaV1 = new ModelCypherNodeDelta(0, oldResourcesMatch, 1, newResourcesMatch, null);
		ModelCypherEdgeDelta edgeDeltaV1 = new ModelCypherEdgeDelta(0, oldResourcesMatch, 1, newResourcesMatch, null);
		
		Map<String, Map<String, Object>> removedNodeQueriesV1 = nodeDeltaV1.removedNodesBatchQueries();
		Map<String, Map<String, Object>> createdNodeQueriesV1 = nodeDeltaV1.createdNodesBatchQueries();
		Map<String, Map<String, Object>> removedEdgeQueriesV1 = edgeDeltaV1.removedEdgesDelta();
		Map<String, Map<String, Object>> createdEdgeQueriesV1 = edgeDeltaV1.createdEdgesDelta();
		
		try (Neo4jTransaction transaction = new Neo4jTransaction("bolt://localhost:7687", "neo4j", "password")) {
			createNodeKeyAttributes(transaction, nodeDeltaV1);
			transaction.commit();
			
			transaction.execute(nodeDeltaV1.increaseVersion());
			transaction.execute(edgeDeltaV1.increaseVersion());
			
			transaction.execute(removedEdgeQueriesV1);
			transaction.execute(createdNodeQueriesV1);
			transaction.execute(removedNodeQueriesV1);
			transaction.execute(createdEdgeQueriesV1);
			
			transaction.commit();
		}
	}

	@Override
	public void stop() {
	}

}
