import datetime
import ntpath
import os
from pathlib import Path
import random
from typing import Union, List, Tuple

from pandas.core.frame import DataFrame  # type: ignore
from stellargraph.layer import GraphSAGE, link_classification  # type: ignore
from stellargraph.mapper import GraphSAGELinkGenerator  # type: ignore
from tensorflow import keras  # type: ignore

import numpy as np  # type: ignore
import pandas as pd  # type: ignore
import stellargraph as sg  # type: ignore
from tensorflow.keras.callbacks import CSVLogger  # type: ignore
from tensorflow.keras.utils import Sequence  # type: ignore


#===============================================================================
# Environmental Information
#===============================================================================
positve_samples_path:str = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\positivesamples_5000/"
negative_samples_path:str = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\negativesamples_5000/"

model_training_save_dir = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\trained_model_" + datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S') + "/"
model_training_checkpoint_dir = model_training_save_dir + "checkpoints/"

#===============================================================================
# Data Processing
#===============================================================================


class DataSet:

    def __init__(self, positve_samples_path:str, negative_samples_path:str):
        self.positve_samples_path:str = positve_samples_path
        self.negative_samples_path:str = negative_samples_path
        
        # Collect all graphs from the given folder:
        self.positive_bug_samples = self.get_samples(self.positve_samples_path)
        self.negative_bug_samples = self.get_samples(self.negative_samples_path, sample_is_negative=True)
        
        self.load_feature_size()
        self.column_names:List[str]
    
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
    
    def load_feature_size(self):
        if self.positive_bug_samples:
            self.feature_size = self.positive_bug_samples[0].load_feature_size()
        else:
            raise Exception('No samples found')
        
    def get_column_names(self) -> List[str]:
        if not self.column_names:
            self.column_names = []
            self.column_names.append("index")
            
            for feature_num in range(0, self.feature_size):
                self.column_names.append("feature" + str(feature_num))
            
            self.column_names.append("tag")
        
        return self.column_names


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
    
    def load_nodes(self, name_prefix:str=None):
        self.nodes = pd.read_table(self.get_nodes_path(), names=self.dataset.get_column_names())
        self.nodes.set_index("index", inplace=True)
        
        if name_prefix:
            self.nodes = self.nodes.rename(index=lambda index: self.add_prefix(name_prefix, index))

        self.nodes = self.nodes.fillna("")
        
    def load_feature_size(self) -> int:
        # Load without header
        tmp_nodes = pd.read_table(self.get_nodes_path())
        # subtract: index and tag column
        return len(tmp_nodes.columns) - 2
    
    def load_edges(self, name_prefix:str=None):
        edge_list_col_names = ["source", "target"]
        self.edges = pd.read_table(self.get_edges_path(), names=edge_list_col_names)
        
        if name_prefix:
            self.edges['source'] = self.edges['source'].apply(lambda index: self.add_prefix(name_prefix, index))
            self.edges['target'] = self.edges['target'].apply(lambda index: self.add_prefix(name_prefix, index))
            self.edges = self.edges.rename(index=lambda index: self.add_prefix(name_prefix, index))
    
    def add_prefix(self, prefix:str, index:int) -> str:
        return str(prefix) + "_" + str(index)


class DataSetEmbedding(DataSet):
    
    def __init__(self, positve_samples_path:str, negative_samples_path:str):
        super().__init__(positve_samples_path, negative_samples_path)
        
    def get_sample(self, edge_list_path:str, sample_is_negative:bool):
        return DataSetBugSampleEmbedding(self, edge_list_path, sample_is_negative)    


class DataSetBugSampleEmbedding(DataSetBugSample):
    
    def __init__(self, dataset:DataSet, sample_file_path:str, sample_is_negative=False):
        super().__init__(dataset, sample_file_path, sample_is_negative)
        
        # Pairs of node IDs: (Bug Report, Location)
        self.bug_location_pairs:List[Tuple[str, str]]
        
        # 0 or 1 for each bug location pair: 0 -> positive sample, 1 -> negative sample
        self.testcase_labels:np.ndarray
    
    # # Path/File Naming Conventions # #
    
    def get_nodes_path(self) -> str:
        return self.path + "/features/" + self.name + ".featurenodelist"
    
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
        
    def load_nodes(self, name_prefix:str=None) -> DataFrame:
        self.nodes = pd.read_pickle(self.get_nodes_path(), compression="zip")
        
        if name_prefix:
            self.nodes = self.nodes.rename(index=lambda index: self.add_prefix(name_prefix, index))
            
    def load_feature_size(self) -> int:
        # subtract: tag column
        tmp_nodes = pd.read_pickle(self.get_nodes_path(), compression="zip")
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

    
class DataSetSplitter:
    
    def __init__(self, dataset:DataSetEmbedding):
        self.dataset:DataSetEmbedding = dataset
        self.bug_sample_sequence = self.create_bug_sample_sequence()
        
    def create_bug_sample_sequence(self) -> List[DataSetBugSampleEmbedding]:
        
        # Mix positive and negative samples to create a full set of samples 
        # that can be split to create a training and validation set of samples:
        bug_sample_sequence = []
        index = 0
        
        while index < len(self.dataset.positive_bug_samples) or index < len(self.dataset.negative_bug_samples):
            if index < len(self.dataset.positive_bug_samples):
                bug_sample_sequence.append(self.dataset.positive_bug_samples[index])
            if index < len(self.dataset.negative_bug_samples):
                bug_sample_sequence.append(self.dataset.negative_bug_samples[index])
                     
            index += 1
            
        return bug_sample_sequence
        
    def split(self, fraction:int) -> Tuple[List[DataSetBugSampleEmbedding], List[DataSetBugSampleEmbedding]]:
        
        # Split training and test data:
        split_idx = len(self.bug_sample_sequence) // fraction
        
        bug_samples_train = self.bug_sample_sequence[:split_idx]
        bug_samples_test = self.bug_sample_sequence[split_idx:]
        
        return bug_samples_train, bug_samples_test

#===============================================================================
# AI Model
#===============================================================================


class BugLocalizationGenerator(Sequence):
    
    # https://www.tensorflow.org/guide/keras/train_and_evaluate#using_a_kerasutilssequence_object_as_input
    
    # Note: A sample is pair of a bug report and model location, each "bug sample" contains one bug report and multiple locations.
    
    def __init__(self, name:str, batch_size:int, shuffle:bool, num_samples:List[int], bug_samples:List[DataSetBugSampleEmbedding], log:bool=False):
        self.name:str = name  # e.g. train, eval for debugging
        self.batch_size:int = batch_size
        self.shuffle = shuffle
        self.num_samples:List[int] = num_samples
        self.bug_samples:List[DataSetBugSampleEmbedding] = bug_samples
        self.log:bool = log
        
    def load_bug_samples_batch(self, start_bug_sample:int) -> Sequence:

        # Load batch of bug samples:
        batch_bug_samples = []
        end_bug_sample = min(start_bug_sample + self.batch_size, len(self.bug_samples) - 1)
        
        for bug_sample_idx in range(start_bug_sample, end_bug_sample):
            bug_sample = self.bug_samples[bug_sample_idx]
            bug_sample.load()
            batch_bug_samples.append(bug_sample)
            
            if self.log:
                print("Loaded Sample:", bug_sample.number)

        # Concatenate all  samples:
        nodes, edges, bug_location_pairs, testcase_labels = self.concat_samples(batch_bug_samples)

        # Create integrated graph:
        graph = sg.StellarGraph(nodes, edges)
        
        if self.log:
            print(graph.info())
        
        # Create Keras sequence:
        test_gen = GraphSAGELinkGenerator(graph, len(bug_location_pairs), self.num_samples)
        flow = test_gen.flow(bug_location_pairs, testcase_labels)   
        
        return flow

    def concat_samples(self, batch_bug_samples:List[DataSetBugSampleEmbedding]) -> Union[List[DataFrame], List[DataFrame], List[Tuple[str, str]], np.ndarray]:
        nodes = []
        edges = []
        bug_location_pairs = []
        testcase_labels = np.zeros(0)
        
        for bug_sample in batch_bug_samples:
            nodes.append(bug_sample.nodes)
            edges.append(bug_sample.edges)
            
            for bug_location_pair in bug_sample.bug_location_pairs:
                bug_location_pairs.append(bug_location_pair)
            
            testcase_labels = np.concatenate((testcase_labels, bug_sample.testcase_labels), axis=0)
        
        pd_nodes = pd.concat(nodes)
        pd_edges = pd.concat(edges)
        
        return pd_nodes, pd_edges, bug_location_pairs, testcase_labels        
    
    def __len__(self):
        """Denotes the number of batches per epoch"""
        return int(np.ceil(len(self.bug_samples) / float(self.batch_size)))
    
    def __getitem__(self, batch_idx):
        """Generate one batch of data"""
        if self.log:
            print("Get Batch:", batch_idx)
        
        # Loading data in parallel to each training batch:
        flow = self.load_bug_samples_batch(batch_idx * self.batch_size)
        
        # Wrapping StellarGraph Sequence which contains one full loaded batch:
        if len(flow) > 1:
            assert len(flow) == 1
            print("WARNING: Unexpected GraphSAGE Batch Count:", len(flow))
        
        return flow.__getitem__(0)
    
    # FIXME: Not calling on_epoch_end... Waiting for bug fix release... Workaround use callbacks
    # https://github.com/tensorflow/tensorflow/issues/35911
    def workaround_on_epoch_end(self):
        """Prepare the data set for the next epoch"""
        
        # Shuffle samples:
        if self.shuffle:
            random.shuffle(self.bug_samples)

    
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
        checkpoints = [checkpoint_dir + '/' + name for name in os.listdir(checkpoint_dir)]
        
        if checkpoints:
            latest_checkpoint = max(checkpoints, key=os.path.getctime)
            print('Restoring from', latest_checkpoint)
            return keras.models.load_model(latest_checkpoint)
        
        print('Creating a new model')
        return self.create_model(num_samples, layer_sizes, feature_size)

    
class BugLocalizationAIModelTrainer:
    
    def __init__(self, dataset_splitter:DataSetSplitter, model:keras.Model, num_samples:List[int], checkpoint_dir:str):
        self.dataset_splitter:DataSetSplitter = dataset_splitter
        self.model:keras.Model = model
        self.num_samples:List[int] = num_samples
        
        self.callbacks = []
        
        # This callback saves a SavedModel every epoch (or X batches).
        self.callbacks.append(keras.callbacks.ModelCheckpoint(filepath=checkpoint_dir, save_freq='epoch'))
        self.callbacks.append(CSVLogger(checkpoint_dir + "model_history_log.csv", append=True))
        
    class ShuffleCallback(keras.callbacks.Callback):
    
        def __init__(self, flow:BugLocalizationGenerator):
            self.flow:BugLocalizationGenerator = flow
        
        def on_epoch_end(self, epoch, logs=None):  # @UnusedVariable
            self.flow.workaround_on_epoch_end()

    def train(self, epochs:int, batch_size:int, shuffle:bool, generator_workers:int=1, log:bool=True):
        
        # Initialize training data:
        bug_samples_train, bug_samples_test = dataset_splitter.split(2)
        train_flow = BugLocalizationGenerator("training", batch_size, shuffle, self.num_samples, bug_samples_train, log)
        test_flow = BugLocalizationGenerator("validation", batch_size, shuffle, self.num_samples, bug_samples_test, log)
        
        # FIXME: Not calling on_epoch_end... Waiting for bug fix release... Workaround use callbacks
        # https://github.com/tensorflow/tensorflow/issues/35911
        self.callbacks.append(self.ShuffleCallback(train_flow))
        self.callbacks.append(self.ShuffleCallback(test_flow))
        
        #=======================================================================
        # Note that our implementation enables the use of the multiprocessing argument of fit(),
        # where the number of threads specified in  workers  are those that generate batches in parallel.
        # [https://stanford.edu/~shervine/blog/keras-how-to-generate-data-on-the-fly]
        #=======================================================================
        
        # # Train Model # #
        history = self.model.fit(
            train_flow,
            epochs=epochs,
            validation_data=test_flow,
            verbose=2,
            use_multiprocessing=True,
            workers=generator_workers,
            callbacks=self.callbacks)

        if log:
            sg.utils.plot_history(history)
        
        # # Save Final Trained Model # #     
        self.model.save(model_training_save_dir)
        
    # TODO: Evaluation for multiple full model versions: bug, all model elements ...
    def evaluate(self, evaluation_flow:Sequence):
        evaluation_metrics = self.model.evaluate(evaluation_flow)

        print("Evaluation metrics of the model:")
        for name, val in zip(self.model.metrics_names, evaluation_metrics):
            print("\t{}: {:0.4f}".format(name, val))


if __name__ == '__main__':
    
    #===========================================================================
    # Create Training and Test Data:
    #===========================================================================
    
    # Collect all graphs from the given folder:
    dataset = DataSetEmbedding(positve_samples_path, negative_samples_path)
    dataset_splitter = DataSetSplitter(dataset)
    
    #===========================================================================
    # Create AI Model:
    #===========================================================================
    
    # GraphSAGE Settings:
    num_samples = [20, 10]
    layer_sizes = [20, 20]
    
    bug_localization_model_builder = BugLocalizationAIModelBuilder()
    model = bug_localization_model_builder.create_or_restore_model(num_samples, layer_sizes, dataset.feature_size, model_training_checkpoint_dir)
    
    #===========================================================================
    # Train and Evaluate AI Model:
    #===========================================================================
    
    # Training Settings:
    epochs = 20  # Number of training epochs. 
    batch_size = 20  # Number of bug location samples, please node that each sample has multiple location samples.
    shuffle = True  # Shuffle training and validation samples after each epoch?
    generator_workers = 4  # Number of threads that load/generate the batches in parallel.
    log = False  # Some console output for debugging...
    
    bug_localization_model_trainer = BugLocalizationAIModelTrainer(
        dataset_splitter=dataset_splitter,
        model=model,
        num_samples=num_samples,
        checkpoint_dir=model_training_checkpoint_dir)
    
    bug_localization_model_trainer.train(epochs, batch_size, shuffle, generator_workers, log)
