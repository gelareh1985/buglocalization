'''
Created on Dec 23, 2020

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from preprocess_word_dictionary import WordDictionary
from nltk.corpus import stopwords
from IPython.display import display
from pandas.api.types import CategoricalDtype

stop_words = set(stopwords.words('english'))

positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"
dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/complete_dictionary_set_smalltest_positive.dictionary"

# positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/positive/"
# dictionary1_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/complete_dictionary_set_5000_positive.dictionary"
# dictionary2_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/complete_dictionary_set_5000_negative.dictionary"

def load_dataset(path):
    
    dataset_nodes = []
    dataset_edges=[]
    
    # Read all samples and create unique node IDs:
    for filename in os.listdir(path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = path + graph_filename + ".nodelist"
            edge_list_path = path + graph_filename + ".edgelist"
            graph_number = graph_filename[0:graph_filename.find("_")]

            # nodes:
            nodes_data = load_nodes(node_list_path,graph_number)
            dataset_nodes.append(nodes_data)
            
            # edges:
            edge_data = load_edges(edge_list_path, graph_number)
            dataset_edges.append(edge_data)
            
            print("Graph: ", graph_number)

    if(not dataset_nodes):
        raise Exception('No samples found')
  
    return dataset_nodes,dataset_edges

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

def load_dict(filename):
    dictionary = {}
    
    with open(filename) as f:
        for i, line in enumerate(f):
            columns = line.strip().split('\t')
            dictionary[columns[0]] = columns[1]
    f.close()
    return dictionary

###############################################################################
# Initialize dictionary:
dictionary_words = WordDictionary(stopwords=stop_words)

# Encode all graphs from the given folder:
table_nodes,table_edges= load_dataset(positve_samples_path)

list_of_list_nodes=[]
list_graph_index=[]

for table in table_nodes:
    display(table)
    list_nodes=[]
    list_graph_index.append(table.index.values.tolist())
    for node_index, node_row in table.iterrows():
        node = node_row["node"]
        list_nodes.append(str(node))
    list_of_list_nodes.append(list_nodes)  
    
#print('initial corpus nodes: ', list_of_list_nodes) 

list_of_file_corpus_words_per_line=[]
list_of_file_corpus_single_string_per_line=[]
for table in list_of_list_nodes:
    list_row_words=[]
    list_row_single_string=[]
    for node_text in table:
        node_text=str(node_text) 
        words , dictionary= dictionary_words.add_text(node_text) 
        single_string_of_words=' '.join(words)
        list_row_words.append(words)
        list_row_single_string.append([single_string_of_words])
    #print('corpus words per line: ',' , (length): ', len(list_row_words), '    ', list_row_words)
    #print('corpus single string per line: ',' , (length): ', len(list_row_single_string), '    ', list_row_single_string)  
    list_of_file_corpus_words_per_line.append(list_row_words) 
    list_of_file_corpus_single_string_per_line.append(list_row_single_string) 
    
###############################################################################
### One Hot Encoding
###############################################################################
# dict1=load_dict(dictionary1_path)
# dict2=load_dict(dictionary2_path)
# #print('dict1 length: ',len(dict1),'     dict2 length: ',len(dict2))
# dict_filename=positve_samples_path+'dict_merged.dictionary'
#     
# #np.savetxt(filename1,z1)
# #np.savetxt(filename2,z2)
# complete_dictionary={}
# for key , value in dict1.items():
#     if (key,value in dict2.items()) and (key,value not in complete_dictionary.items()):
#         complete_dictionary.update({key:value})
# print('complete_dictionary length: ',len(complete_dictionary))       
#     
# with open(dict_filename,'w') as f:
#     for key , value in complete_dictionary.items():
#         strline=str(key)+'\t'+str(value)+'\n'
#         f.write(strline)
# f.close()

complete_dictionary = load_dict(dictionary_path)
list_words=[]
for key , value in complete_dictionary.items():
    list_words.append(value)

###############################################################################
#df=pd.DataFrame(list_words,columns=["vocabulary"]) 

list_words_arr=np.array(list_words)
list_of_file_corpus_words_per_line_arr=np.asarray(list_of_file_corpus_words_per_line)
i=0
table_nodes_list1=[]
#list_of_list_single_vector=[] 
for file_corpus in list_of_file_corpus_words_per_line_arr:
    print('file corpus',' , (length): ',len(file_corpus))
    
    df1=pd.DataFrame(list_words_arr,columns=["vocabulary"],dtype=str) 
    df2=pd.DataFrame(file_corpus,dtype=str)
    df2.rename(columns=lambda x: "line_"+str(x),inplace=True)
    
    print('merged data_frame: ')
    df3=pd.concat([df2, df1], axis=1)
    df3.rename(index=lambda x: "word_"+str(x),inplace=True)
    df3.fillna(value="", inplace=True)
    display(df3)
        
    df2=df2.T
    
    print('categorial df: ')
    df3.drop('vocabulary', axis=1,inplace=True)
    cat_list=df1['vocabulary'].tolist()
    cat_type = CategoricalDtype(categories=cat_list, ordered=True)
    df_cat = df3.astype(cat_type)
    display(df_cat)
    #display(df_cat.loc['word_0',:])
    filename=positve_samples_path+'data_frames/data_frame_categorial_'+str(i)+'.csv'
    df_cat.to_csv(filename, chunksize=1000)
    
#     filename=positve_samples_path+'data_frames/data_frame_categorial_onehot_'+str(i)+'.csv'
#     df_cat.to_csv(filename)
#     print('dummies method: ')
#     df4=pd.get_dummies(df_cat)
#     display(df4)
#     filename=positve_samples_path+'data_frames/data_frame_dummies_'+str(i)+'.csv'
#     df4.to_csv(filename)
    
    i=i+1
