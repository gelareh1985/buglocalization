'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

import time
from typing import Dict, Set

from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset.neo4j_data_set import (BugSampleNeo4j,
                                                    DataSetNeo4j,
                                                    LocationSampleNeo4j,
                                                    Neo4jConfiguration)
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.utils.common_utils import t

# ===============================================================================
# Prediction Neo4j Data Connector
# ===============================================================================


class DataSetPredictionNeo4j(DataSetNeo4j):

    def __init__(self,
                 meta_model: MetaModel,
                 neo4j_config: Neo4jConfiguration,
                 is_negative: bool = False,
                 log_level: int = 0):

        super().__init__(meta_model,  neo4j_config, is_negative=is_negative)
        self.log_level = log_level

        # Library element, e.g., Java String, Integer,  contained in the database by type over all versions:
        # type -> nodes:
        self.model_library_nodes: Dict[str, Set[int]] = self.load_model_library_nodes(meta_model.get_bug_location_types())

    def create_sample(self, db_version: int) -> BugSampleNeo4j:
        return BugSamplePredictionNeo4j(self, db_version)

    def load_model_library_nodes(self, model_meta_type_labels: Set[str]) -> Dict[str, Set[int]]:
        model_nodes_library = {}

        for model_meta_type_label in model_meta_type_labels:
            library_elements_by_type = self.run_query(query.library_elements(model_meta_type_label))

            if not library_elements_by_type.empty:
                model_nodes_library[model_meta_type_label] = set(library_elements_by_type["nodes"])

        return model_nodes_library


class BugSamplePredictionNeo4j(BugSampleNeo4j):
    dataset: DataSetPredictionNeo4j
    
    def __getstate__(self):
        state = super().__getstate__()

        # Make sure the state is not initialized:
        if 'bug_report' in state:
            state['bug_report'] = -2
        if 'bug_locations' in state:
            state['bug_locations'] = set()
        if 'location_samples' in state:
            state['location_samples'] = []
            
        return state

    def _uninitialize(self):
        self.bug_report = -2
        self.bug_locations = set()
        self.location_samples = []

    def _initialize(self, log_level: int = 0):
        start_time = time.time()

        if log_level >= 4:
            print("Start Loading Locations...")

        self.bug_report = self.load_bug_report()

        # Load location samples by type:
        for model_location_types in self.dataset.meta_model.get_bug_location_types():
            model_library_nodes_by_type = self.dataset.model_library_nodes[model_location_types]
            model_locations = self.run_query_by_version(query.nodes_in_version(model_location_types))

            for model_location in model_locations['nodes']:
                nodes_id = model_location.identity
                
                if nodes_id not in model_library_nodes_by_type:
                    self.location_samples.append(LocationSamplePredictionNeo4j(nodes_id, self.bug_report, label=nodes_id))

        if log_level >= 4:
            print("Finished Loading Locations:", t(start_time))
            

class LocationSamplePredictionNeo4j(LocationSampleNeo4j):
    ...
