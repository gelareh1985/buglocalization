package org.sidiff.bug.localization.dataset.database.transaction;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class Neo4jTransaction implements AutoCloseable {
	
	private final Driver driver;
	
	private final Session session;
	
	private Transaction transaction;

	public Neo4jTransaction(String uri, String user, String password) {
		this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
		this.session = driver.session();
		this.transaction = session.beginTransaction();
	}

	@Override
	public void close() throws Exception {
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
	
}
