'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Any, Dict, List, Optional, Set, Tuple

import numpy as np
import pandas as pd
from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.dataset.neo4j_queries import by_version, where_version
from buglocalization.metamodel.meta_model import (MetaModel, NodeSelfEmbedding,
                                                  TypbasedGraphSlicing)
from buglocalization.textembedding.text_utils import text_to_words
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from py2neo import Graph, Node


class MetaModelUML(MetaModel):

    def __init__(self, neo4j_configuration: Neo4jConfiguration = None,
                 word_dictionary: WordToVectorDictionary = None,
                 num_samples: List[int] = None) -> None:

        super().__init__()
        self.neo4j_configuration = neo4j_configuration

        # Node Embedding Configuration:
        if word_dictionary:
            self.node_self_embedding = UMLNodeSelfEmbedding(self, word_dictionary)

        # Graph Slicing Configuration:
        if num_samples:
            self.typebased_slicing = self.create_slicing_criterion(num_samples)

    def get_graph(self) -> Graph:
        if self.neo4j_configuration:
            return Graph(host=self.neo4j_configuration.neo4j_host, port=self.neo4j_configuration.neo4j_port,
                         user=self.neo4j_configuration.neo4j_user, password=self.neo4j_configuration.neo4j_password)
        return None

    def get_node_self_embedding(self) -> NodeSelfEmbedding:
        return self.node_self_embedding

    def get_slicing_criterion(self) -> TypbasedGraphSlicing:
        return self.typebased_slicing

    # Specifies the slicing of subgraph for embedding of model elements.
    def create_slicing_criterion(self, num_samples: List[int]) -> TypbasedGraphSlicing:
        slicing = TypbasedGraphSlicing()

        # FIXME: We should test if the query random sampling behave correctly - but to be sure...
        # TODO: -> raise or log if result > np.cumprod(num_samples) + 1
        hard_limit = ' LIMIT ' + str(np.cumproduct(num_samples)[len(num_samples) - 1] + 10)  # ''

        # Abstract Syntax Tree parent and child nodes:
        query_ast_head = 'MATCH (c) WHERE ID(c)=$node_id AND ' + by_version('c')

        query_ast_parents_path = ' CALL {WITH c MATCH (c)<-[*0..2 {__containment__:true}]-(p) ' + where_version('c') + ' RETURN p} WITH c, p'
        query_ast_parents_return = ' RETURN ID(p) AS parent'
        query_ast_parents = query_ast_head + query_ast_parents_path + query_ast_parents_return + hard_limit

        query_ast_childs = ' CALL { WITH c MATCH (c)-[{__containment__:true}]->(k) ' + where_version('k') + ' RETURN k, rand() AS rk ORDER BY rk LIMIT ' + str(num_samples[0]) + '} WITH c, k'  # noqa: E501
        query_ast_childs_sub = ' CALL {WITH k MATCH (k)-[{__containment__:true}]->(kk) ' + where_version('kk') + ' RETURN kk, rand() AS rkk ORDER BY rkk LIMIT ' + str(num_samples[1]) + '}'  # noqa: E501
        query_ast_childs_return = ' RETURN ID(k) AS child, ID(kk) AS subchild'
        query_ast_childs = query_ast_head + query_ast_childs + query_ast_childs_sub + query_ast_childs_return + hard_limit

        # Realized Interfaces:
        query_interfaces = 'MATCH (c)-[:interfaceRealization]-(z:InterfaceRealization)-[:supplier]->(i:Interface) WHERE ID(c)=$node_id AND ' + by_version('c') + ' AND ' + by_version('z') + ' AND ' + by_version('i') + ' WITH z, i, rand() AS r ORDER BY r LIMIT ' + str(num_samples[1]) + ' RETURN ID(z) AS realization, ID(i) AS interface'  # noqa: E501
        query_interfaces = query_interfaces + hard_limit

        # Super-classes
        query_generalization = 'MATCH (c)-[:generalization]-(g:Generalization)-[:general]->(s:Class) WHERE ID(c)=$node_id AND ' + by_version('c') + ' AND ' + by_version('g') + ' AND ' + by_version('s') + ' WITH g, s, rand() AS r ORDER BY r LIMIT ' + str(num_samples[1]) + ' RETURN ID(g) AS generalization, ID(s) AS superclass'  # noqa: E501
        query_generalization = query_generalization + hard_limit

        # E.g. sub-classes:
        query_crosstree = 'MATCH (c) WHERE ID(c)=$node_id AND ' + by_version('c') + ' CALL { WITH c MATCH (c)-[ {__containment__:false, __container__:false}]-(e) ' + where_version('e') + ' RETURN e, rand() AS re ORDER BY re LIMIT ' + str(num_samples[1]) + '} With e CALL {WITH e MATCH (e)<-[{__containment__:true}]-(t) ' + where_version('t') + ' RETURN t LIMIT 1} RETURN ID(e) AS crosstree, ID(t) AS crosstreecontainer'  # noqa: E501
        query_crosstree = query_crosstree + hard_limit

        classifier_query_slicing = [query_ast_parents, query_ast_childs, query_interfaces, query_generalization, query_crosstree]

        slicing.add_type('Class', classifier_query_slicing)
        slicing.add_type('Interface', classifier_query_slicing)

        datatypes_query_slicing = [query_ast_parents, query_ast_childs, query_crosstree]

        slicing.add_type('Enumeration', datatypes_query_slicing)
        slicing.add_type('DataType', datatypes_query_slicing)

        # Consistency validation:
        for bug_location_model_meta_type_label in self.get_bug_location_types():
            try:
                slicing.get_slicing(bug_location_model_meta_type_label)
            except:
                raise Exception("Missing slicing configuration for model type: " + bug_location_model_meta_type_label)

        return slicing

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
                 word_dictionary: WordToVectorDictionary, stopwords={}, unescape=True):

        self.meta_model: MetaModel = meta_model
        self.graph: Any = None
        self.type_to_properties = meta_model.get_type_to_properties()

        self.word_dictionary = word_dictionary
        self.dictionary_words_length = word_dictionary.dimension()
        self.stopwords = stopwords
        self.unescape = unescape

        self.dictionary_words: Optional[Dict[str, np.ndarray]] = None
        self.embedding_cache: Dict[int, np.ndarray] = {}

    def __getstate__(self):
        # Do not expose the dictionary (for multiprocessing) which fails on pickle.
        state = dict(self.__dict__)

        if 'dictionary_words' in state:
            state['dictionary_words'] = None
            
        if 'graph' in state:
            state['graph'] = None
            
        if 'embedding_cache' in state:
            state['embedding_cache'] = {}

        return state

    def load(self):
        if self.graph is None:
            self.graph = self.meta_model.get_graph()
            
        if self.dictionary_words is None:
            print("Start Loading Dictionary...")

            # use empty dict {} for testing
            # self.dictionary_words = KeyedVectors.load_word2vec_format(self.dictionary_path, binary=True)
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

    def node_to_vector(self, nodes_per_hop: List[List[int]]) -> np.ndarray:
        unseen_nodes = set()
        
        for nodes in nodes_per_hop:
            for node in nodes:
                if node not in self.embedding_cache:
                    unseen_nodes.add(node)
        
        neo4j_nodes = self.graph.run('MATCH (n) WHERE ID(n) IN $node_ids RETURN n', {'node_ids': list(unseen_nodes)}).to_table()

        for neo4j_node in neo4j_nodes:
            neo4j_node = neo4j_node[0]
            text = ""

            for property_name in self.get_properties(neo4j_node):
                value = self.get_property(neo4j_node, property_name)
                text += " " + str(value)

            embedding = self.text_to_vector(text)
            self.embedding_cache[neo4j_node.identity] = embedding

        features = super().node_to_vector(nodes_per_hop)
        features_idx = 0
        
        for nodes in nodes_per_hop:
            for node in nodes:
                features[features_idx] = self.embedding_cache[node]
                features_idx += 1
                
        return features

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
            words = text_to_words(text, self.stopwords, self.unescape)

            for word in words:
                try:
                    word_embedding = self.dictionary_words[word]
                    feature_vector = np.sum((feature_vector, word_embedding), axis=0)
                except KeyError:
                    pass  # ignore...how to handle unseen words...?

        return feature_vector

    """ TODO
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
    """
