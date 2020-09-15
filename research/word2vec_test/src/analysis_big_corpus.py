'''
Created on Sep 7, 2020

@author: Gelareh_mp
'''
import numpy as np
from IPython.display import display, HTML
import matplotlib.pyplot as plt
from sklearn.manifold import TSNE
import pandas as pd
import re
import os

from nltk.corpus import stopwords

from word_to_vec import get_file_data
from word_to_vec import generate_dictinoary_data
from word_to_vec import generate_training_data
from word_to_vec import train
from word_to_vec import word_similarity_scatter_plot
from word_to_vec import word_similarity_scatter_plot_bigger_corpus
from word_to_vec import plot_epoch_loss
from word_to_vec import print_similar_words
#------------------------------------------------------------------------------
## Bigger Corpus
#------------------------------------------------------------------------------
path_to_save = r'D:\\workspaces\\buglocalization\\word2vec_test\\output_images_bigger_data\\word_similarity_scatter_plot\\'
path_to_save_scatter_plot =  r'D:\\workspaces\\buglocalization\\word2vec_test\\output_images_bigger_data\\word_similarity_scatter_plot\\'
# #------------------------------------------------------------------------------
#words_subset=[]

    
def sketch_analysis_B1(path): 
    #global words_subset   
#------------------------------------------------------------------------------
# 1. Stop words effect
#------------------------------------------------------------------------------
    epochs = 200
    top_n_words = 5
    dimension = 50
    window_size = 2
    learning_rate = 0.01
#------------------------------------------------------------------------------
# Without stopwords
#------------------------------------------------------------------------------
    text = get_file_data(path,'yes')
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)
    print('Number of unique words:' , vocab_size)
    print('Length of corpus :',length_of_corpus)
    #words_subset = []
    words_subset = np.random.choice(list(word_to_index.keys()),top_n_words)
    print(words_subset)
#------------------------------------------------------------------------------
    loss_epoch = {}
    dataframe_sim = []
     
    epoch_loss,weights_1,weights_2 = train(dimension,vocab_size,epochs,training_data,learning_rate,'yes',50)
    loss_epoch.update( {'yes': epoch_loss} )
     
    word_similarity_scatter_plot_bigger_corpus(
        index_to_word,
        weights_1[epochs -1],
        'Stopwords_removed_dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        path_to_save_scatter_plot
    )
     
    df = print_similar_words(
        top_n_words,
        weights_1[epochs - 1],
        'sim_matrix for : Stopwords_removed_dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        words_subset, word_to_index, vocab_size,index_to_word
        
    )
    dataframe_sim.append(df)
    display(df) 
    
    for i in range(len(dataframe_sim)):
        print(dataframe_sim[i])
    
    plot_epoch_loss(
        'Stopwords_removed_',
        loss_epoch,
        'Without_Stopwords_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        path_to_save
    )
  
#------------------------------------------------------------------------------
# Similarity matrix
#------------------------------------------------------------------------------
#     print(len(dataframe_sim))
#     for i in range(len(dataframe_sim)):
#         display(dataframe_sim[i])
#         print(dataframe_sim[i])
    #return words_subset 
    
   
    
def sketch_analysis_B2(str_path):            
#------------------------------------------------------------------------------
# With stopwords
#------------------------------------------------------------------------------
    epochs = 200
    top_n_words = 5
    dimension = 50
    window_size = 2
    learning_rate = 0.01
    
    text = get_file_data(str_path,'no')
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)
    print('Number of unique words:' , vocab_size)
    print('Length of corpus :',length_of_corpus)
    #------------------------------------------------------------------------------
    words_subset=[]
    words_subset = np.random.choice(list(word_to_index.keys()),top_n_words)
    
    loss_epoch = {}
    dataframe_sim = []
     
    epoch_loss,weights_1,weights_2 = train(dimension,window_size,epochs,training_data,learning_rate,'yes',50)
    loss_epoch.update( {'no': epoch_loss} )
     
    word_similarity_scatter_plot_bigger_corpus(
        index_to_word,
        weights_1[epochs -1],
        'Stopwords_not_removed_dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        path_to_save_scatter_plot
    )
     
    #words_subset= sketch_analysis1()
    df = print_similar_words(
        top_n_words,
        weights_1[epochs - 1],
        'sim_matrix for : Stopwords_not_removed_dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        words_subset, word_to_index, vocab_size,index_to_word
    )
    dataframe_sim.append(df)
    plot_epoch_loss(
        'Stopwords_removed_',
        loss_epoch,
        'With_Stopwords_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        path_to_save
    )
#------------------------------------------------------------------------------   
# Similarity matrix
#------------------------------------------------------------------------------  
    for i in range(len(dataframe_sim)):
        display(dataframe_sim[i])

def sketch_analysis_B3(str_path):            
#------------------------------------------------------------------------------      
# For further variations we will be removing stopwords
#------------------------------------------------------------------------------ 
    epochs = 200
    top_n_words = 5
    learning_rate = 0.01
    window_size = 2
    
    text = get_file_data(str_path,'yes')
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)
    print('Number of unique words:' , vocab_size)
    print('Length of corpus :',length_of_corpus)
    words_subset = []
    words_subset = np.random.choice(list(word_to_index.keys()),top_n_words)
    print(words_subset)

#------------------------------------------------------------------------------
#Varying dimensions of word embedding 
#------------------------------------------------------------------------------
    dimensions = [10,30,50,70,90]
    loss_epoch = {}
    dataframe_sim = []
    for dim in dimensions:
        print('Running for dimension :' ,dim)
        
        epoch_loss,weights_1,weights_2 = train(dim,window_size,epochs,training_data,learning_rate,'yes',50)
        loss_epoch.update( {dim: epoch_loss} )
        
        word_similarity_scatter_plot_bigger_corpus(
            index_to_word,
            weights_1[epochs -1],
            'varying_dimension_' + str(dim) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
            path_to_save_scatter_plot
        )
        
        df = print_similar_words(
            top_n_words,
            weights_1[epochs - 1],
            'sim_matrix for : dimension_' + str(dim) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
            words_subset, word_to_index, vocab_size,index_to_word
        )
        dataframe_sim.append(df)
        
    plot_epoch_loss(
        'dim:',
        loss_epoch,
        'epochs_' + str(epochs) + '_window_size_' +str(window_size),
        path_to_save
    )
#------------------------------------------------------------------------------
#Similarity matrix
#------------------------------------------------------------------------------
    for i in range(len(dataframe_sim)):
        display(dataframe_sim[i])

def sketch_analysis_B4(str_path):    
#------------------------------------------------------------------------------    
# Varying window size
#------------------------------------------------------------------------------
    epochs = 200
    dimension = 70
    learning_rate = 0.01
    top_n_words = 5
    
    window_size = [2,5,7,9,12] 
    
    text = get_file_data(str_path,'no')
    loss_epoch = {}
    dataframe_sim = []
    for ws in window_size:
        print('Running for window_size :' ,ws)
        word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
        training_data,training_sample_words = generate_training_data(corpus,ws,vocab_size,word_to_index,length_of_corpus)
        
        epoch_loss,weights_1,weights_2 = train(dimension,ws,epochs,training_data,learning_rate,'yes',50)
        loss_epoch.update( {ws: epoch_loss} )
        
        words_subset=[]
        words_subset = np.random.choice(list(word_to_index.keys()),top_n_words)
        
        word_similarity_scatter_plot_bigger_corpus(
            index_to_word,
            weights_1[epochs -1],
            'varuing_window_size_' + str(ws) + '_epochs_' + str(epochs) + 'dimension_' +str(dimension),
            path_to_save_scatter_plot
        )
        
        df = print_similar_words(
            top_n_words,
            weights_1[epochs - 1],
            'sim_matrix for : dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(ws),
            words_subset, word_to_index, vocab_size,index_to_word
        )
        dataframe_sim.append(df)
        
    plot_epoch_loss(
        'window_size:',
        loss_epoch,
        'epochs_' + str(epochs) + '_dimension_' +str(dimension),
        path_to_save
    )
#------------------------------------------------------------------------------
# Similarity matrix
#------------------------------------------------------------------------------
    for i in range(len(dataframe_sim)):
        display(dataframe_sim[i])
        
def sketch_analysis_B5(str_path):   
    epochs = 200
    top_n_words = 5
    learning_rate = 0.01 
    dimension = 70
    text = get_file_data(str_path,'no')
# Final training with dimension = 70 , window_size = 2 , epochs = 1000
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    words_subset=[]
    words_subset = np.random.choice(list(word_to_index.keys()),top_n_words)
    print(words_subset)
    window_size = 2
    dimension = 70
    epochs = 1000
    
    training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)    
    
    loss_epoch = {}
    dataframe_sim = []
        
    epoch_loss,weights_1,weights_2 = train(dimension,window_size,epochs,training_data,learning_rate,'yes',200)
    loss_epoch.update( {'': epoch_loss} )
    
    word_similarity_scatter_plot_bigger_corpus(
        index_to_word,
        weights_1[epochs -1],
        'dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        'final_training_scatter_plot_'
    )
    
        
    df = print_similar_words(
        top_n_words,
        weights_1[epochs - 1],
        'sim_matrix for : dimension_' + str(dimension) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
        words_subset, word_to_index, vocab_size,index_to_word
    )
    dataframe_sim.append(df)
    
    plot_epoch_loss(
        'Final_training',
        loss_epoch,
        ' epochs_' + str(epochs) + '_window_size_' +str(window_size) + '_dimension_' +str(dimension),
        'final_training_epoch_loss_plot_'
    )
    
    for i in range(len(dataframe_sim)):
        display(dataframe_sim[i])





