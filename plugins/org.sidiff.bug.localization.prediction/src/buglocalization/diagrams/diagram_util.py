from json import dump
from typing import List

import pandas as pd
from buglocalization.dataset import neo4j_queries as query
from py2neo import Graph


def slice_diagram(graph: Graph, db_version: int, node_ids: List[int]) -> pd.DataFrame:

    # Slice abstract syntax tree:
    node_query_parameters = {'node_ids': node_ids, 'db_version': db_version}

    # all children/parents - assuming not more than 10 AST depth:
    path_return_query = 'RETURN DISTINCT startNode(edge) AS source, endNode(edge) as target'
    nodes_return_query = 'RETURN DISTINCT ID(nodes) AS index, [ID(nodes), LABELS(nodes), nodes] AS nodes'

    query_children = query.path_nodes(query.edges_to_child_nodes(10, path_return_query), nodes_return_query)
    diagram_child_nodes = graph.run(query_children, node_query_parameters).to_data_frame()
    diagram_child_nodes.set_index('index', inplace=True)

    query_parent = query.path_nodes(query.edges_to_parent_nodes(10, path_return_query), nodes_return_query)
    diagram_parent_nodes = graph.run(query_parent, node_query_parameters).to_data_frame()
    diagram_parent_nodes.set_index('index', inplace=True)

    diagram_nodes_ids = diagram_child_nodes.index.to_list()
    diagram_nodes_ids.append(diagram_parent_nodes.index.to_list())

    # Concat AST and remove duplicated node_ids
    diagram_nodes = pd.concat([diagram_parent_nodes, diagram_child_nodes])
    diagram_nodes = diagram_nodes[~diagram_nodes.index.duplicated(keep='first')]

    # Get all edges between the selected nodes:
    edges_return_query = ' RETURN [TYPE(e), ID(startNode(e)), ID(endNode(e))] as edges'
    edge_query_parameters = {'node_ids': diagram_nodes_ids, 'db_version': db_version}
    edge_query = query.edges_from_nodes_in_version(edges_return_query)
    diagram_edges = graph.run(edge_query, edge_query_parameters).to_data_frame()

    # Combine nodes and edges:
    diagram_graph = {'edges': diagram_edges.edges.to_list(), 'nodes': diagram_nodes.nodes.to_list()}
    return diagram_graph


def save_diagram(diagram_graph: pd.DataFrame, path: str):
    with open(path, 'w') as outfile:
        dump(diagram_graph, outfile, indent=4, sort_keys=True)
