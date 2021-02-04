#===============================================================================
# Configure GPU Device:
#===============================================================================
import tensorflow as tf

# Only allocate needed memory needed by the application:
gpus = tf.config.experimental.list_physical_devices('GPU')

if gpus:
    try:
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
    except RuntimeError as e:
        print(e)
#===============================================================================

import datetime
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
from tensorflow.keras.utils import Sequence, plot_model  # type: ignore

# from tqdm.keras import TqdmCallback  # type: ignore

from bug_localization_data_set import DataSetEmbedding, DataSetBugSampleEmbedding

#===============================================================================
# Environmental Information
#===============================================================================

positve_samples_path:str = r"D:\buglocalization_gelareh_home\data\eclipse.jdt.core_textmodel_samples_encoding_2021-02-02\positivesamples/" + "/"
negative_samples_path:str = r"D:\buglocalization_gelareh_home\data\eclipse.jdt.core_textmodel_samples_encoding_2021-02-02\negativesamples/" + "/"

# NOTE: Paths should not be too long, causes error (on Windows)!
model_training_save_dir = r"D:\buglocalization_gelareh_home\training\trained_model_" + datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S') + "/"
model_training_checkpoint_dir = model_training_save_dir + "checkpoints/"

#===============================================================================
# Data Processing
#===============================================================================


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
        
        # Free memory:
        for bug_sample_idx in range(start_bug_sample, end_bug_sample):
            bug_sample = self.bug_samples[bug_sample_idx]
            bug_sample.unload()
        
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
    
    def create_model(self, num_samples:List[int], layer_sizes:List[int], feature_size:int, dropout:float=0.0, normalize="l2") -> keras.Model:
        graphsage = GraphSAGE(
            n_samples=num_samples,
            layer_sizes=layer_sizes,
            input_dim=feature_size,
            multiplicity=2,  # Link inference!
            bias=True,
            dropout=dropout,  # Dropout supplied to each layer: 0 < dropout < 1, 0 means no dropout. Dropout refers to ignoring units (i.e. neurons) during the training phase of certain set of neurons which is chosen at random, to prevent over-fitting.
            normalize=normalize  # l2 or None
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
    
    def create_or_restore_model(self, num_samples:List[int], layer_sizes:List[int], feature_size:int, checkpoint_dir:str, dropout:float=0.0, normalize="l2") -> keras.Model:
       
        Path(checkpoint_dir).mkdir(parents=True, exist_ok=True)
       
        # Either restore the latest model, or create a fresh one if there is no checkpoint available.
        checkpoints = [checkpoint_dir + '/' + name for name in os.listdir(checkpoint_dir)]
        
        if checkpoints:
            latest_checkpoint = max(checkpoints, key=os.path.getctime)
            print('Restoring from', latest_checkpoint)
            return keras.models.load_model(latest_checkpoint)
        
        print('Creating a new model')
        return self.create_model(num_samples, layer_sizes, feature_size, dropout, normalize)

    
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
        # self.callbacks.append(TqdmCallback(verbose=2)) # logging during training
        
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
        
        # # Evaluate Trained Model # #
        self.evaluate(test_flow)
        
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
    num_samples = [20, 10]  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
    layer_sizes = [20, 20]  # Size of GraphSAGE hidden layers
    
    assert len(num_samples) == len(layer_sizes), "The number of neighbor node samples need to be specified per GraphSAGE layer!"
    
    # Regularization:
    
    # Dropout supplied to each GraphSAGE layer: 0 < dropout < 1, 0 means no dropout. 
    # Dropout refers to ignoring neurons during the training phase which are chosen at random to prevent over-fitting.
    # [https://medium.com/@amarbudhiraja/https-medium-com-amarbudhiraja-learning-less-to-learn-better-dropout-in-deep-machine-learning-74334da4bfc5]
    dropout = 0.0  # 0.3
    
    # GraphSAGE input normalization: l2 or None
    normalize = "l2"
    
    bug_localization_model_builder = BugLocalizationAIModelBuilder()
    model = bug_localization_model_builder.create_or_restore_model(
        num_samples, layer_sizes, dataset.feature_size, model_training_checkpoint_dir, dropout, normalize)
    
    # Plot model:
    # Install pydot, pydotplus, graphviz -> https://graphviz.org/download/ -> add to PATH -> reboot -> check os.environ["PATH"]
    # plot_model(model, to_file=model_training_checkpoint_dir + "model.png") # model_to_dot 
    
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
