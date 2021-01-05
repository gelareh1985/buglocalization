'''
Created on Jan 4, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import re
from IPython.display import display
import stellargraph as sg

#positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/positive/"
positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"

feature_path=r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest\positive\data_frames/"    

def load_dataset(path1,path2):
    
    dataset_nodes = []
    dataset_edges=[]
    dataset_features=[]
    
    # Read all samples and create unique node IDs:
    for filename in os.listdir(path1):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = path1 + graph_filename + ".nodelist"
            edge_list_path = path1 + graph_filename + ".edgelist"
            graph_number = graph_filename[0:graph_filename.find("_")]

            # nodes:
            nodes_data = load_nodes(node_list_path,graph_number)
            dataset_nodes.append(nodes_data)
            
            # edges:
            edge_data = load_edges(edge_list_path, graph_number)
            dataset_edges.append(edge_data)
            print("Graph: ", graph_number)
             
    for filename in os.listdir(path2):        
        if filename.endswith(".csv"):
            dataframe_filename = filename[:filename.rfind(".")]
            dataframe_list_path = path2 + dataframe_filename + ".csv"
            match_str = re.match('.+([0-9])[^0-9]*$', dataframe_filename)
            dataframe_number=match_str.group(1)
            dataframe_features= load_features(dataframe_list_path)
            
            dataset_features.append(dataframe_features)
            
            print("DataFrame: ", dataframe_number)
            
    if(not dataset_nodes):
        raise Exception('No samples found')
  
    return dataset_nodes,dataset_edges,dataset_features

def load_nodes(node_list_path,graph_number):
   
    # Column names:
    node_data_columns = []
    node_data_columns.append("col_index")
    node_data_columns.append("node")
    node_data_columns.append("meta_type")
     
    # Load data:
    node_data = pd.read_table(node_list_path,names=node_data_columns)
  
    # Create index:
    node_data.set_index("col_index", inplace=True)
    node_data = node_data.rename(index=lambda index: add_prefix(graph_number, index))
    node_data = node_data.fillna("")
    
    return node_data

def load_edges(edge_list_path, graph_number):
    edge_list_col_names = ["source", "target"]
    edge_data = pd.read_table(edge_list_path, names=edge_list_col_names)
    
    edge_data['source'] = edge_data['source'].apply(lambda index: add_prefix(graph_number, index))
    edge_data['target'] = edge_data['target'].apply(lambda index: add_prefix(graph_number, index))
    edge_data = edge_data.rename(index=lambda index: add_prefix(graph_number, index))
    
    return edge_data

def add_prefix(prefix, index):
    return str(prefix) + "_" + str(index)

def load_features(dataframe_list_path):
    
    reader = pd.read_csv(dataframe_list_path, chunksize=100)
    list_chunks=[]
    for chunk in reader:
        list_chunks.append(chunk)
    
    df = pd.concat(list_chunks)  
        
    return df
    
#***********************************************************************************************
dataset_nodes,dataset_edges,dataset_features= load_dataset(positve_samples_path,feature_path)   

print('length of dataset features: ', len(dataset_features))

list_of_list_nodes=[]
list_graph_index=[]

for table in dataset_nodes:
    print('nodes table: ')
    display(table)
    list_graph_index.append(table.index.values.tolist())

list_features_df=[]
for feat_table in dataset_features:
    feat_table.drop(feat_table.columns[feat_table.columns.str.contains('unnamed',case = False)],axis = 1, inplace = True)
    feat_table.dropna(how='all',inplace=True)
    print('features table: ')
    display(feat_table)
    list_features_df.append(feat_table)

    
    #feat_table=feat_table.notnull().astype('int')
    #feat_table=feat_table.fillna("0")
    #print('one hot encoded: ')
    #display(feat_table)

list_nodes_df=[]
for list_index in list_graph_index:    
    df=pd.DataFrame(list_index,columns=["index"])
    #df.set_index(df["index"],inplace=True)
    list_nodes_df.append(df)

concatenated_df_list=[]    
for idx in range(len(list_features_df)):
    concatenated_df=pd.concat([list_nodes_df[idx],list_features_df[idx].notnull().astype('int')],axis=1)
    #concatenated_df.set_index(concatenated_df["index"],inplace=True)
    concatenated_df_list.append(concatenated_df)

i=0
for df in concatenated_df_list:
    filename=positve_samples_path+'data_frame_for_Graph/df_graph_'+str(i)+'.csv'
    df.to_csv(filename, chunksize=1000)
    display(df)
    i=i+1
     
# dataset_nodes=concatenated_df_list     
# # Split training set:
# nodes_train = pd.concat(dataset_nodes[:len(dataset_nodes)//2])
# edges_train = pd.concat(dataset_edges[:len(dataset_edges)//2])
# #bug_locations_train = unpack(dataset_bug_locations[:len(dataset_bug_locations)//2])
# #edge_labels_train = np.ones(len(bug_locations_train))
#    
# # Split test set:
# nodes_test = pd.concat(dataset_nodes[len(dataset_nodes)//2:])
# edges_test = pd.concat(dataset_edges[len(dataset_edges)//2:])
# #bug_locations_test = unpack(dataset_bug_locations[len(dataset_bug_locations)//2:])
# #edge_labels_test = np.ones(len(bug_locations_test))
#  
# G_train = sg.StellarGraph(nodes_train, edges_train)
# print(G_train.info())
# G_test = sg.StellarGraph(nodes_test, edges_test)
# print(G_test.info())   


    