'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path

from buglocalization.evaluation import evaluation_util as eval_util

if __name__ == '__main__':

    # Evaluation result tables:
    #evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-13_02-54-25_train90_test10_layer1000"
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-14_03-01-12_lr-4"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"
    
    ranking_results, outliers = eval_util.get_ranking_results(eval_util.load_all_evaluation_results(evaluation_results_path))
    all_expected_locations_df = eval_util.get_all_expected_locations(ranking_results)
    print("Describe:\n", all_expected_locations_df.describe())
    
    # Quantile:
    quantile = eval_util.box_plot_quantiles(all_expected_locations_df)
    print("Quantile:\n", quantile)
    
    # Outliers:
    upper_whiskers = eval_util.box_plot_upper_wiskers(all_expected_locations_df)
    print("Upper Whiskers:", upper_whiskers)
    
    upper_quantile_q3 = eval_util.box_plot_upper_quantile_q3(all_expected_locations_df)
    print("Upper Quantile Q3:", upper_quantile_q3)
    
    outliers = all_expected_locations_df[all_expected_locations_df.ranking > upper_whiskers]
    print("Outliers ", len(outliers.index), ":\n", outliers)
    
    # Draw top k distribution:
    boxplot = all_expected_locations_df.boxplot(column=['ranking'], showfliers=False)
    