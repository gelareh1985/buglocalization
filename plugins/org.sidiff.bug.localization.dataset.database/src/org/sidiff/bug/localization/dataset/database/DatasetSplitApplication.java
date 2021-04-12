package org.sidiff.bug.localization.dataset.database;

import java.util.List;

import org.neo4j.driver.Record;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;

public class DatasetSplitApplication {

	public static double split = 0.9;  
	
	private static String databaseConnection = "bolt://localhost:7687";
	private static String databaseUser = "neo4j";
	private static String databasePassword = "password";
	
	public static void main(String[] args) {
		String queryListBugReportsWithModelChanges = "MATCH (v:TracedVersion)-->(b:TracedBugReport)-[:modelLocations]->(c:Change)-[:location]->(e) RETURN DISTINCT b.__initial__version__ AS versions, v.modelVersionID AS versionID ORDER BY versions";
		List<Record> buggyVersions = null;
		
		try (Neo4jTransaction transaction = new Neo4jTransaction(databaseConnection, databaseUser, databasePassword)) {
			buggyVersions = transaction.execute(queryListBugReportsWithModelChanges).list();
		}
		
		int splitIdx = (int) (buggyVersions.size() * split);
		int splitDatabaseVersion = buggyVersions.get(splitIdx).get("versions").asInt();
		String splitVersionID = buggyVersions.get(splitIdx).get("versionID").asString();
		
		System.out.println();
		System.out.println("Use load and dump to store and reload in the database folder after creating each split:");
		System.out.println("./bin/neo4j-admin dump --database=neo4j --to=./backups/neo4j-eclipse.jdt.core_samples.dump");
		System.out.println("./bin/neo4j-admin load --from=./backups/neo4j-eclipse.jdt.core_samples.dump --database=neo4j --force");
		System.out.println();
		System.out.println("Bug Repors with model bug locations: " + buggyVersions.size());
		System.out.println("With " + split + " split on index: " + splitIdx + " version ID: " + splitVersionID + " database version number: " + splitDatabaseVersion);
		System.out.println();
		System.out.println("Create >training< data set from full data set:");
		System.out.println("MATCH (n)-[r]->() WHERE r.__initial__version__ >= " + splitDatabaseVersion + " DELETE r");
		System.out.println("MATCH (n) WHERE n.__initial__version__ >= " + splitDatabaseVersion + " DELETE n");
		System.out.println();
		System.out.println("Create >test< data set from full data set:");
		System.out.println("MATCH (n)-[r]->() WHERE r.__last__version__ < " + splitDatabaseVersion + " DELETE r");
		System.out.println("MATCH (n) WHERE n.__last__version__ < " + splitDatabaseVersion + " DELETE n");
	}
	
}
