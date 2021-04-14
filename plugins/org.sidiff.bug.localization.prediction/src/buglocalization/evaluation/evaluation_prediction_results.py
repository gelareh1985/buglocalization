'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from json import dump
from pathlib import Path
from time import time
from typing import Dict, List, Union, cast

import numpy as np
import pandas as pd
from buglocalization.metamodel.meta_model import MetaModel

from buglocalization.predictionmodel.bug_localization_model_prediction import (
    BugLocalizationPrediction, BugLocalizationPredictionConfiguration)
from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset.data_set import IBugSample, IDataSet
from buglocalization.dataset.neo4j_data_set_prediction import (
    BugSamplePredictionNeo4j, DataSetPredictionNeo4j)
    
    
class BugLocalizationPredictionTest:
    """
    Recorded Evaluation Files:

    <version number>_bug<number>_<model Git version>_prediction.csv:  The ranked list of bug locations.

    - DatabaseNodeID: The ID in the Neo4j Database. Actually, we should not rely on that value as Neo4j gives no
      guarantee that these IDs will always be preserved. I also don't know if they are the same after loading the
      database from the .dump files.
    - Prediction: The probability value of the link prediction between 0 and 1.
    - IsLocation: Contains 1 if the row/model element is the expected location; 0 otherwise.
    - MetaType: The meta-type of the model element. Corresponds to the label in Neo4j.
    - ModelElementID: The ID that identifies the model element  the UML model. Actually, it is a URL that can also be
      used to reconstruct the original Java file. These IDs are unique within each version of the model, i.e., to get
      one specific element from the Neo4j database we need to combine the ID with a version number.

    <version number>_bug<number>_<model Git version>_info.csv: Some bug report version information.

    - ModelVersionNeo4j: The successive version number of the buggy version.
    - ModelVersionRepository: The corresponding Git version (hash value) of the model repository.
    - CodeVersionRepository: The corresponding Git version (hash value) of the code repository.
    - BugReportNumber: The ID number of the bug report in the bug tracker.
    - PredictionRuntime: The time needed to compute this prediction for a full model.
    - MissingLocations: For debugging, contains expected model elements that are missing in the _prediction.csv.
      Expected to be empty.
    - BugSummary: The short description of the bug report.

    <version number>_bug<number>_<model Git version>_nodes.json: Contains some nodes from the Neo4j database related to
    the bug report.

    - TracedVersion [1]: The node representing the buggy version.
    - TracedBugReport [1]: The main bug report node.
    - BugReportComment [0..*]: The comments related to the bug report.
    - Class/Interface/DataType/Enumeration [1..*]: The expected bug locations on the classifier level
    - Model/Package/Operation/Property: The originally expected bug locations that are not on the classifier level.
    """
    def __init__(self, evaluation_results_path: str) -> None:
        self.evaluation_results_path: str = evaluation_results_path

    def get_file_name(self, bug_sample: BugSamplePredictionNeo4j):
        dataset: DataSetPredictionNeo4j = bug_sample.dataset
        query_database_version_parameter = {'db_version': bug_sample.version}

        query_bug_report_id = query.property_value_in_version('TracedBugReport', 'id')
        bug_report_id = dataset.run_query_value(query_bug_report_id, query_database_version_parameter)

        query_model_version = query.model_repo_version_by_db_version()
        model_version = dataset.run_query_value(query_model_version, query_database_version_parameter)

        return str(bug_sample.version) + '_' + 'bug' + str(bug_report_id) + '_' + model_version

    def record_evaluation_results(
            self, path: str, bug_sample: BugSamplePredictionNeo4j, prediction: np.ndarray, prediction_runtime: float, sort: bool = True):
        dataset: DataSetPredictionNeo4j = bug_sample.dataset
        meta_model = dataset.meta_model
        query_database_version_parameter = {'db_version': bug_sample.version}

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
        if sort:
            prediction_results.sort_values(by=prediction_col, ascending=False, inplace=True)

        # Save table:
        prediction_results.to_csv(path + '/' + file_name_prefix + '_prediction.csv', sep=';')

        # ===========================================================================
        # Bug sample information table :
        # ===========================================================================

        bug_sample_info: Dict[str, List] = {}

        # ModelVersionNeo4j
        model_version_neo4j_col = bug_sample_info_columns[0]
        bug_sample_info[model_version_neo4j_col] = [bug_sample.version]

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

        if not version_node.empty:
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

    def predict(self,
                meta_model: MetaModel,
                sample_data: Union[IDataSet, IBugSample],
                prediction_configuration: BugLocalizationPredictionConfiguration,
                samples_slice: slice = None,
                log_level: int = 0):

        # Initialize Bug Localization Prediction:
        prediction = BugLocalizationPrediction()
        prediction_generator = prediction.predict(meta_model, sample_data, prediction_configuration, samples_slice, log_level)

        start_time_prediction = time()
        bug_sample_counter = 0

        for bug_sample, bug_location_prediction in prediction_generator:
            prediction_runtime = time() - start_time_prediction
            bug_sample_counter += 1

            if log_level >= 1:
                print('Prediction:', bug_sample_counter)

            # Write result log files:
            self.record_evaluation_results(
                self.evaluation_results_path,
                cast(BugSamplePredictionNeo4j, bug_sample),
                bug_location_prediction,
                prediction_runtime,
                sort=True)

            start_time_prediction = time()
            