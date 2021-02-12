'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from typing import List, Set
import numpy as np  # type: ignore
import pandas as pd  # type: ignore


class TypbasedGraphSlicing:
    
    def __init__(self):
        self.type_label_to_graph_slicing:Dict[str, GraphSlicing] = {}
        
    def add_type(self, type_label:str, graph_slicing):
        self.type_label_to_graph_slicing[type_label] = graph_slicing
        
    def get_types(self) -> List[str]:
        return list(self.type_label_to_graph_slicing.keys())
        
    def get_slicing(self, type_label:str):
        return self.type_label_to_graph_slicing[type_label]
    
    
class GraphSlicing:
    
    def __init__(self,
                 dnn_depth:int,  # depth of graphSAGE layers
                 parent_levels:int=2,
                 parent_incoming:bool=False,
                 parent_outgoing:bool=False,
                 self_incoming:bool=True,
                 self_outgoing:bool=True,
                 child_levels:int=2,
                 child_incoming:bool=True,
                 child_outgoing:bool=True,
                 outgoing_distance:int=2,
                 incoming_distance:int=1):
        self.parent_levels:int = min(parent_levels, dnn_depth)
        self.parent_incoming:bool = parent_incoming
        self.parent_outgoing:bool = parent_outgoing
        
        self.self_incoming:bool = self_incoming
        self.self_outgoing:bool = self_outgoing
        
        self.child_levels:int = min(child_levels, dnn_depth)
        self.child_incoming:bool = child_incoming
        self.child_outgoing:bool = child_outgoing
        
        self.outgoing_distance:int = min(outgoing_distance, dnn_depth)
        self.incoming_distance:int = min(incoming_distance, dnn_depth)

    
class NodeSelfEmbedding:
 
    def load(self):
        pass
    
    def unload(self):
        pass
    
    def filter_type(self, meta_type_label:str) -> bool:  # @UnusedVariable
        return False
    
    def filter_node(self, node:pd.Series) -> bool:  # @UnusedVariable
        return False
    
    def get_dimension(self) -> int:
        pass
    
    def get_column_names(self):
        pass
    
    def node_to_vector(self, node:pd.Series) -> np.ndarray:
        pass

    
class MetaModel:
    
    # Specifies the slicing of subgraph for embedding of model elements.
    def get_slicing_criterion(self, dnn_depth:int) -> TypbasedGraphSlicing:
        pass
    
    # Specifies all meta types that will be considered as model elements.
    def get_model_meta_type_labels(self) -> List[str]:
        pass
    
    # Specifies all meta types that will be considered as bug locations.
    def get_bug_location_model_meta_type_labels(self) -> Set[str]:
        pass
    
    # Specifies the properties of nodes that will be considered during embedding.
    def get_meta_type_to_properties(self):
        pass
