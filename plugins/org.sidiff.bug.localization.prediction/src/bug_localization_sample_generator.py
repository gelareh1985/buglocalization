'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from bug_localization_data_set import DataSetBugSampleEmbedding  # type: ignore
from stellargraph.mapper import GraphSAGELinkGenerator  # type: ignore
from tensorflow import keras  # type: ignore
from tensorflow.keras.utils import Sequence  # type: ignore
from typing import Union, List
import numpy as np  # type: ignore
import random
import stellargraph as sg  # type: ignore
from time import time


class IBugLocalizationGenerator:
    
    def __init__(self, num_samples:List[int], batch_size:int, shuffle:bool, generator_workers:int=1, sample_prefetch_count:int=10, multiprocessing:bool=False, log_level=0):
        self.num_samples = num_samples
        self.batch_size = batch_size
        self.shuffle:bool = shuffle
        self.generator_workers:int = generator_workers
        self.sample_prefetch_count:int = sample_prefetch_count
        self.multiprocessing:bool = multiprocessing
        self.log_level = log_level
        
    def get_training_generator(self, bug_samples_train:List[DataSetBugSampleEmbedding]) -> Union[Sequence, List[keras.callbacks.Callback]]:
        pass
    
    def get_evaluation_generator(self, bug_samples_test:List[DataSetBugSampleEmbedding]) -> Union[Sequence, List[keras.callbacks.Callback]]:
        pass


class BugLocalizationGenerator(IBugLocalizationGenerator):
    
    # https://www.tensorflow.org/guide/keras/train_and_evaluate#using_a_kerasutilssequence_object_as_input
    
    # Note: A sample is pair of a bug report and model location, each "bug sample" contains one bug report and multiple locations.
    
    def get_training_generator(self, bug_samples_train:List[DataSetBugSampleEmbedding]) -> Union[Sequence, List[keras.callbacks.Callback]]:
        train_flow = self.BugLocalizationSampleGenerator("training", self.batch_size, self.shuffle, self.num_samples, bug_samples_train, self.log_level)
        shuffle = self.BugLocalizationSampleGenerator.ShuffleCallback(train_flow)  # FIXME https://github.com/tensorflow/tensorflow/issues/35911
        return train_flow, [shuffle]
    
    def get_evaluation_generator(self, bug_samples_eval:List[DataSetBugSampleEmbedding]) -> Union[Sequence, List[keras.callbacks.Callback]]:
        eval_flow = self.BugLocalizationSampleGenerator("validation", self.batch_size, self.shuffle, self.num_samples, bug_samples_eval, self.log_level)
        shuffle = self.BugLocalizationSampleGenerator.ShuffleCallback(eval_flow)  # FIXME https://github.com/tensorflow/tensorflow/issues/35911
        return eval_flow, [shuffle]
    
    class BugLocalizationSampleGenerator(Sequence):
    
        def __init__(self, name:str, batch_size:int, shuffle:bool, num_samples:List[int], bug_samples:List[DataSetBugSampleEmbedding], log_level:int=0):
            self.name:str = name  # e.g. train, eval for debugging
            self.batch_size:int = batch_size
            self.shuffle = shuffle
            self.num_samples:List[int] = num_samples
            self.bug_samples:List[DataSetBugSampleEmbedding] = bug_samples
            self.log_level:int = log_level
            
        def load_bug_samples_batch(self, start_bug_sample:int) -> Union[np.ndarray, np.array]:
            
            # Collect batch of bug samples:
            if self.log_level >= 100:
                t = time()
                
            end_bug_sample = min(start_bug_sample + self.batch_size, len(self.bug_samples) - 1)
            bug_location_samples:List[Tuple] = []
            sample_count = 0
            
            for bug_sample_idx in range(start_bug_sample, end_bug_sample):
                bug_sample = self.bug_samples[bug_sample_idx]
                
                for bug_location_sample_inputs, bug_location_sample_label  in self.bug_sample_generator(bug_sample):
                    bug_location_samples.append((bug_location_sample_inputs, bug_location_sample_label))
                    sample_count += len(bug_location_sample_label)
            
            if self.log_level >= 100:
                print("Sample", time() - t)
                
            # Combine as input batch:
            if self.log_level >= 100:
                t = time()
            
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
              
        def bug_sample_generator(self, bug_sample:DataSetBugSampleEmbedding):

            # Load each bug sample:
            bug_sample.load(add_prefix=False)
            
            if self.log_level >= 4:
                print("Loaded", "negative" if bug_sample.is_negative else "positive", self.name, "sample:", bug_sample.number)
    
            if (len(bug_sample.testcase_labels) <= 0):
                return

            # Convert to StellarGraph:
            graph = sg.StellarGraph(bug_sample.nodes, bug_sample.edges)
        
            if self.log_level >= 5:
                print(graph.info())
        
            # Create Keras Sequence with batch size 1 for generator yield:
            graph_sage_generator = GraphSAGELinkGenerator(graph, batch_size=len(bug_sample.testcase_labels), num_samples=self.num_samples)
            flow = graph_sage_generator.flow(bug_sample.bug_location_pairs, bug_sample.testcase_labels)

            # Free memory:
            bug_sample.unload()
        
            # Convert Keras Sequence to generator:
            for batch_num in range(len(flow)):
                batch_feats, batch_targets = flow.__getitem__(batch_num)
                yield batch_feats, batch_targets       
        
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
