'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

import time
from typing import (Dict, Set)

import numpy as np  # type: ignore
import pandas as pd  # type: ignore

import bug_localization_data_set_neo4j_queries as query
from bug_localization_data_set import IBugSample
from bug_localization_data_set_neo4j import (BugSampleNeo4j, DataSetNeo4j,
                                             LocationSampleBaseNeo4j,
                                             Neo4jConfiguration)
from bug_localization_meta_model import (MetaModel, NodeSelfEmbedding,
                                         TypbasedGraphSlicing)
from bug_localization_util import t

# ===============================================================================
# Prediction Neo4j Data Connector
# ===============================================================================


class DataSetPredictionNeo4j(DataSetNeo4j):

    def __init__(self, 
                 meta_model: MetaModel, 
                 node_self_embedding: NodeSelfEmbedding, 
                 typebased_slicing: TypbasedGraphSlicing,
                 neo4j_config: Neo4jConfiguration, 
                 is_negative: bool = False,
                 log_level: int = 0):
        
        super().__init__(meta_model, node_self_embedding, typebased_slicing, neo4j_config, is_negative=is_negative)

        # All Versions: Model graph nodes: index -> embedding
        self.model_node_embeddings: Dict[int, np.ndarray] = {}
        self.bug_report_node_embeddings: Dict[int, np.ndarray] = {}
        self.load_node_self_embeddings(log_level)

        # Library element, e.g., Java String, Integer,  contained in the database by type over all versions:
        # type -> nodes:
        self.model_library_nodes: Dict[str, Set[int]] = self.load_model_library_nodes(meta_model.get_bug_location_types())
        
    def load_node_self_embeddings(self, log_level: int = 0):
        if log_level >= 2:
            print("Start Loading Dictionary...")
        start_time = time.time()

        self.node_self_embedding.load()

        if log_level >= 2:
            print("Finished Loading Dictionary:", t(start_time))
            print("Start Word Embedding ...")
            start_time = time.time()

        # Bug report nodes:
        for model_location_types in self.meta_model.get_bug_report_node_types():
            bug_report_nodes = self.run_query(query.nodes_by_type(model_location_types), index="index")
            
            if log_level >= 2:
                print("Embedding", model_location_types, len(bug_report_nodes.index))
                
            for node_id, model_node in bug_report_nodes.iterrows():
                node_embedding = self.node_self_embedding.node_to_vector(model_node)
                self.bug_report_node_embeddings[node_id] = node_embedding
        
        # Model nodes:
        for model_location_types in self.meta_model.get_types():
            model_nodes = self.run_query(query.nodes_by_type(model_location_types), index="index")
            
            if log_level >= 2:
                print("Embedding", model_location_types, len(model_nodes.index))
                
            for node_id, model_node in model_nodes.iterrows():
                node_embedding = self.node_self_embedding.node_to_vector(model_node)
                self.model_node_embeddings[node_id] = node_embedding
        
        # Free memory...
        self.node_self_embedding.unload()

        if log_level >= 2:
            print("Finished Word Embedding:", t(start_time))
            start_time = time.time()
        
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
    
    def initialize(self, log_level: int = 0):
        start_time = time.time()
        
        if log_level >= 4:
            print("Start Loading Locations...")
        
        self.model_nodes = self.dataset.model_node_embeddings
        self.load_bug_report(self.dataset.meta_model.find_bug_location_by_container())

        # Load location samples by type:
        for model_location_types in self.dataset.meta_model.get_bug_location_types():
            model_library_nodes_by_type = self.dataset.model_library_nodes[model_location_types]
            model_locations = self.load_dataframe(query.node_ids_in_version(model_location_types))
            
            for model_location in model_locations['nodes']:
                if model_location not in model_library_nodes_by_type:
                    nodes_id = model_location.identity
                    self.location_samples.append(LocationSamplePredictionNeo4j(nodes_id, model_location_types, label=nodes_id))

        if log_level >= 4:
            print("Finished Loading Locations:", t(start_time))
    
    def node_to_vector(self, node_id: int, node: pd.Series):
        try:
            return self.dataset.bug_report_node_embeddings[node_id]
        except KeyError:
            if node_id in self.dataset.model_node_embeddings:
                return self.dataset.model_node_embeddings[node_id]
            else:
                return np.zeros(self.dataset.node_self_embedding.get_dimension())

    def uninitialize(self):
        # TODO: Make field Optional!?
        self.model_nodes = {}
        self.bug_report_node_id = -1
        self.bug_report_nodes = {}
        self.bug_report_edges = None
        self.bug_locations = set()
        self.location_samples = []


class LocationSamplePredictionNeo4j(LocationSampleBaseNeo4j):

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        if isinstance(bug_sample, BugSamplePredictionNeo4j):
            typebased_slicing = bug_sample.dataset.typebased_slicing
            slicing = typebased_slicing.get_slicing(self.mode_location_type)

            bug_localization_subgraph_edges, node_ids = bug_sample.load_subgraph_edges(
                self.neo4j_model_location, slicing)
            subgraph, bug_location_pair = bug_sample.load_bug_location_subgraph(
                self.neo4j_model_location, bug_localization_subgraph_edges)

            self._graph = subgraph
            self._bug_report = bug_location_pair[0]
            self._model_location = bug_location_pair[1]  # Mapped ID in subgraph
        else:
            raise Exception("Unsupported bug sample: " + str(type(bug_sample)))

    def uninitialize(self):
        self._graph = None
        self._bug_report = None
        self._model_location = None
