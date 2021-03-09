import os
from json import dump
from pathlib import Path
from typing import List, Optional, Tuple

import pandas as pd
from py2neo import Graph

from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML


def top_k_ranking_accuracy(found_in_top_k, not_found_in_top_k) -> float:
    return float(found_in_top_k) / float(found_in_top_k + not_found_in_top_k)


def get_first_expected_subgraph_location(tbl_predicted: pd.DataFrame,
                                         graph: Graph,
                                         db_version: int,
                                         top_k_ranking: pd.DataFrame,
                                         meta_model: MetaModel,
                                         K_NEIGHBOURS: int,
                                         UNDIRECTED: bool,
                                         DIAGRAM_NEIGHBOR_SIZE: int) -> Optional[Tuple[pd.Series, pd.DataFrame, pd.DataFrame]]:

    for ranking_idx, ranking_location in top_k_ranking.iterrows():
        labels_mask = list(meta_model.get_bug_location_types())
        subgraph_k = query_util.subgraph_k(graph, ranking_location.DatabaseNodeID, db_version,
                                           K_NEIGHBOURS, UNDIRECTED, meta_model, labels_mask)
        subgraph_k = subgraph_k[subgraph_k.index != ranking_location.DatabaseNodeID]  # without start node
        ranking_of_subgraph_k = eval_util.get_ranking_of_subgraph(subgraph_k, tbl_predicted)
        top_k_ranking_of_subgraph_k = ranking_of_subgraph_k.head(DIAGRAM_NEIGHBOR_SIZE)

        if 1 in top_k_ranking_of_subgraph_k.IsLocation.to_list() or ranking_location.IsLocation == 1:
            return ranking_location, top_k_ranking_of_subgraph_k, subgraph_k

    return None


def top_k_ranking(evaluation_results: List[Tuple[str, pd.DataFrame, str, pd.DataFrame]],
                  meta_model: MetaModel,
                  graph: Graph,
                  TOP_RANKING_K: int,
                  DIAGRAM_NEIGHBOR_SIZE,
                  diagram_save_path: str = None,
                  SAVE_DIAGRAM: bool = False,
                  K_NEIGHBOURS: int = 0,
                  UNDIRECTED: bool = True) -> Tuple[int, int]:
    """
    Args:
        TOP_RANKING_K (int, optional): Compute for top k ranking positions
        DIAGRAM_SIZE (int, optional): Size of the diagram:. Defaults to 60.
        SAVE_DIAGRAM (bool, optional): Write diagram nodes and edges as Json-File. Defaults to False.
        K_NEIGHBOURS (int, optional): Hops from the expected locations. Defaults to 2.
        UNDIRECTED (bool, optional): [description]. Defaults to True.


    Returns:
        (int, int): [0] Number of ranking that contain at least on expexted location in top k.
                    [1] Number of ranking that contain no expexted location in top k.
    """

    found_in_top_k = 0
    not_found_in_top_k = 0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in evaluation_results:
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        top_k_ranking = tbl_predicted.head(TOP_RANKING_K)

        # Is node directly contained in top k results (TOP_RANKING_K)?
        if 1 in top_k_ranking.IsLocation.to_list():
            found_in_top_k += 1
        # Is node indirectly contained by subgraph (K_NEIGHBOURS,DIAGRAM_SIZE)
        else:
            first_expected_location = get_first_expected_subgraph_location(tbl_predicted, graph, db_version,
                                                                           top_k_ranking, meta_model,
                                                                           K_NEIGHBOURS, UNDIRECTED, DIAGRAM_NEIGHBOR_SIZE)

            if first_expected_location is not None:
                found_in_top_k += 1

                if SAVE_DIAGRAM and diagram_save_path is not None:
                    ranking_location_id = first_expected_location[0].DatabaseNodeID
                    top_k_ranking_of_subgraph_k = first_expected_location[1]
                    ranked_node_ids = top_k_ranking_of_subgraph_k.DatabaseNodeID.to_list()
                    ranked_node_ids.append(ranking_location_id)
                    
                    diagram_graph = slice_diagram(graph, db_version, ranked_node_ids)
                    save_diagram(diagram_graph, diagram_save_path + tbl_predicted_file + "_diagram.json")
            else:
                not_found_in_top_k += 1

        print(tbl_info_file, "Found:", found_in_top_k, "Not found:", not_found_in_top_k)

    return found_in_top_k, not_found_in_top_k


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


if __name__ == '__main__':
    
    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04_train90_test10_layer300"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"
    
    evaluation_results = eval_util.load_all_evaluation_results(evaluation_results_path)
    
    # Output folder:
    evaluation_metrics_path = evaluation_results_path + 'metrics/'
    Path(evaluation_metrics_path).mkdir(parents=True, exist_ok=True)

    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")
    
    # # Evaluations # #
    k_neighbors = [0, 2]
    top_k_values = [1, 5, 10, 15, 20, 25, 30, 35]
    diagram_neighbor_sizes = [1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    
    for k_neighbor in k_neighbors:
        print("Experiment: k neighbor:", k_neighbor)

        # [diagram_sizes (index), top_k_ranking_accuracy@top_k_values]
        evaluation = pd.DataFrame(columns=top_k_values, index=diagram_neighbor_sizes)
        evaluation.index.names = ['diagram_sizes']
        
        # Diagram size has no effect if no neigbors are considered:
        if k_neighbor == 0:
            evaluation = evaluation[evaluation.index == 1]
        
        for top_k_value in top_k_values:
            print("Experiment: top k value:", top_k_value)

            for diagram_size in diagram_neighbor_sizes:
                print("Experiment: diagram size:", diagram_size)

                found_in_top_k, not_found_in_top_k = top_k_ranking(evaluation_results=evaluation_results,
                                                                   meta_model=meta_model,
                                                                   graph=buglocation_graph,
                                                                   TOP_RANKING_K=top_k_value,
                                                                   DIAGRAM_NEIGHBOR_SIZE=diagram_size,
                                                                   SAVE_DIAGRAM=False,
                                                                   diagram_save_path=evaluation_results_path,
                                                                   K_NEIGHBOURS=k_neighbor,
                                                                   UNDIRECTED=True)
                top_k_ranking_accuracy_result = top_k_ranking_accuracy(found_in_top_k, not_found_in_top_k)
                evaluation.at[diagram_size, top_k_value] = top_k_ranking_accuracy_result
                
                print("Found:", found_in_top_k, "Not found:", not_found_in_top_k)
                print("Top k Accuracy:", top_k_ranking_accuracy_result)

                # Diagram size has no effect if no neigbors are considered:
                if k_neighbor == 0:
                    break
                
            # Checkpoint save evaluation:
            filename = 'k_neighbor_' + str(k_neighbor) + '.csv'
            evaluation.to_csv(evaluation_metrics_path + filename, sep=';')
                
        # Save evaluation:
        filename = 'k_neighbor_' + str(k_neighbor) + '.csv'
        evaluation.to_csv(evaluation_metrics_path + filename, sep=';')
