'''
Created on Sep 7, 2020

@author: Gelareh_mp
'''
'''
Created on Sep 7, 2020

@author: Gelareh_mp
'''
# import numpy as np
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
# from sklearn.manifold import TSNE
# import pandas as pd
# import re
# import os

# from nltk.corpus import stopwords

from word_to_vec import generate_dictinoary_data
from word_to_vec import generate_training_data
from word_to_vec import train
from word_to_vec import word_similarity_scatter_plot
from word_to_vec import plot_epoch_loss

path_to_save = r'D:\\workspaces\\buglocalization\\word2vec_test\\output_images_sample_data\\epoc_loss_plot\\'
path_to_save_scatter_plot =  r'D:\\workspaces\\buglocalization\\word2vec_test\\output_images_sample_data\\word_similarity_scatter_plot\\'
        
def sketch_analysis1(text): 
    window_size = 2
    epochs = 100
    learning_rate = 0.01
    #text = ["Best way to success is through hardwork and persistence"]
    
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)
    
    dimensions = [5,10,15,20]
    loss_epoch = {}
    
    fig, axes = plt.subplots(nrows=2, ncols=2,figsize=(10,10))
    fig.suptitle("Plots for showing paramaters with varying dimension", fontsize=14)
    row=0
    col=0
    for dim in dimensions:
        
        epoch_loss,weights_1,weights_2 = train(dim,vocab_size,epochs,training_data,learning_rate)
        loss_epoch.update( {dim: epoch_loss} )
        
        word_similarity_scatter_plot(
            index_to_word,
            weights_1[epochs -1],
            'dimension_' + str(dim) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
            axes[row][col]
        )
        if col == 1:
            row += 1
            col = 0
        else:
            col += 1
    plt.savefig(path_to_save_scatter_plot+'varying_dim' +'.png')        
    #plt.show()
    plot_epoch_loss('dim:',loss_epoch,'epochs_' + str(epochs) + '_window_size_' +str(window_size),path_to_save)
    
def sketch_analysis2(text):
    dimension = 20
    epochs = 100
    learning_rate = 0.01
    
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    window_size = [2,3,5,7]
    loss_epoch = {}
    fig, axes = plt.subplots(nrows=2, ncols=2,figsize=(10,10),)
    fig.suptitle("Plots for showing paramaters with varying window_size", fontsize=16)
    row=0
    col=0
    for ws in window_size:
        
        training_data,training_sample_words = generate_training_data(corpus,ws,vocab_size,word_to_index,length_of_corpus)
        
        epoch_loss,weights_1,weights_2 = train(dimension,vocab_size,epochs,training_data,learning_rate)
        loss_epoch.update( {ws: epoch_loss} )
        
        word_similarity_scatter_plot(
            index_to_word,
            weights_1[epochs -1],
            'dimension_20_' + '_epochs_' + str(epochs) + '_window_size_' +str(ws),
            axes[row][col]
        )
        if col == 1:
            row += 1
            col = 0
        else:
            col += 1
    plt.savefig(path_to_save_scatter_plot+'varying_window_size' +'.png')                
    #plt.show()
    plot_epoch_loss('window_size_',loss_epoch,'_epochs_' + str(epochs) + '_dimension_20' ,path_to_save)


def sketch_analysis3(text):        
    window_size = 2
    dimension = 20
    learning_rate = 0.01
    #text = ['Best way to success is through hardwork and persistence']
    
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)    

    epochs = [100,200,300,400]
    loss_epoch = {}
    fig, axes = plt.subplots(nrows=2, ncols=2,figsize=(10,10),)
    fig.suptitle("Plots for showing paramaters with varying epochs", fontsize=16)
    row=0
    col=0
    for epoch in epochs:
        
        epoch_loss,weights_1,weights_2 = train(dimension,vocab_size,epoch,training_data,learning_rate)
        loss_epoch.update( {epoch: epoch_loss} )
        
        word_similarity_scatter_plot(
            index_to_word,
            weights_1[epoch -1],
            'dimension_' + str(dimension) + '_epochs_' + str(epoch) + '_window_size_' +str(window_size),
            axes[row][col]
        )
        if col == 1:
            row += 1
            col = 0
        else:
            col += 1
    plt.savefig(path_to_save_scatter_plot+'varying_epochs' +'.png')                
    #plt.show()
    plot_epoch_loss('epochs_',loss_epoch,'dimension_' + str(dimension) + '_window_size_' +str(window_size),path_to_save)

def show_trained_data(text):
    word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
    window_size = 2
    training_data,training_sample_words = generate_training_data(corpus,2,vocab_size,word_to_index,length_of_corpus)
    for i in range(len(training_data)):
        print('*' * 50)
        print('Target word:%s . Target vector: %s ' %(training_sample_words[i][0],training_data[i][0]))
        print('Context word:%s . Context  vector: %s ' %(training_sample_words[i][1],training_data[i][1]))
    
    