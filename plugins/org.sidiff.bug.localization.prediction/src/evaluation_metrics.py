'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path
from buglocalization.evaluation import evaluation_util as eval_util


if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04_train90_test10_layer300"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    all_tbls_predicted_without_outlier = eval_util.load_all_evaluation_results_without_outlier(
        evaluation_results_path, min_rank=656)

    mean_average_precision = eval_util.calculate_mean_average_precision(all_tbls_predicted_without_outlier)
    print("Mean Average Precision: ", mean_average_precision)
    