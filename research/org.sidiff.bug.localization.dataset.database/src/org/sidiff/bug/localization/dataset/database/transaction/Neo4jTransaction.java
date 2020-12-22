package org.sidiff.bug.localization.dataset.database.transaction;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class Neo4jTransaction implements AutoCloseable {
	
	public static boolean LOGGING = true; 
	
	private String uri;
	
	private String user;
	
	private String password;
	
	private Driver driver;
	
	private Session session;
	
	private Transaction transaction;
	
	public Neo4jTransaction(String uri, String user, String password) {
		this.uri = uri;
		this.user = user;
		this.password = password;
		open();
	}
	
	public void open() {
		if (driver != null) {
			close();
		}
		this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
		this.session = driver.session();
		this.transaction = session.beginTransaction();
	}

	@Override
	public void close() {
		transaction.commit();
		transaction.close();
		session.close();
		driver.close();
	}
	
	public Result execute(final String query) {
		return transaction.run(query);
	}

	public void execute(final Map<String, Map<String, Object>> queries) {
		if (!queries.isEmpty()) {
			for (Entry<String, Map<String, Object>> parameterizedQuery : queries.entrySet()) {
				transaction.run(parameterizedQuery.getKey(), parameterizedQuery.getValue());
			}
		}
	}
	
	public void commit() {
		transaction.commit();
		transaction.close();
		this.transaction = session.beginTransaction();
	}
	
	public void awaitIndexOnline() {
		awaitIndexOnline(100, 1000);
	}
	
	public void awaitIndexOnline(long millis, int retry) {
		for (int i = 0; i < retry; i++) {
			boolean waiting = false;
			
			for (Record record : execute("SHOW INDEXES").list()) {
				String state = record.get("state").asString();
				String name = record.get("name").asString();
				
				if (!record.get("state").asString().equals("ONLINE")) {
					waiting = true;
					try {
						if (LOGGING) {
							System.err.println("Await Index Online (" + millis + "ms): " + name + " (" + state + ")");
						}
						Thread.sleep(millis);
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (!waiting) {
				break;
			}
		}
		commit();
		
		// With Neo4j API:
//		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(...).build();
//		GraphDatabaseService graphDb = managementService.database(...);
//
//		try (org.neo4j.graphdb.Transaction transaction = graphDb.beginTx()) {
//			Schema schema = transaction.schema();
//			IndexCreator indexCreator = schema.indexFor(Label.label(label));
//			IndexDefinition indexDefinition = indexCreator.on(property).withName(indexName).create();
//			transaction.commit();
//			schema.awaitIndexOnline(indexDefinition, 3, TimeUnit.MINUTES);
//		}
	}

}
