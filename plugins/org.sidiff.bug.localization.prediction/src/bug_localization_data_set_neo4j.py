'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

import ntpath
import os
import time  # @UnusedImport
from typing import Any, Dict, List, Optional, Set, Tuple, cast

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
from pandas.core.frame import DataFrame  # type: ignore
from py2neo import Graph, Node  # type: ignore  # @UnusedImport
from stellargraph import StellarGraph  # type: ignore

from bug_localization_data_set import IBugSample, IDataSet, ILocationSample
from bug_localization_meta_model import (GraphSlicing, MetaModel,
                                         NodeSelfEmbedding,
                                         TypbasedGraphSlicing)
from bug_localization_util import t


# ===============================================================================
# Neo4j Data Connector
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

    def __init__(self, meta_model: MetaModel, node_self_embedding: NodeSelfEmbedding, typebased_slicing: TypbasedGraphSlicing,
                 neo4j_config: Neo4jConfiguration, is_negative: bool = False):

        super().__init__(is_negative)

        # Connnection info:
        self.neo4j_config = neo4j_config

        # Meta-model configuration
        self.meta_model = meta_model
        self.node_self_embedding = node_self_embedding
        self.typebased_slicing = self.translate_slicing_criterion(typebased_slicing)

        # Opened Connection:
        self.neo4j_graph = Graph(host=neo4j_config.neo4j_host, port=neo4j_config.neo4j_port,
                                 user=neo4j_config.neo4j_user, password=neo4j_config.neo4j_password)
        self.list_samples()

    def get_samples_neo4j(self) -> List['BugSampleNeo4j']:
        return cast(List[BugSampleNeo4j], self.bug_samples)

    class TypbasedGraphSlicingNeo4j(TypbasedGraphSlicing):

        def get_slicing(self, type_label: str) -> DataSetNeo4j.GraphSlicingNeo4j:
            return cast(DataSetNeo4j.GraphSlicingNeo4j, self.type_label_to_graph_slicing[type_label])

    class GraphSlicingNeo4j(GraphSlicing):

        def __init__(self, dataset, slicing_criterion: GraphSlicing):
            self.parent_levels: int = slicing_criterion.parent_levels
            self.parent_incoming: bool = slicing_criterion.parent_incoming
            self.parent_outgoing: bool = slicing_criterion.parent_outgoing
            self.parent_query: str = dataset.query_edges_to_parent_nodes(self.parent_levels)

            self.self_incoming: bool = slicing_criterion.self_incoming
            self.self_outgoing: bool = slicing_criterion.self_outgoing

            self.child_levels: int = slicing_criterion.child_levels
            self.child_incoming: bool = slicing_criterion.child_incoming
            self.child_outgoing: bool = slicing_criterion.child_outgoing
            self.child_query = dataset.query_edges_to_child_nodes(self.child_levels)

            self.outgoing_distance: int = slicing_criterion.outgoing_distance
            self.outgoing_query = dataset.query_outgoing_cross_tree_edges(self.outgoing_distance)

            self.incoming_distance: int = slicing_criterion.incoming_distance
            self.incoming_query = dataset.query_incoming_cross_tree_edges(self.incoming_distance)

    def translate_slicing_criterion(self, typebased_slicing: TypbasedGraphSlicing) -> DataSetNeo4j.TypbasedGraphSlicingNeo4j:
        neo4j_typebased_slicing = DataSetNeo4j.TypbasedGraphSlicingNeo4j()

        for type_label in typebased_slicing.get_types():
            slicing_criterion = typebased_slicing.get_slicing(type_label)
            neo4j_slicing = self.GraphSlicingNeo4j(self, slicing_criterion)
            neo4j_typebased_slicing.add_type(type_label, neo4j_slicing)

        return neo4j_typebased_slicing

    def list_samples(self):
        for db_version in self.run_query(self.query_buggy_versions())['versions']:
            self.bug_samples.append(self.create_sample(db_version))

    def create_sample(self, db_version: int) -> 'BugSampleNeo4j':
        return BugSampleNeo4j(self, db_version)

    # # Send query to the Neo4j database and parse the result to a Pandas data frame #

    def run_query(self, query: str, parameters: Dict[str, Any] = None) -> DataFrame:
        return self.neo4j_graph.run(query, parameters).to_data_frame()

    def run_query_value(self, query: str, parameters: Dict[str, Any] = None) -> Any:
        result = self.run_query(query, parameters)

        if len(result) > 0:
            return result.iloc[0, 0]

    # # Subgraph Cypher queries # #

    def query_buggy_versions(self):
        return "MATCH (b:TracedBugReport)-[:modelLocations]->(c:Change)-[:location]->(e) RETURN DISTINCT b.__initial__version__ AS versions"

    def query_edge_containment(self, is_value: bool):
        if is_value:
            return '__containment__:true'
        else:
            return '__containment__:false'

    def query_edge_container(self, is_value: bool):
        if is_value:
            return '__container__:true'
        else:
            return '__container__:false'

    def query_edges_to_parent_nodes(self, k=2) -> str:
        return self.query_path(self.query_edge_containment(True), 'b', k)

    def query_edges_to_child_nodes(self, k=2) -> str:
        return self.query_path(self.query_edge_containment(True), 'a', k)

    def query_outgoing_cross_tree_edges(self, k=2) -> str:
        return self.query_path(self.query_edge_containment(False) + ', ' + self.query_edge_container(False), 'a', k)

    def query_incoming_cross_tree_edges(self, k=1) -> str:
        return self.query_path(self.query_edge_containment(False) + ', ' + self.query_edge_container(False), 'b', k)

    def query_path(self, edge_properties: str, start_variable: str, k=2, return_relationship_attributes=False):
        input_nodes = 'UNWIND $node_ids AS node_id '
        node_path_in_version = 'ID(' + start_variable + ')= node_id AND ' + self.query_edges_no_dangling('a', 'b')
        edge_path_in_version = 'UNWIND [edge IN relationships(path) WHERE ' + self.query_by_version('edge') + '] AS edge'
        return_id_source_target_edge = 'RETURN DISTINCT ID(edge) AS index, ID(a) AS source, ID(b) as target'

        if return_relationship_attributes:
            return_id_source_target_edge = return_id_source_target_edge + ', edge AS edges'

        path_filter = 'WHERE ' + node_path_in_version + ' WITH DISTINCT a, b, path ' + edge_path_in_version + ' ' + return_id_source_target_edge
        return input_nodes + ' MATCH path=(a)-[*0..' + str(k) + ' {' + edge_properties + '}]->(b) ' + path_filter

    def query_edges_no_dangling(self, source_variable: str, target_variable: str):
        # FIXME: Missing __last__version__ for removed nodes in data set
        return self.query_by_version(source_variable) + ' AND ' + self.query_by_version(target_variable)

    def query_by_version(self, variable: str) -> str:
        created_in_version = variable + '.__initial__version__ <= $db_version'
        removed_in_version = variable + '.__last__version__ >= $db_version'
        existing_in_latest_version = 'NOT EXISTS(' + variable + '.__last__version__)'
        return created_in_version + ' AND ' + '(' + removed_in_version + ' OR ' + existing_in_latest_version + ')'

    # # Some Cypher queries for testing # #

    def query_node_by_id(self, node_id: int) -> str:
        return 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' RETURN n'

    def query_nodes_by_ids(self, return_query: str) -> str:
        # $node_ids: List[int]
        return 'MATCH (n) WHERE ID(n) IN $node_ids ' + return_query

    def query_edge_by_id(self, edge_id: int) -> str:
        return 'MATCH (s)-[r]-(t) WHERE ID(r)=' + str(edge_id) + ' RETURN s, r, t'

    def query_initial_repo_version(self, node_id: int) -> str:
        node_version = 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' WITH n.__initial__version__ AS version'
        version_node = 'MATCH (vn:TracedVersion {__initial__version__:version}) RETURN vn.modelVersionID'
        return node_version + ' ' + version_node

    def query_last_repo_version(self, node_id: int) -> str:
        node_version = 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' WITH n.__last__version__ AS version'
        version_node = 'MATCH (vn:TracedVersion {__last__version__:version}) RETURN vn.modelVersionID'
        return node_version + ' ' + version_node

    # # Read version information Cypher query # #

    def query_db_version_by_code_repo_version(self) -> str:
        return 'MATCH (n:TracedVersion {modelVersionID:$repo_version}) RETURN n.__initial__version__'

    def query_db_version_by_model_repo_version(self) -> str:
        return 'MATCH (n:TracedVersion {codeVersionID:$repo_version}) RETURN n.__initial__version__'

    def query_code_repo_version_by_db_version(self) -> str:
        return 'MATCH (n:TracedVersion {__initial__version__:$db_version}) RETURN n.codeVersionID'

    def query_model_repo_version_by_db_version(self) -> str:
        return 'MATCH (n:TracedVersion {__initial__version__:$db_version}) RETURN n.modelVersionID'

    # # Read property/attribute Cypher query # #

    def query_property_value_in_version(self, meta_type_label: str, property_name: str) -> str:
        # $db_version: int
        return 'MATCH (n:' + meta_type_label + ') WHERE ' + self.query_by_version('n') + ' RETURN n.' + property_name

    # # Full graph Cypher queries # #

    def query_nodes_in_version(self, meta_type_label: str = '') -> str:
        if meta_type_label != '':
            meta_type_label = ':' + meta_type_label
        return 'MATCH (n' + meta_type_label + ') WHERE ' + self.query_by_version('n') + ' RETURN ID(n) AS index, n AS nodes'

    def query_edges_in_version(self, source_meta_type_label: str = '', edge_meta_type_label: str = '', target_meta_type_label: str = '') -> str:
        if source_meta_type_label != '':
            source_meta_type_label = ':' + source_meta_type_label
        if edge_meta_type_label != '':
            edge_meta_type_label = ':' + edge_meta_type_label
        if target_meta_type_label != '':
            target_meta_type_label = ':' + target_meta_type_label
        is_in_version = 'WHERE ' + self.query_by_version('r') + ' AND ' + self.query_edges_no_dangling('s', 't')

        match_query = 'MATCH (s' + source_meta_type_label + ')-[r' + edge_meta_type_label + ']->(t' + target_meta_type_label + ') ' + is_in_version
        return_query = 'RETURN ID(r) AS index, ID(s) AS source, ID(t) AS target, r AS edges'

        return match_query + ' ' + return_query


class BugSampleNeo4j(IBugSample):
    dataset: DataSetNeo4j

    def __init__(self, dataset: DataSetNeo4j, db_version: int):
        super().__init__(dataset, "version:" + str(db_version))
        self.db_version: int = db_version

        self.nodes_columns = None  # -> load_model_nodes()
        self.edge_columns = ['source', 'target']

        # Model graph nodes: index -> embedding
        self.model_nodes: Dict[int, np.ndarray] = {}

        # Model graph nodes: List[(id:type)]
        self.model_nodes_types: Dict[str, List[int]] = {}

        # Bug report graph:
        self.bug_report_node: Node = None
        self.bug_report_node_id: int = -1
        self.bug_report_nodes: Dict[int, Dict[int, np.ndarray]] = {}
        # StellarGraph requires Panda Data Frames as edges
        self.bug_report_edges: DataFrame = None
        self.bug_location_model_node_index: Set[int] = set()

    # # Load graphs from Neo4j # #

    def load_model_nodes(self, model_meta_type_labels: List[str], embedding: NodeSelfEmbedding):
        if self.nodes_columns is None:
            self.nodes_columns = embedding.get_column_names()

        for model_meta_type_label in model_meta_type_labels:
            node_ids_per_meta_type: List[int] = []
            self.load_node_embeddings([model_meta_type_label], embedding,
                                      nodes_embeddings=self.model_nodes,
                                      node_ids=node_ids_per_meta_type)

            self.model_nodes_types[model_meta_type_label] = node_ids_per_meta_type

    def load_bug_location_subgraph(self, node_id: int, slicing: DataSetNeo4j.GraphSlicingNeo4j) -> Tuple[StellarGraph, Tuple[int, int]]:
        bug_localization_subgraph_edges = self.load_subgraph_edges(node_id, slicing)

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

    def load_bug_report(self, embedding: NodeSelfEmbedding):
        if self.nodes_columns is None:
            self.nodes_columns = embedding.get_column_names()

        # Bug report node:
        bug_report_node_frame = self.load_dataframe(self.dataset.query_nodes_in_version('TracedBugReport'))
        assert len(bug_report_node_frame.index) == 1
        self.bug_report_node_id = bug_report_node_frame.index[0]
        self.bug_report_node = bug_report_node_frame.loc[self.bug_report_node_id]

        # Bug report graph:
        bug_report_meta_type_labels = ['TracedBugReport', 'BugReportComment']
        self.bug_report_nodes = self.load_node_embeddings(bug_report_meta_type_labels, embedding)
        self.bug_report_edges = self.load_dataframe(self.dataset.query_edges_in_version(
            'TracedBugReport', 'comments', 'BugReportComment'))

        # Bug locations:
        bug_location_edges = self.load_dataframe(self.dataset.query_edges_in_version('Change', 'location'))
        self.bug_report_edges.drop(['edges'], axis=1, inplace=True)

        for index, edge in bug_location_edges.iterrows():  # @UnusedVariable
            # location edges point at model elements:
            self.bug_location_model_node_index.add(edge['target'])

    def load_node_embeddings(self,
                             meta_type_labels: List[str],
                             embedding: NodeSelfEmbedding,
                             nodes_embeddings: Dict[int, np.ndarray] = {},
                             node_ids=None) -> Dict[int, np.ndarray]:

        for meta_type_label in meta_type_labels:
            if not embedding.filter_type(meta_type_label):
                nodes_in_version = self.load_dataframe(self.dataset.query_nodes_in_version(meta_type_label))
                print(meta_type_label + ': ' + str(len(nodes_in_version.index)))

                if not nodes_in_version.empty:
                    if not embedding.filter_node(nodes_in_version):
                        for index, node in nodes_in_version.iterrows():
                            nodes_embedding = embedding.node_to_vector(node)
                            nodes_embeddings[index] = nodes_embedding

                            if node_ids is not None:
                                node_ids.append(index)

        return nodes_embeddings

    def dict_to_data_frame(self, data: Dict[int, np.ndarray], columns: str):
        return pd.DataFrame.from_dict(data, orient='index', columns=columns)

    def load_dataframe(self, query: str, parameters: dict = None) -> DataFrame:
        default_parameter = {'db_version': self.db_version}

        if parameters is not None:
            for parameter_name, value in parameters.items():
                default_parameter[parameter_name] = value

        df = self.dataset.run_query(query, default_parameter)

        if not df.empty:
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
            for parent in parent_edges['target']:
                tree_of_outgoing.add(parent)

        if slicing.self_outgoing:
            tree_of_outgoing.add(node_id)

        if slicing.child_outgoing and child_edges is not None and not child_edges.empty:
            for child in child_edges['target']:
                tree_of_outgoing.add(child)

        tree_of_outgoing_query_parameter = {'node_ids': list(tree_of_outgoing)}
        outgoing_edges = self.load_dataframe(
            slicing.outgoing_query, tree_of_outgoing_query_parameter)
        subgraph_edges.append(outgoing_edges)

        # # Cross-Tree Incoming Edges # #
        tree_of_incoming = set()

        if slicing.parent_incoming and parent_edges is not None and not parent_edges.empty:
            for parent in parent_edges['target']:
                tree_of_incoming.add(parent)

        if slicing.self_incoming:
            tree_of_incoming.add(node_id)

        if slicing.child_incoming and child_edges is not None and not child_edges.empty:
            for child in child_edges['target']:
                tree_of_incoming.add(child)

        tree_of_incoming_query_parameter = {'node_ids': list(tree_of_incoming)}
        incoming_eges = self.load_dataframe(slicing.incoming_query, tree_of_incoming_query_parameter)
        subgraph_edges.append(incoming_eges)

        # # Combine Edges # #
        subgraph = pd.concat(subgraph_edges)
        # print("Query Subgraph:", time.time() - t)

        return subgraph


class DataSetPredictionNeo4j(DataSetNeo4j):

    def create_sample(self, db_version: int) -> BugSampleNeo4j:
        return BugSamplePredictionNeo4j(self, db_version)


class BugSamplePredictionNeo4j(BugSampleNeo4j):
    dataset: DataSetPredictionNeo4j

    def initialize(self, log_level: int = 0):
        print("Start Loading Dictionary...")
        start_time = time.time()

        node_self_embedding = self.dataset.node_self_embedding
        node_self_embedding.load()

        print("Finished Loading Dictionary:", t(start_time))
        print("Start Word Embedding ...")
        start_time = time.time()

        meta_model = self.dataset.meta_model
        self.load_bug_report(node_self_embedding)
        self.load_model_nodes(meta_model.get_model_meta_type_labels(), node_self_embedding)

        # Free memory...
        # node_self_embedding.unload()

        print("Finished Word Embedding:", t(start_time))
        print("Start Loading Locations...")
        start_time = time.time()

        for model_location_types in meta_model.get_bug_location_model_meta_type_labels():
            for model_location in self.model_nodes_types[model_location_types]:
                self.location_samples.append(LocationSamplePredictionNeo4j(model_location, model_location_types))

        print("Finished Loading Locations:", t(start_time))


class LocationSamplePredictionNeo4j(ILocationSample):

    def __init__(self, model_location: int, mode_location_type: str, is_negative: bool = None):
        self.neo4j_model_location = model_location
        self.mode_location_type = mode_location_type

        self._graph: Optional[StellarGraph] = None
        self._model_location: Optional[int] = None
        self._bug_report: Optional[int] = None
        self._is_negative: Optional[bool] = is_negative

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        if isinstance(bug_sample, BugSamplePredictionNeo4j):
            typebased_slicing = bug_sample.dataset.typebased_slicing
            subgraph, bug_location_pair = bug_sample.load_bug_location_subgraph(
                self.neo4j_model_location, typebased_slicing.get_slicing(self.mode_location_type))

            self._graph = subgraph
            self._bug_report = bug_location_pair[0]
            self._model_location = bug_location_pair[1]  # Mapped ID in subgraph
        else:
            raise Exception("Unsupported bug sample: " + str(type(bug_sample)))

    def label(self) -> int:
        return self.neo4j_model_location

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

    def is_negative(self) -> bool:
        if self._is_negative is not None:
            return self._is_negative
        else:
            raise Exception("Negative sample flag not set. Location sample not initialized?")
