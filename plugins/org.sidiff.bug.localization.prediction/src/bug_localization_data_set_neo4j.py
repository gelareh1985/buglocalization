'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

import time
from typing import Any, Dict, List, Optional, Set, Tuple, cast

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
from pandas.core.frame import DataFrame  # type: ignore
from py2neo import Graph, Node  # type: ignore
from stellargraph import StellarGraph  # type: ignore

import bug_localization_data_set_neo4j_queries as query
from bug_localization_data_set import IBugSample, IDataSet, ILocationSample
from bug_localization_meta_model import (GraphSlicing, MetaModel,
                                         NodeSelfEmbedding,
                                         TypbasedGraphSlicing)
from bug_localization_util import t

# ===============================================================================
# Neo4j Data Connector:
# ===============================================================================
# -- NOTE: Do not save any "raw" Neo4j Node or Edge objects in the data set.
#    These objects store a reference to py2neo.internal.connectors.BoltConnector
#    which is not pickable and causing problems with multi-processing. Also see
#    __getstate__() makes the intentionally stored connection invisible to pickle.
# ===============================================================================


class Neo4jConfiguration:

    def __init__(self,
                 neo4j_host: str = 'localhost',
                 neo4j_port: int = 7687,
                 neo4j_user: str = None,
                 neo4j_password: str = None):
        """
        The configuration parameters for connection to Neo4j graph database.

        Args:
            neo4j_host (str): Neo4j host address. Defaults to localhost.
            neo4j_port (int, optional): Neo4j connection port. Defaults to bolt port 7687.
            neo4j_user (str, optional): Neo4j user name for connection. Defaults to None.
            neo4j_password (str, optional): Password for connection. Defaults to None.
        """
        self.neo4j_host: str = neo4j_host
        self.neo4j_port: Optional[int] = neo4j_port
        self.neo4j_user: Optional[str] = neo4j_user
        self.neo4j_password: Optional[str] = neo4j_password


class DataSetNeo4j(IDataSet):

    # https://py2neo.org/v4/database.html
    # https://stellargraph.readthedocs.io/en/stable/demos/basics/loading-saving-neo4j.html

    def __init__(self,
                 meta_model: MetaModel,
                 node_self_embedding: NodeSelfEmbedding,
                 typebased_slicing: TypbasedGraphSlicing,
                 neo4j_config: Neo4jConfiguration,
                 is_negative: bool = False):

        super().__init__(is_negative)

        # Connnection info:
        self.neo4j_config = neo4j_config

        # Meta-model configuration
        self.meta_model = meta_model
        self.node_self_embedding = node_self_embedding
        self.typebased_slicing = self.translate_slicing_criterion(typebased_slicing)

        # Opened connection and read bug samlpes:
        self.neo4j_graph: Optional[Graph] = None
        self.connectNeo4j()

        self.list_samples()

    def __getstate__(self):
        # Close connection to allow multiprocessing i.e., each process needs its own connnection.
        self.closeNeo4j()

        # Do not expose Ne4j connection (for multiprocessing) which is not pickable.
        state = dict(self.__dict__)

        # Check again if connection is actually "closed" for exposed state:
        if 'neo4j_graph' in state:
            state['neo4j_graph'] = None

        return state

    def connectNeo4j(self) -> Graph:
        if self.neo4j_graph is None:
            self.neo4j_graph = Graph(host=self.neo4j_config.neo4j_host, port=self.neo4j_config.neo4j_port,
                                     user=self.neo4j_config.neo4j_user, password=self.neo4j_config.neo4j_password)
        return self.neo4j_graph

    def closeNeo4j(self):
        # https://stackoverflow.com/questions/59138809/connection-pool-life-cycle-of-py2neo-graph-would-the-connections-be-released-wh
        # graph.database.connector.close()
        # Database.forget_all()
        self.neo4j_graph = None

    def get_samples_neo4j(self) -> List['BugSampleNeo4j']:
        return cast(List[BugSampleNeo4j], self.bug_samples)

    class TypbasedGraphSlicingNeo4j(TypbasedGraphSlicing):

        def get_slicing(self, type_label: str) -> DataSetNeo4j.GraphSlicingNeo4j:
            return cast(DataSetNeo4j.GraphSlicingNeo4j, self.type_label_to_graph_slicing[type_label])

    class GraphSlicingNeo4j(GraphSlicing):

        def __init__(self, dataset: DataSetNeo4j, slicing_criterion: GraphSlicing):
            self.parent_levels: int = slicing_criterion.parent_levels
            self.parent_incoming: bool = slicing_criterion.parent_incoming
            self.parent_outgoing: bool = slicing_criterion.parent_outgoing
            self.parent_query: str = query.edges_to_parent_nodes(self.parent_levels)

            self.self_incoming: bool = slicing_criterion.self_incoming
            self.self_outgoing: bool = slicing_criterion.self_outgoing

            self.child_levels: int = slicing_criterion.child_levels
            self.child_incoming: bool = slicing_criterion.child_incoming
            self.child_outgoing: bool = slicing_criterion.child_outgoing
            self.child_query: str = query.edges_to_child_nodes(self.child_levels)

            self.outgoing_distance: int = slicing_criterion.outgoing_distance
            self.outgoing_query: str = query.outgoing_cross_tree_edges(self.outgoing_distance)

            self.incoming_distance: int = slicing_criterion.incoming_distance
            self.incoming_query: str = query.incoming_cross_tree_edges(self.incoming_distance)

    def translate_slicing_criterion(self, typebased_slicing: TypbasedGraphSlicing) -> DataSetNeo4j.TypbasedGraphSlicingNeo4j:
        neo4j_typebased_slicing = DataSetNeo4j.TypbasedGraphSlicingNeo4j()

        for type_label in typebased_slicing.get_types():
            slicing_criterion = typebased_slicing.get_slicing(type_label)
            neo4j_slicing = self.GraphSlicingNeo4j(self, slicing_criterion)
            neo4j_typebased_slicing.add_type(type_label, neo4j_slicing)

        return neo4j_typebased_slicing

    def list_samples(self):
        for db_version in self.run_query(query.buggy_versions())['versions']:
            self.bug_samples.append(self.create_sample(db_version))

    def create_sample(self, db_version: int) -> 'BugSampleNeo4j':
        return BugSampleNeo4j(self, db_version)

    # # Send query to the Neo4j database and parse the result to a Pandas data frame #

    def run_query(self, query: str, parameters: Dict[str, Any] = None) -> DataFrame:
        return self.connectNeo4j().run(query, parameters).to_data_frame()

    def run_query_value(self, query: str, parameters: Dict[str, Any] = None) -> Any:
        result = self.run_query(query, parameters)

        if len(result) > 0:
            return result.iloc[0, 0]

    def get_label(self, node: Node) -> str:
        return ':'.join(node.labels)


class BugSampleNeo4j(IBugSample):
    dataset: DataSetNeo4j

    def __init__(self, dataset: DataSetNeo4j, db_version: int):
        super().__init__(dataset, "version:" + str(db_version))
        self.db_version: int = db_version
        self.edge_columns = ['source', 'target']

        # Model graph nodes: index -> embedding
        self.model_nodes: Dict[int, np.ndarray] = {}

        # Model graph nodes: List[(id:type)]
        self.model_nodes_types: Dict[str, List[int]] = {}

        # Bug report graph:
        self.bug_report_node_id: int = -1
        self.bug_report_nodes: Dict[int, Dict[int, np.ndarray]] = {}
        # StellarGraph requires Panda Data Frames as edges
        self.bug_report_edges: DataFrame = None
        self.bug_locations: Set[Tuple[int, str]] = set()  # node ID -> meta-type

    # # Load graphs from Neo4j # #

    def load_model_nodes(self,
                         model_meta_type_labels: List[str],
                         embedding: NodeSelfEmbedding,
                         node_ids: List[int] = None,
                         log_level: int = 0):
        for model_meta_type_label in model_meta_type_labels:
            node_ids_per_meta_type: List[int] = []
            self.load_node_embeddings([model_meta_type_label], embedding,
                                      nodes_embeddings=self.model_nodes,
                                      to_be_embedded_node_ids=node_ids,
                                      embedded_node_ids=node_ids_per_meta_type,
                                      log_level=log_level)

            self.model_nodes_types[model_meta_type_label] = node_ids_per_meta_type

    def load_bug_location_subgraph(self, node_id: int, bug_localization_subgraph_edges: DataFrame) -> Tuple[StellarGraph, Tuple[int, int]]:
        nodes_trace: Dict[int, int] = {}
        edges_ids = set()

        subgraph_nodes_model: List[np.ndarray] = []
        subgraph_edges_model: List[Tuple] = []

        # =======================================================================
        # Model Graph:
        # =======================================================================
        if not bug_localization_subgraph_edges.empty:

            for report_edge_index, report_edge in bug_localization_subgraph_edges.iterrows():

                # FIXME[Workaround]: Filter duplicated edges from Neo4j:
                if report_edge_index not in edges_ids:
                    edges_ids.add(report_edge_index)

                    subgraph_source_node_id = None
                    subgraph_target_node_id = None
                    source_node_id = report_edge['source']
                    target_node_id = report_edge['target']

                    # Source:
                    if subgraph_source_node_id is None:
                        if source_node_id not in nodes_trace:
                            if source_node_id in self.model_nodes:
                                subgraph_source_node_id = len(subgraph_nodes_model)
                                nodes_trace[source_node_id] = subgraph_source_node_id
                                subgraph_nodes_model.append(self.model_nodes[source_node_id])
                        else:
                            subgraph_source_node_id = nodes_trace[source_node_id]

                    # Target:
                    if subgraph_target_node_id is None:
                        if target_node_id not in nodes_trace:
                            if target_node_id in self.model_nodes:
                                subgraph_target_node_id = len(subgraph_nodes_model)
                                nodes_trace[target_node_id] = subgraph_target_node_id
                                subgraph_nodes_model.append(self.model_nodes[target_node_id])
                        else:
                            subgraph_target_node_id = nodes_trace[target_node_id]

                    # Is report_edge included in subgraph?
                    if subgraph_source_node_id is not None and subgraph_target_node_id is not None:
                        subgraph_edges_model.append((subgraph_source_node_id, subgraph_target_node_id))

        # Only the initial given node is contained in subgraph:
        if not subgraph_nodes_model:
            if node_id in self.model_nodes:
                subgraph_node_id = len(subgraph_nodes_model)
                nodes_trace[node_id] = subgraph_node_id
                subgraph_nodes_model.append(self.model_nodes[node_id])

        if not subgraph_nodes_model:
            raise Exception(
                "No (subgraph) embedding found for node: ID " + str(node_id))

        # =======================================================================
        # Bug Report Graph: (append to model graph)
        # =======================================================================

        for report_node_id, report_node_embedding in self.bug_report_nodes.items():
            subgraph_node_id = len(subgraph_nodes_model)
            nodes_trace[report_node_id] = subgraph_node_id
            subgraph_nodes_model.append(report_node_embedding)

        for report_edge_index, report_edge in self.bug_report_edges.iterrows():
            source_node_id = report_edge['source']
            target_node_id = report_edge['target']
            subgraph_node_source = nodes_trace[source_node_id]
            subgraph_node_target = nodes_trace[target_node_id]
            subgraph_edges_model.append((subgraph_node_source, subgraph_node_target))

        # =======================================================================
        # StellarGraph of Bug Localization Subgraph:
        # =======================================================================

        model_subgraph_edges_dataframe = pd.DataFrame(subgraph_edges_model, columns=self.edge_columns)
        subgraph_nodes_model_array = np.asarray(subgraph_nodes_model)
        graph = StellarGraph(nodes=subgraph_nodes_model_array,
                             edges=model_subgraph_edges_dataframe)

        return graph, (nodes_trace[self.bug_report_node_id], nodes_trace[node_id])

    def load_bug_report(self, embedding: NodeSelfEmbedding, locate_by_container: int):

        # Bug report node:
        bug_report_node_frame = self.load_dataframe(query.nodes_in_version('TracedBugReport'))
        assert len(bug_report_node_frame.index) == 1
        self.bug_report_node_id = bug_report_node_frame.index[0]

        # Bug report graph:
        bug_report_meta_type_labels = ['TracedBugReport', 'BugReportComment']
        self.bug_report_nodes = self.load_node_embeddings(bug_report_meta_type_labels, embedding)
        self.bug_report_edges = self.load_dataframe(query.edges_in_version(
            'TracedBugReport', 'comments', 'BugReportComment'))
        self.bug_report_edges.drop(['edges'], axis=1, inplace=True)

        # Bug locations:
        self.bug_locations = self.load_bug_locations(locate_by_container)

    def load_bug_locations(self, locate_by_container: int) -> Set[Tuple[int, str]]:
        bug_locations: Set[Tuple[int, str]] = set()
        bug_location_edges = self.load_dataframe(query.edges_in_version('Change', 'location', return_ids=False))

        for index, edge in bug_location_edges.iterrows():
            # location edges point at model elements:
            bug_location: Node = edge['target']
            bug_locations.add((bug_location.identity, self.dataset.get_label(bug_location)))

        # Find container of bug location if the type is not in specified location:
        if locate_by_container > 0:
            bug_locations = self.locate_bug_locations_by_container(bug_locations, locate_by_container)

        return bug_locations

    def locate_bug_locations_by_container(self, bug_locations: Set[Tuple[int, str]], locate_by_container: int) -> Set[Tuple[int, str]]:
        bug_locations_by_container: Set[Tuple[int, str]] = set()  # Set eliminated duplicted id, type tuples.
        bug_location_types: Set[str] = self.dataset.meta_model.get_bug_location_types()

        query_parent_node = query.path(query.edge_containment(True), 'b', k=2, return_query='RETURN DISTINCT a AS parents')
        get_label = self.dataset.get_label
        query_parent_node_parameter: Dict[str, List[int]] = {'node_ids': []}

        for model_location, bug_location_type in bug_locations:
            if bug_location_type in bug_location_types:
                bug_locations_by_container.add((model_location, bug_location_type))
            else:
                query_parent_node_parameter['node_ids'] = [model_location]
                parent_nodes = self.load_dataframe(
                    query_parent_node, query_parent_node_parameter, set_index=False)

                for parent_node in parent_nodes['parents']:
                    if get_label(parent_node) in bug_location_types:
                        bug_locations_by_container.add((parent_node.identity, get_label(parent_node)))
                        break

        return bug_locations_by_container

    def load_node_embeddings(self,
                             meta_type_labels: List[str],
                             embedding: NodeSelfEmbedding,
                             nodes_embeddings: Dict[int, np.ndarray] = {},
                             to_be_embedded_node_ids: List[int] = None,
                             embedded_node_ids: List[int] = None,
                             log_level: int = 0) -> Dict[int, np.ndarray]:
        """
        Args:
            meta_type_labels (List[str]): All meta-types to be embedded.
            embedding (NodeSelfEmbedding): The embedding algorithm.
            nodes_embeddings (Dict[int, np.ndarray], optional): The dictionary for the node ID -> embedding. Defaults to {}.
            to_be_embedded_node_ids (List[int], optional): Nodes to be emdedded; will be filtered by given meta-type. Defaults to None.
            embedded_node_ids (List[int], optional): The nodes that were added to the embedding dictionary. Defaults to None.

        Returns:
            Dict[int, np.ndarray]: The node ID -> embedding dictionary.
        """

        # Filter by meta-type:
        for meta_type_label in meta_type_labels:
            if not embedding.filter_type(meta_type_label):

                # Filter by given node IDs?
                if to_be_embedded_node_ids is not None:
                    query_nodes_in_version = query.nodes_in_version(meta_type_label, node_ids=True)
                    query_nodes_in_version_parameter = {'node_ids': to_be_embedded_node_ids}
                    nodes_in_version = self.load_dataframe(query_nodes_in_version, query_nodes_in_version_parameter)
                else:
                    nodes_in_version = self.load_dataframe(query.nodes_in_version(meta_type_label))

                if log_level >= 5:
                    print(meta_type_label + ': ' + str(len(nodes_in_version.index)))

                if not nodes_in_version.empty:

                    # Filter specific node?
                    if not embedding.filter_node(nodes_in_version):
                        for index, node in nodes_in_version.iterrows():
                            nodes_embedding = embedding.node_to_vector(node)
                            nodes_embeddings[index] = nodes_embedding

                            if embedded_node_ids is not None:
                                embedded_node_ids.append(index)

        return nodes_embeddings

    def dict_to_data_frame(self, data: Dict[int, np.ndarray], columns: str):
        return pd.DataFrame.from_dict(data, orient='index', columns=columns)

    def load_dataframe(self, query: str, parameters: dict = None, set_index: bool = True) -> DataFrame:
        default_parameter = {'db_version': self.db_version}

        if parameters is not None:
            for parameter_name, value in parameters.items():
                default_parameter[parameter_name] = value

        df = self.dataset.run_query(query, default_parameter)

        if set_index and not df.empty:
            df.set_index("index", inplace=True)

        return df

    def load_subgraph_edges(self, node_id: int, slicing: DataSetNeo4j.GraphSlicingNeo4j) -> DataFrame:
        # t = time.time()

        if slicing is None:
            raise Exception("Missing slicing configuration for node:", node_id)

        subgraph_edges = []
        node_id_query_parameter = {'node_ids': [node_id]}

        # # Containment Tree # #
        parent_edges: DataFrame = None
        child_edges: DataFrame = None

        if slicing.parent_levels > 0:
            parent_edges = self.load_dataframe(slicing.parent_query, node_id_query_parameter)
            subgraph_edges.append(parent_edges)

        if slicing.child_levels > 0:
            child_edges = self.load_dataframe(slicing.child_query, node_id_query_parameter)
            subgraph_edges.append(child_edges)

        # # Cross-Tree Outgoing Edges # #
        tree_of_outgoing = set()

        if slicing.parent_outgoing and parent_edges is not None and not parent_edges.empty:
            for parent in parent_edges['source']:
                tree_of_outgoing.add(parent)
            for parent in parent_edges['target']:
                tree_of_outgoing.add(parent)

        if slicing.self_outgoing:
            tree_of_outgoing.add(node_id)

        if slicing.child_outgoing and child_edges is not None and not child_edges.empty:
            for child in child_edges['source']:
                tree_of_outgoing.add(child)
            for child in child_edges['target']:
                tree_of_outgoing.add(child)

        tree_of_outgoing_query_parameter = {'node_ids': list(tree_of_outgoing)}
        outgoing_edges = self.load_dataframe(
            slicing.outgoing_query, tree_of_outgoing_query_parameter)
        subgraph_edges.append(outgoing_edges)

        # # Cross-Tree Incoming Edges # #
        tree_of_incoming = set()

        if slicing.parent_incoming and parent_edges is not None and not parent_edges.empty:
            for parent in parent_edges['source']:
                tree_of_incoming.add(parent)
            for parent in parent_edges['target']:
                tree_of_incoming.add(parent)

        if slicing.self_incoming:
            tree_of_incoming.add(node_id)

        if slicing.child_incoming and child_edges is not None and not child_edges.empty:
            for child in child_edges['source']:
                tree_of_incoming.add(child)
            for child in child_edges['target']:
                tree_of_incoming.add(child)

        tree_of_incoming_query_parameter = {'node_ids': list(tree_of_incoming)}
        incoming_eges = self.load_dataframe(slicing.incoming_query, tree_of_incoming_query_parameter)
        subgraph_edges.append(incoming_eges)

        # # Combine Edges # #
        subgraph = pd.concat(subgraph_edges)
        # print("Query Subgraph:", time.time() - t)

        return subgraph


class LocationSampleBaseNeo4j(ILocationSample):

    def __init__(self, neo4j_model_location: int, model_location_type: str, label: int):
        self.neo4j_model_location: int = neo4j_model_location
        self.mode_location_type: str = model_location_type

        self._label: int = label
        self._graph: Optional[StellarGraph] = None
        self._model_location: Optional[int] = None
        self._bug_report: Optional[int] = None

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        ...

    def label(self) -> int:
        return self._label

    def is_negative(self) -> bool:
        return self._label == 0

    def graph(self) -> StellarGraph:
        if self._graph is not None:
            return self._graph
        else:
            raise Exception("Graph is missing. Location sample not initialized?")

    def bug_report(self) -> int:
        if self._bug_report is not None:
            return self._bug_report
        else:
            raise Exception("Bug Report is missing. Location sample not initialized?")

    def model_location(self) -> int:
        if self._model_location is not None:
            return self._model_location
        else:
            raise Exception("Model AST element location is missing. Location sample not initialized?")


# ===============================================================================
# Training Neo4j Data Connector
# ===============================================================================


class DataSetTrainingNeo4j(DataSetNeo4j):

    def __init__(self,
                 meta_model: MetaModel,
                 node_self_embedding: NodeSelfEmbedding,
                 typebased_slicing: TypbasedGraphSlicing,
                 neo4j_config: Neo4jConfiguration,
                 is_negative: bool,
                 generate_negative_sample_per_type: int = 10):

        super().__init__(meta_model, node_self_embedding, typebased_slicing, neo4j_config, is_negative=is_negative)
        self.generate_negative_sample_per_type = generate_negative_sample_per_type

    def create_sample(self, db_version: int) -> BugSampleNeo4j:
        return BugSampleTrainingNeo4j(self, db_version)


class BugSampleTrainingNeo4j(BugSampleNeo4j):
    dataset: DataSetTrainingNeo4j

    def load_bug_locations(self, locate_by_container: int) -> Set[Tuple[int, str]]:
        if not self.dataset.is_negative:
            # Load positive sample -> default super class implementation:
            return super().load_bug_locations(locate_by_container)
        else:
            # Generate negative sample:
            bug_locations: Set[Tuple[int, str]] = set()

            for model_type in self.dataset.meta_model.get_bug_location_types():
                count = self.dataset.generate_negative_sample_per_type
                random_nodes = self.load_dataframe(query.random_nodes_in_version(count, model_type), set_index=False)

                for index, random_node_result in random_nodes.iterrows():
                    random_node = random_node_result['nodes']
                    bug_locations.add((random_node.identity, self.dataset.get_label(random_node)))

            return bug_locations

    def initialize(self, log_level: int = 0):
        if log_level >= 4:
            print("Start Loading Dictionary...")
        start_time = time.time()

        node_self_embedding = self.dataset.node_self_embedding
        node_self_embedding.load()

        if log_level >= 4:
            print("Finished Loading Dictionary:", t(start_time))
            print("Start Loading Locations...")
            start_time = time.time()

        meta_model = self.dataset.meta_model
        bug_localization_subgraphs = []

        # Bug report and locations:
        self.load_bug_report(node_self_embedding, meta_model.find_bug_location_by_container())

        # Subgraphs of bug locations:
        for bug_location, bug_location_type in self.bug_locations:
            # Filter by type configured for meta-model:
            if bug_location_type in meta_model.get_bug_location_types():
                label = 0 if self.dataset.is_negative else 1
                location_sample = LocationSampleTrainingNeo4j(bug_location, bug_location_type, label)
                bug_localization_subgraphs.append(location_sample.bug_localization_subgraph_edges(self))
                self.location_samples.append(location_sample)

        self.load_model_nodes(meta_model.get_types(),
                              node_self_embedding,
                              self.collect_model_nodes(bug_localization_subgraphs),
                              log_level=log_level)

        if log_level >= 4:
            print("Finished Loading Locations:", t(start_time))

    def collect_model_nodes(self, bug_localization_subgraphs: List[DataFrame]) -> List[int]:
        node_ids: Set[int] = set()

        for bug_localization_subgraph in bug_localization_subgraphs:
            for source_id in bug_localization_subgraph['source']:
                node_ids.add(source_id)
            for target_id in bug_localization_subgraph['target']:
                node_ids.add(target_id)

        return list(node_ids)

    def uninitialize(self):
        # TODO: Make field Optional!?
        self.model_nodes = {}
        self.model_nodes_types = {}
        self.bug_report_node_id = -1
        self.bug_report_nodes = {}
        self.bug_report_edges = None
        self.bug_locations = set()
        self.location_samples = []


class LocationSampleTrainingNeo4j(LocationSampleBaseNeo4j):

    def __init__(self, neo4j_model_location: int, model_location_type: str, label: int):
        super().__init__(neo4j_model_location, model_location_type, label)
        self._bug_localization_subgraph_edges: Optional[DataFrame] = None

    def bug_localization_subgraph_edges(self, bug_sample: BugSampleTrainingNeo4j) -> DataFrame:
        if self._bug_localization_subgraph_edges is not None:
            return self._bug_localization_subgraph_edges
        else:
            typebased_slicing = bug_sample.dataset.typebased_slicing
            slicing = typebased_slicing.get_slicing(self.mode_location_type)
            self._bug_localization_subgraph_edges = bug_sample.load_subgraph_edges(self.neo4j_model_location, slicing)
            return self._bug_localization_subgraph_edges

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        if isinstance(bug_sample, BugSampleTrainingNeo4j):
            subgraph, bug_location_pair = bug_sample.load_bug_location_subgraph(
                self.neo4j_model_location, self.bug_localization_subgraph_edges(bug_sample))

            self._graph = subgraph
            self._bug_report = bug_location_pair[0]
            self._model_location = bug_location_pair[1]  # Mapped ID in subgraph

            # Free memory:
            self._bug_localization_subgraph_edges = None
        else:
            raise Exception("Unsupported bug sample: " + str(type(bug_sample)))

    def uninitialize(self):
        self._bug_localization_subgraph_edges = None
        self._graph = None
        self._bug_report = None
        self._model_location = None


# ===============================================================================
# Prediction Neo4j Data Connector
# ===============================================================================


class DataSetPredictionNeo4j(DataSetNeo4j):

    def create_sample(self, db_version: int) -> BugSampleNeo4j:
        return BugSamplePredictionNeo4j(self, db_version)


class BugSamplePredictionNeo4j(BugSampleNeo4j):
    dataset: DataSetPredictionNeo4j

    def initialize(self, log_level: int = 0):
        if log_level >= 4:
            print("Start Loading Dictionary...")
        start_time = time.time()

        node_self_embedding = self.dataset.node_self_embedding
        node_self_embedding.load()

        if log_level >= 4:
            print("Finished Loading Dictionary:", t(start_time))
            print("Start Word Embedding ...")
            start_time = time.time()

        meta_model = self.dataset.meta_model
        self.load_bug_report(node_self_embedding, meta_model.find_bug_location_by_container())
        self.load_model_nodes(meta_model.get_types(),
                              node_self_embedding, log_level=log_level)

        # TODO: Free memory...!?
        # node_self_embedding.unload()

        if log_level >= 4:
            print("Finished Word Embedding:", t(start_time))
            print("Start Loading Locations...")
            start_time = time.time()

        for model_location_types in meta_model.get_bug_location_types():
            for model_location in self.model_nodes_types[model_location_types]:
                self.location_samples.append(LocationSamplePredictionNeo4j(model_location, model_location_types, label=model_location))

        if log_level >= 4:
            print("Finished Loading Locations:", t(start_time))

    def uninitialize(self):
        # TODO: Make field Optional!?
        self.model_nodes = {}
        self.model_nodes_types = {}
        self.bug_report_node_id = -1
        self.bug_report_nodes = {}
        self.bug_report_edges = None
        self.bug_locations = set()
        self.location_samples = []


class LocationSamplePredictionNeo4j(LocationSampleBaseNeo4j):

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        if isinstance(bug_sample, BugSamplePredictionNeo4j):
            typebased_slicing = bug_sample.dataset.typebased_slicing
            slicing = typebased_slicing.get_slicing(self.mode_location_type)

            bug_localization_subgraph_edges = bug_sample.load_subgraph_edges(self.neo4j_model_location, slicing)
            subgraph, bug_location_pair = bug_sample.load_bug_location_subgraph(
                self.neo4j_model_location, bug_localization_subgraph_edges)

            self._graph = subgraph
            self._bug_report = bug_location_pair[0]
            self._model_location = bug_location_pair[1]  # Mapped ID in subgraph
        else:
            raise Exception("Unsupported bug sample: " + str(type(bug_sample)))

    def uninitialize(self):
        self._graph = None
        self._bug_report = None
        self._model_location = None
