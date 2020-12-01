'''
Created on Nov 30, 2020

@author: Gelareh_mp
'''
import pandas as pd
import os
from nltk.tokenize import RegexpTokenizer
import re

dictionary_words = {"parameter" : 0, "shadowing" : 1, "nested" : 2}  # dummy
dictionary_types = {"Operation" : 0, "Class" : 1, "Parameter" : 2}  # dummy

#dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset\buglocations_5000\all_files\buglocations/" 
dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset\buglocations_5000/"

def dataset_to_vector(dataset_path, dictionary_words, dictionary_types, log=False):

    for filename in os.listdir(dataset_path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = dataset_path + graph_filename + ".nodelist"
            graph_number = graph_filename[0:graph_filename.find("_")]
    
            # nodes:
            nodes_data = load_nodes(node_list_path)
            nodes_features = node_to_vector(nodes_data, dictionary_words, dictionary_types)
            
            # store node encoding:
            node_features_list_path = dataset_path + graph_filename + ".featurenodelist"
            nodes_features.to_csv(node_features_list_path, sep="\t", header=False, index=True) 
            
            if log:
                print("Graph: ", graph_number)
            

def load_nodes(node_list_path):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
    node_data = node_data.fillna("")
    return node_data


def node_to_vector(node_data, dictionary_words, dictionary_types):
    node_feature_columns = list(dictionary_words) + list(dictionary_types)
    node_feature_data = pd.DataFrame(index=node_data.index, columns=node_feature_columns)
    
    # Initialize with 0:
    for column in node_feature_data.columns:
        node_feature_data[column].values[:] = 0
    
    # Encode words:
    for index, row in node_data.iterrows():
        feature_row = node_feature_data.loc[index]
        
        text = row["text"]
        words = text_to_words(text)
        
        for word in words:
            if word in dictionary_words:
                feature_row[dictionary_words[word]] = 1
                
    # Encode meta-types:
    column_offset = len(dictionary_words)
    
    for index, row in node_data.iterrows():
        feature_row = node_feature_data.loc[index]
        meta_type = row["type"]
        
        if meta_type in dictionary_types:
            feature_row[column_offset + dictionary_types[meta_type]] = 1
    
    # Append locations:
    node_feature_data.insert(len(node_feature_data.columns), "tag", node_data["tag"], allow_duplicates=True)
    
    # Mark bug report node:
    for index, row in node_data.iterrows():
        meta_type = row["type"]
        
        if (meta_type == "BugReportNode"):
            feature_row = node_feature_data.loc[index]
            feature_row["tag"] = "# REPORT"
            
    return node_feature_data


def text_to_words(text):
    words_array=[]
    tokenizer=RegexpTokenizer('[A-Za-z]+')
    words=tokenizer.tokenize(text)
    for word in words:
        splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
        for split_word in splitted:
            split_word=split_word.lower()
            words_array.append(split_word)
    return words_array 

###################################################################################################
# Create Training and Test Data:
###################################################################################################

# Encode all graphs from the given folder:
dataset_to_vector(dataset_path, dictionary_words, dictionary_types, log=True)

print('Finished encoding nodes!\n')
        