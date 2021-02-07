'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from bug_localization_data_set import DataSetNeo4j, DataSetBugSampleNeo4j
from bug_localization_meta_model_uml import MetaModelUML, UMLNodeSelfEmbedding

pretrained_dictionary_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.prediction\data\GoogleNews-vectors-negative300.bin"
dictionary_words_length = 300

if __name__ == '__main__':
    
    dataset = DataSetNeo4j("localhost", 7687, "neo4j", "password")
    meta_model = MetaModelUML()
    
    # TODO: Specify stopwords? May not be in the dictionary anyway!
    embedding = UMLNodeSelfEmbedding(meta_model.get_meta_type_to_properties(), pretrained_dictionary_path, 300)
    
    print("Start Word Embedding ...")
    
    bug_sample = dataset.get_sample("e52a21744a6b7352e6353bbcfaad99e2983dee38")
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
                subgraph = bug_sample.load_model_element_subgraph(model_node_id, typebased_slicing.get_slicing(meta_type_label))
                print(subgraph.info())
                # TODO: ... 
                
    print("Evaluation Finished!")

