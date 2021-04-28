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


class MetaModelUML(MetaModel):

    def __init__(self, neo4j_configuration: Neo4jConfiguration = None,
                 word_dictionary: WordToVectorDictionary = None,
                 num_samples: List[int] = None,
                 embedding_cache_local: bool = False,
                 embedding_cache_limit: int = -1) -> None:

        super().__init__()
        self.neo4j_configuration = neo4j_configuration

        # Node Embedding Configuration:
        if word_dictionary:
            self.node_self_embedding = UMLNodeSelfEmbedding(
                meta_model=self, 
                word_dictionary=word_dictionary, 
                embedding_cache_local=embedding_cache_local, 
                embedding_cache_limit=embedding_cache_limit)

        # Graph Slicing Configuration:
        if num_samples:
            self.query_slicing = self.create_slicing_criterion(num_samples)

    def get_graph(self) -> Graph:
        if self.neo4j_configuration:
            return Graph(host=self.neo4j_configuration.neo4j_host, port=self.neo4j_configuration.neo4j_port,
                         user=self.neo4j_configuration.neo4j_user, password=self.neo4j_configuration.neo4j_password)
        return None

    def get_node_self_embedding(self) -> NodeSelfEmbedding:
        return self.node_self_embedding

    def get_slicing_criterion(self) -> str:
        return self.query_slicing

    # Specifies the slicing of subgraph for embedding of model elements.
    def create_slicing_criterion(self, num_samples: List[int]) -> str:
        query_slicing_label_blacklist = '["Change", "FileChange", "SystemModel", "View", "TracedVersion"'
        query_slicing_label_blacklist += ', "InstanceValue", "LiteralBoolean", "LiteralInteger"'
        query_slicing_label_blacklist += ', "LiteralReal", "LiteralString", "LiteralUnlimitedNatural"]'
        query_slicing_label_blacklist = 'apoc.coll.toSet(' + query_slicing_label_blacklist + ')'
        query_slicing = 'WHERE NOT labels(neighbors)[0] IN ' + query_slicing_label_blacklist + ' AND ' + query.by_version('neighbors')
        return query_slicing

    # Specifies all meta types that will be considered as model elements.
    def get_types(self) -> List[str]:
        model_meta_type_labels = [
            "Model",
            "Package",
            "Class",
            "Interface",
            "Enumeration",
            "DataType",
            "Operation",
            "Parameter",
            "Property",
            "EnumerationLiteral",
            "Generalization",
            "InterfaceRealization",
            "Comment",
            "InstanceValue",
            "LiteralBoolean",
            "LiteralInteger",
            "LiteralReal",
            "LiteralString",
            "LiteralUnlimitedNatural",
        ]
        return model_meta_type_labels

    # Specifies all meta types that will be considered as bug locations.
    def get_bug_location_types(self) -> Set[str]:
        bug_location_model_meta_type_labels = {
            # "Package",
            "Class",
            "Interface",
            "Enumeration",
            "DataType",
            # "Operation",
            # "Property"
        }

        # Consistency validation:
        model_meta_type_labels = self.get_types()

        for bug_location_model_meta_type_label in bug_location_model_meta_type_labels:
            if bug_location_model_meta_type_label not in model_meta_type_labels:
                raise Exception("Missing meta-type for bug location type: " + bug_location_model_meta_type_label)

        return bug_location_model_meta_type_labels

    def get_bug_location_negative_sample_count(self) -> Dict[str, int]:
        bug_location_negative_samples_per_type = {
            # "Package",
            "Class": 5,
            "Interface": 5,
            "Enumeration": 2,
            "DataType": 1,
            # "Operation",
            # "Property"
        }
        return bug_location_negative_samples_per_type

    # Find container of bug location if the type is not in specified location.
    def find_bug_location_by_container(self) -> int:
        return 2

    # Specifies the properties of nodes that will be considered during embedding.
    def get_type_to_properties(self):
        meta_type_to_properties: Dict[str, List[str]] = {

            # ----- Model -----
            "Model": ["name"],
            "Package": ["name"],
            "Class": ["name"],
            "Interface": ["name"],
            "Enumeration": ["name"],
            "DataType": ["name"],
            "Operation": ["name"],
            "Parameter": ["name"],
            "Property": ["name"],
            "EnumerationLiteral": ["name"],
            "Generalization": [],
            "InterfaceRealization": [],
            "InstanceValue": ["value"],
            "LiteralBoolean": ["value"],
            "LiteralInteger": ["value"],
            "LiteralReal": ["value"],
            "LiteralString": ["value"],
            "LiteralUnlimitedNatural": ["value"],
            "Comment": ["body"],

            # ----- Bug Report -----
            "TracedBugReport": ["summary"],
            "BugReportComment": ["text"]
        }

        # Consistency validation:
        for model_meta_type_label in self.get_types():
            try:
                meta_type_to_properties[model_meta_type_label]
            except:
                raise Exception("Missing property configuration for type: " + model_meta_type_label)

        return meta_type_to_properties


class UMLNodeSelfEmbedding(NodeSelfEmbedding):

    def __init__(self, meta_model: MetaModel,
                 word_dictionary: WordToVectorDictionary, 
                 stopwords={}, unescape=True,
                 embedding_cache_local: bool = False,
                 embedding_cache_limit: int = -1):

        self.meta_model: MetaModel = meta_model
        self.graph: Any = None
        self.type_to_properties = meta_model.get_type_to_properties()

        self.word_dictionary = word_dictionary
        self.dictionary_words_length = word_dictionary.dimension()
        self.stopwords = stopwords
        self.unescape = unescape

        self.dictionary_words: Optional[Dict[str, np.ndarray]] = None
        self.embedding_cache_limit = embedding_cache_limit
        self.embedding_cache_local = embedding_cache_local  # Compute cache per process?
        self.embedding_cache: Dict[int, np.ndarray] = {}
        
        # Compute embeddings for all nodes in the database:
        if not self.embedding_cache_local:
            self.copmute_node_self_embedding_chache()

    def __getstate__(self):
        # Do not expose the dictionary (for multiprocessing) which fails on pickle.
        state = dict(self.__dict__)

        if 'dictionary_words' in state:
            state['dictionary_words'] = None

        if 'graph' in state:
            state['graph'] = None

        # Should we share the embeddings for all processes?
        if self.embedding_cache_local:
            if 'embedding_cache' in state:
                state['embedding_cache'] = {}

        return state

    def load(self, load_dictionary: bool = False):
        if self.graph is None:
            self.graph = self.meta_model.get_graph()

        if self.dictionary_words is None and (load_dictionary or self.embedding_cache_local):
            print("Start Loading Dictionary...")
            self.dictionary_words, dictionary_words_length = self.word_dictionary.dictionary()
            assert dictionary_words_length == self.dictionary_words_length, "Word feature size is inconsistently specified!"
            print("Finished Loading Dictionary")

    def unload(self):
        self.graph = None
        self.dictionary_words = None

    def get_dimension(self) -> int:
        return self.dictionary_words_length

    def get_graph(self) -> Graph:
        return self.graph

    def node_to_vector(self, versioned_nodes_per_hop: List[List[List[int]]]) -> np.ndarray:

        # Get empty feature vector of proper size:
        features = super().node_to_vector(versioned_nodes_per_hop)

        # Check if all required node self embeddings are in the chache:
        if self.embedding_cache_local:
            self.update_node_self_embedding_chache(versioned_nodes_per_hop)

        # Add embeddings to feature vector:
        features_idx = 0

        for node_and_version in versioned_nodes_per_hop:
            for node, version in node_and_version:
                if node is not None:  # node embedding will be [0, 0, ...]
                    features[features_idx] = self.embedding_cache[node]
                    features_idx += 1

        # Check if cache limit is exceeded:
        if self.embedding_cache_local:
            if self.embedding_cache_limit > 0:
                if len(self.embedding_cache) > self.embedding_cache_limit:
                    self.embedding_cache = {}

        return features

    def update_node_self_embedding_chache(self, versioned_nodes_per_hop: List[List[List[int]]]):
        unseen_nodes = set()

        # Check if embeddings in chache:
        for node_and_version in versioned_nodes_per_hop:
            for node, version in node_and_version:
                if node is not None:
                    if node not in self.embedding_cache:
                        unseen_nodes.add(node)  # collect for a single query

        # Compute new embeddings:
        if len(unseen_nodes) > 0:
            neo4j_nodes = self.graph.run('MATCH (n) WHERE ID(n) IN $node_ids RETURN n',
                                         {'node_ids': list(unseen_nodes)}).to_table()

            for neo4j_node in neo4j_nodes:
                neo4j_node = neo4j_node[0]
                text = ""

                for property_name in self.get_properties(neo4j_node):
                    value = self.get_property(neo4j_node, property_name)
                    text += " " + str(value)

                embedding = self.text_to_vector(text)
                self.embedding_cache[neo4j_node.identity] = embedding

    def copmute_node_self_embedding_chache(self):
        self.load(load_dictionary=True)
        features_idx = 0

        for label in self.type_to_properties:
            neo4j_nodes = self.graph.run('MATCH (n:' + label + ') RETURN n').to_table()

            for neo4j_node in neo4j_nodes:
                neo4j_node = neo4j_node[0]
                features_idx += 1
                text = ""

                if (features_idx % 10000 == 0):
                    print(features_idx, "of", len(neo4j_nodes), "embedded")

                for property_name in self.get_properties(neo4j_node):
                    value = self.get_property(neo4j_node, property_name)
                    text += " " + str(value)

                embedding = self.text_to_vector(text)
                self.embedding_cache[neo4j_node.identity] = embedding

        self.unload()

    def get_property(self, node: Node, property_name: str) -> str:
        return node[property_name]

    def get_properties(self, node: Node) -> List[str]:
        meta_type = self.get_meta_type(node)
        try:
            return self.type_to_properties[meta_type]
        except KeyError:
            print("WARNING: Node type not configured: " + meta_type)
            return []

    def get_meta_type(self, node: Node) -> str:
        return list(node.labels)[0]

    def text_to_vector(self, text: str):

        # Encode words:
        feature_vector = np.zeros(self.dictionary_words_length, dtype=np.float32)

        if self.dictionary_words is not None:
            words = text_to_words(text, self.unescape)

            for word in words:
                try:
                    word_embedding = self.dictionary_words[word]
                    feature_vector = np.sum((feature_vector, word_embedding), axis=0)
                except KeyError:
                    pass  # ignore...how to handle unseen words...?

        return feature_vector
