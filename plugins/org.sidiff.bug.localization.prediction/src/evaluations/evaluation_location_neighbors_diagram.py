'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path

import pandas as pd
from py2neo import Graph

from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from evaluations.evaluation_ranking_metrics import project_filter

"""
Generates the top-k metrics for a range of different diagram size and neighbor hops.
"""

if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    evaluation_results = eval_util.load_all_evaluation_results(evaluation_results_path, project_filter)

    # Output folder:
    evaluation_metrics_path = evaluation_results_path + 'metrics/'
    Path(evaluation_metrics_path).mkdir(parents=True, exist_ok=True)

    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

    # # Evaluations # #
    k_neighbors = [2]
    top_k_values = [1, 5, 10, 15, 20, 25, 30, 35]
    numbers_of_neighbors = [1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    diagram_aggregation = True

    for k_neighbor in k_neighbors:
        print("Experiment: k neighbor:", k_neighbor)

        # [diagram_sizes (index), top_k_ranking_accuracy@top_k_values]
        evaluation = pd.DataFrame(columns=top_k_values, index=numbers_of_neighbors)
        evaluation.index.names = ['number_of_neighbors']

        # Diagram size has no effect if no neigbors are considered:
        if k_neighbor == 0:
            evaluation = evaluation[evaluation.index == 1]

        for top_k_value in top_k_values:
            print("Experiment: top k value:", top_k_value)

            for number_of_neighbors in numbers_of_neighbors:
                print("Experiment: diagram size:", number_of_neighbors)

                found_in_top_k, not_found_in_top_k = eval_util.top_k_ranking_subgraph_location(
                    evaluation_results=evaluation_results,
                    meta_model=meta_model,
                    graph=buglocation_graph,
                    TOP_RANKING_K=top_k_value,
                    NUMBER_OF_NEIGHBORS=number_of_neighbors,
                    K_NEIGHBOUR_DISTANCE=k_neighbor,
                    UNDIRECTED=True,
                    DIAGRAM_AGGREGATION=diagram_aggregation)
                top_k_ranking_accuracy_result = eval_util.top_k_ranking_accuracy(found_in_top_k, not_found_in_top_k)
                evaluation.at[number_of_neighbors, top_k_value] = top_k_ranking_accuracy_result

                print("Found:", found_in_top_k, "Not found:", not_found_in_top_k)
                print("Top k Accuracy:", top_k_ranking_accuracy_result)

                # Diagram size has no effect if no neigbors are considered:
                if k_neighbor == 0:
                    break

            # Checkpoint save evaluation:
            filename = 'k_neighbor_' + str(k_neighbor) + '.csv'
            evaluation.to_csv(evaluation_metrics_path + filename, sep=';')

        # Save evaluation:
        filename = 'k_neighbor_' + str(k_neighbor) + '.csv'
        evaluation.to_csv(evaluation_metrics_path + filename, sep=';')
        