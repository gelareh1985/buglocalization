'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from __future__ import annotations

import os

from pathlib import Path
from typing import List, Set

import pandas as pd
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.diagrams import diagram_ranking_util, diagram_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from py2neo import Graph

from evaluations.evaluation_ranking_metrics import project_filter

"""
- Pull up relevant position to first diagram that contains the relevant location of the classifier ranking.
- Aggregate ranking: Generate diagram for each position in classifier ranking - do not consider a position that were already seen.
- Generate (best) diagram ranking: SAVE_DIAGRAM, TOP_K_RANKINGS
"""


class DiagramRanking:

    def __init__(self, db_version: int,
                 tbl_info_file: str, tbl_info: pd.DataFrame,
                 tbl_predicted_file: str, tbl_predicted_aggregation: pd.DataFrame,
                 diagram_ranking: List[List[int]]):
        self.db_version: int = db_version
        self.tbl_info_file: str = tbl_info_file
        self.tbl_info: pd.DataFrame = tbl_info
        self.tbl_predicted_file: str = tbl_predicted_file
        self.tbl_predicted_aggregation: pd.DataFram = tbl_predicted_aggregation
        self.diagram_ranking: List[List[int]] = diagram_ranking

        relevant_locations = eval_util.get_relevant_locations(tbl_predicted_aggregation.reset_index(drop=True))
        self.numher_of_relevant = len(relevant_locations)

        if self.numher_of_relevant > 0:
            self.top_ranking_position = relevant_locations.head().index[0]
        else:
            self.top_ranking_position = -1

    def save(self, path: str, buglocation_graph: Graph):
        self.tbl_info.to_csv(path + self.tbl_info_file, sep=';', index=False)
        self.tbl_predicted_aggregation.to_csv(path + self.tbl_predicted_file, sep=';', index=False)

        for diagram_ranking_idx in range(len(self.diagram_ranking)):
            subgraph_location_ids = self.diagram_ranking[diagram_ranking_idx]
            diagram_save_path = path + self.tbl_predicted_file[:self.tbl_predicted_file.rfind("_")]
            diagram_graph = diagram_util.slice_diagram(buglocation_graph, self.db_version, subgraph_location_ids)
            diagram_util.save_diagram(diagram_graph, diagram_save_path + "/" + "{:04d}".format(diagram_ranking_idx) + "_diagram.json")

    @staticmethod
    def compare(rA: DiagramRanking, rB: DiagramRanking):
        """
        Returns a negative, zero or positive number depending on whether the first argument
        is considered smaller than, equal to, or larger than the second argument.
        """
        if rA.top_ranking_position != -1 and rB.top_ranking_position != -1:
            if rA.top_ranking_position < rB.top_ranking_position:
                return -1
            elif rA.top_ranking_position == rB.top_ranking_position:
                if rA.numher_of_relevant > rB.numher_of_relevant:
                    return -1
                else:
                    return 1
        else:
            if rA.top_ranking_position > rB.top_ranking_position:
                return -1  # a > -1
            elif rB.top_ranking_position > rA.top_ranking_position:
                return 1  # b > -1
            elif rA.numher_of_relevant != rB.numher_of_relevant:
                if rA.numher_of_relevant > rB.numher_of_relevant:
                    return -1
                else:
                    return 1
        return 0

    @staticmethod
    def comparator():
        from functools import cmp_to_key
        return cmp_to_key(DiagramRanking.compare)


if __name__ == '__main__':

    # Size of the diagram:
    DIAGRAM_NEIGHBOR_SIZE = 35  # 35, 10
    # Avg Diagram Size 35.482114940383134 = Diagram Size 12165256 / Diagram Count 342856

    # Save only n "best" ranking samples:
    BEST_RANKING_SAMPLES = -1  # -1, 30

    # Slice and save diagram nodes as JSON file:
    SAVE_DIAGRAM = True  # False, True

    #  Generate diagram for each position in classifier ranking - do not consider a position that were already seen.
    DIAGRAM_AGGREGATION = True  # True, True

    # Compute only first k entries of the diagram ranking:
    TOP_RANKING_K = -1  # -1, 15

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

    diagram_rankings: List[DiagramRanking] = []
    diagram_ranking_comparator = DiagramRanking.comparator()
    diagram_size = 0.0
    diagram_count = 0.0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path, project_filter):
        print(tbl_predicted_file)
        db_version = int(tbl_info.ModelVersionNeo4j[0])

        tbl_predicted_aggregation, diagram_ranking, tbl_diagram_size, tbl_diagram_count = diagram_ranking_util.get_subgraph_ranking(
            tbl_predicted,
            meta_model,
            buglocation_graph,
            db_version,
            K_NEIGHBOUR_DISTANCE,
            UNDIRECTED,
            DIAGRAM_NEIGHBOR_SIZE,
            DIAGRAM_AGGREGATION,
            SAVE_DIAGRAM,
            TOP_RANKING_K,
            MATCH_NEO4J_ID
        )

        diagram_size += tbl_diagram_size
        diagram_count += tbl_diagram_count

        # Store ranking:
        ranking = DiagramRanking(db_version, tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted_aggregation, diagram_ranking)

        if BEST_RANKING_SAMPLES == -1:
            ranking.save(evaluation_results_with_subgraphs_path, buglocation_graph)
        else:
            # Only best k rankings:
            diagram_rankings.append(ranking)
            diagram_rankings.sort(key=diagram_ranking_comparator)

            if len(diagram_rankings) > BEST_RANKING_SAMPLES:
                del diagram_rankings[BEST_RANKING_SAMPLES]

    if BEST_RANKING_SAMPLES != -1:
        for diagram_ranking in diagram_rankings:
            print("Save:", diagram_ranking.tbl_predicted_file)
            diagram_ranking.save(evaluation_results_with_subgraphs_path, buglocation_graph)

    print("Avg Diagram Size: ", diagram_size / diagram_count)
