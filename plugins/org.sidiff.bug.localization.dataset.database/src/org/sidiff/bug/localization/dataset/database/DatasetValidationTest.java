package org.sidiff.bug.localization.dataset.database;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Record;
import org.sidiff.bug.localization.dataset.database.transaction.Neo4jTransaction;

public class DatasetValidationTest {
	
	private static boolean testLongRunning = true;

	private static boolean applyQuickFixes = true;
	private static Object[][] applyQuickFixes_createPackages = {{"org.eclipse.pde.ui/org/eclipse/pde/internal/ui/ant", 22}}; // {{"org.eclipse.pde.ui/org/eclipse/pde/internal/ui/ant", 22}};
	
	private static String databaseConnection = "bolt://localhost:7687";
	private static String databaseUser = "neo4j";
	private static String databasePassword = "password";
	
	private static Neo4jTransaction transaction;
	
	public static void applyQuickFixes() {
		
		// Fix: danglingEdgeInModelVersion -> make edge versions consistent with incident node versions:
		transaction.execute("MATCH (s)-[r]->(t) WHERE EXISTS(s.__last__version__) AND NOT EXISTS(r.__last__version__) SET r.__last__version__ = s.__last__version__");
		transaction.commit();
		
		transaction.execute("MATCH (s)-[r]->(t) WHERE EXISTS(t.__last__version__) AND NOT EXISTS(r.__last__version__) SET r.__last__version__ = t.__last__version__");
		transaction.commit();
		
		// Fix: missingContainer -> add reference to package:
		for (Object[] createPackage : applyQuickFixes_createPackages) {
			applyQuickFixCreatePackage((String) createPackage[0], (int) createPackage[1]);
		}
		
		// Fix: missingContainer -> add reference to package:
		String quickfixQuery_missingContainer = "WITH c MATCH (p:Package) WHERE p.__model__element__id__ = replace(substring(c.__model__element__id__, 0, last(apoc.text.indexesOf(c.__model__element__id__, '/'))), '/L', '/')";
		quickfixQuery_missingContainer += "AND p.__initial__version__ <= c.__initial__version__ AND (p.__last__version__ >= c.__last__version__ OR NOT EXISTS(p.__last__version__))";
		quickfixQuery_missingContainer += "WITH p, c CREATE (p)-[r:packagedElement {__containment__: true, __lower__bound__: 0, __container__: false, __upper__bound__: -1, __initial__version__: c.__initial__version__, __last__version__: c.__last__version__}]->(c) RETURN r ";
		
		// Fix Class packages:
		transaction.execute("MATCH (c:Class) WHERE NOT (c)<-[:packagedElement]-() AND NOT (c)<-[:nestedClassifier]-() " + quickfixQuery_missingContainer);
		transaction.commit();
		
		// Fix Interface packages:
		transaction.execute("MATCH (c:Interface) WHERE NOT (c)<-[:packagedElement]-() AND NOT (c)<-[:nestedClassifier]-() " + quickfixQuery_missingContainer);
		transaction.commit();
		
		// Fix Enumeration packages:
		transaction.execute("MATCH (c:Enumeration) WHERE NOT (c)<-[:packagedElement]-() AND NOT (c)<-[:nestedClassifier]-() " + quickfixQuery_missingContainer);
		transaction.commit();
	}
	
	public static void applyQuickFixCreatePackage(String modelElementID, int initialVersion) {
		
		// Does package already exists?
		if (transaction.execute("MATCH (p:Package {__model__element__id__:'" + modelElementID + "'}) RETURN p").list().isEmpty()) {
			String name = modelElementID.substring(modelElementID.lastIndexOf("/") + 1, modelElementID.length());
			String parentModelElementID = modelElementID.substring(0, modelElementID.lastIndexOf("/")).substring(0, modelElementID.lastIndexOf("/"));
			
			String quickfixQuery_createPackage = "MATCH (p:Package {__model__element__id__:'" + parentModelElementID + "'})";
			quickfixQuery_createPackage += "CREATE (p)-[r:packagedElement {__containment__: true, __lower__bound__: 0, __container__: false, __upper__bound__: -1, __initial__version__: " + initialVersion + "}]";
			quickfixQuery_createPackage += "->(sp:Package {name: '" + name + "', visibility: \"public\", __model__element__id__: '" + modelElementID + "', __initial__version__: " + initialVersion + "}) RETURN r";
			
			transaction.execute(quickfixQuery_createPackage);
			transaction.commit();
			
			System.err.println("Package created: " + modelElementID);
		}
	}
	
    @BeforeClass
    public static void initializeNeo4j() {
    	transaction = new Neo4jTransaction(databaseConnection, databaseUser, databasePassword);
    	
    	if (applyQuickFixes) {
    		DatasetValidationTest.applyQuickFixes();
    	}
    }

    @AfterClass
    public static void closeDriver(){
        transaction.close();
    }
	
    @Test
    public void noUnconnectedNodes() {
    	Assert.assertEquals(0, transaction.execute("MATCH (p) WHERE NOT (p)--() RETURN p").list().size());
    }
    
    @Test
    public void operationMustBeContainedInAClassifier() {
    	Assert.assertEquals(0, transaction.execute("MATCH (o:Operation) WHERE NOT (o)<-[:ownedOperation]-() Return o").list().size());
    }
    
    @Test
    public void propertyMustBeContainedInAClassifier() {
    	Assert.assertEquals(0, transaction.execute("MATCH (o:Property) WHERE NOT (o)<-[:ownedAttribute]-() Return o").list().size());
    }
    
    @Test
    public void modelElementIDsHaveToBeUniqueInEachModelVersion() {
    	Assert.assertEquals(0, transaction.execute("MATCH (m) MATCH (n) WHERE n <> m AND LABELS(n) = LABELS(m) AND n.__model__element__id__ = m.__model__element__id__ AND NOT EXISTS(n.__last__version__) AND NOT EXISTS(m.__last__version__) RETURN n, m").list().size());
    }
    
    @Test
    public void allModelElementsMustHaveAnID() {
    	Assert.assertEquals(0, transaction.execute("MATCH (n) WHERE NOT EXISTS(n.__model__element__id__) RETURN n").list().size());
    }
    
    @Test
    public void danglingEdgeInModelVersion() {
    	Assert.assertEquals(0, transaction.execute("MATCH (s)-[r]-(t) WHERE (EXISTS(s.__last__version__) OR EXISTS(t.__last__version__)) AND NOT EXISTS(r.__last__version__) return s,r,t").list().size());
    }
    
    @Test
    public void missingContainer() {
    	List<Record> result = transaction.execute("MATCH (o) WHERE NOT (o)<-[{__containment__:TRUE}]-() AND NOT (o)<-[:model]-() AND NOT LAST(LABELS(o)) = 'SystemModel' Return o").list();
    	
    	if (result.size() > 0) {
    		System.out.println("Missing Containers:");
    		System.out.println("  see applyQuickFixes_createPackages");
    		
    		for (Record record : result) {
    			System.out.println();
    			System.out.println("__model__element__id__:" + record.get(0).get("__model__element__id__"));
    			System.out.println("__initial__version__:" + record.get(0).get("__initial__version__"));
    			System.out.println();
			}
    		
    	}
    	
    	Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void duplicatedEdgesInTheSameVersion() {
    	if (testLongRunning) {
    		Assert.assertEquals(0, transaction.execute("MATCH (a1)-[e1]->(b)<-[e2]-(a2) WHERE TYPE(e1) = TYPE(e2) AND a1 = a2 AND e1 <> e2 AND ((NOT EXISTS(e1.__last__version__) AND NOT EXISTS(e2.__last__version__)) OR e1.__last__version__ = e2.__last__version__ OR e1.__initial__version__ = e2.__initial__version__) RETURN e1, e2, a1, a2").list().size());
    	}
    }
    
}
