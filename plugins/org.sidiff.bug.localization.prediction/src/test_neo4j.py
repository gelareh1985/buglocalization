from py2neo import Graph

# Docker container to container (Docker compose):
#graph =  Graph(host="neo4j", port=7687, user="neo4j", password="password")

# Host to docker container:
graph =  Graph(host="localhost", port=7687, user="neo4j", password="password")

result = graph.run("Match (n) RETURN n LIMIT 10").to_data_frame()
print(result)