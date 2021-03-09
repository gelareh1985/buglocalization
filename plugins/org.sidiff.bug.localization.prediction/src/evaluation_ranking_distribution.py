import os
from pathlib import Path

import pandas as pd

from buglocalization.evaluation import evaluation_util as eval_util

if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04"
    #evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04_k2_undirected"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"
    
    all_expected_locations = []

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path):
        expected_locations = eval_util.get_expected_locations(tbl_predicted)
        expected_locations['ranking'] = expected_locations.index
        
        if not expected_locations.empty:
            # if expected_locations['ranking'].iloc[0] <= 200:
            all_expected_locations.append(expected_locations.head(1))

    # Results:
    all_expected_locations_df = pd.concat(all_expected_locations, ignore_index=True)
    print("Describe:\n", all_expected_locations_df.describe())
    
    # Quantile:
    quantile = all_expected_locations_df.quantile([0.00, 0.25, 0.5, 0.75, 1.0])
    print("Quantile:\n", quantile)
    
    # Outliers:
    q1 = quantile.loc[0.25].ranking
    q3 = quantile.loc[0.75].ranking
    interquartile_range = q3 - q1
    upper_whiskers = (q3 + (interquartile_range * 1.5)) - 1
    print("Upper Whiskers:", upper_whiskers)
    
    outliers = all_expected_locations_df[all_expected_locations_df.ranking > upper_whiskers]
    print("Outliers ", len(outliers.index), ":\n", outliers)
    
    # Draw top k distribution:
    boxplot = all_expected_locations_df.boxplot(column=['ranking'], showfliers=False)
    