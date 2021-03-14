'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from pathlib import Path
from time import time
from typing import List, Tuple

import stellargraph as sg
from buglocalization.dataset.data_set import IBugSample, IDataSet
from buglocalization.dataset.sample_generator import IBugSampleGenerator
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from buglocalization.utils import common_utils as utils
from stellargraph.layer import GraphSAGE, link_classification
from tensorflow import keras
from tensorflow.keras.callbacks import CSVLogger
from tensorflow.keras.utils import Sequence

# ===============================================================================
# Training Parameter
# ===============================================================================


class TrainigConfiguration:

    def __init__(self, 
                 doc_description: str,
                 model_training_base_directory: str,
                 optimizer_learning_rate: float = 1e-4,
                 trainig_epochs: int = 20,
                 trainig_batch_size: int = 20,
                 dataset_split_fraction: int = -1,
                 dataset_shuffle: bool = True,
                 dataset_generator_workers: int = 4,
                 dataset_multiprocessing: bool = True,
                 dataset_sample_prefetch_count: int = 8,
                 graphsage_num_samples: List[int] = [20, 10],
                 graphsage_layer_sizes: List[int] = [300, 300],
                 graphsage_dropout: float = 0.0,
                 graphsage_normalize: str = "l2",
                 word_dictionary: WordToVectorDictionary = None,
                 log_level: int = 2) -> None:
        
        # Doc description:
        self.doc_description = doc_description

        # Save folders:
        self.model_training_save_dir = model_training_base_directory + '/training/trained_model_' + utils.create_timestamp() + '/'
        self.model_training_checkpoint_dir = self.model_training_save_dir + "checkpoints/"

        # Training parameters:
        self.optimizer_learning_rate: float = optimizer_learning_rate
        self.trainig_epochs: int = trainig_epochs  # Number of training epochs.
        self.trainig_batch_size: int = trainig_batch_size  # Number of bug location samples, each sample has multiple location samples.

        self.dataset_split_fraction: int = dataset_split_fraction  # 2 => 50% training, 50% validation, -1 => 100% training data
        self.dataset_shuffle: bool = dataset_shuffle  # Shuffle training and validation samples after each epoch?
        self.dataset_generator_workers: int = dataset_generator_workers  # Number of threads that load/generate the batches in parallel.
        self.dataset_multiprocessing: bool = dataset_multiprocessing  # True -> Workers as process, False -> Workers as threads.
        self.dataset_sample_prefetch_count: int = dataset_sample_prefetch_count  # Preload some data for fast (GPU) processing

        self.log_level: int = log_level  # Some console output for debugging...

        # Word embedding:
        if word_dictionary is not None:
            self.word_dictionary: WordToVectorDictionary = word_dictionary
        else:
            self.word_dictionary = WordToVectorDictionary()

        # GraphSAGE Settings:
        self.graphsage_num_samples = graphsage_num_samples  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
        self.graphsage_layer_sizes = graphsage_layer_sizes  # Size of GraphSAGE hidden layers

        assert len(self.graphsage_num_samples) == len(self.graphsage_layer_sizes), "Inconsistent GraphSAGE layer configuration!"

        # Regularization:

        # Dropout supplied to each GraphSAGE layer: 0 < dropout < 1, 0 means no dropout.
        # Dropout refers to ignoring neurons during the training phase which are chosen at random to prevent over-fitting.
        # [https://medium.com/@amarbudhiraja/https-medium-com-amarbudhiraja-learning-less-to-learn-better-dropout-in-deep-machine-learning-74334da4bfc5]
        self.graphsage_dropout: float = graphsage_dropout  # 0.3

        # GraphSAGE input normalization: l2 or None
        self.graphsage_normalize: str = graphsage_normalize
        
    def dump(self) -> dict:
        dump_state = dict(self.__dict__)
            
        if 'word_dictionary' in dump_state:
            dump_state['word_dictionary'] = self.word_dictionary.dump()

        return dump_state

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

        return bug_sample_sequence

    def split(self, fraction: int) -> Tuple[List[IBugSample], List[IBugSample]]:

        # Split training and test data:
        if fraction > 0:
            split_idx = len(self.bug_sample_sequence) // fraction
        else:
            split_idx = len(self.bug_sample_sequence)  # only training data

        bug_samples_train = self.bug_sample_sequence[:split_idx]

        if split_idx < len(self.bug_sample_sequence):
            bug_samples_eval = self.bug_sample_sequence[split_idx:]
        else:
            bug_samples_eval = []  # only training data

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
                     normalize="l2",
                     optimizer_learning_rate: float = 1e-3) -> keras.Model:

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
            optimizer=keras.optimizers.Adam(lr=optimizer_learning_rate),
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

    def train(self, epochs: int, sample_generator: IBugSampleGenerator, model_training_save_dir: str, dataset_split_fraction: int = 2, log_level=0):

        # Initialize training data:
        bug_samples_train, bug_samples_eval = self.dataset_splitter.split(dataset_split_fraction)
        train_flow = sample_generator.create_bug_sample_generator("training", bug_samples_train, self.callbacks)
        eval_flow = sample_generator.create_bug_sample_generator("evaluation", bug_samples_eval, self.callbacks)

        print("Training Samples:", len(bug_samples_train), " Batches:", len(train_flow))
        print("Evaluation Samples:", len(bug_samples_eval), "Batches:", len(eval_flow))

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

        # # Save Final Trained Model # #
        self.model.save(model_training_save_dir)

        if log_level >= 1:
            for layer in self.model.layers:
                print(layer.get_config(), layer.get_weights())

            sg.utils.plot_history(history)

        # # Evaluate Trained Model # #
        if len(eval_flow) > 0:
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
                print("Training batch", batch, "finished: Accuracy:", logs['acc'], "Loss:", logs['loss'])
            else:
                print("Training batch", batch, "finished")

        def on_test_batch_end(self, batch, logs={}):
            if 'loss' in logs and 'acc' in logs:
                print("Training batch", batch, "finished: Accuracy:", logs['acc'], "Loss:", logs['loss'])
            else:
                print("Training batch", batch, "finished")
