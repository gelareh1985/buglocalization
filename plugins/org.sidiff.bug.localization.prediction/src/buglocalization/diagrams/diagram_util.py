from json import dump
from typing import List, Tuple

import pandas as pd
from buglocalization.dataset import neo4j_queries as query
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from py2neo import Graph


def slice_diagram(graph: Graph, db_version: int, node_ids: List[int], add_parents: bool = False) -> pd.DataFrame:

    # Slice abstract syntax tree:
    node_query_parameters = {'node_ids': node_ids, 'db_version': db_version}

    # all children/parents - assuming not more than 10 AST depth:
    path_return_query = 'RETURN DISTINCT startNode(edge) AS source, endNode(edge) as target'
    nodes_return_query = 'RETURN DISTINCT ID(nodes) AS index, [ID(nodes), LABELS(nodes), nodes] AS nodes'

    query_nodes = query.path_nodes(query.edges_to_child_nodes(10, path_return_query), nodes_return_query)
    diagram_nodes = graph.run(query_nodes, node_query_parameters).to_data_frame()
    diagram_nodes.set_index('index', inplace=True)
    diagram_nodes_ids = diagram_nodes.index.to_list()

    if add_parents:
        query_parent = query.path_nodes(query.edges_to_parent_nodes(10, path_return_query), nodes_return_query)
        diagram_parent_nodes = graph.run(query_parent, node_query_parameters).to_data_frame()
        diagram_parent_nodes.set_index('index', inplace=True)
        diagram_nodes_ids.extend(diagram_parent_nodes.index.to_list())

        # Concat AST and remove duplicated node_ids
        diagram_nodes = pd.concat([diagram_parent_nodes, diagram_nodes])
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


def save_first_relevant_diagram(evaluation_results: List[Tuple[str, pd.DataFrame, str, pd.DataFrame]],
                                meta_model: MetaModel,
                                graph: Graph,
                                TOP_RANKING_K: int,
                                NUMBER_OF_NEIGHBORS: int,
                                diagram_save_path: str,
                                K_NEIGHBOUR_DISTANCE: int = 0,
                                UNDIRECTED: bool = True,
                                DIAGRAM_AGGREGATION: bool = True) -> None:
    """
    Args:
        TOP_RANKING_K (int, optional): Compute for top k ranking positions
        DIAGRAM_SIZE (int, optional): Size of the diagram:. Defaults to 60.
        SAVE_DIAGRAM (bool, optional): Write diagram nodes and edges as Json-File. Defaults to False.
        K_NEIGHBOURS (int, optional): Hops from the expected locations. Defaults to 2.
        UNDIRECTED (bool, optional): Follow undirected edges on diagram slicing. Defaults to True.
        DIAGRAM_AGGREGATION (bool, optional): If a node of the input ranking is already contained in a 
          higher ranked diagram it will the ranking position will not be considered again.Defaults to True.
    """

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in evaluation_results:
        print(tbl_info_file)
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        
        first_expected_location = eval_util.get_first_relevant_subgraph_location(
            tbl_predicted, TOP_RANKING_K, graph, db_version, meta_model,
            K_NEIGHBOUR_DISTANCE, UNDIRECTED, NUMBER_OF_NEIGHBORS, DIAGRAM_AGGREGATION)

        if first_expected_location is not None:
            ranking_location_id = first_expected_location[0].DatabaseNodeID
            top_k_ranking_of_subgraph_k = first_expected_location[1]
            ranked_node_ids = top_k_ranking_of_subgraph_k.DatabaseNodeID.to_list()
            ranked_node_ids.append(ranking_location_id)

            diagram_graph = slice_diagram(graph, db_version, ranked_node_ids)
            save_diagram(diagram_graph, diagram_save_path + tbl_predicted_file + "_diagram.json")
