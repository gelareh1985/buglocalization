'''
Created on Dec 23, 2020

@author: Gelareh_mp
'''
#import matplotlib.pyplot as plt

#from sklearn.manifold import TSNE
#from sklearn.model_selection import train_test_split
#from sklearn.linear_model import LogisticRegressionCV
#from sklearn.metrics import accuracy_score

#import os
#import networkx as nx
#import numpy as np
#import pandas as pd

from stellargraph.data import BiasedRandomWalk
#from stellargraph import StellarGraph
from stellargraph import datasets
from IPython.display import display, HTML

from gensim.models import Word2Vec

dataset = datasets.Cora()
display(HTML(dataset.description))
#G, node_subjects = dataset.load()

G, node_subjects = dataset.load(subject_as_feature=True)
print(G.info())

nodes=G.nodes()
print('nodes    :    ',str(len(nodes)),'    ;    ','\n', nodes)
node_feats=G.node_features(nodes)
print('node features    ','number of rows: ',str(len(node_feats)),'number of columns: ', str(len(node_feats[0])),'    ;    ','\n',node_feats)
print('node subjects    :    ',str(len(node_subjects)),'    ;    \n',node_subjects)



list_nodes=[]
for node in G.nodes():
    list_nodes.append([str(node)])

print(len(list_nodes),'        ',list_nodes)

# rw = BiasedRandomWalk(G)
# 
# walks = rw.run(nodes=list(G.nodes()), length=100, n=10, p=0.5, q=2.0, )
# print("Number of random walks: {}".format(len(walks)))
# 
# str_walks = [[str(n) for n in walk] for walk in walks]
model = Word2Vec(list_nodes, size=128, window=5, min_count=0, sg=1, workers=2, iter=1)

# The embedding vectors can be retrieved from model.wv using the node ID as key.
print(model.wv["19231"].shape)

    
# Retrieve node embeddings and corresponding subjects
node_ids = model.wv.index2word  # list of node IDs
node_embeddings = (model.wv.vectors)  

ii=model.wv.vocab.get("19231").index
print('index: ',ii, '        word: ', model.wv.index2word[ii])
print(node_embeddings[ii])
print('the queried word found',model.wv.get_vector(model.wv.index2word[ii])) 

# # numpy.ndarray of size number of nodes times embeddings dimensionality
# node_targets = node_subjects[[int(node_id) for node_id in node_ids]]

print('node ids ',', length: ',len(node_ids), '    ', node_ids)
print('node embeddings: ', node_embeddings)
node_embedding_size=model.wv.vector_size
print('size of node embeddings: ', node_embedding_size, '    ', len(node_embeddings))

# #model.build_vocab(list_nodes)  # prepare the model vocabulary
# model.train(list_nodes, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors
# node_embeddings = model.wv.vectors  # numpy.ndarray of size number of nodes times embeddings dimensionality
# print('node embeddings: ',node_embeddings)
# node_embedding_size=model.wv.vector_size
# print('node embedding size: ', node_embedding_size, '    ', len(node_embeddings))

# for k, word in enumerate(model.wv.index2word):
#         print(k, '    ', word)    

