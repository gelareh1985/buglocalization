'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

#%%

import os
import sys
from pathlib import Path

"""
Prints box plots to visualize the distribution of the relevant ranking positions.
"""

# WORKAROUND: Initialize PYTHONPATH with src folder for Jupyter extension:
if str(Path(os.path.dirname(os.path.abspath(__file__))).parent) not in sys.path:
    sys.path.append(str(Path(os.path.dirname(os.path.abspath(__file__))).parent))

from buglocalization.evaluation import evaluation_util as eval_util
from evaluations.evaluation_ranking_metrics import project_filter

# from this module location
if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.pde.ui_data-2021-04-09_model-2021-04-12_evaluation" #"word_ranking_eclipse.pde.ui_2021-09-24_03-28-49"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    ranking_results, outliers = eval_util.get_ranking_results(eval_util.load_all_evaluation_results(evaluation_results_path, project_filter))
    #all_relevant_locations_df = eval_util.get_all_relevant_locations(ranking_results)
    all_relevant_locations_df = eval_util.get_all_top_relevant_locations(ranking_results)
    print("Numbers of potential locations (first):", len(ranking_results[0].index))
    print("Numbers of potential locations (last):", len(ranking_results[len(ranking_results) - 1].index))
    print("Describe:\n", all_relevant_locations_df.describe())

    # Quantile:
    quantile = eval_util.box_plot_quantiles(all_relevant_locations_df)
    print("Quantile:\n", quantile)

    # Outliers:
    upper_whiskers = eval_util.box_plot_upper_wiskers(all_relevant_locations_df)
    print("Upper Whiskers:", upper_whiskers)

    upper_quantile_q3 = eval_util.box_plot_upper_quantile_q3(all_relevant_locations_df)
    print("Upper Quantile Q3:", upper_quantile_q3)

    outliers = all_relevant_locations_df[all_relevant_locations_df.ranking > upper_whiskers]
    print("Outliers ", len(outliers.index), ":\n", outliers)

    # Draw top k distribution:
    boxplot = all_relevant_locations_df.boxplot(column=['ranking'], showfliers=False)

# %%
