'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path
from typing import List, Set

import pandas as pd
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from py2neo import Graph

from evaluations.evaluation_ranking_metrics import project_filter
from buglocalization.diagrams import diagram_ranking_util

"""
- Pull up relevant position to first diagram that contains the relevant location of the classifier ranking.
- Aggregate ranking: Generate diagram for each position in classifier ranking - do not consider a position that were already seen.
"""

if __name__ == '__main__':

    # Size of the diagram:
    DIAGRAM_NEIGHBOR_SIZE = 20
    # Avg Diagram Size 35.482114940383134 = Diagram Size 12165256 / Diagram Count 342856
    
    # Slice and save diagram nodes as JSON file:
    SAVE_DIAGRAM = True

    #  Generate diagram for each position in classifier ranking - do not consider a position that were already seen.
    DIAGRAM_AGGREGATION = True

    # Compute only first k entries of the diagram ranking:
    TOP_RANKING_K = 15  # -1

    # Hops from the expected locations:
    K_NEIGHBOUR_DISTANCE = 2
    UNDIRECTED = True

    # Match model elements by Neo4j ID or by model element ID and version?
    MATCH_NEO4J_ID = False  # TODO: USING INDEX n:Nlable

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    evaluation_results_with_subgraphs_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder
    evaluation_results_with_subgraphs_path += "_k" + str(K_NEIGHBOUR_DISTANCE)
    evaluation_results_with_subgraphs_path += '_undirected' if UNDIRECTED else '_directed'
    evaluation_results_with_subgraphs_path += "_aggregated/"
    Path(evaluation_results_with_subgraphs_path).mkdir(parents=True, exist_ok=True)

    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")
    diagram_size = 0.0
    diagram_count = 0.0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path, project_filter):
        print(tbl_predicted_file)
        
        diagram_save_path = evaluation_results_with_subgraphs_path + tbl_predicted_file[:tbl_predicted_file.rfind("_")]
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        
        tbl_predicted_aggregation, tbl_diagram_size, tbl_diagram_count = diagram_ranking_util.get_subgraph_ranking(
            tbl_predicted,
            meta_model,
            buglocation_graph,
            db_version,
            K_NEIGHBOUR_DISTANCE,
            UNDIRECTED,
            DIAGRAM_NEIGHBOR_SIZE,
            DIAGRAM_AGGREGATION,
            SAVE_DIAGRAM,
            diagram_save_path=diagram_save_path,
            TOP_RANKING_K=TOP_RANKING_K,
            MATCH_NEO4J_ID=MATCH_NEO4J_ID
        )
        
        diagram_size += tbl_diagram_size
        diagram_count += tbl_diagram_count

        # Save evaluation tables:
        tbl_info.to_csv(evaluation_results_with_subgraphs_path + tbl_info_file, sep=';', index=False)
        tbl_predicted_aggregation.to_csv(evaluation_results_with_subgraphs_path + tbl_predicted_file, sep=';', index=False)

    print("Avg Diagram Size: ", diagram_size / diagram_count)
