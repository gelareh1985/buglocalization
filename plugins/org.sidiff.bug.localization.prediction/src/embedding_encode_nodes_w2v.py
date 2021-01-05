'''
Created on Nov 30, 2020

@author: Gelareh_mp
'''
import pandas as pd
import os
from IPython.display import display
from word_dictionary import WordDictionary
from nltk.corpus import stopwords
from gensim import corpora
from gensim.models import Word2Vec
from gensim.models.keyedvectors import KeyedVectors

stop_words = set(stopwords.words('english'))

# positve_samples_path =      r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_6000/positive/"
# feature_node_save_path =    positve_samples_path + "/features/"
# dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary_shrinked.dictionary"

positve_samples_path =      r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"
feature_node_save_path =    positve_samples_path + "/features/"
#dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/complete_dictionary_set_smalltest_positive.dictionary"


def load_dataset(path):
    
    dataset_nodes = []
    
    # Read all samples and create unique node IDs:
    for filename in os.listdir(path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = path + graph_filename + ".nodelist"
            graph_number = graph_filename[0:graph_filename.find("_")]

            # nodes:
            nodes_data = load_nodes(node_list_path,graph_number)
            dataset_nodes.append(nodes_data)
            
            print("Graph: ", graph_number)

    if(not dataset_nodes):
        raise Exception('No samples found')
  
    return dataset_nodes

def load_nodes(node_list_path,graph_number):
   
    # Column names:
    node_data_columns = []
    node_data_columns.append("index")
    node_data_columns.append("node")
    node_data_columns.append("metatype")
     
    # Load data:
    node_data = pd.read_table(node_list_path,names=node_data_columns)
  
    return node_data


# *******************************************************************************************
# Initialize dictionary:
dictionary_words = WordDictionary(stopwords=stop_words)

# Encode all graphs from the given folder:
table_nodes= load_dataset(positve_samples_path)
list_of_list_nodes=[]
list_of_list_node_indexes=[]

for table in table_nodes:
    display(table)
    list_nodes=[]
    list_node_indexes=[]
    for node_index, node_row in table.iterrows():
        node = node_row["node"]
        node_indx = node_row["index"]
        list_nodes.append(str(node))
        list_node_indexes.append([str(node_indx)])
    list_of_list_nodes.append(list_nodes)  
    list_of_list_node_indexes.append(list_node_indexes)  
    
print('initial corpus nodes: ', list_of_list_nodes) 

list_of_file_corpus_words_per_line=[]
list_of_file_corpus_single_string_per_line=[]
for table in list_of_list_nodes:
    list_row_words=[]
    list_row_single_string=[]
    for node_text in table:
        #print(node_text)
        node_text=str(node_text) 
        words , dictionary= dictionary_words.add_text(node_text) 
        single_string_of_words=' '.join(words)
        list_row_words.append(words)
        list_row_single_string.append([single_string_of_words])
    print('corpus words per line: ',' , (length): ', len(list_row_words), '    ', list_row_words)
    print('corpus single string per line: ',' , (length): ', len(list_row_single_string), '    ', list_row_single_string)  
    list_of_file_corpus_words_per_line.append(list_row_words) 
    list_of_file_corpus_single_string_per_line.append(list_row_single_string) 

list_node_indx=[]
for node in list_of_list_node_indexes:
    list_node_indx.append(node)

    
##########################################################################################
# Word Embedding: (Word2Vec)
##########################################################################################
complete_dictionary_words =[]

for file_corpus in list_of_file_corpus_words_per_line:
    for row in file_corpus:
        if row not in complete_dictionary_words:
            complete_dictionary_words.append(row)
complete_dictionary = corpora.Dictionary(complete_dictionary_words) 
print('Complete Dictionary -> (length): ') 
     
complete_dict=complete_dictionary.token2id

i=1
list_word2vec_models=[]
for file_corpus in list_of_file_corpus_single_string_per_line:
    dictionary = corpora.Dictionary(file_corpus)
    print('Dictionary -> (length): ', len(dictionary))
    print('file corpus',' , (length): ',len(file_corpus), '    ',file_corpus)
          
#     list_unique_text=[]
#     for key , value in dictionary.items():
#         if complete_dict.get(value)!="":
#             list_unique_text.append(value)
#      
#     #print('unique text: ', list_unique_text)
#     #print('unique text length: ', len(list_unique_text))
#              
#              
#     list_row2=[]
#     list_row_dict={}
#     for row in range(len(file_corpus)):
#         list_row_text=[]
#          
#         cntr=0
#         for value in list_unique_text:
#             #file_corpus[row]=filter(None, file_corpus[row])
#             if value in file_corpus[row] and (value not in list_row_dict) and value.isspace()==False:
#                 list_row_dict.update({value:cntr})
#                 list_row_text.append(value) 
#                 cntr=cntr+1
#         list_row2.append(list_row_text)
    
    #model = Word2Vec(list_row2, size=128, window=5, min_count=0, sg=1, workers=2, iter=1)
    model = Word2Vec(file_corpus, size=128, window=5, min_count=0, sg=1, workers=2, iter=1)
#     # The embedding vectors can be retrieved from model.wv using the node ID as key.
#     print(model.wv["19231"].shape)

    
    # Retrieve node embeddings and corresponding subjects
    node_ids = model.wv.index2word  # list of node IDs
    node_embeddings = (model.wv.vectors) 
    node_embedding_size=model.wv.vector_size
    print('node embedding size: ',node_embedding_size, '    length of embeddings: ', len(node_embeddings))
    
#     sentences = list_row2
#     print('sentences: ',sentences)
#     model = Word2Vec(min_count=1)
#     list_word2vec_models.append(model)
#     model.build_vocab(sentences)  # prepare the model vocabulary
#     #print('vocabs: ')
#     model.train(sentences, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors
#     node_embeddings = model.wv.vectors  # numpy.ndarray of size number of nodes times embeddings dimensionality
#     #print('node embeddings: ',node_embeddings)
#     node_embedding_size=model.wv.vector_size
#     print('node embedding size: ',node_embedding_size, '    length of embeddings: ', len(node_embeddings))
    
    print('\n ##################################################### \n')
    
#     print(model.wv["compiler"])
    node_ids = model.wv.index2word 
    print('node ids',' , (length): ',len(node_ids), '    ',node_ids)
#     print('vocabs: ', model.wv.index2word)
#     
#     ii=model.wv.vocab.get("compiler").index
#     print('index: ',ii, '        word: ', model.wv.index2word[ii])
# 
#     #print(node_embeddings[ii])
#     
#     print('the queried word found',model.wv.get_vector(model.wv.index2word[ii])) 
         
#     for k, word in enumerate(model.wv.index2word):
#         print(k, '    ', word)    
#     path_save_model=models_path+"word2vec"+str(i)+".model"
#     model.save(path_save_model) 

#     path_save_model=models_path+"word2vec_embedding_"+str(i)+'.model'
#      
#     with open(path_save_model,'w')as f:
#         for row in node_embeddings:
#             str_line=str(row)+'\n'
#             f.write(str_line)
#     f.close() 
     
    i=i+1  

print('\n ********************************************************* \n')

for indexes_file_corpus in list_of_list_node_indexes: 
          
    print('corpus node_indexes: ',' , (length): ', len(indexes_file_corpus),'        ',indexes_file_corpus)
    model = Word2Vec(indexes_file_corpus, size=128, window=5, min_count=0, sg=1, workers=2, iter=1)
    # Retrieve node embeddings and corresponding subjects
    node_ids = model.wv.index2word  # list of node IDs
    node_embeddings = (model.wv.vectors) 
    node_embedding_size=model.wv.vector_size
    print('node embedding size: ',node_embedding_size, '    length of embeddings: ', len(node_embeddings))
   
    print('\n ##################################################### \n')
    
    node_ids = model.wv.index2word
    print('node ids',' , (length): ',len(node_ids), '    ',node_ids)
    filename=positve_samples_path+'test.txt'
    model.save(filename) 
    model = KeyedVectors.load_word2vec_format(filename, binary=True)
    print(model['1'])
#     print('vocabs: ', model.wv.index2word)
     
print('Finished encoding nodes!')
