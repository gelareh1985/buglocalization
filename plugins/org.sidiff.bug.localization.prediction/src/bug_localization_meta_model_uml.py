'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from typing import List, Dict, Set

from gensim.models import KeyedVectors  # type: ignore
from py2neo import Node  # type: ignore

from bug_localization_data_set import DataSetNeo4j
from bug_localization_util import text_to_words
import numpy as np  # type: ignore
import pandas as pd  # type: ignore

# TODO: Consistency checks

class MetaModelUML:
    
    # Specifies the slicing of subgraph for embedding of model elements.
    def get_slicing(self, dataset:DataSetNeo4j) -> DataSetNeo4j.TypbasedGraphSlicing:
        slicing = DataSetNeo4j.TypbasedGraphSlicing()
        
        type_model = DataSetNeo4j.GraphSlicing(dataset,
                                  parent_levels=0,
                                  parent_incoming=False,
                                  parent_outgoing=False,
                                  self_incoming=True,
                                  self_outgoing=True,
                                  child_levels=1,
                                  child_incoming=True,
                                  child_outgoing=True,
                                  outgoing_distance=0,
                                  incoming_distance=0)
        slicing.add_type('Model', type_model)
        
        type_package = DataSetNeo4j.GraphSlicing(dataset,
                                  parent_levels=5,
                                  parent_incoming=False,
                                  parent_outgoing=False,
                                  self_incoming=True,
                                  self_outgoing=True,
                                  child_levels=1,
                                  child_incoming=True,
                                  child_outgoing=True,
                                  outgoing_distance=0,
                                  incoming_distance=0)
        slicing.add_type('Package', type_package)
        
        type_classifier = DataSetNeo4j.GraphSlicing(dataset,
                                  parent_levels=5,
                                  parent_incoming=False,
                                  parent_outgoing=False,
                                  self_incoming=True,
                                  self_outgoing=True,
                                  child_levels=2,
                                  child_incoming=True,
                                  child_outgoing=True,
                                  outgoing_distance=1,
                                  incoming_distance=1)
        slicing.add_type('Class', type_classifier)
        slicing.add_type('Interface', type_classifier)
        slicing.add_type('Enumeration', type_classifier)
        slicing.add_type('DataType', type_classifier)
        
        type_operation = DataSetNeo4j.GraphSlicing(dataset,
                                  parent_levels=5,
                                  parent_incoming=False,
                                  parent_outgoing=False,
                                  self_incoming=True,
                                  self_outgoing=True,
                                  child_levels=3,
                                  child_incoming=True,
                                  child_outgoing=True,
                                  outgoing_distance=2,
                                  incoming_distance=1)
        slicing.add_type('Operation', type_operation)
        
        type_property = DataSetNeo4j.GraphSlicing(dataset,
                                  parent_levels=5,
                                  parent_incoming=False,
                                  parent_outgoing=False,
                                  self_incoming=True,
                                  self_outgoing=True,
                                  child_levels=1,
                                  child_incoming=True,
                                  child_outgoing=True,
                                  outgoing_distance=2,
                                  incoming_distance=1)
        slicing.add_type('Property', type_property)
        
        # Consistency validation:
        for bug_location_model_meta_type_label in self.get_bug_location_model_meta_type_labels():
            try:
                slicing.get_slicing(bug_location_model_meta_type_label)
            except:
                raise Exception("Missing slicing configuration for model type: " + bug_location_model_meta_type_label)
        
        return slicing
    
    # Specifies all meta types that will be considered as model elements.
    def get_model_meta_type_labels(self) -> List[str]:
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
                                    
                                    # FIXME: "unnamed" Literal... model elements -> Fixed need new model training
                                    # "InstanceValue",
                                    # "LiteralBoolean",
                                    # "LiteralInteger",
                                    # "LiteralReal",
                                    # "LiteralString",
                                    # "LiteralUnlimitedNatural",
                                    
                                    "Comment"
                                ]
        return model_meta_type_labels
    
    # Specifies all meta types that will be considered as bug locations.
    def get_bug_location_model_meta_type_labels(self) -> Set[str]:
        bug_location_model_meta_type_labels = {
                                    "Package",
                                    "Class",
                                    "Interface",
                                    "Enumeration",
                                    "DataType",
                                    "Operation",
                                    "Property"
                                }
        
        # Consistency validation:
        model_meta_type_labels = self.get_model_meta_type_labels()
        
        for bug_location_model_meta_type_label in bug_location_model_meta_type_labels:
            if bug_location_model_meta_type_label not in model_meta_type_labels:
                raise Exception("Missing slicing configuration for model type: " + bug_location_model_meta_type_label)
        
        return bug_location_model_meta_type_labels
    
    # Specifies the properties of nodes that will be considered during embedding.
    def get_meta_type_to_properties(self):
        meta_type_to_properties:Dict[str, List[str]] = {
                                    # Model:
                                    "Model" : ["name"],
                                    "Package" : ["name"],
                                    "Class" : ["name"],
                                    "Interface" : ["name"],
                                    "Enumeration" : ["name"],
                                    "DataType" : ["name"],
                                    "Operation" : ["name"],
                                    "Parameter" : ["name"],
                                    "Property" : ["name"],
                                    "EnumerationLiteral" : ["name"],
                                    "Generalization" : [],
                                    "InterfaceRealization" : [],
                                    "InstanceValue" : ["value"],
                                    "LiteralBoolean" : ["value"],
                                    "LiteralInteger" : ["value"],
                                    "LiteralReal" : ["value"],
                                    "LiteralString" : ["value"],
                                    "LiteralUnlimitedNatural" : ["value"],
                                    "Comment" : ["body"],
                                    
                                    # Bug Report:
                                    "TracedBugReport" : ["summary"],
                                    "BugReportComment" : ["text"]
                                }
        
        # Consistency validation:
        for model_meta_type_label in self.get_model_meta_type_labels():
            try:
                meta_type_to_properties[model_meta_type_label]
            except:
                raise Exception("Missing property configuration for type: " + model_meta_type_label)
        
        
        return meta_type_to_properties


class UMLNodeSelfEmbedding(DataSetNeo4j.NodeSelfEmbedding):
    
    def __init__(self, meta_type_to_properties:Dict[str, List[str]], dictionary_path:str, dictionary_words_length:int, stopwords={}, unescape=True):
        print('Begin Loading Dictionary ...')

        self.meta_type_to_properties:Dict[str, List[str]] = meta_type_to_properties        
        self.dictionary_words:dict = KeyedVectors.load_word2vec_format(dictionary_path, binary=True)
        self.dictionary_words_length = dictionary_words_length
        self.stopwords = stopwords
        self.unescape = unescape
        self.column_names = self.create_column_names()
        
        print('Finished Loading Dictionary!')
        
    def unload_dictionary(self):
        self.dictionary_words = None
        
    def create_column_names(self):
        column_names = []
        
        for column in range(self.get_dimension()):
            column_names.append(str("feature" + str(column)))
        
        return column_names
    
    def get_column_names(self):
        return self.column_names
    
    def filter_type(self, meta_type_label:str) -> bool:  # @UnusedVariable
        return False  # Filtered by meta type configuration
        
    def filter_node(self, node:pd.Series) -> bool:  # @UnusedVariable
        return False
        
    def get_dimension(self) -> int:  # @UnusedVariable
        return self.dictionary_words_length
        
    def node_to_vector(self, node:pd.Series) -> np.ndarray:
        neo4j_node = node['nodes']
        text = ""
        
        for property_name in self.get_properties(neo4j_node):
            value = self.get_property(neo4j_node, property_name)
            text += " " + str(value)
        
        return self.text_to_vector(text)
    
    def get_property(self, node:Node, property_name:str) -> str:
        return node[property_name]
      
    def get_properties(self, node:Node) -> List[str]:
        meta_type = self.get_meta_type(node)
        return self.meta_type_to_properties[meta_type]
        
    def get_meta_type(self, node:Node) -> str:
        return list(node.labels)[0]
    
    def text_to_vector(self, text:str):
        
        # Encode words:
        feature_vector = np.zeros(self.dictionary_words_length, dtype=np.float32)
        words = text_to_words(text, self.stopwords, self.unescape)
        
        for word in words:
            try:
                word_embedding = self.dictionary_words[word]
                feature_vector = np.sum((feature_vector, word_embedding), axis=0) 
            except KeyError:   
                pass  # ignore...how to handle unseen words...?
                
        return feature_vector
