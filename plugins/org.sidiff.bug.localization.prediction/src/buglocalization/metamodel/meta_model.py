'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Dict, List, Set

import numpy as np
import pandas as pd


class TypbasedGraphSlicing:

    def __init__(self):
        self.type_label_to_graph_slicing: Dict[str, List[str]] = {}

    def add_type(self, type_label: str, graph_slicing):
        self.type_label_to_graph_slicing[type_label] = graph_slicing

    def get_types(self) -> List[str]:
        return list(self.type_label_to_graph_slicing.keys())

    def get_slicing(self, type_label: str) -> List[str]:
        return self.type_label_to_graph_slicing[type_label]


class NodeSelfEmbedding:

    def load(self):
        ...

    def unload(self):
        ...

    def get_dimension(self) -> int:
        raise NotImplementedError()

    def node_to_vector(self, node: pd.Series) -> np.ndarray:
        raise NotImplementedError()


class MetaModel:
    
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

    def get_slicing_criterion(self, num_samples: List[int]) -> TypbasedGraphSlicing:
        """

        Args:
            dnn_depth (int): The maximal depth of the deep neural network, i.e., GraphSAGE layers.

        Returns:
            TypbasedGraphSlicing: Specifies the slicing of subgraph for embedding of model elements.
        """
        raise NotImplementedError()
    
    # # TODO: Create/Move to Training Configuration # #
    
    def find_bug_location_by_container(self) -> int:
        """
        Find container of bug location if the type is not in specified location.

        Returns:
            int: The maximal number of parents to be searched. Default to 2. 0 is off.
        """
        return 2
    
    def filter_comments_newer_as_bugfix(self) -> bool:
        """
        Filter out all bug report comments that were written after the bug fix was commited.

        Returns:
            int: True to enable the filter. Default to False.
        """
        return True
