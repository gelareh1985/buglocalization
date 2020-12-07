package org.sidiff.bug.localization.dataset.database.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class ModelCypherDelta {

	private int oldVersion;
	
	private int newVersion;
	
	private Map<XMLResource, XMLResource> oldResourcesMatch;
	
	private Map<XMLResource, XMLResource> newResourcesMatch;
	
	private Map<String, Integer> matchQueries;
	
	private List<String> setQueries;
	
	private List<String> createQueries;
	
	private int latestCypherVariable;
	
	public ModelCypherDelta(
			int oldVersion, Map<XMLResource, XMLResource> oldResourcesMatch,
			int newVersion, Map<XMLResource, XMLResource> newResourcesMatch) {
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
		this.oldResourcesMatch = oldResourcesMatch;
		this.newResourcesMatch = newResourcesMatch;
		this.matchQueries = new LinkedHashMap<>();
		this.setQueries = new ArrayList<>();
		this.createQueries = new ArrayList<>();
		this.latestCypherVariable = -1;
	}
	
	protected int createNewVariable() {
		return ++latestCypherVariable;
	}

	protected String compileQuery() {
		StringBuilder query = new StringBuilder();
		
		/*
		 * MATCH (v1:eClass {__model__element__id__: 'nXMIID'}) WITH v1 ORDER BY v1.__initial__version__ DESC LIMIT 1 
		 * MATCH (v2:eClass {__model__element__id__: 'pXMIID'}) WITH v1, v2 ORDER BY v2.__initial__version__ DESC LIMIT 1 
		 * SET v1.__last__version__ = 5
		 * SET v2.__last__version__ = 5
		 * 
		 * Or for testing:
		 * RETURN v1, v2
		 */
		List<Integer> variables = new ArrayList<>();
		
		for (Entry<String, Integer> matchQuery : matchQueries.entrySet()) {
			variables.add(matchQuery.getValue());
			
			query.append(matchQuery.getKey());
			query.append(" WITH ");
			
			for (Integer variable : variables) {
				query.append("v");
				query.append(variable);
				query.append(", ");
			}
			
			query.deleteCharAt(query.length() - 2); // last ,~
			
			// latest version:
			query.append(" ORDER BY ");
			query.append("v");
			query.append(matchQuery.getValue());
			query.append(".__initial__version__ DESC LIMIT 1");
			query.append("\n");
		}
		
		/*
		 *  SET v1.__last__version__ = 5
		 */
		for (String setQuery : setQueries) {
			query.append(setQuery);
			query.append("\n");
		}
		
		/*
		 * CREATE 
		 * (v1:Model{...}),
		 * (v2:Class{...})
		 */
		if (!createQueries.isEmpty()) {
			query.append("CREATE");
			query.append("\n");
			
			for (String createQuery : createQueries) {
				query.append(createQuery);
				query.append(",");
				query.append("\n");
			}
			
			query.deleteCharAt(query.length() - 2); // last ,\n
		}
		
		return query.toString();
	}

	public XMLResource getOldResource(Resource newResource) {
		return newResourcesMatch.get(newResource);
	}
	
	public Map<XMLResource, XMLResource> getOldResourcesMatch() {
		return oldResourcesMatch;
	}

	public int getOldVersion() {
		return oldVersion;
	}

	public XMLResource getNewResource(Resource oldResource) {
		return oldResourcesMatch.get(oldResource);
	}
	
	public Map<XMLResource, XMLResource> getNewResourcesMatch() {
		return newResourcesMatch;
	}

	public int getNewVersion() {
		return newVersion;
	}

	protected Map<String, Integer> getMatchQueries() {
		return matchQueries;
	}

	protected List<String> getSetQueries() {
		return setQueries;
	}

	protected List<String> getCreateQueries() {
		return createQueries;
	}

}
