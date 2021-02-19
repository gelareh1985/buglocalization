'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

from typing import Dict, List, Set

import numpy as np  # type: ignore
import pandas as pd  # type: ignore


class TypbasedGraphSlicing:

    def __init__(self):
        self.type_label_to_graph_slicing: Dict[str, GraphSlicing] = {}

    def add_type(self, type_label: str, graph_slicing):
        self.type_label_to_graph_slicing[type_label] = graph_slicing

    def get_types(self) -> List[str]:
        return list(self.type_label_to_graph_slicing.keys())

    def get_slicing(self, type_label: str) -> 'GraphSlicing':
        return self.type_label_to_graph_slicing[type_label]


class GraphSlicing:

    def __init__(self,
                 dnn_depth: int,  # depth of graphSAGE layers
                 parent_levels: int = 2,
                 parent_incoming: bool = False,
                 parent_outgoing: bool = False,
                 self_incoming: bool = True,
                 self_outgoing: bool = True,
                 child_levels: int = 2,
                 child_incoming: bool = True,
                 child_outgoing: bool = True,
                 outgoing_distance: int = 2,
                 incoming_distance: int = 1):
        self.parent_levels: int = min(parent_levels, dnn_depth)
        self.parent_incoming: bool = parent_incoming
        self.parent_outgoing: bool = parent_outgoing

        self.self_incoming: bool = self_incoming
        self.self_outgoing: bool = self_outgoing

        self.child_levels: int = min(child_levels, dnn_depth)
        self.child_incoming: bool = child_incoming
        self.child_outgoing: bool = child_outgoing

        self.outgoing_distance: int = min(outgoing_distance, dnn_depth)
        self.incoming_distance: int = min(incoming_distance, dnn_depth)


class NodeSelfEmbedding:

    def load(self):
        ...

    def unload(self):
        ...

    def filter_type(self, meta_type_label: str) -> bool:
        return False

    def filter_node(self, node: pd.Series) -> bool:
        return False

    def get_dimension(self) -> int:
        raise NotImplementedError()

    def node_to_vector(self, node: pd.Series) -> np.ndarray:
        raise NotImplementedError()


class MetaModel:

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

    def find_bug_location_by_container(self) -> int:
        """
        Find container of bug location if the type is not in specified location.

        Returns:
            int: The maximal number of parents to be searched. Default to 2. 0 is off.
        """
        return 2

    def get_slicing_criterion(self, dnn_depth: int) -> TypbasedGraphSlicing:
        """

        Args:
            dnn_depth (int): The maximal depth of the deep neural network, i.e., GraphSAGE layers.

        Returns:
            TypbasedGraphSlicing: Specifies the slicing of subgraph for embedding of model elements.
        """
        raise NotImplementedError()
