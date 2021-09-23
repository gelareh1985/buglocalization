'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from __future__ import annotations

from typing import Set, Tuple

from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset.neo4j_data_set import (BugSampleNeo4j,
                                                    DataSetNeo4j,
                                                    LocationSampleNeo4j,
                                                    Neo4jConfiguration)
from buglocalization.metamodel.meta_model import MetaModel

# ===============================================================================
# Training Neo4j Data Connector
# ===============================================================================


class DataSetTrainingNeo4j(DataSetNeo4j):

    def __init__(self,
                 meta_model: MetaModel,
                 neo4j_config: Neo4jConfiguration,
                 is_negative: bool):

        super().__init__(meta_model, neo4j_config, is_negative=is_negative)

    def create_sample(self, db_version: int) -> BugSampleNeo4j:
        return BugSampleTrainingNeo4j(self, db_version)


class BugSampleTrainingNeo4j(BugSampleNeo4j):
    dataset: DataSetTrainingNeo4j

    def load_bug_locations(self, locate_by_container: int) -> Set[Tuple[int, str]]:
        if not self.dataset.is_negative:
            # Load positive sample -> default super class implementation:
            return super().load_bug_locations(locate_by_container)
        else:
            positive_bug_locations: Set[Tuple[int, str]] = super().load_bug_locations(locate_by_container)
            positive_bug_locations_ids = {positive_bug_location[0] for positive_bug_location in positive_bug_locations}

            # Generate negative sample:
            bug_locations: Set[Tuple[int, str]] = set()
            type_to_count = self.dataset.meta_model.get_bug_location_negative_sample_count()

            for model_type in self.dataset.meta_model.get_bug_location_types():
                if model_type in type_to_count:
                    count = type_to_count[model_type]
                    random_nodes = self.run_query_by_version(query.random_nodes_in_version(
                        count, model_type, filter_library_elements=True), set_index=False)

                    for index, random_node_result in random_nodes.iterrows():
                        random_node = random_node_result['nodes']
                        node_id = random_node.identity

                        # Avoid intersections with positive sample:
                        if node_id not in positive_bug_locations_ids:
                            bug_locations.add((node_id, query_util.get_label(random_node)))
                        else:
                            print("INFO: Negative sample that overlaps with positive filtered: ", node_id)

            return bug_locations

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
        meta_model = self.dataset.meta_model

        # Bug report and locations:
        self.bug_report = self.load_bug_report()
        self.bug_locations = self.load_bug_locations(meta_model.find_bug_location_by_container())

        # Subgraphs of bug locations:
        for bug_location, bug_location_type in self.bug_locations:
            # Filter by type configured for meta-model:
            if bug_location_type in meta_model.get_bug_location_types():
                label = 0 if self.dataset.is_negative else 1
                location_sample = LocationSampleNeo4j(bug_location, self.bug_report, label)
                self.location_samples.append(location_sample)


class LocationSampleTrainingNeo4j(LocationSampleNeo4j):
    ...
