import ntpath
import os
from typing import List, Tuple
import threading

from pandas.core.frame import DataFrame  # type: ignore

import numpy as np  # type: ignore
import pandas as pd  # type: ignore


class DataSet:

    def __init__(self, positve_samples_path:str, negative_samples_path:str):
        self.positve_samples_path:str = positve_samples_path
        self.negative_samples_path:str = negative_samples_path
        
        # Collect all graphs from the given folder:
        self.positive_bug_samples = self.get_samples(self.positve_samples_path)
        self.negative_bug_samples = self.get_samples(self.negative_samples_path, sample_is_negative=True)
        
        self.nodes_column_names:List[str] = ["index", "text", "type", "tag"]
        self.edges_column_names = ["source", "target"]
    
    def get_nodes_column_names(self) -> List[str]:
        return self.nodes_column_names
    
    def get_edges_column_names(self) -> List[str]:
        return self.edges_column_names
    
    def get_sample(self, edge_list_path:str, sample_is_negative:bool):
        return DataSetBugSample(self, edge_list_path, sample_is_negative)
    
    def get_samples(self, folder:str, sample_is_negative=False, count:int=-1):
        samples = []
        
        for filename in os.listdir(folder):
            if filename.endswith(".edgelist"):
                edge_list_path = folder + filename
                samples.append(self.get_sample(edge_list_path, sample_is_negative))
            if count != -1 and len(samples) == count:
                break
        
        return samples


class DataSetBugSample:
    
    def __init__(self, dataset:DataSet, sample_file_path:str, sample_is_negative=False):
        self.dataset:DataSet = dataset
        
        # File naming pattern PATH/NUMBER_NAME.EXTENSION
        self.path, filename = ntpath.split(sample_file_path)
        self.name = filename[:filename.rfind(".")]
        self.number = filename[0:filename.find("_")]
        
        # Positive Sample = False, Negative Sample = True
        self.is_negative = sample_is_negative
        
        # Nodes of the bug localization graph.
        self.nodes:DataFrame
        
        # Edges of the bug localization graph.
        self.edges:DataFrame
        
    # # Path/File Naming Conventions # #
    
    def get_nodes_path(self) -> str:
        return self.path + "/" + self.name + ".nodelist"
        
    def get_edges_path(self) -> str:
        return self.path + "/" + self.name + ".edgelist"
    
    def load(self):
        self.load_nodes()
        self.load_edges()
    
    def load_nodes(self):
        self.nodes = pd.read_table(self.get_nodes_path(), names=self.dataset.get_nodes_column_names(), index_col="index")
        self.nodes = self.nodes.fillna("")
        
    def load_feature_size(self) -> int:
        # Load without header
        tmp_nodes = pd.read_table(self.get_nodes_path())
        # subtract: index and tag column
        return len(tmp_nodes.columns) - 2
    
    def load_edges(self):
        self.edges = pd.read_table(self.get_edges_path(), names=self.dataset.get_edges_column_names())
    

class DataSetEmbedding(DataSet):
    
    def __init__(self, positve_samples_path:str, negative_samples_path:str):
        super().__init__(positve_samples_path, negative_samples_path)
        self.feature_size = self.load_feature_size()
        
    def load_feature_size(self) -> int:
        if self.positive_bug_samples:
            return self.positive_bug_samples[0].load_feature_size()
        else:
            raise Exception('No samples found')
        
    def get_sample(self, edge_list_path:str, sample_is_negative:bool):
        return DataSetBugSampleEmbedding(self, edge_list_path, sample_is_negative)
    
    def get_column_names(self) -> List[str]:
        if not self.nodes_column_names:
            self.nodes_column_names = []
            self.nodes_column_names.append("index")
            
            for feature_num in range(0, self.feature_size):
                self.nodes_column_names.append("feature" + str(feature_num))
            
            self.nodes_column_names.append("tag")
        
        return self.nodes_column_names
    
    def load_dataset_into_hdf(self):
        
        def save_positive_samples():
            with pd.HDFStore(self.path + "positivesamples.h5", mode='w') as positive_bug_samples_database:
                for positive_bug_sample in self.positive_bug_samples:
                    self.load_into_hdf(positive_bug_samples_database, positive_bug_sample.name, positive_bug_sample)
        
        threading.Thread(target=save_positive_samples)
        
        def save_negative_samples():        
            with pd.HDFStore(self.path + "negativesamples.h5", mode='w') as negative_bug_samples_database:
                for negative_bug_sample in self.negative_bug_samples:
                    self.load_into_hdf(negative_bug_samples_database, negative_bug_sample.name, negative_bug_sample)
                    
        threading.Thread(target=save_negative_samples)
        
    def load_sample_into_hdf(self, database:pd.HDFStore, key:str, bug_sample:DataSetBugSample):
        bug_sample.load_nodes()
        bug_sample.load_edges()
        
        # TODO: Optimize column types?
        # bug_sample.nodes.convert_dtypes().dtypes
        # bug_sample.edges.convert_dtypes().dtypes
        
        database.put(key=key, value=bug_sample.nodes, format='fixed', complevel=9, complib='zlib')
        database.put(key=key, value=bug_sample.edges, format='fixed', formatcomplevel=9, complib='zlib')


class DataSetBugSampleEmbedding(DataSetBugSample):
    
    def __init__(self, dataset:DataSet, sample_file_path:str, sample_is_negative=False):
        self.compression = "gzip"
        super().__init__(dataset, sample_file_path, sample_is_negative)
        
        # Pairs of node IDs: (Bug Report, Location)
        self.bug_location_pairs:List[Tuple[str, str]]
        
        # 0 or 1 for each bug location pair: 0 -> positive sample, 1 -> negative sample
        self.testcase_labels:np.ndarray
    
    # # Path/File Naming Conventions # #
    
    def get_nodes_path(self) -> str:
        return self.path + "/" + self.name + ".featurenodelist"
    
    def load(self):
        
        # Read sample and create unique node IDs:
        bug_sample_name = self.number
        
        # different prefixes for positive and negative samples:
        if self.is_negative:
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
        if self.is_negative:
            self.testcase_labels = np.zeros(len(self.bug_location_pairs))
        else:
            self.testcase_labels = np.ones(len(self.bug_location_pairs))
        
        # remove bug location edges:
        self.remove_bug_location_edges()
        
    def load_nodes(self, name_prefix:str=None):
        self.nodes = pd.read_pickle(self.get_nodes_path(), compression=self.compression)
        
        if name_prefix:
            self.nodes = self.nodes.rename(index=lambda index: self.add_prefix(name_prefix, index))
            
    def load_edges(self, name_prefix:str=None):
        self.edges = pd.read_csv(self.get_edges_path(), sep="\t", index_col=0)
        
        if name_prefix:
            self.edges['source'] = self.edges['source'].apply(lambda index: self.add_prefix(name_prefix, index))
            self.edges['target'] = self.edges['target'].apply(lambda index: self.add_prefix(name_prefix, index))
            self.edges = self.edges.rename(index=lambda index: self.add_prefix(name_prefix, index))
            
    def add_prefix(self, prefix:str, index:int) -> str:
        return str(prefix) + "_" + str(index)
            
    def load_feature_size(self) -> int:
        # subtract: tag column
        tmp_nodes = pd.read_pickle(self.get_nodes_path(), compression=self.compression)
        return len(tmp_nodes.columns) - 1
    
    def extract_bug_locations(self):
        self.bug_location_pairs = []
        bug_report_node:str = ""
        
        for node_index, node_row in self.nodes.iterrows():
            tag = node_row["tag"]
            
            if (tag == "# REPORT"):
                bug_report_node = node_index
            elif (tag == "# LOCATION"):
                bug_location_node:str = node_index
                bug_location_pair:Tuple[str, str] = (bug_report_node, bug_location_node)
                self.bug_location_pairs.append(bug_location_pair)
        
        if not bug_report_node:
            raise Exception("Error: Bug report node not found!")
    
    def remove_bug_location_edges(self):
        bug_location_source = set()
        bug_location_target = set()

        for bug_location in self.bug_location_pairs:
            bug_location_source.add(bug_location[0])
            bug_location_target.add(bug_location[1])
            
        bug_location_edges = self.edges.loc[self.edges["source"].isin(bug_location_source) & self.edges["target"].isin(bug_location_target)]
        self.edges.drop(bug_location_edges.index, inplace=True)
