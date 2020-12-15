package org.sidiff.bug.localization.dataset.database.transaction;

import org.neo4j.driver.Record;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherEdgeDelta;
import org.sidiff.bug.localization.dataset.database.query.ModelCypherNodeDelta;

public class Neo4jUtil {

	public static void clearDatabase(Neo4jTransaction transaction) {
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
