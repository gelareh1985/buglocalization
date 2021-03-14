'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import random
import sys
from time import time
from typing import Any, Callable, List, Optional, Tuple, Union, final

import numpy as np
import pandas as pd
from buglocalization.dataset.data_set import IBugSample, ILocationSample
from buglocalization.textembedding.word_to_vector_dictionary import WordToVectorDictionary
from buglocalization.utils.common_utils import t
from stellargraph.mapper import GraphSAGELinkGenerator, StellarGraph
from tensorflow import keras
from tensorflow.keras import Input, Model
from tensorflow.keras.layers import concatenate
from tensorflow.keras.utils import Sequence

# ===============================================================================
# Data Generator: A "location sample" is pair of a bug report and model location,
#                 each "bug sample" contains one bug report and multiple locations,
#                 i.e., each "bug sample" contains multiple "location sample".
#
# https://www.tensorflow.org/guide/keras/train_and_evaluate#using_a_kerasutilssequence_object_as_input
# ===============================================================================

# Use with caution, to ignore exceptions during sample generation!
RETURN_DUMMY_SAMPLES_ON_EXCEPTION = False


class IBugSampleGenerator:

    def create_bug_sample_generator(self, name: str,
                                    bug_samples: List[IBugSample],
                                    callbacks: List[keras.callbacks.Callback]) -> Sequence:
        """
        Generates a batch from a number of "bug samples".
        Each "bug sample" could contain multiple "location samples", i.e., the batch size is not fixed w.r.t. the "location samples"".
        For example, training a new Keras model from a set of bug samples.

        Args:
            name (str): A descriptive name, e.g., for exceptions and debugging.
            bug_samples (List[IBugSample]): A set of "bug samples".
            callbacks (List[keras.callbacks.Callback]): The list of callbacks for the Keras API.

        Returns:
            Tuple[Sequence, List[keras.callbacks.Callback]]: A Keras input Sequence and some callbacks for the training/prediction.
        """
        raise NotImplementedError


class ILocationSampleGenerator:

    def create_location_sample_generator(self, name: str,
                                         bug_sample: IBugSample,
                                         callbacks: List[keras.callbacks.Callback]) -> Sequence:
        """
        Generates batches from a number of "location samples" which are contained in the same "bug sample".
        For example, predicting the bug locations for a single model verison.

        Args:
            name (str): A descriptive name, e.g., for exceptions and debugging.
            bug_sample (IBugSample): The "bug sample" that contains/corresponds to the given "location samples".
            callbacks (List[keras.callbacks.Callback]): The list of callbacks for the Keras API.

        Returns:
            Tuple[Sequence, List[keras.callbacks.Callback]]: A Keras input Sequence and some callbacks for the training/prediction.
        """
        raise NotImplementedError


class SampleBaseGenerator:

    def __init__(self,
                 batch_size: int,
                 shuffle: bool,
                 num_samples: List[int],
                 log_level: int = 0):

        self.batch_size: int = batch_size
        self.shuffle = shuffle
        self.num_samples: List[int] = num_samples
        self.log_level: int = log_level

        # A model wrapping the giving model, to output sample IDs alongside the prediction results:
        self.reshape_input: Optional[Callable] = None

    def create_prediction_model(self, model: Model, concatenate_results_with_sample_ids: bool = False) -> Model:
        """
        Args:
            model (Model): The actual trained model.
            concatenate_results_with_sample_ids (bool, optional): [description]. Defaults to False.

        Returns:
            Model: A model wrapping the giving model, to output sample IDs alongside the prediction results.
        """

        # Create the second input to the model for passing through the samle IDs to the output
        sample_id_input_layer = Input(shape=(1), name='SampleID')

        # Build the wrapper model
        if concatenate_results_with_sample_ids:
            predicate_and_sample_id_concatenate_layer = concatenate([model.output, sample_id_input_layer])
            prediction_wrapper_model = Model([model.input, sample_id_input_layer], predicate_and_sample_id_concatenate_layer)
        else:
            prediction_wrapper_model = Model([model.input, sample_id_input_layer], [model.output, sample_id_input_layer])

        # Expose the wrapper model to the generator:
        self.reshape_input = self.create_prediction_model_input

        # Return the model to be used for predict()
        return prediction_wrapper_model

    def create_prediction_model_input(self, input_data: Any, sample_ids: Any) -> List:
        return [input_data, sample_ids]


class BaseSequence(Sequence):

    def __init__(self, name: str,
                 samples: Union[List[IBugSample], List[ILocationSample]],
                 batch_size: int,
                 shuffle: bool,
                 num_samples: List[int],
                 reshape_input: Callable = None,
                 log_level: int = 0):

        self.name: str = name
        self.samples: Union[List[IBugSample], List[ILocationSample]] = samples
        self.batch_size: int = batch_size
        self.shuffle = shuffle
        self.num_samples: List[int] = num_samples
        self.reshape_input: Optional[Callable] = reshape_input
        self.log_level: int = log_level

    def create_batch(self, start_sample: int) -> Tuple[np.ndarray, np.array]:
        raise NotImplementedError

    def combine_to_batch(self, sample_count: int, bug_location_samples: List[Tuple]):
        input_node_count = self.get_input_node_count()
        bug_location_input_batch = []  # List of input nodes of the GraphSAGE model
        label_type = self.get_lable_type(bug_location_samples)
        bug_location_label_batch = np.empty(shape=(sample_count), dtype=label_type)

        # Copy input shape with full batch size:
        for input_node_idx in range(input_node_count):
            input_type, samples_per_layer, feature_size = self.get_input_shape(input_node_idx, bug_location_samples, True)
            bug_location_input_batch.append(np.empty(shape=(sample_count, samples_per_layer, feature_size), dtype=input_type))

        # Combine samples:
        current_sample_idx = 0

        for bug_location_inputs, bug_location_labels in bug_location_samples:
            for bug_location_sample_idx in range(len(bug_location_labels)):

                # Input nodes:
                for input_node in range(input_node_count):
                    bug_location_input_batch[input_node][current_sample_idx] = bug_location_inputs[input_node][bug_location_sample_idx]

                # Output lables:
                bug_location_label_batch[current_sample_idx] = bug_location_labels[bug_location_sample_idx]

                current_sample_idx += 1

        return bug_location_input_batch, bug_location_label_batch

    # TODO: Compute or read shape from model!?

    def get_lable_type(self, bug_location_samples: List):
        lable_type = bug_location_samples[0][1].dtype
        return lable_type

    def get_input_node_count(self) -> int:
        return (len(self.num_samples) + 1) * 2

    def get_input_shape(self, input_node_idx: int, bug_location_samples: List, has_labels: bool) -> Tuple[np.dtype, np.float32, np.float32]:
        # Input layer size (x2): 1
        # Hidden layer size (x2): np.cumprod(self.num_samples[:current_layer])
        # Input-Layer-A, Input-Layer-B, 1.Layer-A, 1.Layer-B, 2.Layer-A, 2.Layer-B, ...
        if has_labels:
            # Tuple with input and lable:
            input_node_sample = bug_location_samples[0][0][input_node_idx]
        else:
            # input only:
            input_node_sample = bug_location_samples[0][input_node_idx]
        input_shape = input_node_sample.shape
        input_type = input_node_sample.dtype
        samples_per_layer = input_shape[1]
        feature_size = input_shape[2]

        return input_type, samples_per_layer, feature_size

    def create_location_sequence(self, bug_sample: IBugSample, location_sample: ILocationSample) -> Sequence:
        try:
            location_sample.initialize(bug_sample, self.log_level)  # see finally

            graph = location_sample.graph()
            bug_location_pairs = [(location_sample.bug_report(), location_sample.model_location())]
            bug_location_label = location_sample.label()

            # Read Keras Sequence from StellarGraph (should only be one):
            graph_sage_generator = GraphSAGELinkGenerator(graph, len(bug_location_pairs), num_samples=self.num_samples)

            if bug_location_label is not None:
                flow = graph_sage_generator.flow(bug_location_pairs, [bug_location_label])
            else:
                flow = graph_sage_generator.flow(bug_location_pairs)

            return flow
        except:
            print("Unexpected error:", sys.exc_info()[0], sys.exc_info()[1])

            if RETURN_DUMMY_SAMPLES_ON_EXCEPTION:
                # TODO: Log and investigate if we came here!
                # Return a dummy sample as last rescue before crashing the evaluation:
                print("  Return Dummy Sample...")
                dummy_edges = pd.DataFrame(columns=["source", "target"])
                dummy_nodes = np.asarray(
                    [np.random.random_sample(WordToVectorDictionary().dimension()),
                     np.random.random_sample(WordToVectorDictionary().dimension())])
                dummy_graph = StellarGraph(nodes=dummy_nodes, edges=dummy_edges)
                graph_sage_dummy_generator = GraphSAGELinkGenerator(dummy_graph, 1, num_samples=self.num_samples)
                dummy_flow = graph_sage_dummy_generator.flow([(0, 1)], [0])
                return dummy_flow
            else:
                raise
        finally:
            # Free memory:
            location_sample.uninitialize()

    def __len__(self):
        """Denotes the number of batches per epoch"""
        return int(np.ceil(len(self.samples) / float(self.batch_size)))

    def __getitem__(self, batch_idx):
        """Generate one batch of data"""
        if self.log_level >= 3:
            print("Get", self.name, "batch index:", batch_idx)

        # Loading data batch:
        bug_location_samples, bug_location_sample_labels = self.create_batch(batch_idx * self.batch_size)

        if self.reshape_input is not None:
            # ..., e.g., to fit the input for the prediction wrapper model:
            return self.reshape_input(bug_location_samples, bug_location_sample_labels)
        else:
            return bug_location_samples, bug_location_sample_labels

    class SequenceCallback(keras.callbacks.Callback):

        def __init__(self, flow):
            self.flow = flow

        def on_epoch_end(self, epoch, logs=None):
            """Prepare the data set for the next epoch"""

            # Shuffle samples:
            if self.flow.shuffle:
                random.shuffle(self.flow.samples)


class BugSampleGenerator(IBugSampleGenerator, SampleBaseGenerator):

    def create_bug_sample_generator(self, name: str,
                                    bug_samples: List[IBugSample],
                                    callbacks: List[keras.callbacks.Callback]) -> Sequence:

        flow = self.BugSampleSequence(name,
                                      self.batch_size,
                                      self.shuffle,
                                      self.num_samples,
                                      bug_samples,
                                      callbacks,
                                      self.reshape_input,
                                      self.log_level)
        return flow

    class BugSampleSequence(BaseSequence):

        def __init__(self, name: str,
                     batch_size: int,
                     shuffle: bool,
                     num_samples: List[int],
                     bug_samples: List[IBugSample],
                     callbacks: List[keras.callbacks.Callback],
                     reshape_input: Callable = None,
                     log_level: int = 0):

            super().__init__(name, bug_samples, batch_size, shuffle, num_samples, reshape_input, log_level)
            callbacks.append(self.SequenceCallback(self))
            self.bug_samples: List[IBugSample] = bug_samples

        def create_batch(self, start_bug_sample: int) -> Tuple[np.ndarray, np.array]:

            # Collect batch of location samples:
            if self.log_level >= 100:
                start_time = time()
            else:
                start_time = -1

            end_bug_sample = min(start_bug_sample + self.batch_size, len(self.bug_samples))  # index exlusive
            bug_sample_sequences: List[Tuple] = []
            sample_count = 0

            for bug_sample_idx in range(start_bug_sample, end_bug_sample):
                bug_sample: IBugSample = self.bug_samples[bug_sample_idx]

                try:
                    if self.log_level >= 3:
                        print('+++++ Sample', bug_sample.sample_id, '+++++')

                    bug_sample.initialize(self.log_level)  # see finally

                    for bug_location in bug_sample:
                        flow = self.create_location_sequence(bug_sample, bug_location)

                        for batch_num in range(len(flow)):
                            bug_location_sample_inputs, bug_location_sample_label = flow.__getitem__(batch_num)
                            bug_sample_sequences.append((bug_location_sample_inputs, bug_location_sample_label))
                            sample_count += len(bug_location_sample_label)

                finally:
                    # Free memory:
                    bug_sample.uninitialize()

            if self.log_level >= 100:
                print("Compute Sample Batch", t(start_time))
                start_time = time()

            # Combine as input batch:
            bug_location_input_batch, bug_location_label_batch = self.combine_to_batch(sample_count, bug_sample_sequences)

            if self.log_level >= 100:
                print("Combine to batch", t(start_time))

            return bug_location_input_batch, bug_location_label_batch


class LocationSampleGenerator(ILocationSampleGenerator, SampleBaseGenerator):

    def create_location_sample_generator(self, name: str,
                                         bug_sample: IBugSample,
                                         callbacks: List[keras.callbacks.Callback]) -> Sequence:

        flow = self.LocationSampleSequence(name,
                                           self.batch_size,
                                           self.shuffle,
                                           self.num_samples,
                                           bug_sample,
                                           callbacks,
                                           self.reshape_input,
                                           self.log_level)

        return flow

    class LocationSampleSequence(BaseSequence):

        def __init__(self, name: str,
                     batch_size: int,
                     shuffle: bool,
                     num_samples: List[int],
                     bug_sample: IBugSample,
                     callbacks: List[keras.callbacks.Callback],
                     reshape_input: Callable = None,
                     log_level: int = 0):

            start_time = time()
            bug_sample.initialize()
            location_samples = bug_sample.location_samples

            print('Initialized location samples:', len(location_samples), 'in:', t(start_time))
            start_time = time()

            super().__init__(name, location_samples, batch_size, shuffle, num_samples, reshape_input, log_level)
            callbacks.append(self.SequenceCallback(self))
            callbacks.append(self.LocationSampleSequenceCallback(self))

            self.bug_sample: IBugSample = bug_sample
            self.location_samples: List[ILocationSample] = location_samples

        def __getitem__(self, batch_idx):
            try:
                self.bug_sample.initialize()  # (b)lock uninitialization() - reinitialization unexpected
                result = super().__getitem__(batch_idx)
            finally:
                self.bug_sample.uninitialize()  # do uninitialization() if callback on_*_end() was called in parallel
            return result

        class LocationSampleSequenceCallback(keras.callbacks.Callback):

            def __init__(self, flow):
                self.flow = flow

            def on_train_end(self, logs=None):
                self.on_end(logs)

            def on_test_end(self, logs=None):
                self.on_end(logs)

            def on_predict_end(self, logs=None):
                self.on_end(logs)

            def on_end(self, logs=None):
                # Free mememory:
                print(self.flow.bug_sample.lock.count)
                self.flow.bug_sample.uninitialize()

        def create_batch(self, start_location_sample: int) -> Tuple[np.ndarray, np.array]:

            # Collect batch of location samples:
            if self.log_level >= 100:
                start_time = time()
            else:
                start_time = -1

            end_location_sample = min(start_location_sample + self.batch_size, len(self.location_samples))  # index exlusive
            location_sample_sequences: List[Tuple] = []
            sample_count = 0

            for location_sample_idx in range(start_location_sample, end_location_sample):
                location_sample: ILocationSample = self.location_samples[location_sample_idx]
                flow = self.create_location_sequence(self.bug_sample, location_sample)

                for batch_num in range(len(flow)):
                    bug_location_sample_inputs, bug_location_sample_label = flow.__getitem__(batch_num)
                    location_sample_sequences.append((bug_location_sample_inputs, bug_location_sample_label))
                    sample_count += len(bug_location_sample_label)

            if self.log_level >= 100:
                print("Compute Sample Batch", t(start_time))
                start_time = time()

            # Combine as input batch:
            bug_location_input_batch, bug_location_label_batch = self.combine_to_batch(sample_count, location_sample_sequences)

            if self.log_level >= 100:
                print("Combine to batch", t(start_time))

            return bug_location_input_batch, bug_location_label_batch
