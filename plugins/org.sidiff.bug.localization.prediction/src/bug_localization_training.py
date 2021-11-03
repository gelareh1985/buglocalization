'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

import json
from typing import List, cast

import tensorflow as tf
from stellargraph.layer.graphsage import AttentionalAggregator
from tensorflow.keras.utils import plot_model

from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.dataset.neo4j_data_set_training import \
    DataSetTrainingNeo4j
from buglocalization.dataset.sample_generator import BugSampleGenerator
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.predictionmodel.bug_localization_model_training import (
    BugLocalizationAIModelBuilder, BugLocalizationAIModelTrainer,
    DataSetSplitter, TrainigConfiguration)
from buglocalization.selfembedding.node_self_embedding import NodeSelfEmbedding
from buglocalization.selfembedding.node_self_embedding_dictionary import \
    NodeSelfEmbeddingDictionary
from buglocalization.selfembedding.node_self_embedding_sowe import \
    NodeSelfEmbeddingSOWE
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from buglocalization.utils import common_utils as utils

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


# ===============================================================================
# Environmental Information
# ===============================================================================

# NOTE: Paths should not be too long, causes error (on Windows)!
project_folder: str = utils.get_project_folder()
model_training_base_directory: str = "D:"

doc_description: str = utils.create_timestamp() + " JDT: Training Data: 90%, Validation Evaluation Data: 0%, Test Evaluation Data: 10%"

# Training Parameter:
training_configuration = TrainigConfiguration(
    doc_description=doc_description,
    model_training_base_directory=model_training_base_directory,
    optimizer_learning_rate=1e-4,
    trainig_epochs=20,
    trainig_batch_size=20,
    dataset_split_fraction=-1,  # 2 -> 50/50 training test data
    dataset_shuffle=True,
    dataset_generator_workers=8,  # tested with: 4
    dataset_multiprocessing=False,  # tested with: True, False = Threading
    dataset_sample_prefetch_count=20,
    graphsage_num_samples=[20, 10],
    graphsage_layer_sizes=[300, 300],
    graphsage_aggregator=AttentionalAggregator,  # MeanAggregator, MaxPoolingAggregator, MeanPoolingAggregator, AttentionalAggregator
    graphsage_dropout=0.0,
    graphsage_normalize="l2",
    log_level=2
)

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

# Meta-Model:
meta_model: MetaModel = MetaModelUML(
    neo4j_configuration=neo4j_configuration,
    num_samples=training_configuration.graphsage_num_samples)
training_configuration.meta_model = meta_model

# Node Self Embedding:
# # SOWE:
# node_self_embedding: NodeSelfEmbedding = NodeSelfEmbeddingSOWE(
#     meta_model=training_configuration.meta_model,
#     word_dictionary=WordToVectorDictionary(),
#     embedding_cache_local=False,
#     embedding_cache_limit=-1)
# meta_model.node_self_embedding = node_self_embedding
# # Dictionary:
node_self_embedding: NodeSelfEmbedding = NodeSelfEmbeddingDictionary(
    meta_model=training_configuration.meta_model,
    self_embedding_dictionary_path=r'D:\evaluation\org.eclipse.gmf-runtime\org.eclipse.gmf-runtime_uml.class_neo4j_embedding_sowe_2021-07-28\node_self_embedding.pkl',
    dictionary_words_length=300
)
meta_model.node_self_embedding = node_self_embedding


if __name__ == '__main__':

    # ===========================================================================
    # Create AI Model:
    # ===========================================================================

    print('Save Training:', training_configuration.model_training_save_dir)
    utils.create_folder(training_configuration.model_training_save_dir)

    with open(training_configuration.model_training_save_dir + 'training_configuration.json', 'w') as outfile:
        json.dump(training_configuration.dump(), outfile, sort_keys=False, indent=4)

    bug_localization_model_builder = BugLocalizationAIModelBuilder()
    model = bug_localization_model_builder.create_model(
        num_samples=training_configuration.graphsage_num_samples,
        layer_sizes=training_configuration.graphsage_layer_sizes,
        feature_size=training_configuration.meta_model.get_node_self_embedding().get_dimension(),
        checkpoint_dir=training_configuration.model_training_checkpoint_dir,
        dropout=training_configuration.graphsage_dropout,
        normalize=training_configuration.graphsage_normalize,
        optimizer_learning_rate=training_configuration.optimizer_learning_rate)

    # Plot model:
    # Install pydot, pydotplus, graphviz -> https://graphviz.org/download/ -> add to PATH -> reboot -> check os.environ["PATH"]
    # plot_model(model, to_file=model_training_checkpoint_dir + "model.png") # model_to_dot

    # ===========================================================================
    # Create Training and Test Data:
    # ===========================================================================

    # Test Dataset Containing Bug Samples:
    dataset_positive = DataSetTrainingNeo4j(meta_model, neo4j_configuration, is_negative=False)
    dataset_negative = DataSetTrainingNeo4j(meta_model, neo4j_configuration, is_negative=True)
    dataset_splitter = DataSetSplitter(dataset_positive, dataset_negative)

    # ===========================================================================
    # Train and Evaluate AI Model:
    # ===========================================================================

    bug_localization_generator = BugSampleGenerator(
        batch_size=training_configuration.trainig_batch_size,
        shuffle=training_configuration.dataset_shuffle,
        num_samples=training_configuration.graphsage_num_samples,
        log_level=training_configuration.log_level)

    bug_localization_model_trainer = BugLocalizationAIModelTrainer(
        dataset_splitter=dataset_splitter,
        model=model,
        num_samples=training_configuration.graphsage_num_samples,
        checkpoint_dir=training_configuration.model_training_checkpoint_dir,
        sample_generator_workers=training_configuration.dataset_generator_workers,
        sample_prefetch_count=training_configuration.dataset_sample_prefetch_count,
        use_multiprocessing=training_configuration.dataset_multiprocessing)

    # # Start training # #
    bug_localization_model_trainer.train(
        meta_model=training_configuration.meta_model,
        epochs=training_configuration.trainig_epochs,
        sample_generator=bug_localization_generator,
        model_training_save_dir=training_configuration.model_training_save_dir,
        dataset_split_fraction=training_configuration.dataset_split_fraction,
        log_level=training_configuration.log_level)
