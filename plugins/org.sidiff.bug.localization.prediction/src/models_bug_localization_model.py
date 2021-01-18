import ntpath
import os
from typing import Union, List, Tuple

from pandas.core.frame import DataFrame  # type: ignore
from stellargraph.layer import GraphSAGE, link_classification  # type: ignore
from stellargraph.mapper import GraphSAGELinkGenerator  # type: ignore
from tensorflow import keras  # type: ignore

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
import stellargraph as sg  # type: ignore

###################################################################################################
# Environmental Information
###################################################################################################

positve_samples_path:str = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\positivesamples_5000/"
negative_samples_path:str = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\negativesamples_5000/"

binary_featurenodelist:bool = True

###################################################################################################
# Data Processing
###################################################################################################

class DataSetSample:
    
    def __init__(self):
        
        # Nodes of the bug localization graph (excerpt).
        self.sample_nodes:List[DataFrame] = None
        
        # Edges of the bug localization graph (excerpt).
        self.sample_edges:List[DataFrame] = None
        
        # Pairs of node IDs: (Bug Report, Location)
        self.sample_bug_location_pairs:List[Tuple[str, str]] = None
        
        # 0 or 1 for each bug location pair: 0 -> positive sample, 1 -> negative sample
        self.sample_testcase_labels:np.ndarray = None
        
        
class DataSetLoader:
    
    def __init__(self, positve_samples_path:str, negative_samples_path:str, binary_featurenodelist:bool):
        self.positve_samples_path:str = positve_samples_path
        self.negative_samples_path:str = negative_samples_path
        self.binary_featurenodelist:bool = binary_featurenodelist
        
        self.feature_size:int = self.load_feature_size()
        self.positive_sample_size:int = len(self.get_sample_paths(self.positve_samples_path))
        self.negative_sample_size:int = len(self.get_sample_paths(self.negative_samples_path))
    
    def load_dataset(self, start_with_sample:int=0, number_of_sample:int=-1, log:bool=False) -> List[DataSetSample]:
    
        # Collect all graphs from the given folder:
        
        # Read directory:        
        files_positive_samples = self.get_sample_paths(self.positve_samples_path)
        files_negative_samples = self.get_sample_paths(self.negative_samples_path)
        
        # Mix positive and negative samples to create a full set of samples 
        # that can be split to create a training and validation set of samples:
        all_samples = []
        index = 0
        
        while index < len(files_positive_samples) and index < len(files_negative_samples):
            if index >= start_with_sample:
                if index < len(files_positive_samples):
                    all_samples.append(files_positive_samples[index])
                if index < len(files_negative_samples):
                    all_samples.append(files_negative_samples[index])
                    
            index += 1
            
            if number_of_sample != -1 and len(all_samples) == number_of_sample:
                break
        
        # Read all samples and create unique node IDs:
        dataset_samples = []
        
        for filepath in all_samples:
            if filepath.endswith(".edgelist"):
                graph_path, filename = ntpath.split(filepath)
                graph_filename = filename[:filename.rfind(".")]
                edge_list_path = self.get_edge_list_path(graph_path, graph_filename)
                node_list_path = self.get_featurenodelist_path(graph_path, graph_filename)
                graph_number = graph_filename[0:graph_filename.find("_")]
                
                # different prefixes for positive and negative samples:
                is_positive_sample = False
                
                if self.positve_samples_path.startswith(graph_path):
                    graph_number = "p" + graph_number
                    is_positive_sample = True
                else:
                    graph_number = "n" + graph_number
                    is_positive_sample = False
                    
                # create new sample:
                dataset_sample = DataSetSample()
                dataset_sample.sample_nodes = self.load_nodes(node_list_path, graph_number)
                dataset_sample.sample_edges = self.load_edges(edge_list_path, graph_number)
                dataset_sample.sample_bug_location_pairs = self.extract_bug_locations(dataset_sample.sample_nodes)
                
                # 1 for positive samples; 0 for negative samples:
                if is_positive_sample:
                    dataset_sample.sample_testcase_labels = np.ones(len(dataset_sample.sample_bug_location_pairs))
                else:
                    dataset_sample.sample_testcase_labels = np.zeros(len(dataset_sample.sample_bug_location_pairs))
                
                # remove bug location edges:
                self.remove_bug_location_edges(dataset_sample.sample_edges, dataset_sample.sample_bug_location_pairs)
                
                # add new sample:
                dataset_samples.append(dataset_sample)
                
                if log:
                    print("Graph: ", graph_number)
        
        if not dataset_samples:
            raise Exception('No samples found')
        
        return dataset_samples
    
    def get_sample_paths(self, folder:str, count:int=-1) -> List[str]:
        files_samples = []
        
        for filename in os.listdir(folder):
            if filename.endswith(".edgelist"):
                edge_list_path = folder + filename
                files_samples.append(edge_list_path)
            if count != -1 and len(files_samples) == count:
                break
        
        return files_samples
    
    def get_edge_list_path(self, graph_path:str, graph_filename:str) -> str:
        return graph_path + "/" + graph_filename + ".edgelist"
    
    def get_featurenodelist_path(self, graph_path:str, graph_filename:str) -> str:
        return graph_path + "/features/" + graph_filename + ".featurenodelist"
    
    def load_feature_size(self) -> int:
        
        # read from first positive sample...
        file_positive_sample = self.get_sample_paths(self.positve_samples_path, 1)
        
        if(not file_positive_sample):
            raise Exception('No samples found')
        
        graph_path, filename = ntpath.split(file_positive_sample[0])
        graph_filename = filename[:filename.rfind(".")]
        featurenodelist_path = self.get_featurenodelist_path(graph_path, graph_filename)
        
        # Assumes Tables: index, n-feature, tag
        if self.binary_featurenodelist:
            # subtract: tag column
            return len(self.load_featurenodelist(featurenodelist_path).columns) - 1
        else:
            # subtract: index and tag column
            return len(self.load_featurenodelist(featurenodelist_path, with_columns=False).columns) - 2
    
    def load_nodes(self, node_list_path:str, graph_number:str) -> DataFrame:
        
        # Load data:
        node_data = self.load_featurenodelist(node_list_path)
        
        # Create index:
        node_data = node_data.rename(index=lambda index: self.add_prefix(graph_number, index))
        node_data = node_data.fillna("")
        
        return node_data
    
    def load_featurenodelist(self, node_list_path:str, with_columns:bool=True) -> DataFrame:
        if self.binary_featurenodelist:
            return pd.read_pickle(node_list_path, compression="zip")
        else:
            # Column names:
            # TODO: Just create the header once for all files...
            if with_columns:
                node_data_columns = []
                node_data_columns.append("index")
                
                for feature_num in range(0, self.feature_size):
                    node_data_columns.append("feature" + str(feature_num))
                
                node_data_columns.append("tag")
                    
                node_data = pd.read_table(node_list_path, names=node_data_columns)
                node_data.set_index("index", inplace=True)
                return node_data
            else:
                node_data = pd.read_table(node_list_path)
                return node_data
            
    
    def load_edges(self, edge_list_path:str, graph_number:str) -> DataFrame:
        edge_list_col_names = ["source", "target"]
        edge_data = pd.read_table(edge_list_path, names=edge_list_col_names)
        
        edge_data['source'] = edge_data['source'].apply(lambda index: self.add_prefix(graph_number, index))
        edge_data['target'] = edge_data['target'].apply(lambda index: self.add_prefix(graph_number, index))
        edge_data = edge_data.rename(index=lambda index: self.add_prefix(graph_number, index))
        
        return edge_data
    
    def add_prefix(self, prefix:str, index:int) -> str:
        return str(prefix) + "_" + str(index)
    
    def extract_bug_locations(self, node_data:DataFrame) -> List[Tuple[str, str]]:
        bug_locations = []
        bug_report_node:str = ""
        
        for node_index, node_row in node_data.iterrows():
            tag = node_row["tag"]
            
            if (tag == "# REPORT"):
                bug_report_node = node_index
            elif (tag == "# LOCATION"):
                bug_location_node:str = node_index
                bug_location_pair:Tuple[str, str] = (bug_report_node, bug_location_node)
                bug_locations.append(bug_location_pair)
        
        if not bug_report_node:
            raise Exception("Error: Bug report node not found!")
        
        # Remove the column 'tag' - it is not a numerical column:
        node_data.drop("tag", inplace=True, axis=1)
        return bug_locations
    
    def remove_bug_location_edges(self, edge_data:DataFrame, bug_locations:List[Tuple[str, str]]):
        bug_location_edges = []
        
        for bug_location in bug_locations:
            for edge_index, edge_row in edge_data.iterrows():
                if edge_row[0] == bug_location[0] and edge_row[1] == bug_location[1]:
                    bug_location_edges.append(edge_index)
                    break
            
        edge_data.drop(index=bug_location_edges, inplace=True)

    
class DataSetSplitter:
    
    def __init__(self, dataset_loader:DataSetLoader):
        self.dataset_loader:DataSetLoader = dataset_loader
        
    def load_samples(self, start_with_sample:int=0, number_of_sample:int=-1, log:bool=False) -> Union[sg.StellarGraph, List[Tuple[str, str]], np.ndarray, sg.StellarGraph, List[Tuple[str, str]], np.ndarray]:
        dataset_samples = dataset_loader.load_dataset(start_with_sample, number_of_sample, log)
        
        # Split training and test data:
        dataset_samples_train = dataset_samples[:len(dataset_samples) // 2]
        dataset_samples_test = dataset_samples[len(dataset_samples) // 2:]
        
        # Concatenate all  samples:
        nodes_train, edges_train, bug_location_pairs_train, testcase_labels_train = self.concat_samples(dataset_samples_train)
        nodes_test, edges_test, bug_location_pairs_test, testcase_labels_test = self.concat_samples(dataset_samples_test)

        # Create integrated graph:
        G_train = sg.StellarGraph(nodes_train, edges_train)
        if log:
            print(G_train.info())
        
        G_test = sg.StellarGraph(nodes_test, edges_test)
        if log:
            print(G_test.info())
        
        # Train/Test -> Graph, Node-Pairs, Edge-Exists-Label (positive=1/negative=0 sample)
        return G_train, bug_location_pairs_train, testcase_labels_train, G_test, bug_location_pairs_test, testcase_labels_test

    def concat_samples(self, dataset_samples:List[DataSetSample]) -> Union[List[DataFrame], List[DataFrame], List[Tuple[str, str]], np.ndarray]:
        nodes = []
        edges = []
        bug_location_pairs = []
        testcase_labels = np.zeros(0)
        
        for dataset_sample in dataset_samples:
            nodes.append(dataset_sample.sample_nodes)
            edges.append(dataset_sample.sample_edges)
            
            for bug_location_pair in dataset_sample.sample_bug_location_pairs:
                bug_location_pairs.append(bug_location_pair)
            
            testcase_labels = np.concatenate((testcase_labels, dataset_sample.sample_testcase_labels), axis=0)
        
        pd_nodes = pd.concat(nodes)
        pd_edges = pd.concat(edges)
        
        return pd_nodes, pd_edges, bug_location_pairs, testcase_labels

###################################################################################################
# AI Model
###################################################################################################
    
class BugLocalizationAIModelBuilder:
    
    def create_model(self, num_samples:List[int], layer_sizes:List[int], feature_size:int) -> keras.Model:
        graphsage = GraphSAGE(
            n_samples=num_samples,
            layer_sizes=layer_sizes,
            input_dim=feature_size,
            multiplicity=2,  # link inference
            bias=True,
            dropout=0.3
        )
         
        # Build the model and expose input and output sockets of GraphSAGE model for link prediction
        x_inp, x_out = graphsage.in_out_tensors()
         
        prediction = link_classification(
            output_dim=1, output_act="relu", edge_embedding_method="ip"
        )(x_out)
         
        model = keras.Model(inputs=x_inp, outputs=prediction)
         
        model.compile(
            optimizer=keras.optimizers.Adam(lr=1e-3),
            loss=keras.losses.binary_crossentropy,
            metrics=["acc"],
        )
        
        return model

    
class BugLocalizationAIModelTrainer:
    
    def __init__(self, dataset_splitter:DataSetSplitter, model:keras.Model, num_samples:List[int], batch_size:int, epochs:int):
        self.dataset_splitter = dataset_splitter
        self.model = model
        self.num_samples = num_samples
        self.batch_size = batch_size
        self.epochs = epochs
    
    def train_model(self, start_with_sample:int=0, number_of_sample:int=-1, evaluate_before:bool=True, evaluate_after:bool=True, log:bool=True):
        G_train, bug_location_pairs_train, edge_labels_train, G_test, bug_location_pairs_test, edge_labels_test = self.dataset_splitter.load_samples(start_with_sample, number_of_sample, log)
        
        train_gen = GraphSAGELinkGenerator(G_train, self.batch_size, self.num_samples)
        train_flow = train_gen.flow(bug_location_pairs_train, edge_labels_train, shuffle=True)
         
        test_gen = GraphSAGELinkGenerator(G_test, self.batch_size, self.num_samples)
        test_flow = test_gen.flow(bug_location_pairs_test, edge_labels_test)
        
        if evaluate_before:
            init_train_metrics = model.evaluate(train_flow)
            init_test_metrics = model.evaluate(test_flow)
             
            print("\nTrain Set Metrics of the initial (untrained) model:")
            for name, val in zip(model.metrics_names, init_train_metrics):
                print("\t{}: {:0.4f}".format(name, val))
             
            print("\nTest Set Metrics of the initial (untrained) model:")
            for name, val in zip(model.metrics_names, init_test_metrics):
                print("\t{}: {:0.4f}".format(name, val))
             
        history = model.fit(train_flow, epochs=self.epochs, validation_data=test_flow, verbose=2)
        
        if log:
            sg.utils.plot_history(history)
        
        if evaluate_after:
            train_metrics = model.evaluate(train_flow)
            test_metrics = model.evaluate(test_flow)
             
            print("\nTrain Set Metrics of the trained model:")
            for name, val in zip(model.metrics_names, train_metrics):
                print("\t{}: {:0.4f}".format(name, val))
             
            print("\nTest Set Metrics of the trained model:")
            for name, val in zip(model.metrics_names, test_metrics):
                print("\t{}: {:0.4f}".format(name, val))


if __name__ == '__main__':
    
    ###################################################################################################
    # Create Training and Test Data:
    ###################################################################################################
    
    # Collect all graphs from the given folder:
    dataset_loader = DataSetLoader(positve_samples_path, negative_samples_path, binary_featurenodelist)
    dataset_splitter = DataSetSplitter(dataset_loader)
    
    ###################################################################################################
    # Create AI Model:
    ###################################################################################################
    
    num_samples = [20, 10]
    layer_sizes = [20, 20]
    
    bug_localization_model_builder = BugLocalizationAIModelBuilder()
    model = bug_localization_model_builder.create_model(num_samples, layer_sizes, dataset_loader.feature_size)
     
    ###################################################################################################
    # Train and Evaluate AI Model:
    ###################################################################################################
    
    batch_size_per_training = 20  # see bug_samples_per_training
    epochs_per_training = 1  # see global_epochs 
    
    bug_localization_model_trainer = BugLocalizationAIModelTrainer(
        dataset_splitter=dataset_splitter,
        model=model,
        num_samples=num_samples,
        batch_size=batch_size_per_training,
        epochs=epochs_per_training)
    
    # TODO: Shuffle samples during global epochs?
    global_epochs = 20
    
    # Note: A sample is pair of a bug report and model location, each "bug sample" contains one bug report and multiple locations.
    bug_samples_per_training = 20
    bug_sample_number = dataset_loader.negative_sample_size + dataset_loader.negative_sample_size
    
    for global_epoch in range(global_epochs):
        current_sample = 0
        
        while (current_sample < bug_sample_number):
            bug_localization_model_trainer.train_model(
                start_with_sample=current_sample,
                number_of_sample=bug_samples_per_training,
                evaluate_before=(global_epoch == 0),  # first epoch?
                evaluate_after=(global_epoch == global_epochs),  # last epoch?
                log=True)
            current_sample += bug_samples_per_training      
