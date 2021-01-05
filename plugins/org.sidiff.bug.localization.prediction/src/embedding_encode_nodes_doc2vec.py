'''
Created on Dec 23, 2020

@author: Gelareh_mp
'''
import pandas as pd
import numpy as np
import os
from IPython.display import display
from preprocess_word_dictionary import WordDictionary
from nltk.corpus import stopwords
from gensim import corpora
from gensim.models import Word2Vec
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from nltk.tokenize import word_tokenize


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

##########################################################################################
# Word Embedding: (Doc2Vec)
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
        
    tagged_data = [TaggedDocument(words=row, tags=[str(i)]) for i, row in enumerate(file_corpus)]     
    
    print('tagged data: ', tagged_data)
    
    #model = Doc2Vec(size=20, alpha=0.025, min_alpha=0.00025, min_count=1, dm =1)
    model = Doc2Vec(vector_size=50, min_count=1)  
    model.build_vocab(tagged_data)
    
    #model.train(file_corpus, total_examples=model.corpus_count, epochs=20)
    
    max_epochs = 20
    model.epochs=10
    for epoch in range(max_epochs):
        print('iteration {0}'.format(epoch))
        model.train(tagged_data, total_examples=model.corpus_count, epochs=model.epochs)
#         # decrease the learning rate
#         model.alpha -= 0.0002
#         # fix the learning rate, no decay
#         model.min_alpha = model.alpha

    #     model.save("d2v.model")
    #     print("Model Saved")

    #to find the vector of a document which is not in training data
    test_data = word_tokenize("compiler file class".lower())
    vec1 = model.infer_vector(test_data)

    # to find most similar doc using tags
    similar_doc = model.docvecs.most_similar('1')
    print('similar doc: ',similar_doc)
    
    
    # to find vector of doc in training data using tags or in other words, printing the vector of document at index 1 in training data
    print('find the vector: ',model.docvecs['1'])
    
    #print(f"Word 'compile' appeared {model.wv.get_vecattr('compile', 'count')} times in the training corpus.")
    
    print('vectors: ',model.docvecs)
    vocab=model.wv.index2word
    print('vocabulary',' , (length): ', len(vocab), '    ', vocab)
    for k, word in enumerate(model.wv.index2word):
        print(k, '    ', word)  

#     ranks = []
   
#     second_ranks = []
#     for doc_id in range(len(file_corpus)):
#         inferred_vector = model.infer_vector(file_corpus[doc_id])
#         sims = model.dv.most_similar([inferred_vector], topn=len(model.dv))
#         rank = [docid for docid, sim in sims].index(doc_id)
#         ranks.append(rank)
# 
#     second_ranks.append(sims[1])
#     print('ranks: ', rank)
#     print('second ranks: ', second_ranks)
    
    i=i+1  

print('Finished embedding nodes!')

