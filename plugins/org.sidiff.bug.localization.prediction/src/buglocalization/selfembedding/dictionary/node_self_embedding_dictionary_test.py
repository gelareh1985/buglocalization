import pickle

from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.selfembedding.dictionary.node_self_embedding_word_ranking import \
    load_file

# Output path of the node self embedding dictionary:
node_self_embedding_dictionary_path = 'D:/evaluation/eclipse.pde.ui/node_self_embedding.pkl'

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

# Metamodel configuration:
metamodel = MetaModelUML(neo4j_configuration=neo4j_configuration)


# Check if all nodes are in the dictionary:
def compute_node_self_embedding():
    node_self_embedding_dictionary = load_file(node_self_embedding_dictionary_path)
    print("Node Self Embedding Loaded!")
    
    for metatype, properties in metamodel.get_type_to_properties().items():
        cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
            metatype=metatype
        )
        metamodel_nodes = metamodel.get_graph().run(cypher=cypher_str_command).to_table()
        
        print("Check Node Self Embedding:", len(metamodel_nodes), "nodes of type", metatype)

        for node in metamodel_nodes:
            nodeID = node[0].identity
            
            # if properties:
            if nodeID not in node_self_embedding_dictionary:
                print("Missing Node ID:", nodeID)
                
    print("Node Self Embedding Dictionry Check Complete!")
            