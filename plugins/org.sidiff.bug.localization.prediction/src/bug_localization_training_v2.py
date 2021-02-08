'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

#===============================================================================
# Configure GPU Device:
# https://towardsdatascience.com/setting-up-tensorflow-gpu-with-cuda-and-anaconda-onwindows-2ee9c39b5c44
#===============================================================================
import tensorflow as tf # type: ignore

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
from time import time
from typing import List, Tuple

from stellargraph.layer import GraphSAGE, link_classification  # type: ignore
from stellargraph.mapper import GraphSAGELinkGenerator  # type: ignore
from tensorflow import keras  # type: ignore

from bug_localization_data_set import DataSetEmbedding, DataSetBugSampleEmbedding
import stellargraph as sg  # type: ignore
from tensorflow.keras.callbacks import CSVLogger  # type: ignore
from tensorflow.keras.utils import Sequence  # type: ignore

# from tqdm.keras import TqdmCallback  # type: ignore
# from tensorflow.keras.utils import plot_model # type: ignore
#===============================================================================
# Environmental Information
#===============================================================================

# positve_samples_path:str = r"D:\buglocalization_gelareh_home\data\eclipse.jdt.core_textmodel_samples_encoding_2021-02-02\positivesamples/" + "/"
# negative_samples_path:str = r"D:\buglocalization_gelareh_home\data\eclipse.jdt.core_textmodel_samples_encoding_2021-02-02\negativesamples/" + "/"

positve_samples_path:str = r"C:\Users\manue\git\buglocalization\research\org.sidiff.bug.localization.dataset.domain.eclipse\datasets\eclipse.jdt.core\DataSet_20201123160235\encoding\positivesamples" + "/"
negative_samples_path:str = r"C:\Users\manue\git\buglocalization\research\org.sidiff.bug.localization.dataset.domain.eclipse\datasets\eclipse.jdt.core\DataSet_20201123160235\encoding\negativesamples" + "/"


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


class BugLocalizationGenerator:
    
    #===========================================================================
    # Adapts the GraphSAGE interface with our data and Tensorflow's tf.data.Dataset
    #===========================================================================
    
    # https://cs230.stanford.edu/blog/datapipeline/
    # https://sknadig.dev/TensorFlow2.0-dataset/
    
    # Note: A sample is pair of a bug report and model location, each "bug sample" contains one bug report and multiple locations.

    def __init__(self, name:str, model:keras.Model, batch_size:int, shuffle:bool, num_samples:List[int], bug_samples:List[DataSetBugSampleEmbedding], generator_workers:int=1, prefetch:int=10, log_level:int=0):
        self.name:str = name  # e.g. train, evaluation for debugging
        self.model:keras.Model = model
        self.batch_size:int = batch_size
        self.shuffle = shuffle
        self.generator_workers = generator_workers
        self.num_samples:List[int] = num_samples
        self.bug_samples:List[DataSetBugSampleEmbedding] = bug_samples
        self.prefetch = prefetch
        self.log_level:int = log_level
    
    # TODO: Transfer sample object!?
    def list_samples_generator(self) -> str:
        for bug_sample in self.bug_samples:
            yield bug_sample.path, bug_sample.name, bug_sample.number, bug_sample.is_negative
            
    def sample_generator(self, path:bytes, name:bytes, number:bytes, is_negative:bool):
        bug_sample = DataSetBugSampleEmbedding()
        bug_sample.path = path.decode("utf-8")
        bug_sample.name = name.decode("utf-8")
        bug_sample.number = number.decode("utf-8")
        bug_sample.is_negative = is_negative
        
        # Load each bug sample:
        bug_sample.load(add_prefix=False)
        
        if self.log_level >= 3:
            print("Loaded", "negative" if bug_sample.is_negative else "positive", self.name, "sample:", bug_sample.number)

        if (len(bug_sample.testcase_labels) <= 0):
            return

        # Convert to StellarGraph:
        graph = sg.StellarGraph(bug_sample.nodes, bug_sample.edges)
    
        if self.log_level >= 4:
            print(graph.info())
    
        # Create Keras Sequence with batch size 1 for generator yield:
        graph_sage_generator = GraphSAGELinkGenerator(graph, batch_size=len(bug_sample.testcase_labels), num_samples=self.num_samples)
        flow = graph_sage_generator.flow(bug_sample.bug_location_pairs, bug_sample.testcase_labels)
    
        # Free memory:
        bug_sample.unload()
    
        # Convert Keras Sequence to generator:
        for batch_num in range(len(flow)):
            #=======================================================================
            # [[Layer 0 sources], [Layer 0 targets], [Layer 1 sources], [Layer 1 targets], ...]
            # ,
            # [Binary label]
            #=======================================================================
            batch_feats, batch_targets = flow.__getitem__(batch_num)          
            
            for sample_num in range(len(batch_targets)):
                inputs = []
                outputs = batch_targets[sample_num]
                
                for batch_feat in batch_feats:
                    inputs.append(batch_feat[sample_num])
                
                yield tuple(inputs), outputs  # single sample
       
    def create_generator(self) -> tf.data.Dataset:
        model_input_types, model_input_shapes = self.get_model_input_shape()
        
        # Iterates over all samples (without loading)
        Dataset = tf.data.Dataset
        dataset = Dataset.from_generator(self.list_samples_generator, output_types=(tf.string, tf.string, tf.string, tf.bool))
        
        # Shuffle dataset:
        if self.shuffle:
            dataset = dataset.shuffle(len(self.bug_samples))
        
        # Load dataset from disk in separate threads:
        # print(self.sample_generator().__next__())
        # tf.data.AUTOTUNE
        dataset = dataset.interleave(lambda path, name, number, is_negative: 
                                     Dataset.from_generator(self.sample_generator, output_types=model_input_types, output_shapes=model_input_shapes,
                                                            args=(path, name, number, is_negative)),
                                     cycle_length=self.generator_workers * 2, block_length=1, num_parallel_calls=self.generator_workers)        
        
        # Set the size of the batch to the data pipline:
        dataset = dataset.batch(self.batch_size)
        
        # Preload some samples (for GPU):
        dataset = dataset.prefetch(self.prefetch)
        
        return dataset

    def get_model_input_shape(self):
        # https://stackoverflow.com/questions/57175343/multiple-inputs-of-keras-model-with-tf-data-dataset-from-generator-in-tensorflow
        # num_samples = [20, 10]
        # layer_sizes = [20, 20]
        # node feature size = 300 
        # model_input_types = ((tf.float32, tf.float32, tf.float32, tf.float32, tf.float32, tf.float32), (tf.float32))
        # model_input_shapes = (([1, 300], [1, 300], [20, 300], [20, 300], [200, 300], [200, 300]), ())
        tmp_model_input_types = []
        tmp_model_input_shapes = []
        
        for input_tensor in self.model.input:
            tmp_model_input_types.append(tf.float32)
            input_tensor_shape = [input_tensor.shape.dims[1].value, input_tensor.shape.dims[2].value]
            tmp_model_input_shapes.append(input_tensor_shape)
        
        # model input + labels
        model_input_types = (tuple(tmp_model_input_types), (tf.float32)) 
        model_input_shapes = (tuple(tmp_model_input_shapes), ())
        
        return model_input_types, model_input_shapes

        
class BugLocalizationAIModelBuilder:
    
    def create_model(self, num_samples:List[int], layer_sizes:List[int], feature_size:int, checkpoint_dir:str, dropout:float=0.0, normalize="l2") -> keras.Model:
        print('Creating a new model')
        Path(checkpoint_dir).mkdir(parents=True, exist_ok=True)
        
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
    
    def restore_model(self, model_dir:str) -> keras.Model:
        print('Restoring model from', model_dir)
        # https://stellargraph.readthedocs.io/en/stable/api.html -> stellargraph.custom_keras_layers= {...}
        return keras.models.load_model(model_dir, custom_objects=sg.custom_keras_layers)

    
class BugLocalizationAIModelTrainer:
    
    def __init__(self, dataset_splitter:DataSetSplitter, model:keras.Model, num_samples:List[int], checkpoint_dir:str):
        self.dataset_splitter:DataSetSplitter = dataset_splitter
        self.model:keras.Model = model
        self.num_samples:List[int] = num_samples
        
        self.callbacks = []
        
        # This callback saves a SavedModel every epoch (or X batches).
        self.callbacks.append(self.Timer())
        self.callbacks.append(keras.callbacks.ModelCheckpoint(filepath=checkpoint_dir, save_freq='epoch'))
        self.callbacks.append(CSVLogger(checkpoint_dir + "model_history_log.csv", append=True))
        self.callbacks.append(self.BatchLogger())
    
    def train(self, epochs:int, batch_size:int, shuffle:bool, generator_workers:int=1, prefetch:int=10, log_level=0):
        
        # Initialize training data:
        bug_samples_train, bug_samples_test = dataset_splitter.split(2)
        train_flow = BugLocalizationGenerator("training", self.model, batch_size, shuffle, self.num_samples, bug_samples_train, generator_workers, prefetch, log_level)
        test_flow = BugLocalizationGenerator("validation", self.model, batch_size, shuffle, self.num_samples, bug_samples_test, generator_workers, prefetch, log_level)
        
        # self.callbacks.append(TqdmCallback(verbose=2)) # logging during training
        
        # # Train Model # #
        history = self.model.fit(
            train_flow.create_generator(),
            epochs=epochs,
            validation_data=test_flow.create_generator(),
            verbose=log_level,
            use_multiprocessing=True,
            workers=-generator_workers,
            max_queue_size=prefetch,
            callbacks=self.callbacks)

        if log_level >= 1:
            sg.utils.plot_history(history)
        
        # # Save Final Trained Model # #     
        self.model.save(model_training_save_dir)
        
        # # Evaluate Trained Model # #
        self.evaluate(test_flow)
    
    # TODO: Write to file    
    def evaluate(self, evaluation_flow:Sequence):
        evaluation_metrics = self.model.evaluate(evaluation_flow)

        print("Evaluation metrics of the model:")
        for name, val in zip(self.model.metrics_names, evaluation_metrics):
            print("\t{}: {:0.4f}".format(name, val))

    class Timer(keras.callbacks.Callback):

        def __init__(self):
            self.mark = time()

        def on_epoch_begin(self, epoch, logs=None):  # @UnusedVariable
            self.mark = time()

        def on_epoch_end(self, epoch, logs=None):  # @UnusedVariable
            duration = time() - self.mark
            self.mark = time()
            print("Epoch", epoch, "time:", duration)

    class BatchLogger(keras.callbacks.Callback):

        def __init__(self):
            self.seen = 0
    
        def on_batch_end(self, batch, logs={}):  # @UnusedVariable
            self.seen += logs.get('size', 0)
            metrics_log = ''
            for k in self.params['metrics']:
                if k in logs:
                    val = logs[k]
                    if abs(val) > 1e-3:
                        metrics_log += ' - %s: %.4f' % (k, val)
                    else:
                        metrics_log += ' - %s: %.4e' % (k, val)
            print('{}{}'.format(self.seen, metrics_log))


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
    model = bug_localization_model_builder.create_model(
        num_samples, layer_sizes, dataset.feature_size, model_training_checkpoint_dir, dropout, normalize)
    
    # Plot model:
    # Install pydot, pydotplus, graphviz -> https://graphviz.org/download/ -> add to PATH -> reboot -> check os.environ["PATH"]
    # plot_model(model, to_file=model_training_checkpoint_dir + "model.png") # model_to_dot 
    
    #===========================================================================
    # Train and Evaluate AI Model:
    #===========================================================================
    
    # Training Settings:
    epochs = 20  # Number of training epochs. 
    batch_size = 40  # Number of bug location samples, please node that each sample has multiple location samples.
    shuffle = True  # Shuffle training and validation samples after each epoch?
    
    # Technical Settings:
    generator_workers = 8  # Number of threads that load/generate the batches in parallel.
    prefetch = batch_size * 2  # Preload some data for fast (GPU) processing
    log_level = 2  # Some console output for debugging...
    
    bug_localization_model_trainer = BugLocalizationAIModelTrainer(
        dataset_splitter=dataset_splitter,
        model=model,
        num_samples=num_samples,
        checkpoint_dir=model_training_checkpoint_dir)
    
    bug_localization_model_trainer.train(epochs, batch_size, shuffle, generator_workers, prefetch, log_level)
