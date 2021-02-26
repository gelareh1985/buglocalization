'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

# ===============================================================================
# Configure GPU Device:
# https://towardsdatascience.com/setting-up-tensorflow-gpu-with-cuda-and-anaconda-onwindows-2ee9c39b5c44
# ===============================================================================
import tensorflow as tf  # type: ignore

# Only allocate needed memory needed by the application:
gpus = tf.config.experimental.list_physical_devices('GPU')

if gpus:
    try:
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
    except RuntimeError as e:
        print(e)
# ===============================================================================

import datetime
import os
from json import dump
from pathlib import Path
from time import time
from typing import Dict, List, Mapping, Optional, cast

import numpy as np
import pandas as pd

import bug_localization_data_set_neo4j_queries as query
from bug_localization_data_set_neo4j import (BugSamplePredictionNeo4j,
                                             DataSetPredictionNeo4j,
                                             Neo4jConfiguration)
from bug_localization_meta_model_uml import create_uml_configuration
from bug_localization_prediction import (
    BugLocalizationPrediction, BugLocalizationPredictionConfiguration)
from word_to_vector_shared_dictionary import WordDictionary

# TODO: Dump DL/Meta-Model Configuration

# ===============================================================================
# Evaluation Test max_dnn_depth
# ===============================================================================

# Evaluation Result Records:
plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
evaluation_results_path: str = str(plugin_directory) + "/evaluation/eclipse.jdt.core_" + \
    datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S') + "/"

# For debugging:
log_level: int = 0  # 0-100
peek_location_samples: Optional[int] = None  # Test only the first N location samples per bug sample; or None

# Configuration for bug localization prediction computation:
prediction_configuration = BugLocalizationPredictionConfiguration(

    # Trained bug localization model:
    bug_localization_model_path=str(plugin_directory) + "/training/trained_model_2021-02-24_20-37-42" + "/",

    # DL Model Prediction Configuration:
    # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
    num_samples=[20, 10],
    batch_size=100,

    sample_generator_workers=4,
    sample_generator_workers_multiprocessing=False,
    sample_max_queue_size=8
)

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)


class BugLocalizationPredictionTest:

    def get_file_name(self, bug_sample: BugSamplePredictionNeo4j):
        dataset: DataSetPredictionNeo4j = bug_sample.dataset
        query_database_version_parameter = {'db_version': bug_sample.db_version}

        query_bug_report_id = query.property_value_in_version('TracedBugReport', 'id')
        bug_report_id = dataset.run_query_value(query_bug_report_id, query_database_version_parameter)

        query_model_version = query.model_repo_version_by_db_version()
        model_version = dataset.run_query_value(query_model_version, query_database_version_parameter)

        return str(bug_sample.db_version) + '_' + 'bug' + str(bug_report_id) + '_' + model_version

    def record_evaluation_results(
            self, path: str, bug_sample: BugSamplePredictionNeo4j, prediction: np.ndarray, prediction_runtime: float, sort: bool = True):
        dataset: DataSetPredictionNeo4j = bug_sample.dataset
        query_database_version_parameter = {'db_version': bug_sample.db_version}

        # Tables:
        prediction_results_columns = ["DatabaseNodeID", "Prediction", "IsLocation", "MetaType", "ModelElementID"]
        bug_sample_info_columns = ["ModelVersionNeo4j", "ModelVersionRepository", "CodeVersionRepository",
                                   "BugReportNumber", "PredictionRuntime", "MissingLocations", "BugSummary"]

        Path(path).mkdir(parents=True, exist_ok=True)
        file_name_prefix = self.get_file_name(bug_sample)

        # ===========================================================================
        # Prediction result table:
        # ===========================================================================

        # DatabaseID, Prediction
        database_id_col = prediction_results_columns[0]
        prediction_col = prediction_results_columns[1]

        column_database_id: List[int] = []
        column_prediction: List[float] = []

        for location_sample_idx in range(len(prediction)):
            column_database_id.append(int(prediction[location_sample_idx][1]))
            column_prediction.append(prediction[location_sample_idx][0])

        assert len(column_database_id) == len(column_prediction)

        # Columns without index; MetaType and ModelElementID will be joined.
        prediction_results: pd.DataFrame = pd.DataFrame(index=column_database_id, columns=prediction_results_columns[1:3])
        prediction_results.index.name = database_id_col
        prediction_results[prediction_col] = column_prediction

        # IsLocation
        is_location_col = prediction_results_columns[2]
        missing_locations = []
        bug_locations_by_container_node_ids = []
        bug_locations_by_container = bug_sample.load_bug_locations(meta_model.find_bug_location_by_container())

        for model_location, model_location_type in bug_locations_by_container:
            if model_location in prediction_results.index:
                prediction_results.at[model_location, is_location_col] = 1
            else:
                missing_locations.append(model_location)

            bug_locations_by_container_node_ids.append(model_location)

        prediction_results[is_location_col].fillna(0, inplace=True)

        # MetaType, ModelElementID
        meta_type_col = prediction_results_columns[3]
        model_element_id_col = prediction_results_columns[4]

        query_type_and_names = query.nodes_by_ids('RETURN ID(n) AS ' + database_id_col +
                                                  ', LABELS(n) AS ' + meta_type_col +
                                                  ', n.__model__element__id__ AS ' + model_element_id_col)
        query_type_and_names_parameters = {'node_ids': prediction_results.index.tolist()}
        meta_type_and_model_element_id = dataset.run_query(query_type_and_names, query_type_and_names_parameters)
        meta_type_and_model_element_id.set_index(database_id_col, inplace=True)

        prediction_results = prediction_results.join(meta_type_and_model_element_id)

        # Sort predictions by probability:
        prediction_results.sort_values(by=prediction_col, ascending=False, inplace=True)

        # Save table:
        prediction_results.to_csv(path + '/' + file_name_prefix + '_prediction.csv', sep=';')

        # ===========================================================================
        # Bug sample information table :
        # ===========================================================================

        bug_sample_info: Dict[str, List] = {}

        # ModelVersionNeo4j
        model_version_neo4j_col = bug_sample_info_columns[0]
        bug_sample_info[model_version_neo4j_col] = [bug_sample.db_version]

        # ModelVersionRepository
        model_version_repository_col = bug_sample_info_columns[1]
        query_model_version = query.model_repo_version_by_db_version()
        model_version = dataset.run_query_value(query_model_version, query_database_version_parameter)
        bug_sample_info[model_version_repository_col] = model_version

        # CodeVersionRepository
        code_version_repository_col = bug_sample_info_columns[2]
        query_code_version = query.code_repo_version_by_db_version()
        code_version = dataset.run_query_value(query_code_version, query_database_version_parameter)
        bug_sample_info[code_version_repository_col] = code_version

        # BugReportNumber
        bug_report_id_col = bug_sample_info_columns[3]
        query_bug_report_id = query.property_value_in_version('TracedBugReport', 'id')
        bug_report_id = dataset.run_query_value(query_bug_report_id, query_database_version_parameter)
        bug_sample_info[bug_report_id_col] = [bug_report_id]

        # PredictionRuntime
        prediction_runtime_col = bug_sample_info_columns[4]
        bug_sample_info[prediction_runtime_col] = [prediction_runtime]

        # MissingLocations
        missing_locations_col = bug_sample_info_columns[5]
        bug_sample_info[missing_locations_col] = ['/'.join(map(str, missing_locations))]

        # BugSummary
        bug_summary_col = bug_sample_info_columns[6]
        query_bug_report_summary = query.property_value_in_version('TracedBugReport', 'summary')
        bug_report_summary = dataset.run_query_value(query_bug_report_summary, query_database_version_parameter)
        bug_sample_info[bug_summary_col] = bug_report_summary

        # Save tabel:
        bug_sample_information = pd.DataFrame.from_dict(bug_sample_info).set_index(model_version_neo4j_col)
        bug_sample_information.to_csv(path + '/' + file_name_prefix + '_info.csv', sep=';')

        # ===========================================================================
        # Bug sample information graph:
        # ===========================================================================

        # DatabaseNodeID, MetaType, Properties
        bug_location_graph = []

        # Version:
        query_version_node = query.nodes_in_version('TracedVersion')
        version_node = dataset.run_query(query_version_node, query_database_version_parameter)

        if not datatypes_query_slicing.empty:
            bug_location_graph.extend(version_node['nodes'].tolist())

        # Bug Report:
        query_bug_report_node = query.nodes_in_version('TracedBugReport')
        bug_report_node = dataset.run_query(query_bug_report_node, query_database_version_parameter)

        if not bug_report_node.empty:
            bug_location_graph.extend(bug_report_node['nodes'].tolist())

        # Bug Comments:
        query_bug_report_comment_nodes = query.nodes_in_version('BugReportComment')
        bug_report_comment_nodes = dataset.run_query(query_bug_report_comment_nodes, query_database_version_parameter)

        if not bug_report_comment_nodes.empty:
            bug_location_graph.extend(bug_report_comment_nodes['nodes'].tolist())

        # Bug Model Location: Change -- location --> Model Element
        query_bug_location_edges = query.edges_in_version('Change', 'location')
        bug_location_edges = dataset.run_query(query_bug_location_edges, query_database_version_parameter)

        if not bug_location_edges.empty:
            bug_location_node_ids = bug_location_edges['target'].tolist()

        bug_location_node_ids.extend(bug_locations_by_container_node_ids)
        bug_location_node_ids = list(set(bug_location_node_ids))

        query_bug_location_nodes = query.nodes_by_ids('RETURN n AS nodes')
        bug_location_nodes = dataset.run_query(query_bug_location_nodes, {'node_ids': bug_location_node_ids})
        
        if not bug_location_nodes.empty:
            bug_location_graph.extend(bug_location_nodes['nodes'].tolist())

        for bug_location_node in bug_location_graph:
            bug_location_node['__labels__'] = str(bug_location_node.labels)
            bug_location_node['__db__id__'] = bug_location_node.identity

        with open(path + '/' + file_name_prefix + '_nodes.json', 'w') as outfile:
            dump(bug_location_graph, outfile, indent=4, sort_keys=True)


if __name__ == '__main__':

    # Modeling Language Meta-Model Configuration:
    meta_model, node_self_embedding, typebased_slicing = create_uml_configuration(
        WordDictionary(), prediction_configuration.num_samples)

    # Test Dataset Containing Bug Samples:
    dataset = DataSetPredictionNeo4j(meta_model, node_self_embedding, typebased_slicing, neo4j_configuration)

    # Initialize Bug Localization Prediction:
    prediction_test = BugLocalizationPredictionTest()
    prediction = BugLocalizationPrediction()
    prediction_generator = prediction.predict(dataset, prediction_configuration, log_level, peek_location_samples)

    start_time_prediction = time()

    for bug_sample, bug_location_prediction in prediction_generator:
        prediction_runtime = time() - start_time_prediction

        # Write result log files:
        prediction_test.record_evaluation_results(
            evaluation_results_path,
            cast(BugSamplePredictionNeo4j, bug_sample),
            bug_location_prediction,
            prediction_runtime,
            sort=True)

        start_time_prediction = time()
