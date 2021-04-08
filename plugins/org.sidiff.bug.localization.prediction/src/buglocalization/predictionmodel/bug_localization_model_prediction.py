'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import datetime
from time import time
from typing import Generator, List, Tuple, Union, cast

import numpy as np
from bug_localization_training import BugLocalizationAIModelBuilder
from buglocalization.dataset.data_set import IBugSample, IDataSet
from buglocalization.dataset.sample_generator import LocationSampleGenerator
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.utils import common_utils as utils
from buglocalization.utils.common_utils import t
from tensorflow import keras


class BugLocalizationPredictionConfiguration:

    def __init__(self,
                 evaluation_results_base_path: str,
                 bug_localization_model_path: str,
                 num_samples: List[int],  # TODO: Get this from trained model!
                 batch_size: int = 50,
                 prediction_worker: int = 0,
                 sample_generator_workers: int = 1,
                 sample_generator_workers_multiprocessing: bool = False,
                 sample_max_queue_size: int = 10,
                 log_level: int = 2):
        """
        The configuration parameters for running the prediction.

        Args:
            evaluation_results_path (str): Path for saving the evaluation results.
            bug_localization_model_path (str): Path to the trained Keras bug localization prediction model.
            num_samples (List[int]): Number of nodes to be sampled at each neigbor level starting from the bug report node
                                     and the model element (possible bug location).
            batch_size (int): The number of model element (possible bug location) per batch processed during the prediction.
                              Defaults to 50.
            sample_generator_workers (int): Number of workers for location sample generation. Defaults to 1.
            sample_generator_workers_multiprocessing (bool): True will use multiple processes for each sample generator worker; 
                                                             False will use thread. Defaults to False.
            sample_max_queue_size (int): Size of batch queue during prediction. Defaults to 10
            log_level (int): Logging for infos and debugging: 0-100
        """

        self.evaluation_results_path: str = evaluation_results_base_path + "_" + utils.create_timestamp() + "/"

        # Trained bug localization model:
        self.bug_localization_model_path: str = bug_localization_model_path

        # DL Model Prediction Configuration:
        self.num_samples: List[int] = num_samples  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
        self.batch_size: int = batch_size
        
        self.prediction_worker = prediction_worker

        self.sample_generator_workers: int = sample_generator_workers
        self.sample_generator_workers_multiprocessing: bool = sample_generator_workers_multiprocessing
        self.sample_max_queue_size: int = sample_max_queue_size
        
        # For debugging:
        self.log_level: int = log_level  # 0-100


class BugLocalizationPrediction:

    def predict(self,
                meta_model: MetaModel,
                sample_data: Union[IDataSet, IBugSample],
                config: BugLocalizationPredictionConfiguration,
                sample_data_slice: slice = None,
                log_level=0
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
            
        if sample_data_slice is not None:
            bug_samples = bug_samples[sample_data_slice]

        for bug_sample in bug_samples:
            print("Start Prediction ...")
            start_time_prediction = time()

            callbacks: List[keras.callbacks.Callback] = []
            flow = prediction_generator.create_location_sample_generator("prediction", meta_model, bug_sample, callbacks)

            prediction = model.predict(flow,
                                       callbacks=callbacks,
                                       workers=config.sample_generator_workers,
                                       use_multiprocessing=config.sample_generator_workers_multiprocessing,
                                       max_queue_size=config.sample_max_queue_size,
                                       verbose=1 if log_level > 0 else 0)

            print("Finished Prediction:", bug_sample.sample_id, "in", t(start_time_prediction) + "s")

            yield bug_sample, prediction

        print("Evaluation Finished:", t(start_time_evaluation))
