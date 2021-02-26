'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from __future__ import annotations

import time
from typing import Optional, Set, Tuple

from pandas.core.frame import DataFrame  # type: ignore

import bug_localization_data_set_neo4j_queries as query
from bug_localization_data_set import IBugSample
from bug_localization_data_set_neo4j import (BugSampleNeo4j, DataSetNeo4j,
                                             LocationSampleBaseNeo4j,
                                             Neo4jConfiguration)
from bug_localization_meta_model import (MetaModel, NodeSelfEmbedding,
                                         TypbasedGraphSlicing)
from bug_localization_util import t

# ===============================================================================
# Training Neo4j Data Connector
# ===============================================================================


class DataSetTrainingNeo4j(DataSetNeo4j):

    def __init__(self,
                 meta_model: MetaModel,
                 node_self_embedding: NodeSelfEmbedding,
                 typebased_slicing: TypbasedGraphSlicing,
                 neo4j_config: Neo4jConfiguration,
                 is_negative: bool):

        super().__init__(meta_model, node_self_embedding, typebased_slicing, neo4j_config, is_negative=is_negative)

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
                    random_nodes = self.load_dataframe(query.random_nodes_in_version(
                        count, model_type, filter_library_elements=True), set_index=False)

                    for index, random_node_result in random_nodes.iterrows():
                        random_node = random_node_result['nodes']
                        node_id = random_node.identity

                        # Avoid intersections with positive sample:
                        if node_id not in positive_bug_locations_ids:
                            bug_locations.add((node_id, self.dataset.get_label(random_node)))
                        else:
                            print("INFO: Negative sample that overlaps with positive filtered: ", node_id)

            return bug_locations

    def initialize(self, log_level: int = 0):
        if log_level >= 4:
            print("Start Loading Dictionary...")
        start_time = time.time()

        node_self_embedding = self.dataset.node_self_embedding
        node_self_embedding.load()

        if log_level >= 4:
            print("Finished Loading Dictionary:", t(start_time))
            print("Start Loading Locations...")
            start_time = time.time()

        meta_model = self.dataset.meta_model
        bug_localization_subgraphs = set()

        # Bug report and locations:
        self.load_bug_report(meta_model.find_bug_location_by_container())

        # Subgraphs of bug locations:
        for bug_location, bug_location_type in self.bug_locations:
            # Filter by type configured for meta-model:
            if bug_location_type in meta_model.get_bug_location_types():
                label = 0 if self.dataset.is_negative else 1
                location_sample = LocationSampleTrainingNeo4j(bug_location, bug_location_type, label)
                bug_localization_subgraph_edges, nodes_ids = location_sample.bug_localization_subgraph_edges(self)
                bug_localization_subgraphs.update(nodes_ids)
                self.location_samples.append(location_sample)

        self.load_model_nodes(meta_model.get_types(),
                              list(bug_localization_subgraphs),
                              log_level=log_level)

        if log_level >= 4:
            print("Finished Loading Locations:", t(start_time))

    def uninitialize(self):
        # TODO: Make field Optional!?
        self.model_nodes = {}
        self.bug_report_node_id = -1
        self.bug_report_nodes = {}
        self.bug_report_edges = None
        self.bug_locations = set()
        self.location_samples = []


class LocationSampleTrainingNeo4j(LocationSampleBaseNeo4j):

    def __init__(self, neo4j_model_location: int, model_location_type: str, label: int):
        super().__init__(neo4j_model_location, model_location_type, label)
        self._bug_localization_subgraph_edges: Optional[DataFrame] = None
        self.node_ids: Optional[Set[int]] = None

    def bug_localization_subgraph_edges(self, bug_sample: BugSampleTrainingNeo4j) -> Tuple[DataFrame, Set[int]]:
        if self._bug_localization_subgraph_edges is not None and self.node_ids is not None:
            return self._bug_localization_subgraph_edges, self.node_ids
        else:
            typebased_slicing = bug_sample.dataset.typebased_slicing
            slicing = typebased_slicing.get_slicing(self.mode_location_type)
            self._bug_localization_subgraph_edges, self.node_ids = bug_sample.load_subgraph_edges(self.neo4j_model_location, slicing)
            return self._bug_localization_subgraph_edges, self.node_ids

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        if isinstance(bug_sample, BugSampleTrainingNeo4j):
            _bug_localization_subgraph_edges, node_ids = self.bug_localization_subgraph_edges(bug_sample)
            subgraph, bug_location_pair = bug_sample.load_bug_location_subgraph(
                self.neo4j_model_location, _bug_localization_subgraph_edges)

            self._graph = subgraph
            self._bug_report = bug_location_pair[0]
            self._model_location = bug_location_pair[1]  # Mapped ID in subgraph

            # Free memory:
            self._bug_localization_subgraph_edges = None
            self.node_ids = None
        else:
            raise Exception("Unsupported bug sample: " + str(type(bug_sample)))

    def uninitialize(self):
        self._bug_localization_subgraph_edges = None
        self.node_ids = None
        self._graph = None
        self._bug_report = None
        self._model_location = None
