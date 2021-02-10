'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import ntpath
import os
import threading
import time  # @UnusedImport
from typing import *  # @UnusedWildImport

from pandas.core.frame import DataFrame  # type: ignore
from py2neo import Graph  # type: ignore
from stellargraph import StellarGraph  # type: ignore

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
    
    def unload(self):
        self.nodes = None
        self.edges = None
    
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
    
    def __init__(self, dataset:DataSet=None, sample_file_path:str=None, sample_is_negative=False):
        self.compression = "gzip"
        
        if dataset and sample_file_path:
            super().__init__(dataset, sample_file_path, sample_is_negative)
        else:
            # Positive Sample = False, Negative Sample = True
            self.is_negative = sample_is_negative
        
        # Pairs of node IDs: (Bug Report, Location)
        self.bug_location_pairs:List[Tuple[str, str]]
        
        # 0 or 1 for each bug location pair: 0 -> positive sample, 1 -> negative sample
        self.testcase_labels:np.ndarray
    
    # # Path/File Naming Conventions # #
    
    def get_nodes_path(self) -> str:
        return self.path + "/" + self.name + ".featurenodelist"
    
    def unload(self):
        self.nodes = None
        self.edges = None
        self.bug_location_pairs = None
        self.testcase_labels = None
    
    def load(self, add_prefix:bool=False):
        
        # Create unique node IDs?
        bug_sample_name = None
        
        if add_prefix:
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
            self.testcase_labels = np.zeros(len(self.bug_location_pairs), dtype=np.float32)
        else:
            self.testcase_labels = np.ones(len(self.bug_location_pairs), dtype=np.float32)
        
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


class DataSetNeo4j:
    
    # https://py2neo.org/v4/database.html
    # https://stellargraph.readthedocs.io/en/stable/demos/basics/loading-saving-neo4j.html

    def __init__(self, host:str, port:int=None, user:str=None, password:str=None):
        
        # Connnection info:
        self.host:str = host
        self.port:Optional[int] = port
        self.user:Optional[str] = user
        self.password:Optional[str] = password
        
        # Opened Connection:
        self.neo4j_graph = Graph(host=host, port=port, user=user, password=password)
        
        # Contained samples:
        self.bug_samples:List[DataSetBugSampleNeo4j] = []
        
    class GraphSlicing:
        
        def __init__(self,
                     dataset,  # : DataSetNeo4j
                     dnn_depth:int, # depth of graphSAGE layers
                     parent_levels:int=2,
                     parent_incoming:bool=False,
                     parent_outgoing:bool=False,
                     self_incoming:bool=True,
                     self_outgoing:bool=True,
                     child_levels:int=2,
                     child_incoming:bool=True,
                     child_outgoing:bool=True,
                     outgoing_distance:int=2,
                     incoming_distance:int=1):
            self.parent_levels:int = min(parent_levels, dnn_depth)
            self.parent_incoming:bool = parent_incoming
            self.parent_outgoing:bool = parent_outgoing
            self.parent_query:str = dataset.query_edges_to_parent_nodes(self.parent_levels)
            
            self.self_incoming:bool = self_incoming
            self.self_outgoing:bool = self_outgoing
            
            self.child_levels:int = min(child_levels, dnn_depth)
            self.child_incoming:bool = child_incoming
            self.child_outgoing:bool = child_outgoing
            self.child_query = dataset.query_edges_to_child_nodes(self.child_levels)
            
            self.outgoing_distance:int = min(outgoing_distance, dnn_depth)
            self.outgoing_query = dataset.query_outgoing_cross_tree_edges(self.outgoing_distance)
            
            self.incoming_distance:int = min(incoming_distance, dnn_depth)
            self.incoming_query = dataset.query_incoming_cross_tree_edges(self.incoming_distance)
            
    class TypbasedGraphSlicing:
        
        def __init__(self):
            self.type_label_to_graph_slicing:Dict[str, DataSetNeo4j.GraphSlicing] = {}
            
        def add_type(self, type_label:str, graph_slicing):
            self.type_label_to_graph_slicing[type_label] = graph_slicing
            
        def get_types(self) -> List[str]:
            return list(self.type_label_to_graph_slicing.keys())
            
        def get_slicing(self, type_label:str):
            return self.type_label_to_graph_slicing[type_label]
        
    class NodeSelfEmbedding:
        
        def filter_type(self, meta_type_label:str) -> bool:  # @UnusedVariable
            return False
        
        def filter_node(self, node:pd.Series) -> bool:  # @UnusedVariable
            return False
        
        def get_dimension(self) -> int:
            pass
        
        def get_column_names(self):
            pass
        
        def node_to_vector(self, node:pd.Series) -> np.ndarray:
            pass
        
    def get_sample(self, repo_version:str):
        new_sample = DataSetBugSampleNeo4j(self, repo_version)
        self.bug_samples.append(new_sample)
        return new_sample
     
    # # Send query to the Neo4j database and parse the result to a Pandas data frame # 
     
    def run_query(self, query:str, parameters:Dict[str, Any]=None) -> DataFrame:
        return self.neo4j_graph.run(query, parameters).to_data_frame()
    
    def run_query_value(self, query:str, parameters:Dict[str, Any]=None) -> Any:
        result = self.run_query(query, parameters)
        
        if len(result) > 0:
            return result.iloc[0, 0]
    
    # # Subgraph Cypher queries # #
    
    def query_edge_containment(self, is_value:bool):
        if is_value:
            return '__containment__:true'
        else:
            return '__containment__:false'
        
    def query_edge_container(self, is_value:bool):
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
    
    def query_path(self, edge_properties:str, start_variable:str, k=2, return_relationship_attributes=False):
        input_nodes = 'UNWIND $node_ids AS node_id '
        node_path_in_version = 'ID(' + start_variable + ')= node_id AND ' + self.query_edges_no_dangling('a', 'b')
        edge_path_in_version = 'UNWIND [edge IN relationships(path) WHERE ' + self.query_by_version('edge') + '] AS edge'
        return_id_source_target_edge = 'RETURN DISTINCT ID(edge) AS index, ID(a) AS source, ID(b) as target'
        
        if return_relationship_attributes:
            return_id_source_target_edge = return_id_source_target_edge + ', edge AS edges'
        
        path_filter = 'WHERE ' + node_path_in_version + ' WITH DISTINCT a, b, path ' + edge_path_in_version + ' ' + return_id_source_target_edge
        return input_nodes + ' MATCH path=(a)-[*0..' + str(k) + ' {' + edge_properties + '}]->(b) ' + path_filter

    def query_edges_no_dangling(self, source_variable:str, target_variable:str):
        # FIXME: Missing __last__version__ for removed nodes in data set
        return self.query_by_version(source_variable) + ' AND ' + self.query_by_version(target_variable)
    
    def query_by_version(self, variable:str) -> str:
        created_in_version = variable + '.__initial__version__ <= $db_version'
        removed_in_version = variable + '.__last__version__ >= $db_version'
        existing_in_latest_version = 'NOT EXISTS(' + variable + '.__last__version__)'
        return created_in_version + ' AND ' + '(' + removed_in_version + ' OR ' + existing_in_latest_version + ')'

    # # Some Cypher queries for testing # #
    
    def query_node_by_id(self, node_id:int) -> str:
        return 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' RETURN n'
    
    def query_edge_by_id(self, edge_id:int) -> str:
        return 'MATCH (s)-[r]-(t) WHERE ID(r)=' + str(edge_id) + ' RETURN s, r, t'
    
    def query_initial_repo_version(self, node_id:int) -> str:
        node_version = 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' WITH n.__initial__version__ AS version'
        version_node = 'MATCH (vn:TracedVersion {__initial__version__:version}) RETURN vn.modelVersionID'
        return node_version + ' ' + version_node
    
    def query_last_repo_version(self, node_id:int) -> str:
        node_version = 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' WITH n.__last__version__ AS version'
        version_node = 'MATCH (vn:TracedVersion {__last__version__:version}) RETURN vn.modelVersionID'
        return node_version + ' ' + version_node

        
class DataSetBugSampleNeo4j:
    
    def __init__(self, dataset:DataSetNeo4j, repo_version:str):
        self.dataset = dataset
        self.repo_version:str = repo_version
        
        query_parameters = {'repo_version' : self.repo_version}
        self.db_version:int = self.dataset.run_query_value(self.query_db_version_by_model_repo_version(), query_parameters)
        
        if self.db_version is None:
            self.db_version = self.dataset.run_query_value(self.query_db_version_by_code_repo_version(), query_parameters)
            
            if self.db_version is None:
                raise Exception("Version not found in database: " + repo_version)
        
        if self.db_version is not None:
            self.db_version = int(self.db_version)
        
        self.nodes_columns = None  # -> load_model_nodes()
        self.edge_columns = ['source', 'target']
       
        # Model graph nodes: index -> embedding
        self.model_nodes:Dict[int, np.ndarray] = {}
        
        # Model graph nodes: meta_type -> index
        self.model_nodes_types:Dict[str:List[int]] = {}
        
        # Bug report graph:
        self.bug_report_node:Node = None
        self.bug_report_node_id:int = -1
        self.bug_report_nodes:Dict[str, Dict[int, np.ndarray]] = None
        self.bug_report_edges:DataFrame = None # StellarGraph requires Panda Data Frames as edges
        self.bug_location_model_node_index:Set[int] = set()
        
    # # Load graphs from Neo4j # #
    
    def load_model_nodes(self, model_meta_type_labels:List[str], embedding:DataSetNeo4j.NodeSelfEmbedding):
        if self.nodes_columns is None:
            self.nodes_columns = embedding.get_column_names()
            
        for model_meta_type_label in model_meta_type_labels:
            node_ids_per_meta_type = []
            self.load_node_embeddings([model_meta_type_label], embedding, nodes_embeddings=self.model_nodes, node_ids=node_ids_per_meta_type)
            self.model_nodes_types[model_meta_type_label] = node_ids_per_meta_type
        
    def load_bug_location_subgraph(self, node_id:int, slicing:DataSetNeo4j.GraphSlicing=None) -> Union[StellarGraph, Tuple[int, int]]:
        bug_localization_subgraph_edges = self.load_subgraph_edges(node_id, slicing)
       
        nodes_trace:Dict[int, int] = {} 
        edges_ids = set()
        
        subgraph_nodes_model:List[np.ndarray] = []
        subgraph_edges_model:List[Tuple] = []
        
        #=======================================================================
        # Model Graph:
        #=======================================================================
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
            raise Exception("No (subgraph) embedding found for node: ID " + str(node_id))
        
        #=======================================================================
        # Bug Report Graph: (append to model graph)
        #=======================================================================
        
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
        
        #=======================================================================
        # StellarGraph of Bug Localization Subgraph:
        #=======================================================================
            
        model_subgraph_edges_dataframe = pd.DataFrame(subgraph_edges_model, columns=self.edge_columns)
        subgraph_nodes_model_array = np.asarray(subgraph_nodes_model)
        graph = StellarGraph(nodes=subgraph_nodes_model_array, edges=model_subgraph_edges_dataframe)
        
        return graph, (nodes_trace[self.bug_report_node_id], nodes_trace[node_id])
        
    def load_bug_report(self, embedding:DataSetNeo4j.NodeSelfEmbedding):
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
        self.bug_report_edges = self.load_dataframe(self.query_edges_in_version('TracedBugReport', 'comments', 'BugReportComment'))
        
        # Bug locations:
        bug_location_edges = self.load_dataframe(self.query_edges_in_version('Change', 'location'))
        self.bug_report_edges.drop(['edges'], axis=1, inplace=True)
        
        for index, edge in bug_location_edges.iterrows():  # @UnusedVariable
            # location edges point at model elements:
            self.bug_location_model_node_index.add(edge['target'])
    
    def load_node_embeddings(self, 
                             meta_type_labels:List[str], 
                             embedding:DataSetNeo4j.NodeSelfEmbedding, 
                             nodes_embeddings:Dict[int,np.ndarray]={}, 
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
    
    def dict_to_data_frame(self, data:Dict[int, np.ndarray], columns:str):
        return pd.DataFrame.from_dict(data, orient='index', columns=columns)
    
    def load_dataframe(self, query:str, parameters:dict=None) -> DataFrame:
        default_parameter = {'db_version' : self.db_version}
        
        if parameters is not None:
            for parameter_name, value in parameters.items():
                default_parameter[parameter_name] = value
        
        df = self.dataset.run_query(query, default_parameter)
        
        if not df.empty:
            df.set_index("index", inplace=True)
            
        return df
    
    def load_subgraph_edges(self, node_id:int, slicing:DataSetNeo4j.GraphSlicing=None) -> DataFrame:
        # t = time.time()
        
        if slicing is None:
            slicing = DataSetNeo4j.GraphSlicing(self.dataset)
        
        subgraph_edges = []    
        node_id_query_parameter = {'node_ids' : [node_id]}
        
        # # Containment Tree # #
        parent_edges = None
        child_edges = None
        
        if slicing.parent_levels > 0:
            parent_edges = self.load_dataframe(slicing.parent_query, node_id_query_parameter)
            subgraph_edges.append(parent_edges)
        
        if slicing.child_levels > 0:
            child_edges = self.load_dataframe(slicing.child_query, node_id_query_parameter)
            subgraph_edges.append(child_edges)
        
        # # Cross-Tree Outgoing Edges # #
        tree_of_outgoing = set()
        
        if slicing.parent_outgoing and not parent_edges is not None:
            for parent in parent_edges['target']:
                tree_of_outgoing.add(parent)
                
        if slicing.self_outgoing:
            tree_of_outgoing.add(node_id)
        
        if slicing.child_outgoing and not child_edges is not None:
            for child in child_edges['target']:
                tree_of_outgoing.add(child)
        
        tree_of_outgoing_query_parameter = {'node_ids' : list(tree_of_outgoing)}
        outgoing_edges = self.load_dataframe(slicing.outgoing_query, tree_of_outgoing_query_parameter)
        subgraph_edges.append(outgoing_edges)
        
        # # Cross-Tree Incoming Edges # #
        tree_of_incoming = set()
        
        if slicing.parent_incoming and not parent_edges is not None:
            for parent in parent_edges['target']:
                tree_of_incoming.add(parent)
                
        if slicing.self_incoming:
            tree_of_incoming.add(node_id)
        
        if slicing.child_incoming and not child_edges is not None:
            for child in child_edges['target']:
                tree_of_incoming.add(child)
        
        tree_of_incoming_query_parameter = {'node_ids' : list(tree_of_incoming)}
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
    
    def query_nodes_in_version(self, meta_type_label:str='') -> str:
        if meta_type_label != '':
            meta_type_label = ':' + meta_type_label
        return 'MATCH (n' + meta_type_label + ') WHERE ' + self.dataset.query_by_version('n') + ' RETURN ID(n) AS index, n AS nodes'
    
    def query_edges_in_version(self, source_meta_type_label:str='', edge_meta_type_label:str='', target_meta_type_label:str='') -> str:
        if source_meta_type_label != '':
            source_meta_type_label = ':' + source_meta_type_label
        if edge_meta_type_label != '':
            edge_meta_type_label = ':' + edge_meta_type_label
        if target_meta_type_label != '':
            target_meta_type_label = ':' + target_meta_type_label
        is_in_version = 'WHERE ' + self.dataset.query_by_version('r') + ' AND ' + self.dataset.query_edges_no_dangling('s', 't')
        return 'MATCH (s' + source_meta_type_label + ')-[r' + edge_meta_type_label + ']->(t' + target_meta_type_label + ') ' + is_in_version + ' RETURN ID(r) AS index, ID(s) AS source, ID(t) AS target, r AS edges'
