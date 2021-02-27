'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

# ===============================================================================
# Configure GPU Device:
# https://towardsdatascience.com/setting-up-tensorflow-gpu-with-cuda-and-anaconda-onwindows-2ee9c39b5c44
# ===============================================================================
import tensorflow as tf  # type: ignore

# Only allocate needed memory needed by the application:
gpus = tf.config.experimental.list_physical_devices('GPU')

if gpus:
    try:
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
    except RuntimeError as e:
        print(e)
# ===============================================================================

import datetime
import os
from pathlib import Path
from time import time
from typing import List, Tuple
from word_to_vector_shared_dictionary import WordDictionary

import stellargraph as sg  # type: ignore
from stellargraph.layer import GraphSAGE, link_classification  # type: ignore
from tensorflow import keras  # type: ignore
from tensorflow.keras.callbacks import CSVLogger  # type: ignore
from tensorflow.keras.utils import Sequence  # type: ignore
from tensorflow.keras.utils import plot_model  # @UnusedImport

from bug_localization_data_set import IBugSample, IDataSet
from bug_localization_data_set_neo4j_training import DataSetTrainingNeo4j
from bug_localization_data_set_neo4j import Neo4jConfiguration
from bug_localization_meta_model_uml import create_uml_configuration
from bug_localization_sample_generator import (BugSampleGenerator,
                                               IBugSampleGenerator)


# from tqdm.keras import TqdmCallback  # type: ignore

# ===============================================================================
# Environmental Information
# ===============================================================================

# TODO: Legacy:
# positve_samples_path:str = r"D:\buglocalization_gelareh_home\data\eclipse.jdt.core_textmodel_samples_encoding_2021-02-02\positivesamples/" + "/"  # noqa: E501
# negative_samples_path:str = r"D:\buglocalization_gelareh_home\data\eclipse.jdt.core_textmodel_samples_encoding_2021-02-02\negativesamples/" + "/"  # noqa: E501

# NOTE: Paths should not be too long, causes error (on Windows)!
plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent
model_training_save_dir = str(plugin_directory) + '/training/trained_model_' + \
    datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S') + '/'
model_training_checkpoint_dir = model_training_save_dir + "checkpoints/"

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

# ===============================================================================
# Data Processing
# ===============================================================================


class DataSetSplitter:

    def __init__(self, dataset_positive: IDataSet, dataset_negative: IDataSet):
        self.dataset_positive: IDataSet = dataset_positive
        self.dataset_negative: IDataSet = dataset_negative
        self.bug_sample_sequence: List[IBugSample] = self.create_bug_sample_sequence()

    def create_bug_sample_sequence(self) -> List[IBugSample]:

        # Mix positive and negative samples to create a full set of samples
        # that can be split to create a training and validation set of samples:
        bug_sample_sequence = []
        index = 0

        while index < len(self.dataset_positive.bug_samples) or index < len(self.dataset_negative.bug_samples):
            if index < len(self.dataset_positive.bug_samples):
                bug_sample_sequence.append(self.dataset_positive.bug_samples[index])
            if index < len(self.dataset_negative.bug_samples):
                bug_sample_sequence.append(self.dataset_negative.bug_samples[index])

            index += 1

        return bug_sample_sequence[:50]

    def split(self, fraction: int) -> Tuple[List[IBugSample], List[IBugSample]]:

        # Split training and test data:
        split_idx = len(self.bug_sample_sequence) // fraction

        bug_samples_train = self.bug_sample_sequence[:split_idx]
        bug_samples_eval = self.bug_sample_sequence[split_idx:]

        return bug_samples_train, bug_samples_eval

# ===============================================================================
# AI Model
# ===============================================================================


class BugLocalizationAIModelBuilder:

    def create_model(self,
                     num_samples: List[int],
                     layer_sizes: List[int],
                     feature_size: int,
                     checkpoint_dir: str,
                     dropout: float = 0.0,
                     normalize="l2") -> keras.Model:

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

    def restore_model(self, model_dir: str) -> keras.Model:
        print('Restoring model from', model_dir)
        # https://stellargraph.readthedocs.io/en/stable/api.html -> stellargraph.custom_keras_layers= {...}
        return keras.models.load_model(model_dir, custom_objects=sg.custom_keras_layers)


class BugLocalizationAIModelTrainer:

    def __init__(self,
                 dataset_splitter: DataSetSplitter,
                 model: keras.Model,
                 num_samples: List[int],
                 checkpoint_dir: str,
                 use_multiprocessing: bool = False,
                 sample_generator_workers: int = 1,
                 sample_prefetch_count: int = 10):

        self.dataset_splitter: DataSetSplitter = dataset_splitter
        self.model: keras.Model = model
        self.num_samples: List[int] = num_samples
        self.use_multiprocessing = use_multiprocessing
        self.sample_generator_workers: int = sample_generator_workers
        self.sample_prefetch_count: int = sample_prefetch_count

        self.callbacks = []

        # This callback saves a SavedModel every epoch (or X batches).
        self.callbacks.append(self.Timer())
        self.callbacks.append(keras.callbacks.ModelCheckpoint(filepath=checkpoint_dir, save_freq='epoch'))
        self.callbacks.append(CSVLogger(checkpoint_dir + "model_history_log.csv", append=True))
        self.callbacks.append(self.BatchLogger())

    def train(self, epochs: int, sample_generator: IBugSampleGenerator, log_level=0):

        # Initialize training data:
        bug_samples_train, bug_samples_eval = dataset_splitter.split(2)
        train_flow = sample_generator.create_bug_sample_generator("training", bug_samples_train, self.callbacks)
        eval_flow = sample_generator.create_bug_sample_generator("evaluation", bug_samples_eval, self.callbacks)

        # # Train Model # #
        # self.callbacks.append(TqdmCallback(verbose=2)) # logging during training
        history = self.model.fit(
            train_flow,
            epochs=epochs,
            validation_data=eval_flow,
            verbose=log_level,
            use_multiprocessing=self.use_multiprocessing,
            workers=self.sample_generator_workers,
            max_queue_size=self.sample_prefetch_count,
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
    def evaluate(self, evaluation_flow: Sequence):
        evaluation_metrics = self.model.evaluate(evaluation_flow)

        print("Evaluation metrics of the model:")
        for name, val in zip(self.model.metrics_names, evaluation_metrics):
            print("\t{}: {:0.4f}".format(name, val))

    class Timer(keras.callbacks.Callback):

        def __init__(self):
            self.mark = time()

        def on_epoch_begin(self, epoch, logs=None):
            self.mark = time()

        def on_epoch_end(self, epoch, logs=None):
            duration = time() - self.mark
            self.mark = time()
            print("Epoch", epoch, "time:", duration)

    class BatchLogger(keras.callbacks.Callback):

        def __init__(self):
            self.seen = 0

        def on_train_batch_end(self, batch, logs={}):
            if 'loss' in logs and 'acc' in logs:
                print("Training batch finished:", batch, "Loss:", logs['loss'], "Accuracy:", logs['acc'])
            else:
                print("Training batch finished:", batch)

        def on_test_batch_end(self, batch, logs={}):
            if 'loss' in logs and 'acc' in logs:
                print("Evaluation batch finished:", batch, "Loss:", logs['loss'], "Accuracy:", logs['acc'])
            else:
                print("Evaluation batch finished:", batch)


if __name__ == '__main__':

    # ===========================================================================
    # Create AI Model:
    # ===========================================================================

    # Wprd embedding:
    word_dictionary = WordDictionary()

    # GraphSAGE Settings:
    num_samples = [20, 10]  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
    layer_sizes = [20, 20]  # Size of GraphSAGE hidden layers

    assert len(num_samples) == len(layer_sizes), "The number of neighbor node samples need to be specified per GraphSAGE layer!"

    print('Save Training:', model_training_save_dir)

    # Regularization:

    # Dropout supplied to each GraphSAGE layer: 0 < dropout < 1, 0 means no dropout.
    # Dropout refers to ignoring neurons during the training phase which are chosen at random to prevent over-fitting.
    # [https://medium.com/@amarbudhiraja/https-medium-com-amarbudhiraja-learning-less-to-learn-better-dropout-in-deep-machine-learning-74334da4bfc5]
    dropout = 0.0  # 0.3

    # GraphSAGE input normalization: l2 or None
    normalize = "l2"

    bug_localization_model_builder = BugLocalizationAIModelBuilder()
    model = bug_localization_model_builder.create_model(
        num_samples, layer_sizes, word_dictionary.dimension(), model_training_checkpoint_dir, dropout, normalize)

    # Plot model:
    # Install pydot, pydotplus, graphviz -> https://graphviz.org/download/ -> add to PATH -> reboot -> check os.environ["PATH"]
    # plot_model(model, to_file=model_training_checkpoint_dir + "model.png") # model_to_dot

    # ===========================================================================
    # Create Training and Test Data:
    # ===========================================================================

    # Modeling Language Meta-Model Configuration:
    meta_model, node_self_embedding, typebased_slicing = create_uml_configuration(word_dictionary, num_samples)

    # Test Dataset Containing Bug Samples:
    dataset_positive = DataSetTrainingNeo4j(meta_model, node_self_embedding, typebased_slicing, neo4j_configuration, is_negative=False)
    dataset_negative = DataSetTrainingNeo4j(meta_model, node_self_embedding, typebased_slicing, neo4j_configuration, is_negative=True)
    dataset_splitter = DataSetSplitter(dataset_positive, dataset_negative)

    # ===========================================================================
    # Train and Evaluate AI Model:
    # ===========================================================================

    # Training Settings:
    epochs = 20  # Number of training epochs.
    batch_size = 20  # Number of bug location samples, please node that each sample has multiple location samples.
    shuffle = True  # Shuffle training and validation samples after each epoch?
    generator_workers = 4  # Number of threads that load/generate the batches in parallel.
    multiprocessing = False  # # True -> Workers as process, False -> Workers as threads. Might cause deadlocks with more then 2-3 worker processes!
    sample_prefetch_count = 8  # Preload some data for fast (GPU) processing
    log_level = 2  # Some console output for debugging...

    bug_localization_generator = BugSampleGenerator(
        batch_size,
        shuffle,
        num_samples,
        log_level)

    bug_localization_model_trainer = BugLocalizationAIModelTrainer(
        dataset_splitter=dataset_splitter,
        model=model,
        num_samples=num_samples,
        checkpoint_dir=model_training_checkpoint_dir,
        sample_generator_workers=generator_workers,
        sample_prefetch_count=sample_prefetch_count,
        use_multiprocessing=multiprocessing)

    bug_localization_model_trainer.train(epochs, bug_localization_generator, log_level)
