'''
Created on Nov 30, 2020

@author: Gelareh_mp
'''
import pandas as pd
import os
import csv
from nltk.tokenize import RegexpTokenizer
from nltk.corpus import stopwords
import re


dictionary_words = {"parameter" : 0, "shadowing" : 1, "nested" : 2}  # dummy

#dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset\buglocations_5000\all_files\buglocations/" 
dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset\buglocations_5000/"

def load_dataset(dataset_path, dictionary_words, log=False):

    # Collect all graphs from the given folder:
    dataset_nodes = []
    dataset_edges = []
    dataset_bug_locations = []
    
    for filename in os.listdir(dataset_path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = dataset_path + graph_filename + ".nodelist"
            edge_list_path = dataset_path + graph_filename + ".edgelist"
            graph_number = graph_filename[0:graph_filename.find("_")]
    
            # nodes:
            nodes_data = load_nodes(node_list_path, graph_number)
            nodes_features = node_to_vector(nodes_data, dictionary_words)
            dataset_nodes.append(nodes_features)
            
            # edges:
            edge_data = load_edges(edge_list_path, graph_number)
            dataset_edges.append(edge_data)
            
            # bug locations:
            bug_locations = extract_bug_locations(nodes_data)
            dataset_bug_locations.append(bug_locations)
            
            # remove bug location edges:
            remove_bug_location_edges(edge_data, bug_locations)
            
            if log:
                print("Graph: ", graph_number)
            
    return dataset_nodes, dataset_edges, dataset_bug_locations


def load_nodes(node_list_path, graph_number):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
    node_data = node_data.rename(index=lambda index: add_prefix(graph_number, index))
    node_data = node_data.fillna("")
    return node_data


def node_to_vector(node_data, dictionary_words):
    node_feature_data = pd.DataFrame(index=node_data.index, columns=dictionary_words)
    
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
    #return text.split(" ")


def load_edges(edge_list_path, graph_number):
    edge_list_col_names = ["source", "target"]
    edge_data = pd.read_table(edge_list_path, names=edge_list_col_names)
    
    edge_data['source'] = edge_data['source'].apply(lambda index: add_prefix(graph_number, index))
    edge_data['target'] = edge_data['target'].apply(lambda index: add_prefix(graph_number, index))
    edge_data = edge_data.rename(index=lambda index: add_prefix(graph_number, index))
    
    return edge_data


def add_prefix(prefix, index):
    return str(prefix) + "_" + str(index)


def extract_bug_locations(node_data):
    bug_locations = []
    bug_report_node = None
    
    for node_index, node_row in node_data.iterrows():
        type_name = node_row["type"]
        tag = node_row["tag"]
        
        if (type_name == "BugReportNode"):
            bug_report_node = node_index
        elif (type_name == "BugReportCommentNode"):
            pass
        elif (tag == "# LOCATION"):
            bug_location_node = node_index
            bug_locations.append([bug_report_node, bug_location_node])
        else:
            # Assuming that the data set starts with the bug report and its locations.
            break  # optimization
        
    return bug_locations


def remove_bug_location_edges(edge_data, bug_locations):
    bug_location_edges = []
    
    for bug_location in bug_locations:
        for edge_index, edge_row in edge_data.iterrows():
            if edge_row[0] == bug_location[0] and edge_row[1] == bug_location[1]:
                bug_location_edges.append(edge_index)
                break
        
    edge_data.drop(index=bug_location_edges, inplace=True)


def unpack(packed_list):
    unpacked_list = []
    
    for sublist in packed_list:
        unpacked_list.extend(sublist)
        
    return unpacked_list

###################################################################################################
# Create Training and Test Data:
###################################################################################################

# Collect all graphs from the given folder:
dataset_nodes, dataset_edges, dataset_bug_locations = load_dataset(dataset_path, dictionary_words, log=True)

pd.concat(dataset_nodes).to_csv('dataset_nodes.csv',index=False) 
pd.concat(dataset_nodes).to_pickle('dataset_nodes.pkl')

print('Finished saving nodes!\n')

pd.concat(dataset_edges).to_csv('dataset_edges.csv',index=False) 
pd.concat(dataset_edges).to_pickle('dataset_edges.pkl')

print('Finished saving edges!\n')
    
with open('dataset_bug_locations.csv', 'w') as f:
    wr = csv.writer(f)
    wr.writerow(dataset_bug_locations)
    f.close()
    
print('Finished saving bug locations!')   

pd.read_csv('dataset_nodes.csv')
pd.read_pickle('dataset_nodes.pkl')

pd.read_csv('dataset_edges.csv')
pd.read_pickle('dataset_edges.pkl')

bug_locations=[]
with open('dataset_bug_locations.csv', 'r') as f:
    freader = csv.reader(f)
    for row in freader:
        print(row)
        bug_locations.append(row)
    f.close() 
        
        