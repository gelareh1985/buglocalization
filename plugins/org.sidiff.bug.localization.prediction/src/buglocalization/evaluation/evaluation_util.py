'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from typing import Generator, List, Optional, Set, Tuple

import pandas as pd
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.metamodel.meta_model import MetaModel
from py2neo import Graph


def load_evaluation_results(path: str,
                            filters: List[str],
                            at_leat_one_relevant: bool = True) -> Generator[Tuple[str, pd.DataFrame, str, pd.DataFrame], None, None]:
    filtered = 0

    for filename in os.listdir(path):
        if filename.endswith("_info.csv"):
            evaluation_filename = filename[:filename.rfind("_")]

            tbl_info_file = evaluation_filename + "_info.csv"
            tbl_info_path = path + tbl_info_file
            tbl_info = pd.read_csv(tbl_info_path, sep=';', header=0)

            tbl_predicted_file = evaluation_filename + "_prediction.csv"
            tbl_predicted_path = path + tbl_predicted_file
            tbl_predicted = pd.read_csv(tbl_predicted_path, sep=';', header=0)

            if filters is not None:
                for filter_word in filters:
                    tbl_predicted = tbl_predicted[~tbl_predicted.ModelElementID.str.contains(filter_word)]
                tbl_predicted.reset_index(inplace=True)

            if not at_leat_one_relevant or (tbl_predicted.IsLocation == 1).any():
                yield tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted
            else:
                print("Filtered (no relevant locations):", tbl_predicted_file)
                filtered += 1

    print("Number of Filtered:", filtered)


def load_all_evaluation_results(path: str,
                                filters: List[str],
                                at_leat_one_relevant: bool = True) -> List[Tuple[str, pd.DataFrame, str, pd.DataFrame]]:
    return list(load_evaluation_results(path, filters, at_leat_one_relevant))


def get_ranking_results(evaluation_results: List[Tuple[str, pd.DataFrame, str, pd.DataFrame]],
                        min_rank: int = None) -> Tuple[List[pd.DataFrame], int]:

    all_ranking_results = []
    outliers = 0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in evaluation_results:
        expected_locations = get_relevant_locations(tbl_predicted)
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


def get_relevant_locations(tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    return tbl_predicted_data[tbl_predicted_data.IsLocation == 1].copy()


def get_relevant_location_ids(tbl_predicted_data: pd.DataFrame) -> Set[int]:
    return set(get_relevant_locations(tbl_predicted_data).DatabaseNodeID.to_list())


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


def get_all_top_relevant_locations(tbls_predicted: List[pd.DataFrame]) -> pd.DataFrame:
    """[summary]

    Args:
        tbls_predicted (List[pd.DataFrame]): A list of rankings.

    Returns:
        pd.DataFrame: The first/top relevant location of each ranking.
    """
    all_expected_locations = []

    for tbl_predicted in tbls_predicted:
        expected_locations = get_relevant_locations(tbl_predicted)
        expected_locations['ranking'] = expected_locations.index

        if not expected_locations.empty:
            all_expected_locations.append(expected_locations.head(1))

    all_expected_locations_df = pd.concat(all_expected_locations, ignore_index=True)
    return all_expected_locations_df


def get_all_relevant_locations(tbls_predicted: List[pd.DataFrame]) -> pd.DataFrame:
    """

    Args:
        tbls_predicted (List[pd.DataFrame]): A list of rankings.

    Returns:
        pd.DataFrame: All relevant locations of all rankings.
    """
    all_expected_locations = []

    for tbl_predicted in tbls_predicted:
        expected_locations = get_relevant_locations(tbl_predicted)
        expected_locations['ranking'] = expected_locations.index

        all_expected_locations.append(expected_locations)

    all_expected_locations_df = pd.concat(all_expected_locations, ignore_index=True)
    return all_expected_locations_df


def get_ranking_outliers(all_relevant_locations_df: pd.DataFrame) -> Tuple[int, int, int, int]:
    median = box_plot_median(all_relevant_locations_df)
    upper_quantile_q3 = box_plot_upper_quantile_q3(all_relevant_locations_df)
    upper_whiskers = box_plot_upper_wiskers(all_relevant_locations_df)

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
        ni_expected_locations = get_relevant_locations(tbl_predicted)
        ni_number_of_relevant = len(ni_expected_locations.index)

        # Ignore rankings without any ground truths:
        if ni_number_of_relevant > 0:

            # Average precision of recommendation:
            average_precision_sum_i = 0.0

            for j_ranking in range(1, n2_number_potential_bug_locations + 1):
                if tbl_predicted.loc[j_ranking - 1].IsLocation == 1:
                    tbl_predicted_j = tbl_predicted.head(j_ranking)
                    expected_locations_j = get_relevant_locations(tbl_predicted_j)
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
        expected_locations = get_relevant_locations(tbl_predicted)

        # Ignore rankings without any ground truths:
        if not expected_locations.empty:
            rank = get_relevant_locations(tbl_predicted).index[0]
            reciprocal_rank = 1.0 / float(rank + 1)  # we count the ranking from 0
            reciprocal_rank_sum += reciprocal_rank

    n1_number_bug_reports = len(tbls_predicted)
    mean_reciprocal_rank = reciprocal_rank_sum / float(n1_number_bug_reports)
    return mean_reciprocal_rank
