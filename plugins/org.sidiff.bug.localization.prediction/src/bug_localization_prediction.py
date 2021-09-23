'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from multiprocessing import Process
from typing import List

import tensorflow as tf

from bug_localization_training import training_configuration
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.dataset.neo4j_data_set_prediction import \
    DataSetPredictionNeo4j
from buglocalization.evaluation.evaluation_prediction_results import \
    BugLocalizationPredictionTest
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.predictionmodel.bug_localization_model_prediction import \
    BugLocalizationPredictionConfiguration
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from buglocalization.utils import common_utils

# ===============================================================================
# Configure GPU Device:
# https://towardsdatascience.com/setting-up-tensorflow-gpu-with-cuda-and-anaconda-onwindows-2ee9c39b5c44
# ===============================================================================
# Only allocate needed memory needed by the application:
gpus = tf.config.experimental.list_physical_devices('GPU')

if gpus:
    try:
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
    except RuntimeError as e:
        print(e)
# ===============================================================================


# Evaluation Result Records:
# NOTE: Paths should not be too long, causes error (on Windows)!
project_folder: str = common_utils.get_project_folder()
evaluation_results_base_path: str = project_folder + "/evaluation/eclipse.jdt.core"
bug_localization_model_path: str = project_folder + "/training/eclipse.pde.ui_data-2021-04-09_model-2021-04-12" + "/"

# Configuration for bug localization prediction computation:
prediction_configuration = BugLocalizationPredictionConfiguration(
    
    # Meta-model modeling language configuration:
    meta_model=training_configuration.meta_model,
    
    # Evaluation Result Records:
    evaluation_results_base_path=evaluation_results_base_path,
    
    # Trained bug localization model:
    bug_localization_model_path=bug_localization_model_path,

    # DL Model Prediction Configuration:
    # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
    num_samples=training_configuration.graphsage_num_samples,  # must be consistent with the trained model!
    batch_size=100,

    prediction_worker=2,

    sample_generator_workers=4,
    sample_generator_workers_multiprocessing=False,
    sample_max_queue_size=8,
    
    # For debugging:
    log_level=2  # 0-100
)

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

if __name__ == '__main__':
    
    def dataset_slice(prediction_worker: int, bug_sample_count: int, dataset_slice_idx: int) -> slice:
        dataset_slice_count = prediction_worker + 1  # worker plus main process
        dataset_slice_size = int(bug_sample_count / dataset_slice_count)
        
        dataset_slice_start = dataset_slice_idx * dataset_slice_size
            
        if dataset_slice_idx == dataset_slice_count - 1:
            dataset_slice_end = bug_sample_count  # last takes remainers
        else:
            dataset_slice_end = dataset_slice_start + dataset_slice_size
            
        dataset_slice = slice(dataset_slice_start, dataset_slice_end)
        return dataset_slice

    log_level = prediction_configuration.log_level
    meta_model = prediction_configuration.meta_model
    
    # Test Dataset Containing Bug Samples:
    dataset = DataSetPredictionNeo4j(meta_model, neo4j_configuration, log_level=log_level)
    bug_sample_count = len(dataset.bug_samples)
    
    evaluation_results_path_slices = prediction_configuration.evaluation_results_path
    print("Prediction Results:", evaluation_results_path_slices)

    # Multiprocesing?
    processes: List[Process] = []
    prediction_worker = prediction_configuration.prediction_worker
    
    for dataset_slice_idx in range(1, prediction_worker + 1):  # main process #0 worker #1,...
        prediction_test = BugLocalizationPredictionTest(evaluation_results_path_slices)
        dataset_slice_by_idx = dataset_slice(prediction_worker, bug_sample_count, dataset_slice_idx)
        prediction_process = Process(
            target=prediction_test.predict, 
            args=(meta_model, dataset, prediction_configuration, dataset_slice_by_idx, log_level))
        processes.append(prediction_process)
        prediction_process.start()
                
    # Main process:
    prediction_test = BugLocalizationPredictionTest(evaluation_results_path_slices)
    dataset_slice_by_idx = dataset_slice(prediction_worker, bug_sample_count, 0)
    prediction_test.predict(meta_model, dataset, prediction_configuration, dataset_slice_by_idx, log_level)
    
    # Wait for workers:
    # FIXME: How can we see if a single process fails with an exception!?
    print("Prediction with", len(processes), "worker processes")
    
    for process in processes:
        process.join()
        
    print("Prediction Test Finished!")
        
