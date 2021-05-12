'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from typing import Generator, List, Optional, Set, Tuple

import pandas as pd
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from py2neo import Graph
from buglocalization.diagrams import diagram_ranking_util, diagram_util


def get_ranking_of_subgraph_nodes(subgraph_k: pd.DataFrame, tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    # subgraph_k -> index -> node IDs
    return tbl_predicted_data[tbl_predicted_data.DatabaseNodeID.isin(subgraph_k.index)]


def get_subgraph_location_ids(tbl_predicted: pd.DataFrame,
                              ranking_location: pd.Series,
                              graph: Graph,
                              db_version: int,
                              meta_model: MetaModel,
                              K_NEIGHBOURS: int,
                              UNDIRECTED: bool,
                              DIAGRAM_NEIGHBOR_SIZE: int) -> List[int]:

    labels_mask = list(meta_model.get_bug_location_types())
    subgraph_k = query_util.subgraph_k(graph, ranking_location.DatabaseNodeID, db_version,
                                       K_NEIGHBOURS, UNDIRECTED, meta_model, labels_mask)
    subgraph_k = subgraph_k[subgraph_k.index != ranking_location.DatabaseNodeID]  # without start node
    ranking_of_subgraph_k = get_ranking_of_subgraph_nodes(subgraph_k, tbl_predicted)
    top_k_ranking_of_subgraph_k = ranking_of_subgraph_k.head(DIAGRAM_NEIGHBOR_SIZE)

    subgraph_location_ids = top_k_ranking_of_subgraph_k.DatabaseNodeID.to_list()
    subgraph_location_ids.append(ranking_location.DatabaseNodeID)

    return subgraph_location_ids


def get_subgraph_ranking(tbl_predicted: pd.DataFrame,
                         meta_model: MetaModel,
                         buglocation_graph: Graph,
                         db_version: int,
                         K_NEIGHBOUR_DISTANCE: int,
                         UNDIRECTED: bool,
                         DIAGRAM_NEIGHBOR_SIZE: int,
                         DIAGRAM_AGGREGATION: bool = True,
                         SAVE_DIAGRAM: bool = False,
                         diagram_save_path: str = "",
                         TOP_RANKING_K: int = -1,
                         MATCH_NEO4J_ID: bool = False) -> Tuple[pd.DataFrame, float, float]:
    """
    - Pull up relevant position to first diagram that contains the relevant location of the classifier ranking.
    - Aggregate ranking: Generate diagram for each position in classifier ranking - do not consider a position that were already seen.
    """
    expected_location_ids = eval_util.get_relevant_location_ids(tbl_predicted)
    relevant_database_node_ids = set()
    not_relevant_database_node_ids = set()
    seen_location_ids: Set[int] = set()

    diagram_size = 0
    diagram_count = 0

    for ranking_idx, ranking_location in tbl_predicted.iterrows():
        model_element_id = ranking_location.ModelElementID
        neo4j_id_stored = ranking_location.DatabaseNodeID

        if TOP_RANKING_K != -1 and diagram_count >= TOP_RANKING_K:
            break

        # print(tbl_info_file, ranking_idx, "unseen:", len(tbl_predicted.index) - len(seen_location_ids))

        if MATCH_NEO4J_ID:
            neo4j_id = query_util.get_neo4j_node_id(buglocation_graph, model_element_id, db_version)

            if (neo4j_id_stored != neo4j_id):
                print("WARNING: Neo4j node ID changed.")
        else:
            neo4j_id = neo4j_id_stored

        # Aggregate ranking:
        if not DIAGRAM_AGGREGATION or neo4j_id not in seen_location_ids:
            subgraph_location_ids: List[int] = get_subgraph_location_ids(
                tbl_predicted, ranking_location, buglocation_graph, db_version,
                meta_model, K_NEIGHBOUR_DISTANCE, UNDIRECTED, DIAGRAM_NEIGHBOR_SIZE)
            seen_location_ids.update(subgraph_location_ids)

            if SAVE_DIAGRAM:
                diagram_graph = diagram_util.slice_diagram(buglocation_graph, db_version, subgraph_location_ids)
                diagram_util.save_diagram(diagram_graph, diagram_save_path + "/" + "{:04d}".format(diagram_count) + "_diagram.json")

            # Pull up relevant position to first diagram that contains the relevant location:
            expected_location_ids_len = len(expected_location_ids)
            expected_location_ids -= set(subgraph_location_ids)

            diagram_size += len(subgraph_location_ids)
            diagram_count += 1

            # Found unseen locations?
            if expected_location_ids_len != len(expected_location_ids):
                relevant_database_node_ids.add(neo4j_id)
            else:
                not_relevant_database_node_ids.add(neo4j_id)

    # Set subgraph nodes as locations:
    tbl_predicted_aggregation = tbl_predicted[tbl_predicted.DatabaseNodeID.isin(
        relevant_database_node_ids) | tbl_predicted.DatabaseNodeID.isin(not_relevant_database_node_ids)]

    for ranking_idx, model_element in tbl_predicted_aggregation.iterrows():
        database_node_id = model_element.DatabaseNodeID

        # Pull up ranking to first diagram that contains the location:
        if database_node_id in relevant_database_node_ids:
            tbl_predicted_aggregation.at[ranking_idx, 'IsLocation'] = 1
        else:
            tbl_predicted_aggregation.at[ranking_idx, 'IsLocation'] = 0

    return tbl_predicted_aggregation, diagram_size, diagram_count


def get_first_relevant_subgraph_location(tbl_predicted: pd.DataFrame,
                                         TOP_RANKING_K: int,
                                         graph: Graph,
                                         db_version: int,
                                         meta_model: MetaModel,
                                         K_NEIGHBOUR_DISTANCE: int,
                                         UNDIRECTED: bool,
                                         NUMBER_OF_NEIGHBORS: int,
                                         DIAGRAM_AGGREGATION: bool = True) -> Optional[Tuple[pd.Series, pd.DataFrame, pd.DataFrame]]:

    locations_added_to_subgraphs: Set[int] = set()
    current_ranking_idx = 0  # Need to be recalculated when using diagram aggregation.

    for ranking_idx, ranking_location in tbl_predicted.iterrows():
        if current_ranking_idx >= TOP_RANKING_K:
            break

        node_id = ranking_location.DatabaseNodeID

        if node_id not in locations_added_to_subgraphs:
            current_ranking_idx += 1

            labels_mask = list(meta_model.get_bug_location_types())
            subgraph_k = query_util.subgraph_k(graph, ranking_location.DatabaseNodeID, db_version,
                                               K_NEIGHBOUR_DISTANCE, UNDIRECTED, meta_model, labels_mask)
            subgraph_k = subgraph_k[subgraph_k.index != ranking_location.DatabaseNodeID]  # without start node
            ranking_of_subgraph_k = get_ranking_of_subgraph_nodes(subgraph_k, tbl_predicted)
            top_k_ranking_of_subgraph_k = ranking_of_subgraph_k.head(NUMBER_OF_NEIGHBORS)

            if DIAGRAM_AGGREGATION:
                locations_added_to_subgraphs.update(subgraph_k.index.to_list())

            if 1 in top_k_ranking_of_subgraph_k.IsLocation.to_list() or ranking_location.IsLocation == 1:
                return ranking_location, top_k_ranking_of_subgraph_k, subgraph_k

    return None


def top_k_ranking_subgraph_location(evaluation_results: List[Tuple[str, pd.DataFrame, str, pd.DataFrame]],
                                    meta_model: MetaModel,
                                    graph: Graph,
                                    TOP_RANKING_K: int,
                                    NUMBER_OF_NEIGHBORS: int,
                                    K_NEIGHBOUR_DISTANCE: int = 0,
                                    UNDIRECTED: bool = True,
                                    DIAGRAM_AGGREGATION: bool = True) -> Tuple[int, int]:
    """
    Args:
        TOP_RANKING_K (int, optional): Compute for top k ranking positions
        DIAGRAM_SIZE (int, optional): Size of the diagram:. Defaults to 60.
        SAVE_DIAGRAM (bool, optional): Write diagram nodes and edges as Json-File. Defaults to False.
        K_NEIGHBOURS (int, optional): Hops from the expected locations. Defaults to 2.
        UNDIRECTED (bool, optional): Follow undirected edges on diagram slicing. Defaults to True.
        DIAGRAM_AGGREGATION (bool, optional): If a node of the input ranking is already contained in a 
          higher ranked diagram it will the ranking position will not be considered again.Defaults to True.

    Returns:
        (int, int): [0] Number of ranking that contain at least on expexted location in top k.
                    [1] Number of ranking that contain no expexted location in top k.
    """

    found_in_top_k = 0
    not_found_in_top_k = 0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in evaluation_results:
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        top_k_ranking_subgraph_location = tbl_predicted.head(TOP_RANKING_K)

        # Is node directly contained in top k results (TOP_RANKING_K)?
        if 1 in top_k_ranking_subgraph_location.IsLocation.to_list():
            found_in_top_k += 1
        # Is node indirectly contained by subgraph (K_NEIGHBOURS,DIAGRAM_SIZE)
        else:
            first_expected_location = get_first_relevant_subgraph_location(
                tbl_predicted, TOP_RANKING_K, graph, db_version, meta_model,
                K_NEIGHBOUR_DISTANCE, UNDIRECTED, NUMBER_OF_NEIGHBORS, DIAGRAM_AGGREGATION)

            if first_expected_location is not None:
                found_in_top_k += 1
            else:
                not_found_in_top_k += 1

        print(tbl_info_file, "Found:", found_in_top_k, "Not found:", not_found_in_top_k)

    return found_in_top_k, not_found_in_top_k
