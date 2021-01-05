'''
Created on Nov 30, 2020

@author: Gelareh_mp
'''
import pandas as pd
import os
from IPython.display import display
from word_dictionary import WordDictionary
from pathlib import Path

positve_samples_path =      r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_6000/positive/"
feature_node_save_path =    positve_samples_path + "/features/"
dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary_shrinked.dictionary"


def dataset_to_vector(positve_samples_path, dictionary_words, dictionary_types, log=False):

    for filename in os.listdir(positve_samples_path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = positve_samples_path + graph_filename + ".nodelist"
            graph_number = graph_filename[0:graph_filename.find("_")]
    
            # nodes:
            nodes_data = load_nodes(node_list_path)
            nodes_features = node_to_vector(nodes_data, dictionary_words, dictionary_types)
            
            # store node encoding:
            Path(feature_node_save_path).mkdir(parents=True, exist_ok=True)
            node_features_list_path = feature_node_save_path + graph_filename + ".featurenodelist"
            nodes_features.to_csv(node_features_list_path, sep="\t", header=False, index=True) 
            
            if log:
                print("Graph: ", graph_number)
    return nodes_data, nodes_features

    
def load_nodes(node_list_path):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
    node_data = node_data.fillna("")
    return node_data


def node_to_vector(node_data, dictionary_words, dictionary_types):
    dictionary_words_dict = dictionary_words.get_dictionary()
    dictionary_types_dict = dictionary_types.get_dictionary()
    
    node_feature_columns = list(dictionary_words_dict) + list(dictionary_types_dict)
    node_feature_data = pd.DataFrame(index=node_data.index, columns=node_feature_columns)
    
    # Initialize with 0:
    for column in node_feature_data.columns:
        node_feature_data[column].values[:] = 0
    
    # Encode words:
    for index, row in node_data.iterrows():
        feature_row = node_feature_data.loc[index]
        
        text = row["text"]
        words = dictionary_words.text_to_words(text)
        
        for word in words:
            if word in dictionary_words_dict:
                feature_row[dictionary_words_dict[word]] = 1
                
    # Encode meta-types:
    column_offset = len(dictionary_words_dict)
    
    for index, row in node_data.iterrows():
        feature_row = node_feature_data.loc[index]
        meta_type = row["type"]
        
        if meta_type in dictionary_types_dict:
            feature_row[column_offset + dictionary_types_dict[meta_type]] = 1
    
    # Append locations:
    # (Columns are named by the words in the dictionary, so we should not use simple words as user defined columns.)
    node_feature_data.insert(len(node_feature_data.columns), "__tag__", node_data["tag"], allow_duplicates=True)
    
    # Mark bug report node:
    for index, row in node_data.iterrows():
        meta_type = row["type"]
        
        if (meta_type == "BugReportNode"):
            feature_row = node_feature_data.loc[index]
            feature_row[len(feature_row) - 1] = "# REPORT"
            
    return node_feature_data


# *******************************************************************************************
# Initialize dictionaries:
dictionary_words = WordDictionary()
dictionary_words.load(dictionary_path)
print("Dictionary Words:", len(dictionary_words.get_dictionary()))

dictionary_types = WordDictionary()
#filename_types = dictionary_path + "complete_version_of_dictionary.dictionary"
#dictionary_types.load(filename_types)
#print("Dictionary Types:", len(dictionary_types.get_dictionary()))
                 
# Encode all graphs from the given folder:
nodes_data, node_features = dataset_to_vector(positve_samples_path, dictionary_words, dictionary_types, log=True)
display(nodes_data)
display(node_features)

print('Finished encoding nodes!')
