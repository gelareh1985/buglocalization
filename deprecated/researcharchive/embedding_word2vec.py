'''
Created on Dec 17, 2020

@author: Gelareh_mp
'''
import pandas as pd

import os
from IPython.display import display

from preprocess_word_dictionary import WordDictionary
from nltk.corpus import stopwords

from gensim import corpora
import pprint
from gensim.models import Word2Vec


stop_words = set(stopwords.words('english'))

path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest\positive/"
models_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest\models\word2vec/"
# path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000\positive/"
# models_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000\models\word2vec\positive/"
#dictionary_path= r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary_shrinked.dictionary"

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

def add_prefix(prefix, index):
    return str(prefix) + "_" + str(index)

#######################################################################################
dictionary_words = WordDictionary(stopwords=stop_words)

table_nodes= load_dataset(path)
list_of_list_nodes=[]
for table in table_nodes:
    display(table)
    list_nodes=[]
    list_words=[]
    for node_index, node_row in table.iterrows():
        node = node_row["node"]
        list_nodes.append(str(node))
    
    list_of_list_nodes.append(list_nodes)    
    
list_of_file_corpus=[]
for table in list_of_list_nodes:
    list_row_words=[]
    list_row_dicts=[]
    for node_text in table:
        #print(node_text)
        node_text=str(node_text) 
        words , dictionary= dictionary_words.add_text(node_text) 
        
        list_row_words.append(words)
        list_row_dicts.append(dictionary)
    print('length of words lines in node list: ', len(list_row_words)) 
    list_of_file_corpus.append(list_row_words) 

##########################################################################################
# Doc2vec Embedding: (Word2Vec)
##########################################################################################
complete_dictionary_words =[]

for file_corpus in list_of_file_corpus:
    for row in file_corpus:
        if row not in complete_dictionary_words:
            complete_dictionary_words.append(row)
complete_dictionary = corpora.Dictionary(complete_dictionary_words) 
print('Complete Dictionary -> (length): ')           
#pprint.pprint(len(complete_dictionary.token2id))
#complete_dictionary.token2id

complete_dict=complete_dictionary.token2id
# print(complete_dict)

# complete_dictionary_path=path+"complete_dictionary_set_5000_positive_word2vec.dictionary"
# with open(complete_dictionary_path,'w') as f:
#     for key, value in complete_dict.items():
#         str_line=str(value)+'\t'+str(key)+'\n'
#         f.write(str_line)
# f.close()
    
sentences = complete_dictionary_words
print('complete set sentences: ',sentences)
model = Word2Vec(min_count=1)
model.build_vocab(sentences)  # prepare the model vocabulary
#print('vocabs: ')
model.train(sentences, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors
node_embeddings = model.wv.vectors  # numpy.ndarray of size number of nodes times embeddings dimensionality
print('length of all node embeddings: ',len(node_embeddings))
node_embedding_size=model.wv.vector_size
print('complete set node embedding size: ',node_embedding_size)    
for k, word in enumerate(model.wv.index2word):
    print(k, '    ', word)  

i=1
list_word2vec_models=[]
for file_corpus in list_of_file_corpus:
    dictionary = corpora.Dictionary(file_corpus)
    print('Dictionary -> (length): ', len(dictionary))
    print('file corpus: ',file_corpus)
     
    list_unique_text=[]
    for key , value in dictionary.items():
        if complete_dict.get(value)!="":
            list_unique_text.append(value)
     
    #print('unique text: ', list_unique_text)
    #print('unique text length: ', len(list_unique_text))
             
             
    list_row2=[]
    list_row_dict={}
    for row in range(len(file_corpus)):
        list_row_text=[]
         
        cntr=0
        for value in list_unique_text:
            #file_corpus[row]=filter(None, file_corpus[row])
            if value in file_corpus[row] and (value not in list_row_dict) and value.isspace()==False:
                list_row_dict.update({value:cntr})
                list_row_text.append(value) 
                cntr=cntr+1
        list_row2.append(list_row_text)
    
    print('list of corpus sentences: ', ' (length): ',len(list_row2), '    ',list_row2)
    
    sentences = file_corpus
    print('sentences: ',sentences)
    model = Word2Vec(min_count=1)
    list_word2vec_models.append(model)
    model.build_vocab(sentences)  # prepare the model vocabulary
    #print('vocabs: ')
    model.train(sentences, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors
    node_embeddings = model.wv.vectors  # numpy.ndarray of size number of nodes times embeddings dimensionality
    #print('node embeddings: ',node_embeddings)
    node_embedding_size=model.wv.vector_size
    print('node embedding size: ',node_embedding_size, '    length of embeddings: ', len(node_embeddings))
    
    print('\n ##################################################### \n')
    
    print(model.wv["compiler"])
    node_ids = model.wv.index2word 
    print(len(node_ids), '    ',node_ids)
    print('vocabs: ', model.wv.index2word)
    
    ii=model.wv.vocab.get("compiler").index
    print('index: ',ii, '        word: ', model.wv.index2word[ii])

    print(node_embeddings[ii])
    
    print('the queried word found',model.wv.get_vector(model.wv.index2word[ii])) 
         
    for k, word in enumerate(model.wv.index2word):
        print(k, '    ', word)    
#     path_save_model=models_path+"word2vec"+str(i)+".model"
#     model.save(path_save_model) 

#     path_save_model=models_path+"word2vec_embedding_"+str(i)+'.model'
#      
#     with open(path_save_model,'w')as f:
#         for row in node_embeddings:
#             str_line=str(row)+'\n'
#             f.write(str_line)
#     f.close() 

#     sentences = ['word1','word2','word3', 'word4', 'word5', 'word6']
#     model = gensim.models.Word2Vec(sentences=sentences)
#     filename=positve_samples_path+'test.txt'
#     model.save(filename)
#     model = gensim.models.KeyedVectors.load_word2vec_format(filename, binary=True)
#     print(model['word1'])
#     for index, word in enumerate(model.wv.index_to_key):
#         print(index, word)
     
    i=i+1  