'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from typing import Generator, List, Optional, Tuple

import pandas as pd
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.metamodel.meta_model import MetaModel
from py2neo import Graph
from buglocalization.diagrams import diagram_util as dia_util


def load_evaluation_results(path: str) -> Generator[Tuple[str, pd.DataFrame, str, pd.DataFrame], None, None]:
    for filename in os.listdir(path):
        if filename.endswith("_info.csv"):
            evaluation_filename = filename[:filename.rfind("_")]

            tbl_info_file = evaluation_filename + "_info.csv"
            tbl_info_path = path + tbl_info_file
            tbl_info = pd.read_csv(tbl_info_path, sep=';', header=0)

            tbl_predicted_file = evaluation_filename + "_prediction.csv"
            tbl_predicted_path = path + tbl_predicted_file
            tbl_predicted = pd.read_csv(tbl_predicted_path, sep=';', header=0)

            yield tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted


def load_all_evaluation_results(path: str) -> List[Tuple[str, pd.DataFrame, str, pd.DataFrame]]:
    return list(load_evaluation_results(path))


def get_ranking_results(evaluation_results: List[Tuple[str, pd.DataFrame, str, pd.DataFrame]],
                        min_rank: int = None) -> Tuple[List[pd.DataFrame], int]:

    all_ranking_results = []
    outliers = 0

    for evaluation_result in evaluation_results:
        tbl_predicted = evaluation_result[3]
        expected_locations = get_expected_locations(tbl_predicted)
        first_expected_location = expected_locations.head(1)

        if min_rank is not None and min_rank != -1:
            if not first_expected_location.empty and int(first_expected_location.index.values[0]) <= min_rank:
                all_ranking_results.append(tbl_predicted)
            else:
                outliers += 1
        else:
            # Ignore rankings without any ground truths:
            if not first_expected_location.empty:
                all_ranking_results.append(tbl_predicted)
            else:
                outliers += 1

    print("Number of outliers:", outliers)
    return all_ranking_results, outliers


def get_expected_locations(tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    return tbl_predicted_data[tbl_predicted_data.IsLocation == 1].copy()


def get_ranking_of_subgraph(subgraph_k: pd.DataFrame, tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    # subgraph_k -> index -> node IDs
    return tbl_predicted_data[tbl_predicted_data.DatabaseNodeID.isin(subgraph_k.index)]


def top_k_ranking_accuracy(found_in_top_k, not_found_in_top_k) -> float:
    return float(found_in_top_k) / float(found_in_top_k + not_found_in_top_k)


def box_plot_quantiles(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe.quantile([0.00, 0.25, 0.5, 0.75, 1.0])


def box_plot_median(dataframe_with_ranking_col: pd.DataFrame) -> float:
    return box_plot_quantiles(dataframe_with_ranking_col).loc[0.50].ranking


def box_plot_upper_wiskers(dataframe_with_ranking_col: pd.DataFrame) -> float:
    quantile = box_plot_quantiles(dataframe_with_ranking_col)

    q1 = quantile.loc[0.25].ranking
    q3 = quantile.loc[0.75].ranking
    interquartile_range = q3 - q1

    upper_whiskers = (q3 + (interquartile_range * 1.5)) - 1
    return upper_whiskers


def box_plot_upper_quantile_q3(dataframe_with_ranking_col: pd.DataFrame) -> float:
    return box_plot_quantiles(dataframe_with_ranking_col).loc[0.75].ranking


def top_k_ranking(ranking_results: List[pd.DataFrame], TOP_RANKING_K: int) -> Tuple[int, int]:
    found_in_top_k = 0
    not_found_in_top_k = 0

    for ranking_result in ranking_results:
        top_k_ranking = ranking_result.head(TOP_RANKING_K)

        # Is node directly contained in top k results (TOP_RANKING_K)?
        if 1 in top_k_ranking.IsLocation.to_list():
            found_in_top_k += 1
        else:
            not_found_in_top_k += 1

    return found_in_top_k, not_found_in_top_k


def get_first_relevant_subgraph_location(tbl_predicted: pd.DataFrame,
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
        ranking_of_subgraph_k = get_ranking_of_subgraph(subgraph_k, tbl_predicted)
        top_k_ranking_of_subgraph_k = ranking_of_subgraph_k.head(DIAGRAM_NEIGHBOR_SIZE)

        if 1 in top_k_ranking_of_subgraph_k.IsLocation.to_list() or ranking_location.IsLocation == 1:
            return ranking_location, top_k_ranking_of_subgraph_k, subgraph_k

    return None


def top_k_ranking_subgraph_location(evaluation_results: List[Tuple[str, pd.DataFrame, str, pd.DataFrame]],
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
        top_k_ranking_subgraph_location = tbl_predicted.head(TOP_RANKING_K)

        # Is node directly contained in top k results (TOP_RANKING_K)?
        if 1 in top_k_ranking_subgraph_location.IsLocation.to_list():
            found_in_top_k += 1
        # Is node indirectly contained by subgraph (K_NEIGHBOURS,DIAGRAM_SIZE)
        else:
            first_expected_location = get_first_relevant_subgraph_location(tbl_predicted, graph, db_version,
                                                                           top_k_ranking_subgraph_location, meta_model,
                                                                           K_NEIGHBOURS, UNDIRECTED, DIAGRAM_NEIGHBOR_SIZE)

            if first_expected_location is not None:
                found_in_top_k += 1

                if SAVE_DIAGRAM and diagram_save_path is not None:
                    ranking_location_id = first_expected_location[0].DatabaseNodeID
                    top_k_ranking_of_subgraph_k = first_expected_location[1]
                    ranked_node_ids = top_k_ranking_of_subgraph_k.DatabaseNodeID.to_list()
                    ranked_node_ids.append(ranking_location_id)

                    diagram_graph = dia_util.slice_diagram(graph, db_version, ranked_node_ids)
                    dia_util.save_diagram(diagram_graph, diagram_save_path + tbl_predicted_file + "_diagram.json")
            else:
                not_found_in_top_k += 1

        print(tbl_info_file, "Found:", found_in_top_k, "Not found:", not_found_in_top_k)

    return found_in_top_k, not_found_in_top_k


def get_all_expected_locations(tbls_predicted: List[pd.DataFrame]) -> pd.DataFrame:
    all_expected_locations = []

    for tbl_predicted in tbls_predicted:
        expected_locations = get_expected_locations(tbl_predicted)
        expected_locations['ranking'] = expected_locations.index

        if not expected_locations.empty:
            all_expected_locations.append(expected_locations.head(1))

    all_expected_locations_df = pd.concat(all_expected_locations, ignore_index=True)
    return all_expected_locations_df


def get_ranking_outliers(tbls_predicted: List[pd.DataFrame]) -> Tuple[int, int, int, int]:
    all_expected_locations_df = get_all_expected_locations(tbls_predicted)

    median = box_plot_median(all_expected_locations_df)
    upper_quantile_q3 = box_plot_upper_quantile_q3(all_expected_locations_df)
    upper_whiskers = box_plot_upper_wiskers(all_expected_locations_df)

    return -1, int(upper_whiskers), int(upper_quantile_q3), int(median)


def calculate_mean_average_precision(tbls_predicted: List[pd.DataFrame]) -> float:
    """
    MAP(Mean Average Precision) is a standard metric to verify the performance, and is used widely in information
    retrieval[[16]]. It considers not only the accuracy but also the ranking result. Similar to information retrieval,
    the higher score of MAP, the better performance for bug localization. MAP can be calculated as follows:

    MAP = 1/n1 * (SUM[i=1,n1] SUM[j=1,n2] (Prec(j) ∗ bool(j)) / N_i)
    
    Prec(j) = Q(j) / j

    where n1, n2 denote the number ofbug reports and candidate source files respectively. N_i is the number of relevant
    source files to the bug report i and bool(j) is the vector indicating whether the source files in ranking j is relevant
    or not.Prec(j) is the precision function,and Q(j) is the number of relevant source file in the ranking j.

    Args:
        tbls_predicted (List[pd.DataFrame]): All rankings.

    Returns:
        float: MAP (Mean Average Precision) of all given rankings
    """

    # https://towardsdatascience.com/breaking-down-mean-average-precision-map-ae462f623a52

    average_precision_sum = 0.0
    n1_number_bug_reports = len(tbls_predicted)
    i = 0

    for tbl_predicted in tbls_predicted:
        n2_number_potential_bug_locations = len(tbl_predicted.index)
        ni_expected_locations = get_expected_locations(tbl_predicted)
        ni_number_of_relevant = len(ni_expected_locations.index)

        # Ignore rankings without any ground truths:
        if ni_number_of_relevant > 0:
            
            # Average precision of recommendation:
            average_precision_sum_i = 0.0

            for j_ranking in range(1, n2_number_potential_bug_locations + 1):
                if tbl_predicted.loc[j_ranking - 1].IsLocation == 1:
                    tbl_predicted_j = tbl_predicted.head(j_ranking)
                    expected_locations_j = get_expected_locations(tbl_predicted_j)
                    number_of_relevant_j = len(expected_locations_j.index)
                    prec_j = float(number_of_relevant_j) / float(j_ranking)
                    average_precision_sum_i += prec_j

            average_precision_i = average_precision_sum_i / float(ni_number_of_relevant)
            average_precision_sum += average_precision_i

            # For intermediate results:
            i += 1
            mean_average_precision = average_precision_sum / float(i)
            print("Mean Average Precision:", i, "  ", mean_average_precision)

    mean_average_precision = average_precision_sum / float(n1_number_bug_reports)
    return mean_average_precision


def calculate_mean_reciprocal_rank(tbls_predicted: List[pd.DataFrame]) -> float:
    reciprocal_rank_sum = 0.0

    for tbl_predicted in tbls_predicted:
        expected_locations = get_expected_locations(tbl_predicted)

        # Ignore rankings without any ground truths:
        if not expected_locations.empty:
            rank = get_expected_locations(tbl_predicted).index[0]
            reciprocal_rank = 1.0 / float(rank + 1)  # we count the ranking from 0
            reciprocal_rank_sum += reciprocal_rank

    n1_number_bug_reports = len(tbls_predicted)
    mean_reciprocal_rank = reciprocal_rank_sum / float(n1_number_bug_reports)
    return mean_reciprocal_rank
