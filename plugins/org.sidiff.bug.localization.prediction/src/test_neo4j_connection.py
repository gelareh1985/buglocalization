from py2neo import Graph
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration

# Docker container to container (Docker compose):
#graph =  Graph(host="neo4j", port=7687, user="neo4j", password="password")

# Host to docker container:
# graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

# result = graph.run("Match (n) RETURN ID(n)").to_data_frame()
# print(len(result.index))

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

metamodel = MetaModelUML(neo4j_configuration=neo4j_configuration)
file_corpus = []
for metatype, properties in metamodel.get_type_to_properties().items():
    #print(type, properties)
    cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
        metatype=metatype
    )
    metamodel_nodes = metamodel.get_graph().run(cypher=cypher_str_command).to_table()
    for node in metamodel_nodes:
        # node comments (long text)
        # node (short text)
        for property in properties:
            #print(node[0][property]) 
            file_corpus.append(node[0][property])  # split to words - each word to vector - sowe
