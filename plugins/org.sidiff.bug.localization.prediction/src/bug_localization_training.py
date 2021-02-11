'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

#===============================================================================
# Configure GPU Device:
# https://towardsdatascience.com/setting-up-tensorflow-gpu-with-cuda-and-anaconda-onwindows-2ee9c39b5c44
#===============================================================================
import tensorflow as tf  # type: ignore

# Only allocate needed memory needed by the application:
gpus = tf.config.experimental.list_physical_devices('GPU')

if gpus:
    try:
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
    except RuntimeError as e:
        print(e)
#===============================================================================

from bug_localization_data_set import DataSetEmbedding, DataSetBugSampleEmbedding  # type: ignore
from bug_localization_sample_generator import IBugLocalizationGenerator, BugLocalizationGenerator
from pathlib import Path
from stellargraph.layer import GraphSAGE, link_classification  # type: ignore
from tensorflow import keras  # type: ignore
from tensorflow.keras.callbacks import CSVLogger  # type: ignore
from tensorflow.keras.utils import Sequence, plot_model  # type: ignore @UnusedImport
from time import time
from typing import List, Tuple
import datetime
import stellargraph as sg  # type: ignore

# from tqdm.keras import TqdmCallback  # type: ignore

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
        bug_samples_eval = self.bug_sample_sequence[split_idx:]
        
        return bug_samples_train, bug_samples_eval

#===============================================================================
# AI Model
#===============================================================================


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
            dropout=dropout,  # Dropout supplied to each layer: 0 < dropout < 1, 0 means no dropout.
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
        
    def train(self, epochs:int, sample_generator:IBugLocalizationGenerator, log_level=0):
        
        # Initialize training data:
        bug_samples_train, bug_samples_eval = dataset_splitter.split(2)
        train_flow, train_callbacks = sample_generator.get_evaluation_generator(bug_samples_train)
        eval_flow, eval_callbacks = sample_generator.get_evaluation_generator(bug_samples_eval)
        
        self.callbacks.extend(train_callbacks)
        self.callbacks.extend(eval_callbacks)
        
        # # Train Model # #
        # self.callbacks.append(TqdmCallback(verbose=2)) # logging during training
        history = self.model.fit(
            train_flow,
            epochs=epochs,
            validation_data=eval_flow,
            verbose=log_level,
            use_multiprocessing=sample_generator.multiprocessing,
            workers=sample_generator.generator_workers,
            max_queue_size=sample_generator.sample_prefetch_count,
            callbacks=self.callbacks)

        if log_level >= 1:
            for layer in model.layers: 
                print(layer.get_config(), layer.get_weights())
                
            sg.utils.plot_history(history)
        
        # # Save Final Trained Model # #     
        self.model.save(model_training_save_dir)
        
        # # Evaluate Trained Model # #
        self.evaluate(eval_flow)
    
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
            if 'metrics' in self.params:
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
            else:
                print("Batch:", batch)


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
    generator_workers = 2  # Number of threads that load/generate the batches in parallel.
    multiprocessing = True  # # True -> Workers as process, False -> Workers as threads. Might cause deadlocks with more then 2-3 worker processes!
    sample_prefetch_count = batch_size * 2  # Preload some data for fast (GPU) processing
    log_level = 2  # Some console output for debugging...
    
    bug_localization_generator = BugLocalizationGenerator(
        num_samples,
        batch_size,
        shuffle,
        generator_workers,
        sample_prefetch_count,
        multiprocessing,
        log_level)
    
    bug_localization_model_trainer = BugLocalizationAIModelTrainer(
        dataset_splitter=dataset_splitter,
        model=model,
        num_samples=num_samples,
        checkpoint_dir=model_training_checkpoint_dir)
    
    bug_localization_model_trainer.train(epochs, bug_localization_generator, log_level)
