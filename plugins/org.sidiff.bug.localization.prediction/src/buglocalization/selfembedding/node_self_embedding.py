'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Dict, List, Set

import numpy as np
from py2neo import Graph


class NodeSelfEmbedding:
    """
    Computes the local node self embedding vectors for the given nodes.
    """

    def load(self):
        """ 
        Initialization of the node self embedding object.
        """
        ...

    def unload(self):
        """
        Deinitialization of the node self embedding object, e.g., free memory.
        """
        ...

    def get_dimension(self) -> int:
        """
        Returns:
            int: The dimension of a single node self embedding vector.
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
