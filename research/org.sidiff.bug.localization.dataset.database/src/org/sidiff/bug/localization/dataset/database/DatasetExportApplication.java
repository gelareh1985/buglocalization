package org.sidiff.bug.localization.dataset.database;

import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.database.model.ModelDelta;
import org.sidiff.bug.localization.dataset.database.model.ModelHistory2Neo4j;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;

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
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		// bolt://localhost:7687 , neo4j , password
		String databaseConnection = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_CONNECTION);
		String databaseName = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_NAMES);
		String databasePassword = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_PASSWORD);
		
//		if (ApplicationUtil.containsProgramArgument(context, ARGUMENT_DATABASE_CLEAR)) {
//			test();
//			return IApplication.EXIT_OK;
//		}
		
		this.datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);

		Path modelRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_MODEL_REPOSITORY);
		this.modelRepository = new GitRepository(modelRepositoryPath.toFile()); 
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseConnection, databaseName, databasePassword)) {
			ModelHistory2Neo4j modelHistory2Neo4j = new ModelHistory2Neo4j(modelRepository, transaction);
			
			// Initially clear Neo4j database?
			if (ApplicationUtil.containsProgramArgument(context, ARGUMENT_DATABASE_CLEAR)) {
				modelHistory2Neo4j.clearDatabase();
			}
			
			// Write model repository history to Neo4j:
			modelHistory2Neo4j.commitHistory(dataset);
		}
		
		return IApplication.EXIT_OK;
	}

	public Path getRepositoryFile(Path localPath) {
		return modelRepository.getWorkingDirectory().resolve(localPath);
	}
	
	@Override
	public void stop() {
	}

	@SuppressWarnings("unused")
	private void test() throws Exception {
		String databaseURI = "bolt://localhost:7687";
		String databaseName = "neo4j";
		String databasePassword = "password";
		
		URI baseUriV0 = URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/test/1", true);
		ResourceSet resourceSetV0 = new ResourceSetImpl();
		XMLResource umlResourceV0 = (XMLResource) resourceSetV0.getResource(baseUriV0.appendSegment("testdi.uml"), true);

		URI baseUriV1 = URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/test/2", true);
		ResourceSet resourceSetV1 = new ResourceSetImpl();
		XMLResource umlResourceV1 = (XMLResource) resourceSetV1.getResource(baseUriV1.appendSegment("testdi.uml"), true);
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseURI, databaseName, databasePassword)) {
			ModelDelta modelDelta = new ModelDelta(transaction);
			modelDelta.clearDatabase();
			
			System.out.println("######################################## VERSION V0 ########################################");
			modelDelta.commitDelta(0, null, null, baseUriV0, Collections.singletonList(umlResourceV0));
			
			System.out.println("######################################## VERSION V1 ########################################");
			modelDelta.commitDelta(1, baseUriV0, Collections.singletonList(umlResourceV0), baseUriV1, Collections.singletonList(umlResourceV1));
		}
	}

}
