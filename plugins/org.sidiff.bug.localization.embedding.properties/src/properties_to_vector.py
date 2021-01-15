'''
Created on Jan 14, 2021

@author: Gelareh_mp
'''

import os
from pathlib import Path

from gensim.models import KeyedVectors

import numpy as np
import pandas as pd
from word_dictionary import WordDictionary
import concurrent.futures

# samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/positive/"
samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000\negative/"
feature_node_save_path = samples_path + "/features/"
type_dictionary_path = r"C:\Users\Gelareh_mp\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\type_dictionary.dictionary"
pretrained_dictionary_path = r"D:\files_MDEAI_original\Data_sets\GoogleNews-vectors-negative300.bin"


class DatasetPropertyEmbedding:
    
    def __init__(self, node_property_embedding):
        self.node_feature_embedding = node_property_embedding
    
    def dataset_to_vector(self, samples_path, log=False):
        node_feature_columns = self.create_column_names()
        
        with concurrent.futures.ThreadPoolExecutor(max_workers=32) as executor:
            for filename in os.listdir(samples_path):
                if filename.endswith(".nodelist"):
                    executor.submit(self.sample_embedding, samples_path, filename, node_feature_columns, log)
                    
    def sample_embedding(self, samples_path, filename, node_feature_columns, log):
        graph_filename = filename[:filename.rfind(".")]
        node_list_path = samples_path + graph_filename + ".nodelist"
        graph_number = graph_filename[0:graph_filename.find("_")]

        # encode nodes:
        nodes_data = self.load_nodes(node_list_path)
        node_feature_data = pd.DataFrame(index=nodes_data.index, columns=node_feature_columns)
        
        for index, node in nodes_data.iterrows():
            feature_vector, tag = self.node_feature_embedding.node_to_vector(node)
            feature_vector_data = feature_vector.tolist()
            feature_vector_data.append(tag)
            
            node_feature_data.loc[index] = feature_vector_data

        # store node encoding:
        Path(feature_node_save_path).mkdir(parents=True, exist_ok=True)
        node_features_list_path = feature_node_save_path + graph_filename + ".featurenodelist"
        
        # TODO: Make the storage format configurable...
        # node_feature_data.to_csv(node_features_list_path, sep="\t", header=False, index=True) 
        node_feature_data.to_pickle(path=node_features_list_path, compression="zip")
        
        if log:
            print("Graph: ", graph_number)
    
    def create_column_names(self):
        column_names = []
        
        for column in range(self.node_feature_embedding.get_dimension()):
            column_names.append(str("feature" + str(column)))
        
        column_names.append("tag")
        return column_names
    
    def load_nodes(self, node_list_path):
        node_list_col_names = ["index", "text", "type", "tag"]
        node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
        node_data = node_data.fillna("")
        return node_data


class NodePropertyEmbedding:
    
    def __init__(self, dictionary_words, dictionary_words_length, dictionary_types, dictionary_types_length):
        self.dictionary_words = dictionary_words
        self.dictionary_words_length = dictionary_words_length
        self.dictionary_types = dictionary_types
        self.dictionary_types_length = dictionary_types_length
        self.vector_dimension = dictionary_words_length + dictionary_types_length
        
    def get_dimension(self):
        return self.vector_dimension
 
    def node_to_vector(self, node):
        text_vector = self.text_to_vector(node)
        type_vector = self.metatype_to_vector(node)
        feature_vector = np.concatenate((text_vector, type_vector), axis=None)
        
        tag = self.get_tag(node)
        
        return feature_vector, tag

    def text_to_vector(self, node):
        
        # Encode words:
        feature_vector = np.zeros(dictionary_words_length)
        
        text = node["text"]
        words = dictionary_types.text_to_words(text)  # TODO: Refactoring: move text_to_words function
        
        for word in words:
            try:
                word_embedding = dictionary_words[word]
                feature_vector = np.sum((feature_vector, word_embedding), axis=0) 
            except KeyError:   
                pass  # ignore...how to handle unseen words...?
                
        return feature_vector
    
    def metatype_to_vector(self, node):
        dictionary_types_dict = dictionary_types.get_dictionary()
        feature_vector = np.zeros(len(dictionary_types_dict))
                     
        # Encode meta-types:
        meta_type = node["type"]
         
        if meta_type in dictionary_types_dict:
            feature_vector[dictionary_types_dict[meta_type]] = 1
                 
        return feature_vector
   
    def get_tag(self, node): 
        
        # Is locations?
        tag = node["tag"]
        
        if tag is not None and tag:
            return tag
         
        # Mark bug report node:
        meta_type = node["type"]
         
        if (meta_type == "BugReportNode"):
            return "# REPORT"
        
        return None

# *******************************************************************************************


# Load type dictionary:
dictionary_types = WordDictionary()
dictionary_types.load(type_dictionary_path)
print("Dictionary Types:", len(dictionary_types.get_dictionary()))

# ************** Load Pretrained Dictionary **********************
print('Begin Loading Pretrained Dictionary Model ...')
dictionary_words = KeyedVectors.load_word2vec_format(pretrained_dictionary_path, binary=True)
dictionary_words_length = 300
print('Finished Loading Pretrained Dictionary Model!\n')
# *************** Process List of File Corpus ********************  
                 
# Encode all graphs from the given folder:
node_property_embedding = NodePropertyEmbedding(
    dictionary_words, dictionary_words_length,
    dictionary_types, len(dictionary_types.get_dictionary()))
dataset_property_embedding = DatasetPropertyEmbedding(node_property_embedding)
dataset_property_embedding.dataset_to_vector(samples_path, log=True)

print('Finished encoding nodes!')
