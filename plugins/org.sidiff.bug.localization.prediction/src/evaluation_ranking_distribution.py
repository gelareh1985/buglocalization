import os
from pathlib import Path

import pandas as pd

from buglocalization.evaluation import evaluation_util as eval_util

if __name__ == '__main__':

    # Evaluation result tables:
    #evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04"
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04_k2_undirected"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"
    
    all_expected_locations = []

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path):
        expected_locations = eval_util.get_expected_locations(tbl_predicted)
        expected_locations['ranking'] = expected_locations.index
        
        if not expected_locations.empty:
            # if expected_locations['ranking'].iloc[0] <= 200:
            all_expected_locations.append(expected_locations.head(1))

    all_expected_locations_df = pd.concat(all_expected_locations, ignore_index=True)
    boxplot = all_expected_locations_df.boxplot(column=['ranking'], showfliers=False)
    