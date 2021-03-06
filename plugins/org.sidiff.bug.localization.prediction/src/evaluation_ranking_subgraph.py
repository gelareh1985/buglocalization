import os
from pathlib import Path

import pandas as pd
from py2neo import Graph

from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML


def is_expected_subgraph_location(top_k_ranking: pd.DataFrame, meta_model: MetaModel,
                         K_NEIGHBOURS: int, UNDIRECTED: bool, DIAGRAM_SIZE: int):

    for ranking_idx, node in top_k_ranking.iterrows():
        labels_mask = list(meta_model.get_bug_location_types())
        subgraph_k = query_util.subgraph_k(buglocation_graph, node.DatabaseNodeID, db_version,
                                           K_NEIGHBOURS, UNDIRECTED, meta_model, labels_mask)
        ranking_of_subgraph_k = eval_util.get_ranking_of_subgraph(subgraph_k, tbl_predicted)
        diagram_nodes = ranking_of_subgraph_k.head(DIAGRAM_SIZE)

        if 1 in diagram_nodes.IsLocation.to_list():
            return True

    return False


if __name__ == '__main__':

    # Compute for top k ranking positions:
    TOP_RANKING_K = 35

    # Hops from the expected locations:
    K_NEIGHBOURS = 2
    UNDIRECTED = True

    # Size of the diagram:
    DIAGRAM_SIZE = 60

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

    found_in_top_k = 0
    not_found_in_top_k = 0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path):
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        expected_locations = eval_util.get_expected_locations(tbl_predicted)
        top_k_ranking = tbl_predicted.head(TOP_RANKING_K)

        # Is node directly contained in top k results (TOP_RANKING_K)?
        if 1 in top_k_ranking.IsLocation.to_list():
            found_in_top_k += 1
        # Is node indirectly contained by subgraph (K_NEIGHBOURS,DIAGRAM_SIZE)
        else:
            if is_expected_subgraph_location(top_k_ranking, meta_model, K_NEIGHBOURS, UNDIRECTED, DIAGRAM_SIZE):
                found_in_top_k += 1
            else:
                not_found_in_top_k += 1
                
        print(tbl_info_file, "Found:", found_in_top_k, "Not found:", not_found_in_top_k)

    print("Found:", found_in_top_k, "Not found:", not_found_in_top_k)
    print("Accuracy:", float(found_in_top_k) / (float(found_in_top_k) + float(not_found_in_top_k)))
