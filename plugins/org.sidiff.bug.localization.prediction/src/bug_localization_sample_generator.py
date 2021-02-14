'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from bug_localization_data_set import ISample
from tensorflow import keras  # type: ignore
from tensorflow.keras.utils import Sequence  # type: ignore
from typing import List, Tuple
import numpy as np  # type: ignore
import random
from time import time


class IBugLocalizationGenerator:

    def __init__(self,
                 num_samples: List[int],
                 batch_size: int,
                 shuffle: bool,
                 generator_workers: int = 1,
                 sample_prefetch_count: int = 10,
                 multiprocessing: bool = False,
                 log_level=0):

        self.num_samples = num_samples
        self.batch_size = batch_size
        self.shuffle: bool = shuffle
        self.generator_workers: int = generator_workers
        self.sample_prefetch_count: int = sample_prefetch_count
        self.multiprocessing: bool = multiprocessing
        self.log_level = log_level

    def get_generator(self, name: str, bug_samples: List[ISample]) -> Tuple[Sequence, List[keras.callbacks.Callback]]:  # type: ignore
        pass


class BugLocalizationGenerator(IBugLocalizationGenerator):

    # https://www.tensorflow.org/guide/keras/train_and_evaluate#using_a_kerasutilssequence_object_as_input

    # Note: A sample is pair of a bug report and model location, each "bug sample" contains one bug report and multiple locations.

    def get_generator(self, name: str, bug_samples: List[ISample]) -> Tuple[Sequence, List[keras.callbacks.Callback]]:
        flow = self.BugLocalizationSampleGenerator(name, self.batch_size, self.shuffle, self.num_samples, bug_samples, self.log_level)
        shuffle = self.BugLocalizationSampleGenerator.ShuffleCallback(flow)  # FIXME https://github.com/tensorflow/tensorflow/issues/35911
        return flow, [shuffle]

    class BugLocalizationSampleGenerator(Sequence):

        def __init__(self, name: str, batch_size: int, shuffle: bool, num_samples: List[int], bug_samples: List[ISample], log_level: int = 0):
            self.name: str = name  # e.g. train, eval for debugging
            self.batch_size: int = batch_size
            self.shuffle = shuffle
            self.num_samples: List[int] = num_samples
            self.bug_samples: List[ISample] = bug_samples
            self.log_level: int = log_level

        def load_bug_samples_batch(self, start_bug_sample: int) -> Tuple[np.ndarray, np.array]:

            # Collect batch of bug samples:
            if self.log_level >= 100:
                t = time()
            else:
                t = -1

            end_bug_sample = min(start_bug_sample + self.batch_size, len(self.bug_samples) - 1)
            bug_location_samples: List[Tuple] = []
            sample_count = 0

            for bug_sample_idx in range(start_bug_sample, end_bug_sample):
                bug_sample: ISample = self.bug_samples[bug_sample_idx]

                for flow in bug_sample.sample_generator(self.num_samples, self.log_level):
                    # Convert Keras Sequence to generator:
                    for batch_num in range(len(flow)):
                        bug_location_sample_inputs, bug_location_sample_label = flow.__getitem__(batch_num)
                        bug_location_samples.append((bug_location_sample_inputs, bug_location_sample_label))
                        sample_count += len(bug_location_sample_label)

            if self.log_level >= 100:
                print("Sample", time() - t)
                t = time()

            # Combine as input batch:
            input_node_count = (len(self.num_samples) + 1) * 2
            bug_location_input_batch = []  # List of input nodes of the GraphSAGE model
            bug_location_label_batch = np.empty(shape=(sample_count), dtype=bug_location_samples[0][1].dtype)

            # Copy input shape with full batch size:
            for input_node in range(input_node_count):
                input_node_sample = bug_location_samples[0][0][input_node]
                input_shape = input_node_sample.shape
                input_type = input_node_sample.dtype
                bug_location_input_batch.append(np.empty(shape=(sample_count, input_shape[1], input_shape[2]), dtype=input_type))

            current_sample_idx = 0

            for bug_location_inputs, bug_location_labels in bug_location_samples:
                for bug_location_sample_idx in range(len(bug_location_labels)):
                    for input_node in range(input_node_count):
                        bug_location_input_batch[input_node][current_sample_idx] = bug_location_inputs[input_node][bug_location_sample_idx]
                    bug_location_label_batch[current_sample_idx] = bug_location_labels[bug_location_sample_idx]
                    current_sample_idx += 1

            if self.log_level >= 100:
                print("Combine to batch", time() - t)

            return bug_location_input_batch, bug_location_label_batch

        def __len__(self):
            """Denotes the number of batches per epoch"""
            return int(np.ceil(len(self.bug_samples) / float(self.batch_size)))

        def __getitem__(self, batch_idx):
            """Generate one batch of data"""
            if self.log_level >= 3:
                print("Get", self.name, "batch:", batch_idx)

            # Loading data in parallel to each training batch:
            bug_location_samples, bug_location_sample_labels = self.load_bug_samples_batch(batch_idx * self.batch_size)
            return bug_location_samples, bug_location_sample_labels

        # FIXME: Not calling on_epoch_end... Waiting for bug fix release... Workaround use callbacks
        # https://github.com/tensorflow/tensorflow/issues/35911
        def workaround_on_epoch_end(self):
            """Prepare the data set for the next epoch"""

            # Shuffle samples:
            if self.shuffle:
                random.shuffle(self.bug_samples)

        class ShuffleCallback(keras.callbacks.Callback):

            def __init__(self, flow):
                self.flow = flow

            def on_epoch_end(self, epoch, logs=None):  # @UnusedVariable
                self.flow.workaround_on_epoch_end()
