'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

import time
from typing import Any, Callable, Dict, List, Optional, Sequence, Set, Tuple, cast

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
from pandas.core.frame import DataFrame  # type: ignore
from py2neo import Graph, Node  # type: ignore
from stellargraph import StellarGraph  # type: ignore

import bug_localization_data_set_neo4j_queries as query
from bug_localization_data_set import IBugSample, IDataSet, ILocationSample
from bug_localization_meta_model import (MetaModel, NodeSelfEmbedding,
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
                 is_negative: bool = False,
                 list_bug_samples: bool = True):

        super().__init__(is_negative)

        # Opened connection and read bug samlpes:
        self.neo4j_config: Neo4jConfiguration = neo4j_config
        self.connectionCounter: int = 0
        self.neo4j_graph: Optional[Graph] = None
        self.connectNeo4j()

        # Meta-model configuration
        self.meta_model: MetaModel = meta_model
        self.node_self_embedding: NodeSelfEmbedding = node_self_embedding
        self.typebased_slicing: TypbasedGraphSlicing = typebased_slicing

        # Load all (uninitialized) bug samples:
        if list_bug_samples:
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
        if self.connectionCounter > 2000:
            self.closeNeo4j()  # prevent resource leaks
            self.connectionCounter = 0
        else:
            self.connectionCounter += 1

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

    def list_samples(self):
        for db_version in self.run_query(query.buggy_versions())['versions']:
            self.bug_samples.append(self.create_sample(db_version))

    def create_sample(self, db_version: int) -> 'BugSampleNeo4j':
        return BugSampleNeo4j(self, db_version)

    # # Send query to the Neo4j database and parse the result to a Pandas data frame #

    def run_query(self, query: str, parameters: Dict[str, Any] = None, index: str = None) -> DataFrame:
        df = self.connectNeo4j().run(query, parameters).to_data_frame()
        if not df.empty and index is not None:
            df.set_index(index, inplace=True)
        return df

    def run_query_to_table(self, query: str, parameters: Dict[str, Any] = None) -> List[Tuple[Any]]:
        return self.connectNeo4j().run(query, parameters).to_table()

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

        # Bug report graph:
        self.bug_report_node_id: int = -1
        self.bug_report_nodes: Dict[int, np.ndarray] = {}
        # StellarGraph requires Panda Data Frames as edges
        self.bug_report_edges: DataFrame = None
        self.bug_locations: Set[Tuple[int, str]] = set()  # node ID -> meta-type

    # # Load graphs from Neo4j # #

    def load_model_nodes(self,
                         model_meta_type_labels: List[str],
                         node_ids: List[int] = None,
                         log_level: int = 0):
        for model_meta_type_label in model_meta_type_labels:
            node_ids_per_meta_type: List[int] = []
            self.load_node_embeddings([model_meta_type_label],
                                      nodes_embeddings=self.model_nodes,
                                      to_be_embedded_node_ids=node_ids,
                                      embedded_node_ids=node_ids_per_meta_type,
                                      log_level=log_level)

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

        # Fallback
        if node_id not in nodes_trace:
            print("WARNING: No connected subgraph for node:", node_id)
            subgraph_node_id = len(subgraph_nodes_model)
            nodes_trace[node_id] = subgraph_node_id
            subgraph_nodes_model.append(self.model_nodes[node_id])

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

        if len(subgraph_nodes_model) > 1000:
            print("WARNING: Large Sub-Graph Created - Nodes:", len(subgraph_nodes_model))
            print("  Bug Report:", str(self.bug_report_node_id))
            print("  Model Node:", str(node_id))
            print("  Is Negative Sample:", str(self.dataset.is_negative))

        model_subgraph_edges_dataframe = pd.DataFrame(subgraph_edges_model, columns=self.edge_columns)
        subgraph_nodes_model_array = np.asarray(subgraph_nodes_model)

        graph = StellarGraph(nodes=subgraph_nodes_model_array,
                             edges=model_subgraph_edges_dataframe)

        return graph, (nodes_trace[self.bug_report_node_id], nodes_trace[node_id])

    def load_bug_report(self, locate_by_container: int):

        # Bug report node:
        bug_report_node_frame = self.load_dataframe(query.nodes_in_version('TracedBugReport'))
        assert len(bug_report_node_frame.index) == 1
        self.bug_report_node_id = bug_report_node_frame.index[0]

        # Bug report graph:
        bug_report_meta_type_labels = self.dataset.meta_model.get_bug_report_node_types()
        self.bug_report_nodes = {}
        self.load_node_embeddings(bug_report_meta_type_labels, self.bug_report_nodes)
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

                if not parent_nodes.empty:
                    for parent_node in parent_nodes['parents']:
                        if get_label(parent_node) in bug_location_types:
                            bug_locations_by_container.add((parent_node.identity, get_label(parent_node)))
                            break
                else:
                    print("WARNING: No parent found for node ID", model_location)

        return bug_locations_by_container

    def load_node_embeddings(self,
                             meta_type_labels: List[str],
                             nodes_embeddings: Dict[int, np.ndarray],
                             to_be_embedded_node_ids: List[int] = None,
                             embedded_node_ids: List[int] = None,
                             log_level: int = 0) -> None:
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
                for node_id, node in nodes_in_version.iterrows():
                    nodes_embedding = self.node_to_vector(node_id, node)
                    nodes_embeddings[node_id] = nodes_embedding

                    if embedded_node_ids is not None:
                        embedded_node_ids.append(node_id)

    def node_to_vector(self, node_id: int, node: pd.Series):
        self.dataset.node_self_embedding.node_to_vector(node)

    def dict_to_data_frame(self, data: Dict[int, np.ndarray], columns: str):
        return pd.DataFrame.from_dict(data, orient='index', columns=columns)

    def load_dataframe(self, query: str, parameters: dict = None, set_index: bool = True) -> DataFrame:
        default_parameter = {'db_version': self.db_version}

        if parameters is not None:
            for parameter_name, value in parameters.items():
                default_parameter[parameter_name] = value

        df = self.dataset.run_query(query, default_parameter, index="index" if set_index else None)
        return df

    def load_subgraph_edges(self, node_id: int, slicing_queries: List[str]) -> Tuple[DataFrame, Set[int]]:
        node_ids: Set[int] = set()
        node_ids.add(node_id)

        for query_nodes in slicing_queries:
            self.read_node_ids(query_nodes, node_id, node_ids)

        query_edges = query.edges_from_nodes_in_version()
        parameters = {'node_ids': list(node_ids)}
        edges = self.load_dataframe(query_edges, parameters)

        if len(node_ids) > 500:
            print("WARNING: Large Sub Graph Found: ", len(node_ids), "node", node_id)
            for query_nodes in slicing_queries:
                print(query_nodes)

        return edges, node_ids

    def read_node_ids(self, query: str, node_id: int, node_ids: Set[int]):
        parameters = {'node_id': node_id, 'db_version': self.db_version}

        for row in self.dataset.run_query_to_table(query, parameters):
            for entry in row:
                node_ids.add(entry)


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
