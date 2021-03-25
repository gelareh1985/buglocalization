'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path

from py2neo import Graph

from buglocalization.diagrams import diagram_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from evaluation_ranking_metrics import jdt_project_filter

if __name__ == '__main__':

    # Evaluation result tables:
    evaluation_results_folder = "trained_model_2021-03-13_16-16-02_lr-4_layer300_test"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    evaluation_results = eval_util.load_all_evaluation_results(evaluation_results_path, jdt_project_filter)

    # Output folder:
    diagram_save_path = evaluation_results_path + 'diagrams/'
    Path(diagram_save_path).mkdir(parents=True, exist_ok=True)

    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

    # # Evaluations # #
    k_neighbor = 2
    top_k_value = 35
    diagram_sizes = [35]
    diagram_aggregation = True

    for diagram_size in diagram_sizes:
        print("Diagram size:", diagram_size)

        diagram_util.save_first_relevant_diagram(
            evaluation_results=evaluation_results,
            meta_model=meta_model,
            graph=buglocation_graph,
            TOP_RANKING_K=top_k_value,
            DIAGRAM_SIZE=diagram_size,
            diagram_save_path=diagram_save_path,
            K_NEIGHBOUR_DISTANCE=k_neighbor,
            UNDIRECTED=True,
            DIAGRAM_AGGREGATION=diagram_aggregation)
