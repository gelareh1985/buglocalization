'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path
from typing import Any, List

import pandas as pd

from buglocalization.evaluation import evaluation_util as eval_util

"""
Computes the metrics Top-k, MAP and MRR for a given set of rankings.
"""

project_filter: List[str] = []  # e.g. ['.test', 'converterJclMin', 'library/']

if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folders = ["eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation_k2_undirected_aggregated",
                                  "eclipse.pde.ui_data-2021-04-09_model-2021-04-12_evaluation_k2_undirected_aggregated",
                                  "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation_k2_undirected_aggregated_core"]
    
    top_k_values_base = [1, 5, 10, 15, 20, 25, 30, 35]
    
    for evaluation_results_folder in evaluation_results_folders:
       
        # Load evaluations:
        plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent
        evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

        evaluation_results = eval_util.load_all_evaluation_results(evaluation_results_path, project_filter)
        
        # Evaluation settings:
        all_ranking_results, outliers = eval_util.get_ranking_results(evaluation_results)
        all_relevant_locations_df = eval_util.get_all_top_relevant_locations(all_ranking_results)
        min_ranks = eval_util.get_ranking_outliers(all_relevant_locations_df)
        
        top_k_values = top_k_values_base.copy()
        top_k_values_min_ranks = list(min_ranks[1:])
        top_k_values_min_ranks.reverse()
        top_k_values.extend(top_k_values_min_ranks)
        
        # Output folder:
        evaluation_metrics_path = evaluation_results_path + 'metrics/'
        Path(evaluation_metrics_path).mkdir(parents=True, exist_ok=True)
        
        # Result table:
        ranking_metrics_columns: List[Any] = ['FilteredOutliers', 'MeanAveragePrecision', 'MeanReciprocalRank']
        ranking_metrics_columns.extend(map(lambda top_k_value: 'TopkAccuracy@' + str(top_k_value), top_k_values))
        ranking_metrics = pd.DataFrame(columns=ranking_metrics_columns, index=min_ranks)
        ranking_metrics.index.names = ['MinimalRank']

        # Compute metrics for different outlier definitions:
        for min_rank in min_ranks:
            ranking_results, outliers = eval_util.get_ranking_results(evaluation_results, min_rank)
            ranking_metrics.at[min_rank, 'FilteredOutliers'] = outliers
            print("Minimal Rank:", min_rank, "Filtered Outliers:", outliers)

            mean_average_precision = eval_util.calculate_mean_average_precision(ranking_results)
            ranking_metrics.at[min_rank, 'MeanAveragePrecision'] = mean_average_precision
            print("Mean Average Precision:", mean_average_precision)

            mean_reciprocal_rank = eval_util.calculate_mean_reciprocal_rank(ranking_results)
            ranking_metrics.at[min_rank, 'MeanReciprocalRank'] = mean_reciprocal_rank
            print("Mean Reciprocal Rank:", mean_reciprocal_rank)
            
            for top_k_value in top_k_values:
                found_in_top_k, not_found_in_top_k = eval_util.top_k_ranking(ranking_results, top_k_value)
                top_k_ranking_accuracy_result = eval_util.top_k_ranking_accuracy(found_in_top_k, not_found_in_top_k)
                ranking_metrics.at[min_rank, 'TopkAccuracy@' + str(top_k_value)] = top_k_ranking_accuracy_result

                print("Found:", found_in_top_k, "Not found:", not_found_in_top_k)
                print("Top k Accuracy:", top_k_ranking_accuracy_result)
                
        # Save evaluation:
        filename = 'map_mrr_topk.csv'
        ranking_metrics.to_csv(evaluation_metrics_path + filename, sep=';')
