'''
Created on Dec 20, 2020

@author: Gelareh_mp
'''
import pandas as pd

import os
from IPython.display import display

from word_dictionary import WordDictionary
from nltk.corpus import stopwords

from gensim import corpora
import pprint
from gensim import models
from collections import defaultdict


stop_words = set(stopwords.words('english'))

path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest\positive/"
models_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest\models/"
# path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000\positive/"
# models_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000\models\TFIDF_unique_bug_corpus_docs\positive/"
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

def count_words(counter,list_text):
    for line in list_text:
        for pair in line: 
            counter=counter+1 
    #print("counter ",file_index,": ",counter)
    return counter

def count_frequency_of_words(file_corpus):
    frequency = defaultdict(int)
    for text in file_corpus:
        for token in text:
            frequency[token] += 1
    return frequency        
#######################################################################################
dictionary_words = WordDictionary(stopwords=stop_words)

table_nodes= load_dataset(path)
list_of_list_nodes=[]
list_of_list_bug_nodes=[]
for table in table_nodes:
    display(table)
    list_nodes=[]
    list_bug_nodes=[]
    list_words=[]
    for node_index, node_row in table.iterrows():
        node = node_row["node"]
        list_nodes.append(str(node))
        if node_row["metatype"]=="BugReportNode" or node_row["metatype"]=="BugReportCommentNode":
            bug_node = node_row["node"]
            list_bug_nodes.append(bug_node) 
    
    list_of_list_nodes.append(list_nodes)  
    list_of_list_bug_nodes.append(list_bug_nodes)  
    
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
    
list_of_file_bug_corpus=[]
for table in list_of_list_bug_nodes:
    list_row_words=[]
    list_row_dicts=[]
    for node_text in table:
        #print(node_text)
        node_text=str(node_text) 
        words , dictionary= dictionary_words.add_text(node_text) 
        
        list_row_words.append(words)
        list_row_dicts.append(dictionary)
    print('length of words lines in node list: ', len(list_row_words)) 
    list_of_file_bug_corpus.append(list_row_words) 
##########################################################################################
# Doc2vec Embedding: (BOW and TFIDF)
##########################################################################################
complete_dictionary_words =[]

for file_corpus in list_of_file_corpus:
    for row in file_corpus:
        if row not in complete_dictionary_words:
            complete_dictionary_words.append(row)
            
complete_dictionary = corpora.Dictionary(complete_dictionary_words) 
print('Complete Dictionary -> (length): ')           
pprint.pprint(len(complete_dictionary.token2id))
#complete_dictionary.token2id

complete_dict=complete_dictionary.token2id
# print(complete_dict)

# complete_dictionary_path=path+"complete_dictionary_set_smalltest_positive.dictionary"
# with open(complete_dictionary_path,'w') as f:
#     for key, value in complete_dict.items():
#         str_line=str(value)+'\t'+str(key)+'\n'
#         f.write(str_line)
# f.close()

i=1
#for file_bug_corpus in list_of_file_bug_corpus:
for file_corpus in list_of_file_corpus:
#     # vectors of all documents (lines) in file corpus
#     bow_corpus = [complete_dictionary.doc2bow(text) for text in file_corpus]
#     
#     print('\n Bag Of Words Corpus1: ' , len(bow_corpus), ' ,  file corpus1:  ', len(file_corpus))
#     #pprint.pprint(bow_corpus)
#     
#     file_index=1
#     c1=count_words(counter=0,list_text=file_corpus)
#     frequency_words=count_frequency_of_words(file_corpus)
#     print("counter corpus",file_index,": ",c1)
#     print("frequency corpus1: ",len(frequency_words),'    ',frequency_words) 
#     
#     # Only keep words that appear more than once
#     processed_corpus = [[token for token in text if frequency_words[token] > 1] for text in file_corpus]
#     #pprint.pprint(processed_corpus)
#     print('len of processed_corpus: ', len(processed_corpus))
   
    dictionary = corpora.Dictionary(file_corpus)
    print('Dictionary -> (length): ', len(dictionary))
#     dictt=dictionary.token2id
#     complete_dictionary_path=path+"dictionary_set_smalltest_positive"+str(i)+".dictionary"
#     with open(complete_dictionary_path,'w') as f:
#         for key, value in dictt.items():
#             str_line=str(value)+'\t'+str(key)+'\n'
#             f.write(str_line)
#     f.close()   
   
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
    
    bow_corpus2=[complete_dictionary.doc2bow(text) for text in list_row2]
    #print('\n Bag Of Words Corpus: ' , len(bow_corpus2),' ,  file corpus2:  ', len(list_row2))
    #pprint.pprint(bow_corpus3)
   
#     file_index=2
#     c2=count_words(counter=0,list_text=list_row2)
#     frequency_words=count_frequency_of_words(list_row2)
#     print("counter corpus",file_index,": ",c2)
#     print("frequency corpus2: ",len(frequency_words),'    ',frequency_words) 
#     
#     # step 1 -- initialize a model
#     tfidf = models.TfidfModel(bow_corpus)
#     
#     # Apply transformation to a whole corpus
#     corpus_tfidf = tfidf[bow_corpus]
#     corpus_tfidf_list=[]
#     for doc in corpus_tfidf:
#         corpus_tfidf_list.append(doc)
#         #print('Document: ',doc)
#     print('Document Shape: ',len(corpus_tfidf_list))
#       
#     path_save_model=models_path+"tfidf_1"+str(i)+'.model'
#       
#     with open(path_save_model,'w')as f:
#         for row in corpus_tfidf_list:
#             str_line=str(row)+'\n'
#             f.write(str_line)
#     f.close()  
    
    # step 1 -- initialize a model
    tfidf = models.TfidfModel(bow_corpus2)
    
    # Apply transformation to a whole corpus
    corpus_tfidf = tfidf[bow_corpus2]
    corpus_tfidf_list=[]
    for doc in corpus_tfidf:
        corpus_tfidf_list.append(doc)
        #print('Document: ',doc)
    print('Document Shape: ',len(corpus_tfidf_list))
      
    path_save_model=models_path+"tfidf_bug_nodes_"+str(i)+'.model'
      
    with open(path_save_model,'w')as f:
        for row in corpus_tfidf_list:
            str_line=str(row)+'\n'
            f.write(str_line)
    f.close()
     
    i=i+1  
