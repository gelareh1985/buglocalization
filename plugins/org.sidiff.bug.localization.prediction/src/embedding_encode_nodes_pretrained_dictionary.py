'''
Created on Jan 12, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
from preprocess_word_dictionary import WordDictionary
from nltk.corpus import stopwords
from gensim.models import KeyedVectors

stop_words = set(stopwords.words('english'))

# positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/positive/"
# feature_path=r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000\positive\data_frames/"

positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"
dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary_shrinked.dictionary"
pretrained_dictionary_path= r"D:\files_MDEAI_original\Data_sets\GoogleNews-vectors-negative300.bin"

def load_dataset(path):
    
    dataset_nodes = []
    dataset_edges=[]
    dataset_features=[]
    
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
  
    return dataset_nodes,dataset_edges,dataset_features

def load_nodes(node_list_path,graph_number):
   
    # Column names:
    node_data_columns = []
    node_data_columns.append("__col_index__")
    node_data_columns.append("__node__")
    node_data_columns.append("__meta_type__")
    node_data_columns.append("__tag__")
    
#     table.fillna(value="", inplace=True)
     
    # Load data:
    node_data = pd.read_table(node_list_path,names=node_data_columns)
  
    # Create index:
    node_data.set_index("__col_index__", inplace=True)
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

    return df
    
def add_tag_types(table1,table2):    
    
    print('length of table1: ',len(table1["__tag__"]), '    length of table2: ', len(table2["__tag__"]))
    table2["__tag__"]=table1["__tag__"]
    
    metatype=table1["__meta_type__"].values.tolist()
    locations_tag=table1["__tag__"].values.tolist() 
    
    for typ_indx in range(len(metatype)):
        if(metatype[typ_indx]=="BugReportNode"):
            table2.loc[typ_indx,"__tag__"]="# REPORT"
    for loc_indx in range(len(locations_tag)):
        if(locations_tag[loc_indx]=="# LOCATION"):
            table2.loc[loc_indx,"__tag__"]="# LOCATION"

    return table2

def load_dict(filename):
    dictionary = {}
    
    with open(filename) as f:
        for i, line in enumerate(f):
            columns = line.strip().split('\t')
            dictionary[columns[0]] = columns[1]
    f.close()
    return dictionary

def iterate_tuple(itr):
    while not itr.finished:
        print(itr[0].shape)
        itr.iternext()
    return itr.operands[0]

def flatten_df(x):
    # note this is not very robust, but works for this case
    return [*x[0]]
            
# ***************************************************************

dataset_nodes,dataset_edges,dataset_features= load_dataset(positve_samples_path)   

print('length of dataset features: ', len(dataset_features))

list_of_list_nodes=[]
list_graph_index=[]

for table in dataset_nodes:
    print('nodes table: ')
    display(table)
    list_nodes=[]
    list_graph_index.append(table.index.values.tolist())
    for node_index, node_row in table.iterrows():
        node = node_row["__node__"]
        list_nodes.append(str(node))
    list_of_list_nodes.append(list_nodes)  

# *************** Dictionary Definition ************************* 
     
# Initialize dictionary:
dictionary_words = WordDictionary(stopwords=stop_words)    
complete_dictionary = load_dict(dictionary_path)

list_words=[]
for key , value in complete_dictionary.items():
    list_words.append(key)
list_words_arr=np.array(list_words)

# ************** Get List of File corpus ***********************  
 
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
        
    list_of_file_corpus_words_per_line.append(list_row_words) 
    list_of_file_corpus_single_string_per_line.append(list_row_single_string) 


list_of_file_corpus_words_per_line_arr=np.asarray(list_of_file_corpus_words_per_line)

# ************** Load Pretrained Dictionary **********************
print('Begin Loading Pretrained Dictionary Model ...')
model = KeyedVectors.load_word2vec_format(pretrained_dictionary_path, binary=True)
print('Finished Loading Pretrained Dictionary Model!\n')
# *************** Process List of File Corpus ********************   
 
List_of_all_files_vectors=[]

for file_corpus in list_of_file_corpus_words_per_line_arr:
    print('file corpus',' , (length): ',len(file_corpus))
    file_vectors=[]
    
    for phrases in file_corpus:
        line_single_vector=[]
        line_vectors=[]
        for word in phrases:
            try:
                vect=model[word]
                line_vectors.append(vect)
            except KeyError:   
                pass # ignore...how to handle unseen words...?
        
        line_vect_Sum=np.sum(line_vectors, axis=0) 
        #line_vect_Sum=line_vect_Sum.reshape(1,300)
        #line_single_vector.append(line_vect_Sum)
        file_vectors.append(line_vect_Sum)
        
    List_of_all_files_vectors.append(file_vectors)

col_num=np.arange(300).tolist()
list_cols=[]
for idx in range(len(col_num)):
    str_col="column_"+str(col_num[idx])
    list_cols.append(str_col)    
    
i=0
list_dfs=[]
for vect_corpus_list in List_of_all_files_vectors:
    print('file corpus',' , (length): ',len(vect_corpus_list))
#     num_arr=np.zeros((1,300))
#     for vects in vect_corpus_list:
#         
#         #arr=np.asarray(vects[0], dtype=np.float32).reshape(1,300)
#         num_arr=num_arr.append(vects[0])
    corp_arr=np.asarray(vect_corpus_list)
    
    #print('dimensions: ',' , (length): ',vect_corpus_list[:][0].shape)    
#     arr2=np.empty((len(vect_corpus_list),300))
#     arr3=np.hstack()
    
    ##arr1=np.asarray(vect_corpus_list)
#     arr2=np.asarray(list(vect_corpus_list))
#     tuple_arr1=list(tuple(arr1))
#     print('shapes:\n', 'arr1: ',arr1.shape, '    arr2: ',arr2.shape)
    df1= pd.DataFrame(pd.Series(corp_arr),columns=['vector'])
    display('DF1: ',df1)
    
    print(df1['vector'].values.shape, '    ',df1['vector'].ndim, '    ',df1['vector'].values.ndim)
    #df1=df1['vector'].values.reshape(63,300)
    display(corp_arr[:][1].shape,'    ',corp_arr[:][2].shape,'    ',corp_arr[:][3].shape)
    #arr_col_vecs=np.zeros(63,300)
#     arr_col_vecs=np.array([])
#     arr_col_vecs=np.append(arr_col_vecs,corp_arr[:][0:len(corp_arr)])
#     print(arr_col_vecs.shape, '    ', arr_col_vecs.ndim)
    print('final arr dims:',corp_arr[:][0:len(corp_arr)].shape,'\n')
    print('\n info of nd-array1: ',np.info(df1['vector'].values))
    #print('\n info of nd-array2: ',np.info(corp_arr[:][0:len(corp_arr)]))
#     for x in np.nditer(corp_arr):
#         print(x.shape)
#     for vec in np.nditer(df1.to_records(index=False),flags=['refs_ok']):
#         print('each vec type: ',type(vec)) # numpy nd array
          
    vec_tuples = np.nditer(df1.to_records(),flags=['refs_ok'],op_flags=['readwrite']) 
    pd.DataFrame(np.array(vec_tuples).reshape(63,300))
    print('vector tuple sizes: ',np.info(vec_tuples)) 
    
# #     oprnd=iterate_tuple(vec_tuples)
# #     display('operand: ',oprnd)
# #     for vec in vec_tuples: 
# #         display('vector tuples: ',vec[1])        
    i=i+1  
    
# col_num=np.arange(300).tolist()
# list_cols=[]
# for idx in range(len(col_num)):
#     str_col="column_"+str(col_num[idx])
#     list_cols.append(str_col)


