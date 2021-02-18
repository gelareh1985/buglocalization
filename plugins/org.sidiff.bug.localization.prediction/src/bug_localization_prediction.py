'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from time import time
from typing import Generator, List, Optional, Tuple, Union, cast

import numpy as np

from bug_localization_data_set import IBugSample, IDataSet
from bug_localization_sample_generator import LocationSampleGenerator
from bug_localization_training import BugLocalizationAIModelBuilder
from bug_localization_util import t


class BugLocalizationPredictionConfiguration:

    def __init__(self,
                 bug_localization_model_path: str,
                 num_samples: List[int],  # TODO: Get this from trained model!
                 batch_size: int=50,
                 sample_generator_workers: int = 1,
                 sample_generator_workers_multiprocessing: bool = False,
                 sample_max_queue_size: int = 10):
        """
        The configuration parameters for running the prediction.

        Args:
            bug_localization_model_path (str): Path to the trained Keras bug localization prediction model.
            num_samples (List[int]): Number of nodes to be sampled at each neigbor level starting from the bug report node
                                     and the model element (possible bug location).
            batch_size (int): The number of model element (possible bug location) per batch processed during the prediction.
                              Defaults to 50.
            sample_generator_workers (int): Number of workers for location sample generation. Defaults to 1.
            sample_generator_workers_multiprocessing (bool): True will use multiple processes for each sample generator worker; 
                                                             False will use thread. Defaults to False.
            sample_max_queue_size (int): Size of batch queue during prediction. Defaults to 10
        """

        # Trained bug localization model:
        self.bug_localization_model_path: str = bug_localization_model_path

        # DL Model Prediction Configuration:
        self.num_samples: List[int] = num_samples  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
        self.batch_size: int = batch_size

        self.sample_generator_workers: int = sample_generator_workers
        self.sample_generator_workers_multiprocessing: bool = sample_generator_workers_multiprocessing
        self.sample_max_queue_size: int = sample_max_queue_size


class BugLocalizationPrediction:

    def predict(self,
                sample_data: Union[IDataSet, IBugSample],
                config: BugLocalizationPredictionConfiguration,
                log_level=0,
                peek_location_samples=0
                ) -> Generator[Tuple[IBugSample, np.ndarray], None, None]:
        """
        Predicts the bug locations for a given set of bug samples.

        Args:
            sample_data (Union[IDataSet, IBugSample]): The bug sample(s); given as data set or a single bug.
            config (PredictionConfiguration): The configuration parameters for running the prediction.
            log_level (int, optional): For debugging: Logging level 0-100. Defaults to 0.
            peek_location_samples(int, optional): For debugging: Test only the first N location samples per bug sample. Default to None.

        Yields:
            Generator[Tuple[IBugSample, np.ndarry], None, None]: A bug sample and an array of prediction pairs (probability, node ID).
        """
        print("Start Evaluation ...")
        start_time_evaluation = time()

        model_builder = BugLocalizationAIModelBuilder()
        model = model_builder.restore_model(config.bug_localization_model_path)

        prediction_generator = LocationSampleGenerator(
            batch_size=config.batch_size,
            shuffle=False,
            num_samples=config.num_samples,
            log_level=log_level)

        # Create the second input to the model for passing through the sample IDs to the output:
        model = prediction_generator.create_prediction_model(model, True)

        if isinstance(sample_data, IBugSample):
            bug_samples = [cast(IBugSample, sample_data)]
        else:
            bug_samples = cast(IDataSet, sample_data).bug_samples

        for bug_sample in bug_samples:
            print("Start Prediction ...")
            start_time_prediction = time()
            bug_sample.initialize()
            location_samples = bug_sample.location_samples
            
            # Test only the first N location samples per bug sample
            if peek_location_samples is not None:
                print('WARNING: Prediction is set to use only the first ' + str(peek_location_samples) + 'samples.')
                location_samples = location_samples[:min(peek_location_samples, len(location_samples))]
                
            print('Initialization (Location Samples:', len(location_samples), '):', t(start_time_prediction))
            start_time_prediction = time()

            flow, callbacks = prediction_generator.create_location_sample_generator("prediction", bug_sample, location_samples)
            prediction = model.predict(flow,
                                       callbacks=callbacks,
                                       workers=config.sample_generator_workers,
                                       use_multiprocessing=config.sample_generator_workers_multiprocessing,
                                       max_queue_size=config.sample_max_queue_size,
                                       verbose=1 if log_level > 0 else 0)
            bug_sample.uninitialize()
            
            print("Finished Prediction:", t(start_time_prediction))

            yield bug_sample, prediction

        print("Evaluation Finished:", t(start_time_evaluation))
