from py2neo import Graph
import numpy as np

# Docker container to container (Docker compose):
#graph =  Graph(host="neo4j", port=7687, user="neo4j", password="password")

# Host to docker container:
graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

result = graph.run("Match (n) RETURN ID(n)").to_data_frame()
print(len(result.index))
