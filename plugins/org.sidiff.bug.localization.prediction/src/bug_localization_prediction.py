'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from bug_localization_data_set import DataSetPredictionNeo4j
from bug_localization_meta_model_uml import MetaModelUML, UMLNodeSelfEmbedding
from bug_localization_training import BugLocalizationAIModelBuilder
from bug_localization_util import t
from bug_localization_sample_generator import BugLocalizationGenerator
from time import time

pretrained_dictionary_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\data\GoogleNews-vectors-negative300.bin"  # noqa: E501
dictionary_words_length = 300

bug_localization_model_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\trained_model_2021-02-05_03-38-53_train90_test10" + "/"  # noqa: E501

if __name__ == '__main__':
    
    num_samples = [20, 10]  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
    model_builder = BugLocalizationAIModelBuilder()
    model = model_builder.restore_model(bug_localization_model_path)
    
    # TODO: Specify stopwords? May not be in the dictionary anyway!
    meta_model = MetaModelUML()
    node_self_embedding = UMLNodeSelfEmbedding(meta_model.get_meta_type_to_properties(), pretrained_dictionary_path, 300)
    typebased_slicing = meta_model.get_slicing_criterion(len(num_samples))
    dataset = DataSetPredictionNeo4j(meta_model, node_self_embedding, typebased_slicing, "localhost", 7687, "neo4j", "password")

    print("Start Prediction ...")
    start_time = time()

    prediction_generator = BugLocalizationGenerator(
        num_samples, 
        batch_size=20, 
        shuffle=False, 
        generator_workers=1, 
        sample_prefetch_count=10, 
        multiprocessing=False, 
        log_level=100)
    flow, callbacks = prediction_generator.get_generator("prediction", dataset.get_samples())
         
    prediction = model.predict(flow, callbacks=callbacks)
    print(prediction)
    
    print("Finished Prediction:", t(start_time))            
    print("Evaluation Finished:")
