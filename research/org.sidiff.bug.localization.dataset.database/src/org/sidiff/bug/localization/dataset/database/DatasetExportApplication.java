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
import org.sidiff.bug.localization.dataset.database.systemmodel.ModelHistory2Neo4j;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;
import org.sidiff.bug.localization.dataset.history.repository.GitRepository;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;

public class DatasetExportApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";

	public static final String ARGUMENT_MODEL_REPOSITORY = "-modelrepository";
	
	public static final String ARGUMENT_DATABASE_CONNECTION = "-databaseconnection";
	
	public static final String ARGUMENT_DATABASE_USER = "-databaseuser";
	
	public static final String ARGUMENT_DATABASE_PASSWORD = "-databasepassword";
	
	public static final String ARGUMENT_DATABASE_CLEAR = "-clear";

	private Path datasetPath;

	private DataSet dataset;

	private GitRepository modelRepository;
	
	/*
	 *  Experimental Flags:
	 */
	
	// TODO: MATCH (n:TracedVersion) RETURN n ORDER BY n.__initial__version__ DESC LIMIT 1
	//       -> Last Version + 1
	private int restartWithVersion = -1; // next version number or -1
	private boolean startWithFullVersion = false;
	private boolean runTestCases = false;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		// bolt://localhost:7687 , neo4j , password
		String databaseConnection = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_CONNECTION);
		String databaseUser = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_USER);
		String databasePassword = ApplicationUtil.getStringFromProgramArguments(context, ARGUMENT_DATABASE_PASSWORD);
		
		if (runTestCases) {
			test();
			return IApplication.EXIT_OK;
		}
		
		this.datasetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		this.dataset = DataSetStorage.load(datasetPath);

		Path modelRepositoryPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_MODEL_REPOSITORY);
		this.modelRepository = new GitRepository(modelRepositoryPath.toFile()); 
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseConnection, databaseUser, databasePassword)) {
			ModelHistory2Neo4j modelHistory2Neo4j = new ModelHistory2Neo4j(modelRepository, transaction);
			modelHistory2Neo4j.setOnlyBuggyVersions(true);
			
			if (restartWithVersion != -1) {
				// Restart conversion...
				modelHistory2Neo4j.setRestartWithVersion(restartWithVersion);
				modelHistory2Neo4j.setStartWithFullVersion(startWithFullVersion);
				
				if (startWithFullVersion) {
					modelHistory2Neo4j.clearDatabase();
				}
			} else {
				// Initially clear Neo4j database?
				if (ApplicationUtil.containsProgramArgument(context, ARGUMENT_DATABASE_CLEAR)) {
					modelHistory2Neo4j.clearDatabase();
				}
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

	private void test() throws Exception {
		String databaseURI = "bolt://localhost:7687";
		String databaseName = "neo4j";
		String databasePassword = "password";
		
		URI baseUri = URI.createPlatformPluginURI("org.sidiff.bug.localization.dataset.database/testdata", true);
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseURI, databaseName, databasePassword)) {
			ModelDelta modelDelta = new ModelDelta(transaction);
			modelDelta.clearDatabase();
			XMLResource lastUMLResource = null;
			int version = 0;
			
			// Version 0:
			lastUMLResource = test_commitDelta(transaction, baseUri, version++, lastUMLResource, 
					"# The initial model verison.");
			
			// Version 1:
			lastUMLResource = test_commitDelta(transaction, baseUri, version++, lastUMLResource, 
					  "# Remove class B with property attrB:String (String is external reference, not in scope).\n"
					+ "# Create data type M\n"
					+ "# Create operation opA2() in class A.");
			
			// Version 2: A node is identified by its UUID and its version number!
			lastUMLResource = test_commitDelta(transaction, baseUri, version++, lastUMLResource, 
					  "# 'Undo' (including UUIDs!) the removing of class B and attrB (with new datatype M).");
			
			// Version 3:
			lastUMLResource = test_commitDelta(transaction, baseUri, version++, lastUMLResource, 
					  "# Add paramOpA:B to opA() in class A.");
			
			// Version 4:
			lastUMLResource = test_commitDelta(transaction, baseUri, version++, lastUMLResource, 
					  "# Rename opA() to operationA().\n"
					+ "# Rename opA2() to operationA2().");
			
			// Version 5:
			lastUMLResource = test_commitDelta(transaction, baseUri, version++, lastUMLResource, 
					  "# Copy operationA(paramOpA:B) from class A to class B.\n"
					+ "# Copy operationA2() from class A to class B.");
		}
	}

	private XMLResource test_getResource(URI baseUri, int version) {
		URI modelUri = baseUri.appendSegment("" + version).appendSegment("model.uml");
		ResourceSet resourceSet = new ResourceSetImpl();
		XMLResource umlResource = (XMLResource) resourceSet.getResource(modelUri, true);
		return umlResource;
	}

	private XMLResource test_commitDelta(Neo4jTransaction transaction, URI baseUri, int version, XMLResource oldUMLResource, String description) {
		XMLResource newUMLResource = test_getResource(baseUri, version);
		ModelDelta modelDelta = new ModelDelta(transaction);
		
		System.out.println("######################################## VERSION V" + version + " ########################################");
		System.out.println(description);
		System.out.println("############################################################################################");
		
		if (oldUMLResource == null) { // initial resource?
			modelDelta.commitDelta(version, null, null, baseUri, Collections.singletonList(newUMLResource));
		} else {
			modelDelta.commitDelta(version, 
					oldUMLResource.getURI().trimSegments(1), Collections.singletonList(oldUMLResource), 
					newUMLResource.getURI().trimSegments(1), Collections.singletonList(newUMLResource));
		}
		
		return newUMLResource;
	}

}
