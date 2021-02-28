from bug_localization_data_set_neo4j import Neo4jConfiguration, DataSetNeo4j, BugSampleNeo4j, LocationSampleNeo4j
from bug_localization_meta_model_uml import create_uml_configuration
from word_to_vector_shared_dictionary import WordDictionary
from typing import cast

# List of node IDs from the Neo4j database:
nodes = [(2059, 7747)]  # ID -> DB Version
num_samples = [20, 10]  # Node per graph sampling level/hop

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

# Meta-model configuration:
meta_model, node_self_embedding, typebased_slicing = create_uml_configuration(WordDictionary(), num_samples)

# Build data set from given nodes:
data_set = DataSetNeo4j(meta_model, node_self_embedding, typebased_slicing, neo4j_configuration, list_bug_samples=False)

for node in nodes:
    node_id = node[0]
    node_label: str = data_set.get_label(data_set.run_query_value('MATCH (n) WHERE ID(n) = ' + str(node_id) + ' RETURN n'))
    db_version = node[1]
    
    bug_sample_neo4j = BugSampleNeo4j(data_set, db_version)
    data_set.bug_samples.append(bug_sample_neo4j)

    location_sample_neo4j = LocationSampleNeo4j(node_id, node_label, node_id)
    bug_sample_neo4j.location_samples.append(location_sample_neo4j)

# Create sub-graphs:
for bug_sample in data_set:
    bug_sample_neo4j = cast(BugSampleNeo4j, bug_sample)

    for location_sample in bug_sample_neo4j:
        location_sample_neo4j = cast(LocationSampleNeo4j, location_sample)

        typebased_slicing = bug_sample_neo4j.dataset.typebased_slicing
        slicing = typebased_slicing.get_slicing(location_sample_neo4j.mode_location_type)
        edges, node_ids = bug_sample_neo4j.load_subgraph_edges(int(location_sample_neo4j.neo4j_model_location), slicing)

        print('Version:', bug_sample_neo4j.db_version)
        print('  Node ID:', location_sample_neo4j.neo4j_model_location)
        print('  Node Type:', location_sample_neo4j.mode_location_type)
        print('  Node Count:', len(node_ids))
        print('  Edge Count:', len(edges.index))
        print('  Head Node Contained:', location_sample_neo4j.neo4j_model_location in node_ids)
        print('  Nodes:', 'MATCH (n) WHERE ID(n) IN ' + str(list(node_ids)) + ' RETURN n')
        