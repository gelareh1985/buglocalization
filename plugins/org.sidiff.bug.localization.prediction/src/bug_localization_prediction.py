'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from bug_localization_data_set import DataSetNeo4j, DataSetBugSampleNeo4j
from bug_localization_meta_model_uml import MetaModelUML, UMLNodeSelfEmbedding
from bug_localization_training_v1 import BugLocalizationAIModelBuilder
from stellargraph.mapper import GraphSAGELinkGenerator  # type: ignore

pretrained_dictionary_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\data\GoogleNews-vectors-negative300.bin"
dictionary_words_length = 300

bug_localization_model_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\trained_model_2021-02-05_03-38-53_train90_test10" + "/"

if __name__ == '__main__':
    
    num_samples = [20, 10]  # List of number of neighbor node samples per GraphSAGE layer (hop) to take.
    model_builder = BugLocalizationAIModelBuilder()
    model = model_builder.restore_model(bug_localization_model_path)
    
    dataset = DataSetNeo4j("localhost", 7687, "neo4j", "password")
    meta_model = MetaModelUML()
    # TODO: Specify stopwords? May not be in the dictionary anyway!
    embedding = UMLNodeSelfEmbedding(meta_model.get_meta_type_to_properties(), pretrained_dictionary_path, 300)
    
    print("Start Word Embedding ...")
    
    bug_sample = dataset.get_sample("b2873327a0ad6c3e445a79794e3d5c4301b2a19d") #199
    bug_sample.load_bug_report(embedding)
    bug_sample.load_model_nodes(meta_model.get_model_meta_type_labels(), embedding)
    
    # Free memory...
    embedding.unload_dictionary()
    
    print("Finished Word Embedding!")
    print("Start Prediction ...")
    typebased_slicing = meta_model.get_slicing(dataset)
    
    for meta_type_label, model_nodes in bug_sample.model_nodes.items():
        for model_node_id, model_node in model_nodes.iterrows():
            if meta_type_label in meta_model.get_bug_location_model_meta_type_labels():
                subgraph = bug_sample.load_bug_location_subgraph(model_node_id, typebased_slicing.get_slicing(meta_type_label))
                # print(subgraph.info())
                
                graph_sage_generator = GraphSAGELinkGenerator(subgraph, 1, num_samples=num_samples)
                
                bug_location_node = model_node_id
                bug_report_node = bug_sample.bug_report_node_id
                bug_location_pair = (bug_report_node, bug_location_node)
                
                print("Predicting:", bug_location_node, bug_report_node)
                
                flow = graph_sage_generator.flow([bug_location_pair])
                prediction = model.predict(flow)
                
                print("Prediction:", bug_location_node, bug_report_node, "Result:", prediction)
                
    print("Evaluation Finished!")

