import ntpath
import os

from IPython.display import display
from stellargraph.layer import GraphSAGE, link_classification
from stellargraph.mapper import GraphSAGELinkGenerator
from tensorflow import keras

import numpy as np
import pandas as pd
import stellargraph as sg


positve_samples_path = r"D:/files_MDEAI_original/Data_sets/buglocations_dataset/small/buglocations_5000/"
negative_samples_path = r"D:/files_MDEAI_original/Data_sets/buglocations_dataset/small/negativesamples_5000/"


class DataSetLoader:
    
    def __init__(self, positve_samples_path, negative_samples_path):
        self.positve_samples_path = positve_samples_path
        self.negative_samples_path = negative_samples_path
        self.feature_size = self.load_feature_size()
        self.positive_sample_size = len(self.get_sample_paths(self.positve_samples_path))
        self.negative_sample_size = len(self.get_sample_paths(self.negative_samples_path))
    
    def load_dataset(self, start_with_sample=0, number_of_sample=None, log=False):
    
        # Collect all graphs from the given folder:
        dataset_nodes = []
        dataset_edges = []
        dataset_bug_locations = []
        
        # 1 for positive samples; 0 for negative samples
        testcase_labels = np.zeros(0)
        
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
            
            if number_of_sample is not None and len(all_samples) == number_of_sample:
                break
        
        # Read all samples and create unique node IDs:
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
        
                # nodes:
                nodes_data = self.load_nodes(node_list_path, self.feature_size, graph_number)
                display(nodes_data.shape)
                dataset_nodes.append(nodes_data)
                
                # edges:
                edge_data = self.load_edges(edge_list_path, graph_number)
                dataset_edges.append(edge_data)
                
                # bug locations:
                bug_locations = self.extract_bug_locations(nodes_data)
                dataset_bug_locations.append(bug_locations)
                
                # 1 for positive samples; 0 for negative samples:
                if is_positive_sample:
                    testcase_labels = np.concatenate((testcase_labels, np.ones(len(bug_locations))), axis=None)
                else:
                    testcase_labels = np.concatenate((testcase_labels, np.zeros(len(bug_locations))), axis=None)
                
                # remove bug location edges:
                self.remove_bug_location_edges(edge_data, bug_locations)
                
                if log:
                    print("Graph: ", graph_number)
        
        if(not dataset_nodes or not dataset_edges or not dataset_bug_locations):
            raise Exception('No samples found')
        
        return dataset_nodes, dataset_edges, dataset_bug_locations, testcase_labels
    
    def get_sample_paths(self, folder, count=None):
        files_samples = []
        
        for filename in os.listdir(folder):
            if filename.endswith(".edgelist"):
                edge_list_path = folder + filename
                files_samples.append(edge_list_path)
            if count is not None and len(files_samples) == count:
                break
        
        return files_samples
    
    def get_edge_list_path(self, graph_path, graph_filename):
        return graph_path + "/" + graph_filename + ".edgelist"
    
    def get_featurenodelist_path(self, graph_path, graph_filename):
        return graph_path + "/features/" + graph_filename + ".featurenodelist"
    
    def load_feature_size(self):
        
        # read from first positive sample...
        file_positive_sample = self.get_sample_paths(self.positve_samples_path, 1)
        
        if(not file_positive_sample):
            raise Exception('No samples found')
        
        graph_path, filename = ntpath.split(file_positive_sample[0])
        graph_filename = filename[:filename.rfind(".")]
        featurenodelist_path = self.get_featurenodelist_path(graph_path, graph_filename)
        
        # Assumes Tables: index, n-feature, tag
        return len(pd.read_table(featurenodelist_path).columns) - 2
    
    def load_nodes(self, node_list_path, feature_size, graph_number):
        
        # Column names:
        node_data_columns = []
        node_data_columns.append("index")
        
        for feature_num in range(0, feature_size):
            node_data_columns.append("feature" + str(feature_num))
        
        node_data_columns.append("tag")
        
        # Load data:
        node_data = pd.read_table(node_list_path, names=node_data_columns)
        
        # Create index:
        node_data.set_index("index", inplace=True)
        node_data = node_data.rename(index=lambda index: self.add_prefix(graph_number, index))
        node_data = node_data.fillna("")
        
        return node_data
    
    def load_edges(self, edge_list_path, graph_number):
        edge_list_col_names = ["source", "target"]
        edge_data = pd.read_table(edge_list_path, names=edge_list_col_names)
        
        edge_data['source'] = edge_data['source'].apply(lambda index: self.add_prefix(graph_number, index))
        edge_data['target'] = edge_data['target'].apply(lambda index: self.add_prefix(graph_number, index))
        edge_data = edge_data.rename(index=lambda index: self.add_prefix(graph_number, index))
        
        return edge_data
    
    def add_prefix(self, prefix, index):
        return str(prefix) + "_" + str(index)
    
    def extract_bug_locations(self, node_data):
        bug_locations = []
        bug_report_node = None
        
        for node_index, node_row in node_data.iterrows():
            tag = node_row["tag"]
            
            if (tag == "# REPORT"):
                bug_report_node = node_index
            elif (tag == "# LOCATION"):
                bug_location_node = node_index
                bug_locations.append([bug_report_node, bug_location_node])
        
        if (bug_report_node is None):
            raise Exception("Error: Bug report node not found!")
        
        # Remove the column 'tag' - it is not a numerical column:
        node_data.drop("tag", inplace=True, axis=1)
        return bug_locations
    
    def remove_bug_location_edges(self, edge_data, bug_locations):
        bug_location_edges = []
        
        for bug_location in bug_locations:
            for edge_index, edge_row in edge_data.iterrows():
                if edge_row[0] == bug_location[0] and edge_row[1] == bug_location[1]:
                    bug_location_edges.append(edge_index)
                    break
            
        edge_data.drop(index=bug_location_edges, inplace=True)
    
    def unpack(self, packed_list):
        unpacked_list = []
        
        for sublist in packed_list:
            unpacked_list.extend(sublist)
            
        return unpacked_list

    
class DataSetSplitter:
    
    def __init__(self, dataset_loader):
        self.dataset_loader = dataset_loader
        
    def load_samples(self, start_with_sample=0, number_of_sample=None, log=False):
        dataset_nodes, dataset_edges, dataset_bug_locations, testcase_labels = dataset_loader.load_dataset(start_with_sample, number_of_sample, log)
        
        # Split training set:
        nodes_train = pd.concat(dataset_nodes[:len(dataset_nodes) // 2])
        edges_train = pd.concat(dataset_edges[:len(dataset_edges) // 2])
        bug_locations_train = dataset_loader.unpack(dataset_bug_locations[:len(dataset_bug_locations) // 2])
        edge_labels_train = testcase_labels[:len(testcase_labels) // 2]
         
        # Split test set:
        nodes_test = pd.concat(dataset_nodes[len(dataset_nodes) // 2:])
        edges_test = pd.concat(dataset_edges[len(dataset_edges) // 2:])
        bug_locations_test = dataset_loader.unpack(dataset_bug_locations[len(dataset_bug_locations) // 2:])
        edge_labels_test = testcase_labels[len(testcase_labels) // 2:]
        
        G_train = sg.StellarGraph(nodes_train, edges_train)
        if log:
            print(G_train.info())
        
        G_test = sg.StellarGraph(nodes_test, edges_test)
        if log:
            print(G_test.info())
        
        # Train/Test -> Graph, Node-Pairs, Edge-Exists-Label (positive=1/negative=0 sample)
        return G_train, bug_locations_train, edge_labels_train, G_test, bug_locations_test, edge_labels_test

    
class BugLocalizationAIModelBuilder:
    
    def create_model(self, num_samples, layer_sizes, feature_size):
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
    
    def __init__(self, model, num_samples, batch_size, epochs):
        self.model = model
        self.num_samples = num_samples
        self.batch_size = batch_size
        self.epochs = epochs
    
    def train_model(self, start_with_sample=0, number_of_sample=None, evaluate_before=True, evaluate_after=True, log=True):
        G_train, bug_locations_train, edge_labels_train, G_test, bug_locations_test, edge_labels_test = dataset_splitter.load_samples(start_with_sample, number_of_sample, log)
        
        train_gen = GraphSAGELinkGenerator(G_train, self.batch_size, self.num_samples)
        train_flow = train_gen.flow(bug_locations_train, edge_labels_train, shuffle=True)
         
        test_gen = GraphSAGELinkGenerator(G_test, self.batch_size, self.num_samples)
        test_flow = test_gen.flow(bug_locations_test, edge_labels_test)
        
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
        
###################################################################################################
# Create Training and Test Data:
###################################################################################################


# Collect all graphs from the given folder:
dataset_loader = DataSetLoader(positve_samples_path, negative_samples_path)
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

