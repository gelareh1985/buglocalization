import datetime
import ntpath
import os
from pathlib import Path
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

model_training_save_dir = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\trained_model_" + datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S') + "/"
model_training_checkpoint_dir = model_training_save_dir + "checkpoints/"

binary_featurenodelist:bool = True

###################################################################################################
# Data Processing
###################################################################################################


class DataSetBugSample:
    
    def __init__(self, sample_file_path:str, sample_is_negative=False):
        
        # File naming pattern PATH/NUMBER_NAME.EXTENSION
        self.path, filename = ntpath.split(sample_file_path)
        self.name = filename[:filename.rfind(".")]
        self.number = filename[0:filename.find("_")]
        
        # Positive Sample = False, Negative Sample = True
        self.is_negative = sample_is_negative
        
        # Nodes of the bug localization graph (excerpt).
        self.nodes:List[DataFrame]
        
        # Edges of the bug localization graph (excerpt).
        self.edges:List[DataFrame]
        
        # Pairs of node IDs: (Bug Report, Location)
        self.bug_location_pairs:List[Tuple[str, str]]
        
        # 0 or 1 for each bug location pair: 0 -> positive sample, 1 -> negative sample
        self.testcase_labels:np.ndarray
    
    # # Path/File Naming Conventions ##
    
    def get_nodelist_path(self) -> str:
        return self.path + "/" + self.name + ".nodelist"
        
    def get_edgelist_path(self) -> str:
        return self.path + "/" + self.name + ".edgelist"
    
    def get_featurenodelist_path(self) -> str:
        return self.path + "/features/" + self.name + ".featurenodelist"
        
        
class DataSetLoader:

    def __init__(self, positve_samples_path:str, negative_samples_path:str, binary_featurenodelist:bool):
        self.positve_samples_path:str = positve_samples_path
        self.negative_samples_path:str = negative_samples_path
        self.binary_featurenodelist:bool = binary_featurenodelist
        
        self.feature_size:int = self.load_feature_size()
        self.positive_bug_samples:List[DataSetBugSample] = self.get_samples(self.positve_samples_path)
        self.negative_bug_samples:List[DataSetBugSample] = self.get_samples(self.negative_samples_path, sample_is_negative=True)
        
        # Collect all graphs from the given folder:
        self.bug_sample_sequence:List[DataSetBugSample] = self.create_bug_sample_sequence()
        
    def create_bug_sample_sequence(self) -> List[DataSetBugSample]:
        
        # Mix positive and negative samples to create a full set of samples 
        # that can be split to create a training and validation set of samples:
        bug_sample_sequence = []
        index = 0
        
        while index < len(self.positive_bug_samples) or index < len(self.negative_bug_samples):
            if index < len(self.positive_bug_samples):
                bug_sample_sequence.append(self.positive_bug_samples[index])
            if index < len(self.negative_bug_samples):
                bug_sample_sequence.append(self.negative_bug_samples[index])
                     
            index += 1
            
        return bug_sample_sequence
    
    def load_dataset(self, start_with_sample:int=0, number_of_sample:int=-1, log:bool=False) -> List[DataSetBugSample]:
        
        # Read all samples and create unique node IDs:
        loaded_samples = []
        sub_bug_sample_sequence = self.bug_sample_sequence
        
        if number_of_sample != -1:
            sub_bug_sample_sequence = self.bug_sample_sequence[start_with_sample:start_with_sample + number_of_sample]
        elif start_with_sample > 0:
            sub_bug_sample_sequence = self.bug_sample_sequence[start_with_sample:]
        
        for bug_sample in sub_bug_sample_sequence:
            
            # different prefixes for positive and negative samples:
            bug_sample_name = bug_sample.number
            
            if bug_sample.is_negative:
                bug_sample_name = "n" + bug_sample_name
            else:
                bug_sample_name = "p" + bug_sample_name
                
            # create new sample:
            bug_sample.nodes = self.load_nodes(bug_sample.get_featurenodelist_path(), bug_sample_name)
            bug_sample.edges = self.load_edges(bug_sample.get_edgelist_path(), bug_sample_name)
            bug_sample.bug_location_pairs = self.extract_bug_locations(bug_sample.nodes)
            
            # 1 for positive samples; 0 for negative samples:
            if bug_sample.is_negative:
                bug_sample.testcase_labels = np.zeros(len(bug_sample.bug_location_pairs))
            else:
                bug_sample.testcase_labels = np.ones(len(bug_sample.bug_location_pairs))
            
            # remove bug location edges:
            self.remove_bug_location_edges(bug_sample.edges, bug_sample.bug_location_pairs)
            
            # add new sample:
            loaded_samples.append(bug_sample)
            
            if log:
                print("Graph: ", bug_sample_name)
        
        if not loaded_samples:
            raise Exception('No samples found')
        
        return loaded_samples
    
    def get_samples(self, folder:str, sample_is_negative=False, count:int=-1) -> List[DataSetBugSample]:
        samples = []
        
        for filename in os.listdir(folder):
            if filename.endswith(".edgelist"):
                edge_list_path = folder + filename
                samples.append(DataSetBugSample(edge_list_path, sample_is_negative))
            if count != -1 and len(samples) == count:
                break
        
        return samples
    
    def load_feature_size(self) -> int:
        
        # read from first positive sample...
        positive_bug_sample = self.get_samples(self.positve_samples_path, 1)
        
        if(not positive_bug_sample):
            raise Exception('No samples found')
        
        featurenodelist_path = positive_bug_sample[0].get_featurenodelist_path()
        
        # Assumes Tables: index, n-feature, tag
        if self.binary_featurenodelist:
            # subtract: tag column
            return len(self.load_featurenodelist(featurenodelist_path).columns) - 1
        else:
            # subtract: index and tag column
            return len(self.load_featurenodelist(featurenodelist_path, with_columns=False).columns) - 2
    
    def load_nodes(self, node_list_path:str, name_prefix:str) -> DataFrame:
        
        # Load data:
        node_data = self.load_featurenodelist(node_list_path)
        
        # Create index:
        node_data = node_data.rename(index=lambda index: self.add_prefix(name_prefix, index))
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
        bug_location_source = set()
        bug_location_target = set()

        for bug_location in bug_locations:
            bug_location_source.add(bug_location[0])
            bug_location_target.add(bug_location[1])
            
        bug_location_edges = edge_data.loc[edge_data["source"].isin(bug_location_source) & edge_data["target"].isin(bug_location_target)]
        edge_data.drop(bug_location_edges.index, inplace=True)
        
    
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

    def concat_samples(self, dataset_samples:List[DataSetBugSample]) -> Union[List[DataFrame], List[DataFrame], List[Tuple[str, str]], np.ndarray]:
        nodes = []
        edges = []
        bug_location_pairs = []
        testcase_labels = np.zeros(0)
        
        for dataset_sample in dataset_samples:
            nodes.append(dataset_sample.nodes)
            edges.append(dataset_sample.edges)
            
            for bug_location_pair in dataset_sample.bug_location_pairs:
                bug_location_pairs.append(bug_location_pair)
            
            testcase_labels = np.concatenate((testcase_labels, dataset_sample.testcase_labels), axis=0)
        
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
            loss=keras.losses.binary_crossentropy,  # two-class classification problem
            metrics=["acc"],
        )
        
        return model
    
    def create_or_restore_model(self, num_samples:List[int], layer_sizes:List[int], feature_size:int, checkpoint_dir:str) -> keras.Model:
       
        Path(checkpoint_dir).mkdir(parents=True, exist_ok=True)
       
        # Either restore the latest model, or create a fresh one if there is no checkpoint available.
        checkpoints = [checkpoint_dir + '/' + name
                       for name in os.listdir(checkpoint_dir)]
        if checkpoints:
            latest_checkpoint = max(checkpoints, key=os.path.getctime)
            print('Restoring from', latest_checkpoint)
            # return keras.models.load_model(latest_checkpoint)
            return "Hallo"
        
        print('Creating a new model')
        return self.create_model(num_samples, layer_sizes, feature_size)

    
class BugLocalizationAIModelTrainer:
    
    def __init__(self, dataset_splitter:DataSetSplitter, model:keras.Model, num_samples:List[int], batch_size:int, epochs:int, checkpoint_dir:str):
        self.dataset_splitter = dataset_splitter
        self.model = model
        self.num_samples = num_samples
        self.batch_size = batch_size
        self.epochs = epochs
        
        # This callback saves a SavedModel every 100 batches.
        # We include the training loss in the folder name.
        self.callbacks = [
            keras.callbacks.ModelCheckpoint(
                filepath=checkpoint_dir,  # + '/ckpt-loss={loss:.2f}',
                save_freq=100)
        ]
    
    def train_model(self, start_with_sample:int=0, number_of_sample:int=-1, evaluate_before:bool=True, evaluate_after:bool=True, log:bool=True):
        G_train, bug_location_pairs_train, edge_labels_train, G_test, bug_location_pairs_test, edge_labels_test = self.dataset_splitter.load_samples(start_with_sample, number_of_sample, log)
        
        train_gen = GraphSAGELinkGenerator(G_train, self.batch_size, self.num_samples)
        train_flow = train_gen.flow(bug_location_pairs_train, edge_labels_train, shuffle=True)
         
        test_gen = GraphSAGELinkGenerator(G_test, self.batch_size, self.num_samples)
        test_flow = test_gen.flow(bug_location_pairs_test, edge_labels_test)
        
        if evaluate_before:
            init_train_metrics = model.evaluate(train_flow)
            init_test_metrics = model.evaluate(test_flow)
             
            print("\nTrain Set Metrics of the initial model:")
            for name, val in zip(model.metrics_names, init_train_metrics):
                print("\t{}: {:0.4f}".format(name, val))
             
            print("\nTest Set Metrics of the initial model:")
            for name, val in zip(model.metrics_names, init_test_metrics):
                print("\t{}: {:0.4f}".format(name, val))
             
        history = model.fit(train_flow, epochs=self.epochs, validation_data=test_flow, verbose=2, callbacks=self.callbacks)
        
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
    model = bug_localization_model_builder.create_or_restore_model(num_samples, layer_sizes, dataset_loader.feature_size, model_training_checkpoint_dir)
    
    model.save(model_training_save_dir)
     
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
        epochs=epochs_per_training,
        checkpoint_dir=model_training_checkpoint_dir)
    
    # TODO: Shuffle samples during global epochs?
    global_epochs = 20
    
    # Note: A sample is pair of a bug report and model location, each "bug sample" contains one bug report and multiple locations.
    bug_samples_per_training = 20
    bug_sample_number = len(dataset_loader.bug_sample_sequence)
    
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
