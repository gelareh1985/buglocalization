'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path
from typing import List, Set

from py2neo import Graph

from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from evaluations.evaluation_ranking_metrics import project_filter

if __name__ == '__main__':

    # Size of the diagram:
    DIAGRAM_NEIGHBOR_SIZE = 35
    # Avg Diagram Size 35.482114940383134 = Diagram Size 12165256 / Diagram Count 342856

    # Hops from the expected locations:
    K_NEIGHBOURS = 2
    UNDIRECTED = True

    # Match model elements by Neo4j ID or by model element ID and version?
    MATCH_NEO4J_ID = False  # TODO: USING INDEX n:Nlable

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"

    evaluation_results_with_subgraphs_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder
    evaluation_results_with_subgraphs_path += "_k" + str(K_NEIGHBOURS)
    evaluation_results_with_subgraphs_path += '_undirected' if UNDIRECTED else '_directed'
    evaluation_results_with_subgraphs_path += "_aggregated/"
    Path(evaluation_results_with_subgraphs_path).mkdir(parents=True, exist_ok=True)

    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")
    
    diagram_size = 0
    diagram_count = 0

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path, project_filter):
        print(tbl_predicted_file)
        
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        expected_location_ids = eval_util.get_relevant_location_ids(tbl_predicted)
        relevant_database_node_ids = set()
        not_relevant_database_node_ids = set()
        seen_location_ids: Set[int] = set()
        
        for ranking_idx, ranking_location in tbl_predicted.iterrows():
            model_element_id = ranking_location.ModelElementID
            neo4j_id_stored = ranking_location.DatabaseNodeID

            # print(tbl_info_file, ranking_idx, "unseen:", len(tbl_predicted.index) - len(seen_location_ids))

            if MATCH_NEO4J_ID:
                neo4j_id = query_util.get_neo4j_node_id(buglocation_graph, model_element_id, db_version)
                
                if (neo4j_id_stored != neo4j_id):
                    print("WARNING: Neo4j node ID changed.")
            else:
                neo4j_id = neo4j_id_stored

            # Aggregate ranking:
            if neo4j_id not in seen_location_ids:
                subgraph_location_ids: List[int] = eval_util.get_subgraph_location_ids(
                    tbl_predicted, ranking_location, buglocation_graph, db_version,
                    meta_model, K_NEIGHBOURS, UNDIRECTED, DIAGRAM_NEIGHBOR_SIZE)
                seen_location_ids.update(subgraph_location_ids)

                # Pull up ranking to first diagram that contains the location:
                expected_location_ids_len = len(expected_location_ids)
                expected_location_ids -= set(subgraph_location_ids)
                
                diagram_size += len(subgraph_location_ids)
                diagram_count += 1

                # Found unseen locations?
                if expected_location_ids_len != len(expected_location_ids):
                    relevant_database_node_ids.add(neo4j_id)
                else:
                    not_relevant_database_node_ids.add(neo4j_id)

        # Set subgraph nodes as locations:
        tbl_predicted_aggregation = tbl_predicted[tbl_predicted.DatabaseNodeID.isin(
            relevant_database_node_ids) | tbl_predicted.DatabaseNodeID.isin(not_relevant_database_node_ids)]

        for ranking_idx, model_element in tbl_predicted_aggregation.iterrows():
            database_node_id = model_element.DatabaseNodeID

            # Pull up ranking to first diagram that contains the location:
            if database_node_id in relevant_database_node_ids:
                tbl_predicted_aggregation.at[ranking_idx, 'IsLocation'] = 1
            else:
                tbl_predicted_aggregation.at[ranking_idx, 'IsLocation'] = 0

        # Save evaluation tables:
        tbl_info.to_csv(evaluation_results_with_subgraphs_path + tbl_info_file, sep=';', index=False)
        tbl_predicted_aggregation.to_csv(evaluation_results_with_subgraphs_path + tbl_predicted_file, sep=';', index=False)
        
    print("Avg Diagram Size", float(diagram_size) / float(diagram_count), "=", "Diagram Size", diagram_size, "/", "Diagram Count", diagram_count)
