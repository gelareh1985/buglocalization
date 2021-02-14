'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

import ntpath
import os
import time  # @UnusedImport
from typing import Any, Dict, Generator, List, Optional, Set, Tuple, cast

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
from pandas.core.frame import DataFrame  # type: ignore
from py2neo import Graph, Node  # type: ignore  # @UnusedImport
from stellargraph import StellarGraph  # type: ignore
from stellargraph.mapper import GraphSAGELinkGenerator  # type: ignore
from tensorflow.keras.utils import TFSequence  # type: ignore # @UnusedImport

from bug_localization_meta_model import (GraphSlicing, MetaModel,
                                         NodeSelfEmbedding,
                                         TypbasedGraphSlicing)
from bug_localization_util import t


class IDataSet:

    def __init__(self, is_negative: bool = False):
        self.bug_samples: List[ISample] = []
        self.is_negative: bool = is_negative

    def get_samples(self) -> List[ISample]:
        return self.bug_samples


class ISample:

    def __init__(self, dataset: IDataSet, sample_id: str):
        self.dataset: IDataSet = dataset
        self.sample_id: str = sample_id

    def sample_generator(self, num_samples: List[int], log_level=0) -> Generator[TFSequence, None, None]:  # type: ignore
        pass

# ===============================================================================
# Text Graph Data Connector
# ===============================================================================


class DataSetTextGraph(IDataSet):
    bug_samples: List[SampleTextGraph]  # type: ignore

    def __init__(self, samples_path: str, is_negative: bool = False):
        super().__init__(is_negative)
        self.samples_path: str = samples_path

        # Collect all graphs from the given folder:
        self.nodes_column_names: List[str] = ["index", "text", "type", "tag"]
        self.edges_column_names: List[str] = ["source", "target"]
        self.list_samples(self.samples_path)

    def list_samples(self, folder: str):
        for filename in os.listdir(folder):
            if filename.endswith(".edgelist"):
                edge_list_path = folder + filename
                self.bug_samples.append(self.create_sample(edge_list_path))

    def create_sample(self, edge_list_path: str):
        return SampleTextGraph(self, edge_list_path)


class SampleTextGraph(ISample):
    dataset: DataSetTextGraph

    def __init__(self, dataset: DataSetTextGraph, sample_file_path: str):
        super().__init__(dataset, sample_file_path)

        # File naming pattern PATH/NUMBER_NAME.EXTENSION
        if sample_file_path is not None:
            self.path, filename = ntpath.split(sample_file_path)
            self.name = filename[:filename.rfind(".")]
            self.number = filename[0:filename.find("_")]

        # Nodes of the bug localization graph.
        self.nodes: DataFrame = None

        # Edges of the bug localization graph.
        self.edges: DataFrame = None

    # # Path/File Naming Conventions # #

    def get_nodes_path(self) -> str:
        return self.path + "/" + self.name + ".nodelist"

    def get_edges_path(self) -> str:
        return self.path + "/" + self.name + ".edgelist"

    def load(self):
        self.load_nodes()
        self.load_edges()

    def load_nodes(self):
        self.nodes = pd.read_table(self.get_nodes_path(), names=self.dataset.nodes_column_names, index_col="index")
        self.nodes = self.nodes.fillna("")

    def load_edges(self):
        self.edges = pd.read_table(self.get_edges_path(), names=self.dataset.edges_column_names)

    def unload(self):
        self.nodes = None
        self.edges = None


class DataSetTextGraphEmbedding(DataSetTextGraph):
    bug_samples: List[SampleTextGraphEmbedding]  # type: ignore

    def __init__(self, samples_path: str, is_negative: bool = False):
        super().__init__(samples_path, is_negative)
        self.compression = "gzip"
        self.load_feature_size()
        self.create_nodes_column_names()

    def load_feature_size(self):
        if self.bug_samples:
            self.feature_size = self.bug_samples[0].load_feature_size()
        else:
            raise Exception('No samples found')

    def create_sample(self, edge_list_path: str):
        return SampleTextGraphEmbedding(self, edge_list_path)

    def create_nodes_column_names(self):
        self.nodes_column_names = []
        self.nodes_column_names.append("index")

        for feature_num in range(0, self.feature_size):
            self.nodes_column_names.append("feature" + str(feature_num))

        self.nodes_column_names.append("tag")

    def save_dataset_as_hdf(self, name: str):
        with pd.HDFStore(self.samples_path + name + ".h5", mode='w') as database:
            for bug_sample in self.bug_samples:
                database.put(key="nodes:" + bug_sample.sample_id,
                             value=bug_sample.nodes, format='fixed', complevel=9, complib='zlib')
                database.put(key="edges:" + bug_sample.sample_id,
                             value=bug_sample.edges, format='fixed', formatcomplevel=9, complib='zlib')


class SampleTextGraphEmbedding(SampleTextGraph):
    dataset: DataSetTextGraphEmbedding

    def __init__(self, dataset: DataSetTextGraph, sample_file_path: str):
        super().__init__(dataset, sample_file_path)

        # 0 or 1 for each bug location pair: 0 -> positive sample, 1 -> negative sample
        self.testcase_labels: np.ndarray = None

        # Pairs of node IDs: (Bug Report, Location)
        self.bug_location_pairs: Optional[List[Tuple[str, str]]] = None

    # # Path/File Naming Conventions # #

    def get_nodes_path(self) -> str:
        return self.path + "/" + self.name + ".featurenodelist"

    def load_bug_sample(self, add_prefix: bool = False):

        # Create unique node IDs?
        bug_sample_name = None

        if add_prefix:
            bug_sample_name = self.number

            # different prefixes for positive and negative samples:
            if self.dataset.is_negative:
                bug_sample_name = "n" + bug_sample_name
            else:
                bug_sample_name = "p" + bug_sample_name

        # create new sample:
        self.load_nodes(bug_sample_name)
        self.load_edges(bug_sample_name)
        self.extract_bug_locations()

        # Remove the column 'tag' - it is not a numerical column:
        self.nodes.drop("tag", inplace=True, axis=1)

        # 1 for positive samples; 0 for negative samples:
        if self.bug_location_pairs is not None:
            if self.dataset.is_negative:
                self.testcase_labels = np.zeros(len(self.bug_location_pairs), dtype=np.float32)
            else:
                self.testcase_labels = np.ones(len(self.bug_location_pairs), dtype=np.float32)

        # remove bug location edges:
        self.remove_bug_location_edges()

    def load_nodes(self, name_prefix: str = None):
        self.nodes = pd.read_pickle(self.get_nodes_path(), compression=self.dataset.compression)

        if name_prefix:
            self.nodes = self.nodes.rename(index=lambda index: self.add_prefix(name_prefix, index))

    def load_edges(self, name_prefix: str = None):
        self.edges = pd.read_csv(self.get_edges_path(), sep="\t", index_col=0)

        if name_prefix:
            self.edges['source'] = self.edges['source'].apply(
                lambda index: self.add_prefix(name_prefix, index))
            self.edges['target'] = self.edges['target'].apply(
                lambda index: self.add_prefix(name_prefix, index))
            self.edges = self.edges.rename(
                index=lambda index: self.add_prefix(name_prefix, index))

    def add_prefix(self, prefix: str, index: int) -> str:
        return str(prefix) + "_" + str(index)

    def load_feature_size(self) -> int:
        # subtract: tag column
        tmp_nodes = pd.read_pickle(self.get_nodes_path(), compression=self.dataset.compression)
        return len(tmp_nodes.columns) - 1

    def extract_bug_locations(self):
        self.bug_location_pairs = []
        bug_report_node: str = ""

        for node_index, node_row in self.nodes.iterrows():
            tag = node_row["tag"]

            if (tag == "# REPORT"):
                bug_report_node = node_index
            elif (tag == "# LOCATION"):
                bug_location_node: str = node_index
                bug_location_pair: Tuple[str, str] = (bug_report_node, bug_location_node)
                self.bug_location_pairs.append(bug_location_pair)

        if not bug_report_node:
            raise Exception("Error: Bug report node not found!")

    def remove_bug_location_edges(self):
        bug_location_source = set()
        bug_location_target = set()

        for bug_location in self.bug_location_pairs:
            bug_location_source.add(bug_location[0])
            bug_location_target.add(bug_location[1])

        bug_location_edges = self.edges.loc[self.edges["source"].isin(
            bug_location_source) & self.edges["target"].isin(bug_location_target)]
        self.edges.drop(bug_location_edges.index, inplace=True)

    def unload(self):
        self.nodes = None
        self.edges = None
        self.bug_location_pairs = None
        self.testcase_labels = None


class DataSetTrainingTextGraphEmbedding(DataSetTextGraphEmbedding):
    bug_samples: List[SampleTrainingTextGraphEmbedding]  # type: ignore

    def create_sample(self, edge_list_path: str) -> ISample:
        return SampleTrainingTextGraphEmbedding(self, edge_list_path)


class SampleTrainingTextGraphEmbedding(SampleTextGraphEmbedding):
    dataset: DataSetTrainingTextGraphEmbedding

    def sample_generator(self, num_samples: List[int], log_level=0) -> Generator[TFSequence, None, None]:

        # Load each bug sample:
        self.load_bug_sample()

        if log_level >= 4:
            print("Loaded", "negative" if self.dataset.is_negative else "positive", "sample:", self.sample_id)

        if (len(self.testcase_labels) <= 0):
            return

        # Convert to StellarGraph:
        graph = StellarGraph(nodes=self.nodes, edges=self.edges)

        if log_level >= 5:
            print(graph.info())

        # Create Keras Sequence with batch size 1 for generator yield:
        graph_sage_generator = GraphSAGELinkGenerator(graph, batch_size=len(self.testcase_labels), num_samples=num_samples)
        flow = graph_sage_generator.flow(self.bug_location_pairs, self.testcase_labels)

        # Free memory:
        self.unload()

        yield flow

# ===============================================================================
# Neo4j Data Connector
# ===============================================================================


class DataSetNeo4j(IDataSet):
    bug_samples: List[SampleNeo4j]  # type: ignore

    # https://py2neo.org/v4/database.html
    # https://stellargraph.readthedocs.io/en/stable/demos/basics/loading-saving-neo4j.html

    def __init__(self, meta_model: MetaModel, node_self_embedding: NodeSelfEmbedding, typebased_slicing: TypbasedGraphSlicing,
                 host: str, port: int = None, user: str = None, password: str = None, is_negative: bool = False):

        super().__init__(is_negative)

        # Connnection info:
        self.host: str = host
        self.port: Optional[int] = port
        self.user: Optional[str] = user
        self.password: Optional[str] = password

        # Meta-model configuration
        self.meta_model = meta_model
        self.node_self_embedding = node_self_embedding
        self.typebased_slicing = self.translate_slicing_criterion(typebased_slicing)

        # Opened Connection:
        self.neo4j_graph = Graph(host=host, port=port, user=user, password=password)
        self.list_samples()

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

    def create_sample(self, db_version: int):
        return SampleNeo4j(self, db_version)

    # # Send query to the Neo4j database and parse the result to a Pandas data frame #

    def run_query(self, query: str, parameters: Dict[str, Any] = None) -> DataFrame:
        return self.neo4j_graph.run(query, parameters).to_data_frame()

    def run_query_value(self, query: str, parameters: Dict[str, Any] = None) -> Any:
        result = self.run_query(query, parameters)

        if len(result) > 0:
            return result.iloc[0, 0]

    # # Subgraph Cypher queries # #

    def query_buggy_versions(self):
        return "MATCH (n:TracedBugReport) RETURN n.__initial__version__ AS versions"

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


class SampleNeo4j(ISample):
    dataset: DataSetNeo4j

    def __init__(self, dataset: DataSetNeo4j, db_version: int):
        super().__init__(dataset, "version:" + str(db_version))
        self.db_version: int = db_version

        self.nodes_columns = None  # -> load_model_nodes()
        self.edge_columns = ['source', 'target']

        # Model graph nodes: index -> embedding
        self.model_nodes: Dict[int, np.ndarray] = {}

        # Model graph nodes: meta_type -> index
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
        bug_report_node_frame = self.load_dataframe(self.query_nodes_in_version('TracedBugReport'))
        assert len(bug_report_node_frame.index) == 1
        self.bug_report_node_id = bug_report_node_frame.index[0]
        self.bug_report_node = bug_report_node_frame.loc[self.bug_report_node_id]

        # Bug report graph:
        bug_report_meta_type_labels = ['TracedBugReport', 'BugReportComment']
        self.bug_report_nodes = self.load_node_embeddings(bug_report_meta_type_labels, embedding)
        self.bug_report_edges = self.load_dataframe(self.query_edges_in_version(
            'TracedBugReport', 'comments', 'BugReportComment'))

        # Bug locations:
        bug_location_edges = self.load_dataframe(self.query_edges_in_version('Change', 'location'))
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
                nodes_in_version = self.load_dataframe(self.query_nodes_in_version(meta_type_label))
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

        if slicing.parent_outgoing and parent_edges is not None:
            for parent in parent_edges['target']:
                tree_of_outgoing.add(parent)

        if slicing.self_outgoing:
            tree_of_outgoing.add(node_id)

        if slicing.child_outgoing and child_edges is not None:
            for child in child_edges['target']:
                tree_of_outgoing.add(child)

        tree_of_outgoing_query_parameter = {'node_ids': list(tree_of_outgoing)}
        outgoing_edges = self.load_dataframe(
            slicing.outgoing_query, tree_of_outgoing_query_parameter)
        subgraph_edges.append(outgoing_edges)

        # # Cross-Tree Incoming Edges # #
        tree_of_incoming = set()

        if slicing.parent_incoming and parent_edges is not None:
            for parent in parent_edges['target']:
                tree_of_incoming.add(parent)

        if slicing.self_incoming:
            tree_of_incoming.add(node_id)

        if slicing.child_incoming and child_edges is not None:
            for child in child_edges['target']:
                tree_of_incoming.add(child)

        tree_of_incoming_query_parameter = {'node_ids': list(tree_of_incoming)}
        incoming_eges = self.load_dataframe(slicing.incoming_query, tree_of_incoming_query_parameter)
        subgraph_edges.append(incoming_eges)

        # # Combine Edges # #
        subgraph = pd.concat(subgraph_edges)
        # print("Query Subgraph:", time.time() - t)

        return subgraph

    # # Read version information Cypher query # #

    def query_db_version_by_code_repo_version(self) -> str:
        return 'MATCH (n:TracedVersion {modelVersionID:$repo_version}) RETURN n.__initial__version__'

    def query_db_version_by_model_repo_version(self) -> str:
        return 'MATCH (n:TracedVersion {codeVersionID:$repo_version}) RETURN n.__initial__version__'

    # # Full graph Cypher queries # #

    def query_nodes_in_version(self, meta_type_label: str = '') -> str:
        if meta_type_label != '':
            meta_type_label = ':' + meta_type_label
        return 'MATCH (n' + meta_type_label + ') WHERE ' + self.dataset.query_by_version('n') + ' RETURN ID(n) AS index, n AS nodes'

    def query_edges_in_version(self, source_meta_type_label: str = '', edge_meta_type_label: str = '', target_meta_type_label: str = '') -> str:
        if source_meta_type_label != '':
            source_meta_type_label = ':' + source_meta_type_label
        if edge_meta_type_label != '':
            edge_meta_type_label = ':' + edge_meta_type_label
        if target_meta_type_label != '':
            target_meta_type_label = ':' + target_meta_type_label
        is_in_version = 'WHERE ' + self.dataset.query_by_version('r') + ' AND ' + self.dataset.query_edges_no_dangling('s', 't')

        match_query = 'MATCH (s' + source_meta_type_label + ')-[r' + edge_meta_type_label + ']->(t' + target_meta_type_label + ') ' + is_in_version
        return_query = 'RETURN ID(r) AS index, ID(s) AS source, ID(t) AS target, r AS edges'

        return match_query + ' ' + return_query


class DataSetPredictionNeo4j(DataSetNeo4j):
    bug_samples: List[SamplePredictionNeo4j]  # type: ignore

    def create_sample(self, db_version: int):
        return SamplePredictionNeo4j(self, db_version)


class SamplePredictionNeo4j(SampleNeo4j):
    dataset: DataSetPredictionNeo4j

    def sample_generator(self, num_samples: List[int], log_level=0) -> Generator[TFSequence, None, None]:
        typebased_slicing = self.dataset.typebased_slicing
        meta_model = self.dataset.meta_model

        print("Start Loading Dictionary...")
        start_time = time.time()

        node_self_embedding = self.dataset.node_self_embedding
        node_self_embedding.load()

        print("Finished Loading Dictionary:", t(start_time))

        print("Start Word Embedding ...")
        start_time = time.time()

        self.load_bug_report(node_self_embedding)
        self.load_model_nodes(meta_model.get_model_meta_type_labels(), node_self_embedding)

        # Free memory...
        # node_self_embedding.unload()

        print("Finished Word Embedding:", t(start_time))

        for meta_type_label in meta_model.get_bug_location_model_meta_type_labels():

            if log_level >= 1:
                print("Prediction for meta-type", len(self.model_nodes_types[meta_type_label]), meta_type_label)

            for model_node_id in self.model_nodes_types[meta_type_label]:
                subgraph, bug_location_pair = self.load_bug_location_subgraph(
                    model_node_id, typebased_slicing.get_slicing(meta_type_label))

                graph_sage_generator = GraphSAGELinkGenerator(subgraph, 1, num_samples=num_samples)
                flow = graph_sage_generator.flow([bug_location_pair])

                yield flow
