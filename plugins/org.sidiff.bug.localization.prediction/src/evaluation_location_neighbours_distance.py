import os
from pathlib import Path
from py2neo import Graph

from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.evaluation import evaluation_util as eval_util
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML


if __name__ == '__main__':
    
    # Hops from the expected locations:
    K_NEIGHBOURS = 2
    UNDIRECTED = True
    
    # Match model elements by Neo4j ID or by model element ID and version?
    MATCH_MODEL_ELEMENTS_BY_NEO4J_ID = True

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-14_03-01-12_lr-4"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"
    
    evaluation_results_with_subgraphs_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder
    evaluation_results_with_subgraphs_path += "_k" + str(K_NEIGHBOURS)
    evaluation_results_with_subgraphs_path += '_undirected' if UNDIRECTED else '_directed'
    evaluation_results_with_subgraphs_path += "/"
    Path(evaluation_results_with_subgraphs_path).mkdir(parents=True, exist_ok=True)
    
    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in eval_util.load_evaluation_results(evaluation_results_path):
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        expected_locations = eval_util.get_expected_locations(tbl_predicted)
        subgraph_model_element_ids = set()

        for idx_position, expected_location in expected_locations.iterrows():
            model_element_id = expected_location.ModelElementID
            neo4j_id_stored = expected_location.DatabaseNodeID
            
            if MATCH_MODEL_ELEMENTS_BY_NEO4J_ID:
                neo4j_id = neo4j_id_stored
            else:
                neo4j_id = query_util.get_neo4j_node_id(buglocation_graph, model_element_id, db_version)

                if (neo4j_id_stored != neo4j_id):
                    print("WARNING: Neo4j node ID chenged.")

            labels_mask = list(meta_model.get_bug_location_types())
            subgraph = query_util.subgraph_k(buglocation_graph, neo4j_id, db_version, K_NEIGHBOURS, UNDIRECTED, meta_model, labels_mask)

            for idx, subgraph_node in subgraph.iterrows():
                neigbour_model_element_id = subgraph_node.nodes['__model__element__id__']
                subgraph_model_element_ids.add(neigbour_model_element_id)

        # Set subgraph nodes as locations:
        for idx_position, model_element in tbl_predicted.iterrows():
            model_element_id = model_element.ModelElementID
            
            if model_element_id in subgraph_model_element_ids:
                tbl_predicted.at[idx_position, 'IsLocation'] = 1
                    
        # Save evaluation tables:
        tbl_info.to_csv(evaluation_results_with_subgraphs_path + tbl_info_file, sep=';', index=False)
        tbl_predicted.to_csv(evaluation_results_with_subgraphs_path + tbl_predicted_file, sep=';', index=False)
