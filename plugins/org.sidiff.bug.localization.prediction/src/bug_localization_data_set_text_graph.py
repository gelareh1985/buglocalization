'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

import ntpath
import os
from typing import List, Optional, Tuple, Union, cast

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
from pandas.core.frame import DataFrame  # type: ignore
from stellargraph import StellarGraph  # type: ignore

from bug_localization_data_set import IBugSample, IDataSet, LocationSampleBase
from bug_localization_util import t

# ===============================================================================
# Text Graph Data Connector
# ===============================================================================


class DataSetTextGraph(IDataSet):

    def __init__(self, samples_path: str, is_negative: bool = False):
        super().__init__(is_negative)
        self.samples_path: str = samples_path

        # Collect all graphs from the given folder:
        self.nodes_column_names: List[str] = ["index", "text", "type", "tag"]
        self.edges_column_names: List[str] = ["source", "target"]
        self.list_samples(self.samples_path)

    def get_samples_text_graph(self) -> List['BugSampleTextGraph']:
        return cast(List[BugSampleTextGraph], self.bug_samples)

    def list_samples(self, folder: str):
        for filename in os.listdir(folder):
            if filename.endswith(".edgelist"):
                edge_list_path = folder + filename
                self.bug_samples.append(self.create_sample(edge_list_path))

    def create_sample(self, edge_list_path: str) -> 'BugSampleTextGraph':
        return BugSampleTextGraph(self, edge_list_path)


class BugSampleTextGraph(IBugSample):
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

    def uninitialize(self):
        self.nodes = None
        self.edges = None


class DataSetTextGraphEmbedding(DataSetTextGraph):

    def __init__(self, samples_path: str, is_negative: bool = False):
        super().__init__(samples_path, is_negative)
        self.compression = "gzip"
        self.load_feature_size()
        self.create_nodes_column_names()

    def get_samples_text_graph_embedding(self) -> List['BugSampleTextGraphEmbedding']:
        return cast(List[BugSampleTextGraphEmbedding], self.bug_samples)

    def load_feature_size(self):
        if self.bug_samples:
            self.feature_size = self.get_samples_text_graph_embedding()[0].load_feature_size()
        else:
            raise Exception('No samples found')

    def create_sample(self, edge_list_path: str):
        return BugSampleTextGraphEmbedding(self, edge_list_path)

    def create_nodes_column_names(self):
        self.nodes_column_names = []
        self.nodes_column_names.append("index")

        for feature_num in range(0, self.feature_size):
            self.nodes_column_names.append("feature" + str(feature_num))

        self.nodes_column_names.append("tag")

    def save_dataset_as_hdf(self, name: str):
        with pd.HDFStore(self.samples_path + name + ".h5", mode='w') as database:
            for bug_sample in self.get_samples_text_graph_embedding():
                database.put(key="nodes:" + bug_sample.sample_id,
                             value=bug_sample.nodes, format='fixed', complevel=9, complib='zlib')
                database.put(key="edges:" + bug_sample.sample_id,
                             value=bug_sample.edges, format='fixed', formatcomplevel=9, complib='zlib')


class BugSampleTextGraphEmbedding(BugSampleTextGraph):
    dataset: DataSetTextGraphEmbedding

    def __init__(self, dataset: DataSetTextGraph, sample_file_path: str):
        super().__init__(dataset, sample_file_path)

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

    def uninitialize(self):
        self.nodes = None
        self.edges = None
        self.bug_location_pairs = None


class DataSetTrainingTextGraphEmbedding(DataSetTextGraphEmbedding):

    def create_sample(self, edge_list_path: str) -> BugSampleTextGraphEmbedding:
        return BugSampleTrainingTextGraphEmbedding(self, edge_list_path)


class BugSampleTrainingTextGraphEmbedding(BugSampleTextGraphEmbedding):
    dataset: DataSetTrainingTextGraphEmbedding
    
    def __init__(self, dataset: DataSetTextGraph, sample_file_path: str):
        super().__init__(dataset, sample_file_path)
        self.graph: Optional[StellarGraph] = None

    def initialize(self, log_level: int = 0):
        self.load_bug_sample()

        # Convert to structural location element:
        if log_level >= 4:
            print("Generating", "negative" if self.dataset.is_negative else "positive", "sample:", self.sample_id)

        # Convert to StellarGraph:
        self.graph = StellarGraph(nodes=self.nodes, edges=self.edges)

        if log_level >= 5:
            print(self.graph.info())

        if self.bug_location_pairs is not None:
            for location_sample_idx in range(len(self.bug_location_pairs)):
                bug_report, model_location = self.bug_location_pairs[location_sample_idx]
                testcase_label = 0.0 if self.dataset.is_negative else 1.0
                locationSample = LocationSampleTextGraphEmbedding(self,
                                                                  bug_report,
                                                                  model_location,
                                                                  testcase_label,
                                                                  self.dataset.is_negative)
                self.location_samples.append(locationSample)

        # Free memory:
        super().uninitialize()
        
    def uninitialize(self):
        # Free memory:
        self.graph = None


class LocationSampleTextGraphEmbedding(LocationSampleBase):

    def __init__(self,
                 bug_sample: BugSampleTrainingTextGraphEmbedding,
                 bug_report: Union[int, str],
                 model_location: Union[int, str],
                 label: float, is_negative: bool):
        super().__init__(bug_report, model_location, label, is_negative)
        self.bug_sample: BugSampleTrainingTextGraphEmbedding = bug_sample

    def graph(self) -> StellarGraph:
        if self.bug_sample.graph is not None:
            return self.bug_sample.graph
        else:
            raise Exception("Bug location graph not initialized!")
