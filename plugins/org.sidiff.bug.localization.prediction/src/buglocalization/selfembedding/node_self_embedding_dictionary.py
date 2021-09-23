'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Any, Dict, List, Optional, Set

import numpy as np
from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.metamodel.meta_model import MetaModel, NodeSelfEmbedding
from buglocalization.textembedding.text_utils import text_to_words
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from py2neo import Graph, Node
import pickle


class NodeSelfEmbeddingDictionary(NodeSelfEmbedding):

    def __init__(self, meta_model: MetaModel,
                 self_embedding_dictionary_path: str,
                 dictionary_words_length: int):

        self.meta_model: MetaModel = meta_model
        self.graph: Any = None

        self.self_embedding_dictionary_path: str = self_embedding_dictionary_path
        self.self_embedding_dictionary: Dict[int, np.ndarray] = None
        self.dictionary_words_length: int = dictionary_words_length
        self.missing_node_ids = set()

    def __getstate__(self):
        # Do not expose the dictionary and graph (for multiprocessing) which fails on pickle.
        state = dict(self.__dict__)

        if 'self_embedding_dictionary' in state:
            state['self_embedding_dictionary'] = None

        if 'graph' in state:
            state['graph'] = None

        return state

    def load(self, load_dictionary: bool = False):
        if self.graph is None:
            self.graph = self.meta_model.get_graph()
            
        # smallestID = -1
        # largestID = -1

        if self.self_embedding_dictionary is None:
            print("Start Loading Self Embedding Dictionary...")
            self.self_embedding_dictionary = self.load_file()

            # TODO: Save as dictionary for similar word embeddings approach!
            # word_index_train = self.load_file()
            # self.self_embedding_dictionary = {}

            # for key, value in word_index_train.items():
            #     node_id = value[0]
            #     self.self_embedding_dictionary[node_id] = value[1]
                
            #     if smallestID == -1 or node_id < smallestID:
            #         smallestID = node_id
            #     if largestID == -1 or node_id > largestID:
            #         largestID = node_id

            # print("Finished Loading Self Embedding Dictionary:", len(self.self_embedding_dictionary), "Words")
            # print("Smallest ID:", smallestID)
            # print("Largest ID", largestID)

    def load_file(self):
        open_file = open(self.self_embedding_dictionary_path, "rb")
        loaded_list = pickle.load(open_file)
        open_file.close()
        return loaded_list

    def unload(self):
        self.graph = None
        self.self_embedding_dictionary = None

    def get_dimension(self) -> int:
        return self.dictionary_words_length

    def node_to_vector(self, versioned_nodes_per_hop: List[List[List[int]]]) -> np.ndarray:

        # Get empty feature vector of proper size:
        features = super().node_to_vector(versioned_nodes_per_hop)

        # Add embeddings to feature vector:
        features_idx = 0

        for node_and_version in versioned_nodes_per_hop:
            for node, version in node_and_version:
                if node is not None:  # node embedding will be [0, 0, ...]
                    try:
                        features[features_idx] = self.self_embedding_dictionary[node]
                    except KeyError:
                        if node not in self.missing_node_ids:
                            print("(!) Node note found in dictionary: " + str(node))
                            self.missing_node_ids.add(node)
                features_idx += 1

        return features
