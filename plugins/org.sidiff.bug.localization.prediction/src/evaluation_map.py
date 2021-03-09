'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
import matplotlib.pyplot as plt
from pathlib import Path
from buglocalization.evaluation import evaluation_util as eval_util

# https://towardsdatascience.com/breaking-down-mean-average-precision-map-ae462f623a52

if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04_train90_test10_layer300"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    all_tbls_predicted_without_outlier = []

    outliers = 0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path):
        expected_locations = eval_util.get_expected_locations(tbl_predicted)
        first_expected_location = expected_locations.head(1)

        if not first_expected_location.empty and int(first_expected_location.index.values[0]) <= 656.0:
            all_tbls_predicted_without_outlier.append(tbl_predicted)
        else:
            outliers += 1

    print("Number of outliers:", outliers)

    average_precision_sum = 0.0
    n1_number_bug_reports = len(all_tbls_predicted_without_outlier)
    i = 0

    for tbl_predicted_without_outlier in all_tbls_predicted_without_outlier:
        n2_number_potential_bug_locations = len(tbl_predicted_without_outlier.index)
        ni_expected_locations = eval_util.get_expected_locations(tbl_predicted_without_outlier)
        ni_number_of_relevant = len(ni_expected_locations.index)

        average_precision_sum_i = 0.0
        
        for j_ranking in range(1, n2_number_potential_bug_locations + 1):
            if tbl_predicted_without_outlier.loc[j_ranking - 1].IsLocation == 1:
                tbl_predicted_j = tbl_predicted_without_outlier.head(j_ranking)
                expected_locations_j = eval_util.get_expected_locations(tbl_predicted_j)
                number_of_relevant_j = len(expected_locations_j.index)
                prec_j = float(number_of_relevant_j) / float(j_ranking)
                average_precision_sum_i += prec_j
                
        average_precision_i = average_precision_sum_i / float(ni_number_of_relevant)
        average_precision_sum += average_precision_i
            
        i += 1
        mean_average_precision = average_precision_sum / float(i)
        print("Mean Average Precision:", i, "  ", mean_average_precision)

    mean_average_precision = average_precision_sum / float(n1_number_bug_reports)
    print("Mean Average Precision:: ", mean_average_precision)
