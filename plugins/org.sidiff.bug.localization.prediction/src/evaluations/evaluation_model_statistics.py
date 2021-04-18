'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from py2neo import Graph
from buglocalization.dataset import neo4j_queries as query
from buglocalization.metamodel.meta_model_uml import MetaModelUML

model_element_filter = None  # '.test'
meta_model = MetaModelUML()


def filter_model_element_id(operator: str, variable: str) -> str:
    if model_element_filter is not None:
        return operator + ' NOT ' + variable + '.__model__element__id__ CONTAINS "' + model_element_filter + '"'
    else:
        return ''


if __name__ == '__main__':
    graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

    latest_version_query = 'MATCH (v:TracedVersion) RETURN DISTINCT v.__initial__version__ AS versions ORDER BY versions DESC LIMIT 1'
    latest_version = graph.run(latest_version_query)
    latest_version = latest_version.to_table()[0][0]

    system_model_query = 'MATCH (n:SystemModel) ' + query.where_version('n') + ' RETURN n.name'
    system_model = graph.run(system_model_query, {'db_version': latest_version})
    system_model = system_model.to_table()[0][0]
    print("System Model:", system_model)

    model_nodes_query = 'MATCH (n) ' + query.where_version('n') + filter_model_element_id(' AND', 'n') + ' RETURN COUNT(n)'
    model_nodes = graph.run(model_nodes_query, {'db_version': latest_version})
    model_nodes = model_nodes.to_table()[0][0]
    print("Model node count:", model_nodes)

    model_edges_query = 'MATCH (n)-[e]->() ' + query.where_version('e') + filter_model_element_id(' AND', 'n') + ' RETURN COUNT(e)'
    model_edges = graph.run(model_edges_query, {'db_version': latest_version})
    model_edges = model_edges.to_table()[0][0]
    print("Model edge count:", model_edges)

    bug_fixes_query = 'MATCH (b:TracedBugReport)-[:modelLocations]->(c:Change)-[:location]->(e)'
    bug_fixes_query += filter_model_element_id(' WHERE', 'e')
    bug_fixes_query += ' WITH DISTINCT b.__initial__version__ AS versions RETURN COUNT(versions)'
    bug_fixes = graph.run(bug_fixes_query)
    bug_fixes = bug_fixes.to_table()[0][0]
    print("Bug fix count:", bug_fixes)

    bug_locations_query = 'MATCH (n) ' + query.where_version('n') + ' AND LABELS(n)[0] IN $bug_locations'
    bug_locations_query += filter_model_element_id(' AND', 'n')
    bug_locations_query += ' AND NOT n.__model__element__id__ STARTS WITH "library/" RETURN COUNT(n)'
    bug_locations = graph.run(bug_locations_query, {'db_version': latest_version,
                                                    'bug_locations': list(meta_model.get_bug_location_types())})
    bug_locations = bug_locations.to_table()[0][0]
    print("Potential bug locations (Classifier) count:", bug_locations)
