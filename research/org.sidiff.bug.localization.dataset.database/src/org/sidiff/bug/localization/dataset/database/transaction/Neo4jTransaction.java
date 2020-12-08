package org.sidiff.bug.localization.dataset.database.transaction;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

public class Neo4jTransaction implements AutoCloseable {
	
	private final Driver driver;

	public Neo4jTransaction(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public Result execute(final String query) {
		try (Session session = driver.session()) {
			return session.writeTransaction(new TransactionWork<Result>() {
				@Override
				public Result execute(Transaction tx) {
					return tx.run(query);
				}
			});
		}
	}
}
