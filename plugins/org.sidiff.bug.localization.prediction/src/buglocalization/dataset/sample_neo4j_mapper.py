# Based on [2021-04-07]
# https://github.com/m0baxter/stellargraph/blob/feature/add_Neo4jGraphSAGELinkGenerator/stellargraph/connector/neo4j/graph.py
# https://github.com/m0baxter/stellargraph/blob/feature/add_Neo4jGraphSAGELinkGenerator/stellargraph/connector/neo4j/sampler.py

# -*- coding: utf-8 -*-
#
# Copyright 2020 Data61, CSIRO
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

__all__ = [
    "Neo4jGraphSAGELinkGenerator",
    "Neo4jSampledBreadthFirstWalk",
]

from concurrent.futures import ThreadPoolExecutor
from typing import Any, List, Tuple, Union

import numpy as np
from buglocalization.metamodel.meta_model import MetaModel
from py2neo import Graph
from stellargraph.mapper import LinkSequence


class Neo4jGraphSAGELinkGenerator:
    """
    A data generator for link prediction with Homogeneous GraphSAGE models

    At minimum, supply the Neo4jStellarGraph, the batch size, and the number of
    node samples for each layer of the GraphSAGE model.

    The supplied graph should be a Neo4jStellarGraph object with node features.

    Use the :meth:`flow` method supplying the links to get an object that can be
    used as a Keras data generator.

    Example::

        G_generator = GraphSAGELinkGenerator(G, 50, [10,10])
        train_data_gen = G_generator.flow(train_link_ids, train_link_labels)
        test_data_gen = G_generator.flow(test_link_ids)

    Args:
        graph (Graph): Neo4j Graph object
        num_samples (list): The number of samples per layer (hop) to take.
        node_self_embedding (NodeSelfEmbedding): Maps Neo4j node IDs to feature vectors.
        batch_size (int, optional): Size of batch to return or -1 for all samples per batch.
        num_workers (int): Workers for parallel sampling of head nodes.
    """

    def __init__(self, meta_model: MetaModel, num_samples: List[int], batch_size: int = -1, num_workers: int = 4):
        self.meta_model = meta_model
        self.num_samples = num_samples
        self.batch_size = batch_size
        self.num_workers = num_workers

        self.node_self_embedding: Any = None
        self.executor: Any = None
        self.sampler: Any = None

        # This is a node generator and requries a model with two root nodes per query
        self.multiplicity = 2

    def initialize(self):
        if self.node_self_embedding is None:
            self.node_self_embedding = self.meta_model.get_node_self_embedding()
            self.node_self_embedding.load()
            self.executor = ThreadPoolExecutor(max_workers=self.num_workers)
            self.sampler = Neo4jSampledBreadthFirstWalk(
                self.node_self_embedding.get_graph(),
                self.meta_model.get_slicing_criterion())

    def __getstate__(self):
        # Do not expose the executor (for multiprocessing) which fails on pickle.
        state = dict(self.__dict__)

        if 'node_self_embedding' in state:
            state['node_self_embedding'] = None

        if 'executor' in state:
            state['executor'] = None

        if 'sampler' in state:
            state['sampler'] = None

        return state

    def flow(self, link_ids: List[Tuple[int, int, int]],
             targets: List[Union[float, int]] = None,
             shuffle: bool = False, seed: int = None):
        """
        link_ids -> [bug report, location, version]
        """

        self.initialize()
        batch_size = self.batch_size

        # All samples in one batch:
        if (batch_size == -1):
            batch_size = len(link_ids)

        return LinkSequence(
            self.sample_features,
            batch_size,
            link_ids,
            targets=targets,
            shuffle=shuffle,
            seed=seed,
        )

    def __sample_features_nodes(self, versions: List[int], head_nodes: List[int], batch_num: int):
        versioned_nodes_per_hop: List[List[List[int]]] = self.sampler.run(versions=versions, nodes=head_nodes, n=1, n_size=self.num_samples)

        # batch_nodes = np.concatenate(nodes_per_hop)
        batch_features = self.node_self_embedding.node_to_vector(versioned_nodes_per_hop)

        features = self.reformat_feature_array(
            versioned_nodes_per_hop, batch_features, len(head_nodes)
        )

        return features

    def sample_features(self, head_links: List[Tuple[int, int, int]], batch_num: int):
        """
        Collect the features of the nodes sampled from Neo4j,
        and return these as a list of feature arrays for the GraphSAGE
        algorithm.

        Args:
            head_links: An iterable of (source, target) nodes to perform sampling on.
            batch_num: Ignored, because this is not reproducible.

        Returns:
            A list of the same length as ``num_samples`` of collected features from
            the sampled nodes of shape:
            ``(len(head_links), num_sampled_at_layer, feature_size)``
            where ``num_sampled_at_layer`` is the cumulative product of ``num_samples``
            for that layer.
        """

        features_source, features_target = self.threaded_feature_sampling(
            self.executor,
            self.__sample_features_nodes,
            [edge[2] for edge in head_links],  # version
            [edge[0] for edge in head_links],  # node_list_1
            [edge[1] for edge in head_links],  # node_list_2
            batch_num,
        )

        features = []

        for source, target in zip(features_source, features_target):

            features.append(source)
            features.append(target)

        return features

    def reformat_feature_array(self, nodes_per_hop, batch_features, N):

        features = []
        idx = 0

        for nodes in nodes_per_hop:

            features_for_slot = batch_features[idx: idx + len(nodes)]
            resize = -1 if np.size(features_for_slot) > 0 else 0

            features.append(
                np.reshape(features_for_slot, (N, resize, features_for_slot.shape[1]),)
            )

            idx += len(nodes)

        return features

    def threaded_feature_sampling(self, executor, sample_function,
                                  versions: List[int], node_list_1: List[int], node_list_2: List[int], batch_num):

        future_samples = [
            executor.submit(sample_function, versions, node_list_1, batch_num,),
            executor.submit(sample_function, versions, node_list_2, batch_num,),
        ]

        features_source, features_target = (
            future_samples[0].result(),
            future_samples[1].result(),
        )

        return features_source, features_target


class Neo4jSampledBreadthFirstWalk:
    """
    Breadth First Walk that generates a sampled number of paths from a starting node.
    It can be used to extract a random sub-graph starting from a set of initial nodes from Neo4j database.
    """

    def __init__(self, graph: Graph, slicing_criterion: str):
        self.graph = graph
        self.slicing_criterion = slicing_criterion

    def run(self, versions: List[int], nodes: List[int], n=1, n_size=None) -> List[List[List[int]]]:
        """
        Send queries to Neo4j graph databases and collect sampled breadth-first walks starting from
        the root nodes.
        Args:
            nodes (list of hashable): A list of root node ids such that from each node n BFWs will
                be generated up to the given depth d.
            n_size (list of int): The number of neighbouring nodes to expand at each depth of the
                walk. Sampling of neighbours with replacement is always used regardless of the node
                degree and number of neighbours requested.
            n (int): Number of walks per node id.
            seed (int, optional): Random number generator seed; default is None
        Returns:
            A list of lists, each list is a sequence of sampled node ids at a certain hop.
        """

        samples = [[[nodes[head_node_idx], versions[head_node_idx]] for head_node_idx in range(len(nodes)) for _ in range(n)]]
        neighbor_query = self._bfs_neighbor_query(sampling_direction="BOTH", slicing_criterion=self.slicing_criterion)

        # this sends O(number of hops) queries to the database, because the code is cleanest like that
        for num_sample in n_size:
            cur_nodes = samples[-1]
            result = self.graph.run(
                neighbor_query,
                parameters={"versioned_node_id_list": cur_nodes, "num_samples": num_sample},
            )
            samples.append(result.data()[0]["next_samples"])

        return samples

    def _bfs_neighbor_query(self, sampling_direction, slicing_criterion):
        """
        Generate the Cypher neighbor sampling query for a batch of nodes.
        Args:
            sampling_direction (String): indicate type of neighbors needed to sample. Direction must be 'in', 'out' or 'both'.
            id_property (str): Cypher-escaped property name for node IDs.
            node_label (str, optional): Common label for all nodes in the graph, if such label exists.
                Providing this is useful if there are any indexes created on this label (e.g. on node IDs),
                as it will improve performance of queries.
        Returns:
            The cypher query that samples the neighbor ids for a batch of nodes.
        """
        direction_arrow = {"BOTH": "--", "IN": "<--", "OUT": "-->"}[sampling_direction]

        # FIXME: This query is very inefficient for nodes with a high number of neighbors, e.g., model library nodes.
        #
        # Workaround A: Compute a reasonable limit = num_sample * epoche:
        # MATCH (cur_node){direction_arrow}(neighbors) {slicing_criterion}
        # WITH neighbors LIMIT {limit}
        #
        # Workaround B: Use SKIP with different random step sizes. (The skip value can not be ccmputed from a variable.):
        # MATCH (cur_node){direction_arrow}(neighbors) {slicing_criterion}
        # RETURN neighbors LIMIT {num_sample}
        # UNION MATCH (cur_node){direction_arrow}(neighbors) {slicing_criterion}
        # RETURN neighbors SKIP toInteger(rand()*10) LIMIT {num_sample}
        # UNION MATCH (cur_node){direction_arrow}(neighbors) {slicing_criterion}
        # WITH neighbors SKIP toInteger(rand()*100) LIMIT {num_sample}

        return f"""
            // expand the list of node id in seperate rows of ids.
            UNWIND $versioned_node_id_list AS versioned_node_id
            WITH versioned_node_id[0] AS node_id, versioned_node_id[1] AS db_version
            // for each node id in every row, collect the random list of its neighbors.
            CALL apoc.cypher.run(
                'MATCH(cur_node) WHERE ID(cur_node) = $node_id
                // find the neighbors
                MATCH (cur_node){direction_arrow}(neighbors) {slicing_criterion}
                // collect neighbors in a list
                WITH CASE collect(neighbors) WHEN [] THEN [null] ELSE collect(neighbors) END AS in_neighbors_list
                // pick random nodes with replacement -> If fewer neighbors than $num_samples, missing slots will be filled with duplicates.
                WITH apoc.coll.randomItems(in_neighbors_list, $num_samples, True) AS sampled
                // pull the ids of the sampled nodes only
                UNWIND sampled AS nn
                // collect ignores nulls, so re-handle the no-neighbours case to ensure we get [null, null, ...] output
                WITH CASE collect([ID(nn), $db_version]) WHEN [] THEN sampled ELSE collect([ID(nn), $db_version]) END AS in_samples_list
                RETURN in_samples_list',
                // parameters for apoc.cypher.run
                {{ node_id: node_id, db_version: db_version, num_samples: $num_samples  }}) YIELD value
            RETURN apoc.coll.flatten(collect(value.in_samples_list)) AS next_samples
            """
