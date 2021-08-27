'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Any, Dict, List, Optional, Set

import numpy as np
from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.selfembedding.node_self_embedding import NodeSelfEmbedding
from buglocalization.selfembedding.node_self_embedding_sowe import \
    NodeSelfEmbeddingSOWE
from buglocalization.textembedding.text_utils import text_to_words
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from py2neo import Graph, Node


class MetaModelUML(MetaModel):

    def __init__(self, neo4j_configuration: Neo4jConfiguration,
                 num_samples: List[int]) -> None:
        super().__init__()
        self.neo4j_configuration: Neo4jConfiguration = neo4j_configuration

        # Graph Slicing Configuration:
        if num_samples:
            self.query_slicing = self.create_slicing_criterion(num_samples)

    def get_node_self_embedding(self) -> NodeSelfEmbedding:

        # Use Summe Of Words Embedding as default:
        if self.node_self_embedding is None:
            print("Use Summe Of Words Embedding as default node self embedding.")
            self.node_self_embedding = NodeSelfEmbeddingSOWE(
                meta_model=self,
                word_dictionary=WordToVectorDictionary(),
                embedding_cache_local=False,
                embedding_cache_limit=-1)

        return self.node_self_embedding

    def get_graph(self) -> Graph:
        if self.neo4j_configuration:
            return Graph(host=self.neo4j_configuration.neo4j_host, port=self.neo4j_configuration.neo4j_port,
                         user=self.neo4j_configuration.neo4j_user, password=self.neo4j_configuration.neo4j_password)
        return None

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
            # "InstanceValue",
            # "LiteralBoolean",
            # "LiteralInteger",
            # "LiteralReal",
            # "LiteralString",
            # "LiteralUnlimitedNatural",
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
            # "InstanceValue": ["value"],
            # "LiteralBoolean": ["value"],
            # "LiteralInteger": ["value"],
            # "LiteralReal": ["value"],
            # "LiteralString": ["value"],
            # "LiteralUnlimitedNatural": ["value"],
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
