'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Dict, List, Set

import numpy as np
from py2neo import Graph


class MetaModel:
    
    def get_graph(self) -> Graph:
        """
        Returns:
            Graph: The database storing the model.
        """
        raise NotImplementedError()
    
    def get_node_self_embedding(self) -> NodeSelfEmbedding:
        """
        Returns:
            NodeSelfEmbedding: Get the numeric feature vectors for specified nodes.
        """
        raise NotImplementedError()
    
    def get_bug_report_node_types(self) -> List[str]:
        """
        Returns:
            List[str]: Types of nodes that describe the bug report graph.
        """
        return ['TracedBugReport', 'BugReportComment']
    
    def get_system_model_types(self) -> List[str]:
        """
        Returns:
            List[str]: All type in the SystemModel (model view wrapper).
        """
        return ['TracedBugReport', 'BugReportComment', 'SystemModel', 'TracedVersion', 'View' 'Change']
    
    def get_system_model_connection_types(self) -> List[str]:
        """
        Returns:
            List[str]: Nodes that connect the SystemModel (model view wrapper) with the actual model.
        """
        return ['Change']

    def get_types(self) -> List[str]:
        """
        Returns:
            List[str]: Specifies all meta types that will be considered as model elements.
        """
        raise NotImplementedError()

    def get_type_to_properties(self):
        """
        Raises:
            NotImplementedError: pecifies the properties of nodes that will be considered during embedding.
        """
        raise NotImplementedError()

    def get_bug_location_types(self) -> Set[str]:
        """
        Returns:
            Set[str]: Specifies all meta types that will be considered as bug locations.
        """
        raise NotImplementedError()
    
    def get_bug_location_negative_sample_count(self) -> Dict[str, int]:
        """
        Returns:
            Set[str]: Specifies all meta types that will be considered as negative bug locations and the count of generated samples.
        """
        raise NotImplementedError()

    def get_slicing_criterion(self) -> str:
        """

        Args:
            dnn_depth (int): The maximal depth of the deep neural network, i.e., GraphSAGE layers.

        Returns:
            TypbasedGraphSlicing: Specifies the slicing of subgraph for embedding of model elements.
        """
        raise NotImplementedError()
    
    def find_bug_location_by_container(self) -> int:
        """
        Find container of bug location if the type is not in specified location.

        Returns:
            int: The maximal number of parents to be searched. Default to 2. 0 is off.
        """
        return 2


class NodeSelfEmbedding:

    def load(self):
        ...

    def unload(self):
        ...

    def get_dimension(self) -> int:
        raise NotImplementedError()
    
    def get_graph(self) -> Graph:
        """
        Returns:
            Graph: The database storing the model.
        """
        raise NotImplementedError()

    def node_to_vector(self, nodes_per_hop: List[List[List[int]]]) -> np.ndarray:
        """
        Get the numeric feature vectors for the specified nodes.

        Args:
            nodes (List[List[List[int]]]): List of node layers with pairs consisting of a node ID and a version.
        Returns:
            Numpy array containing the node features for the requested nodes.
        """
        size = sum([len(nodes) for nodes in nodes_per_hop])
        return np.zeros(shape=(size, self.get_dimension()), dtype=np.float32)
