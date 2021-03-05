import os
from pathlib import Path
from typing import Generator, Tuple

import pandas as pd
from py2neo import Graph

import buglocalization.dataset.neo4j_queries as query
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML


def load_evaluation_results(path: str) -> Generator[Tuple[str, pd.DataFrame, str, pd.DataFrame], None, None]:
    for filename in os.listdir(path):
        if filename.endswith("_info.csv"):
            evaluation_filename = filename[:filename.rfind("_")]

            tbl_info_file = evaluation_filename + "_info.csv"
            tbl_info_path = path + tbl_info_file
            tbl_info = pd.read_csv(tbl_info_path, sep=';', header=0)
            
            tbl_predicted_file = evaluation_filename + "_prediction.csv"
            tbl_predicted_path = path + tbl_predicted_file
            tbl_predicted = pd.read_csv(tbl_predicted_path, sep=';', header=0)

            yield tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted


def get_expected_locations(tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    return tbl_predicted_data[tbl_predicted_data.IsLocation == 1].copy()


def get_neo4j_node_id(graph: Graph, model_element_id: str, db_version: int) -> int:
    """
    Args:
        model_element_id (str): The XMI-ID of the model element.
        db_version (int): The version number in the database.

    Returns:
        (int): The resolved node ID in the database.
    """
    db_version_parrameter = {'db_version': db_version}
    model_element = graph.run(query.nodes_in_version(model_element_id=model_element_id), db_version_parrameter).to_data_frame()

    assert len(model_element.index) == 1, 'ID not unique in version.'
    neo4j_id = model_element.nodes.iloc[0].identity

    return neo4j_id


if __name__ == '__main__':
    
    # Hops from the expected locations:
    K_VALUE = 2
    UNDIRECTED = True
    
    # Match model elements by Neo4j ID or by model element ID and version?
    MATCH_MODEL_ELEMENTS_BY_NEO4J_ID = True

    # Evaluation result tables:
    evaluation_results_folder = "eclipse.jdt.core_evaluation_2021-03-04_02-06-04"
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
    evaluation_results_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder + "/"
    
    evaluation_results_with_subgraphs_path: str = str(plugin_directory) + "/evaluation/" + evaluation_results_folder
    evaluation_results_with_subgraphs_path += "_k" + str(K_VALUE)
    evaluation_results_with_subgraphs_path += '_undirected' if UNDIRECTED else '_directed'
    evaluation_results_with_subgraphs_path += "/"
    Path(evaluation_results_with_subgraphs_path).mkdir(parents=True, exist_ok=True)
    
    # Meta-Model:
    meta_model: MetaModel = MetaModelUML()

    # Bug location garph in Neo4j:
    buglocation_graph = Graph(host="localhost", port=7687, user="neo4j", password="password")

    for tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted in load_evaluation_results(evaluation_results_path):
        db_version = int(tbl_info.ModelVersionNeo4j[0])
        db_version_parrameter = {'db_version': db_version}
        expected_locations = get_expected_locations(tbl_predicted)
        subgraph_model_element_ids = set()

        for idx_position, expected_location in expected_locations.iterrows():
            model_element_id = expected_location.ModelElementID
            neo4j_id_stored = expected_location.DatabaseNodeID
            
            if MATCH_MODEL_ELEMENTS_BY_NEO4J_ID:
                neo4j_id = neo4j_id_stored
            else:
                neo4j_id = get_neo4j_node_id(buglocation_graph, model_element_id, db_version)

                if (neo4j_id_stored != neo4j_id):
                    print("WARNING: Neo4j node ID chenged.")

            labels_mask = list(meta_model.get_bug_location_types())
            labels_blacklist = meta_model.get_system_model_connection_types()
            subgraph_query = query.subgraph_k(k=K_VALUE, node_id=neo4j_id, 
                                              labels_mask=labels_mask, 
                                              labels_blacklist=labels_blacklist, 
                                              undirected=UNDIRECTED)
            subgraph = buglocation_graph.run(subgraph_query, db_version_parrameter).to_data_frame()

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
