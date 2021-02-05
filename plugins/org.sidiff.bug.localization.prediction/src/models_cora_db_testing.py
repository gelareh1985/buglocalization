'''
Created on Dec 23, 2020

@author: Gelareh_mp
'''
import numpy as np
import pandas as pd

from tensorflow.python.keras.preprocessing.text import Tokenizer

from keras.preprocessing.sequence import pad_sequences
from keras.preprocessing.text import one_hot
from keras.preprocessing.text import text_to_word_sequence
from keras.models import Sequential
from keras.layers.embeddings import Embedding
from keras.layers import Dense, Embedding, LSTM, GRU
from keras.layers import Flatten

from stellargraph import datasets
from IPython.display import display, HTML

#from gensim.models import Word2Vec

dataset = datasets.Cora()
display(HTML(dataset.description))
#G, node_subjects = dataset.load()

G, node_subjects = dataset.load(subject_as_feature=True)
print(G.info())

nodes=G.nodes()
print('nodes    :    ',str(len(nodes)),'    ;    ','\n', nodes)
node_feats=G.node_features(nodes)
print('node features    ','number of rows: ',str(len(node_feats)),'number of columns: ', str(len(node_feats[0])),'    ;    ','\n',node_feats)
print('Cora node features type: ', type(node_feats))
print('node subjects: ',str(len(node_subjects)),'    ;    ',type(node_subjects))
print(node_subjects)

list_nodes=[]
for node in G.nodes():
    list_nodes.append([str(node)])

print(len(list_nodes),'        ',list_nodes)

print('shape of node subjects: ',node_subjects.shape)

df_feats=pd.DataFrame(node_feats,dtype=str)
print('\n Features dataframe: ')
display(df_feats)

df_cora=pd.DataFrame(list_nodes,columns=['Node_ID'],dtype=str)
df_cora['Node_Subject']=node_subjects.values
print('\n Cora Dataframe: ')
display(df_cora)

concatenated_df=pd.concat([df_cora,df_feats],axis=1)
print('\n Concatenated Dataframe: ')
display(concatenated_df)

docs=concatenated_df.loc[:len(concatenated_df),'Node_Subject'].values

tokenizer_obj=Tokenizer()
tokenizer_obj.fit_on_texts(docs)

max_length=4
vocab_size=len(tokenizer_obj.word_index)+1 # define vocabulary size

print('\n type of vocabulary: ', type(tokenizer_obj.word_index), '    ', vocab_size)
print('\n vocabulary: ',tokenizer_obj.index_word)

print('\n maximum length: ', max_length,'    vocabulary size: ', vocab_size)

#labels=np.array(list(tokenizer_obj.word_index.items()))

#list_all_labels=[]
labels_list=[]
labels=np.zeros(len(docs))
idx=0
for doc in docs:
    text=text_to_word_sequence(doc,filters='!"#$%&()*+,-./:;<=>?@[\\]^_`{|}~\t\n',lower=True, split=' ')
    dict_values=tokenizer_obj.index_word.values()
    if set(text).issubset(set(dict_values)):
        labels_list.append(text)
        labels[idx]=1
    idx=idx+1    
    #list_all_labels.append(labels_list)        
labels_arr=np.asarray(labels_list)

print('\n labels: (length): ',len(labels),'    labels: ',labels)

encoded_docs=[one_hot(d, vocab_size) for d in docs]
#encoded_docs=concatenated_df.loc[:len(concatenated_df),concatenated_df.columns.difference(['Node_Subject','Node_ID'])].values
padded_docs=pad_sequences(encoded_docs, maxlen=max_length, padding='post')

print('\n documents: (length): ', len(docs),'    documents: ',docs)
print('\n encoded documents: (length): ',len(encoded_docs),'    encoded documents: ',encoded_docs)
print('\n pad sequences documents: (length): ',len(padded_docs),'    pad sequences: ',padded_docs)

EMBEDDING_DIM=100

print('\n build model ...')

model=Sequential()
model.add(Embedding(vocab_size,EMBEDDING_DIM, input_length=max_length))
model.add(GRU(units=32,dropout=0.2,recurrent_dropout=0.2))
model.add(Flatten())
model.add(Dense(1,activation='sigmoid'))

########## using different optimizers and different optimizer configurations #######
model.compile(loss='binary_crossentropy',optimizer='adam', metrics=['accuracy'])
print('\n model information: ',model.summary())

print('\n Train ...')
model.fit(padded_docs, labels,batch_size=10, epochs=50, verbose=0)
loss, accuracy=model.evaluate(padded_docs, labels, verbose=0)
print('\n Accuracy: %f' % (accuracy*100))
#model.fit(X_train_pad,y_train, batch_size=128,epochs=25,validation_data=(X_test_pad,y_test),verbose=2)

################################ Word2vec model #####################################
# model = Word2Vec(list_nodes, size=128, window=5, min_count=0, sg=1, workers=2, iter=1)
# 
# # The embedding vectors can be retrieved from model.wv using the node ID as key.
# print('shape of a word vector: ',model.wv["19231"].shape)
# 
#     
# # Retrieve node embeddings and corresponding subjects
# node_ids = model.wv.index2word  # list of node IDs
# node_embeddings = (model.wv.vectors)  
# 
# ii=model.wv.vocab.get("19231").index
# print('index: ',ii, '        word: ', model.wv.index2word[ii])
# #print(node_embeddings[ii])
# print('the queried word found: (length)',len(model.wv.get_vector(model.wv.index2word[ii])))
# 
# # # numpy.ndarray of size number of nodes times embeddings dimensionality
# # node_targets = node_subjects[[int(node_id) for node_id in node_ids]]
# 
# print('node ids ',', length: ',len(node_ids), '    ', node_ids)
# node_embedding_size=model.wv.vector_size
# print('size of node embeddings: ', node_embedding_size, '    ', len(node_embeddings))

# #model.build_vocab(list_nodes)  # prepare the model vocabulary
# model.train(list_nodes, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors
# node_embeddings = model.wv.vectors  # numpy.ndarray of size number of nodes times embeddings dimensionality
# print('node embeddings: ',node_embeddings)
# node_embedding_size=model.wv.vector_size
# print('node embedding size: ', node_embedding_size, '    ', len(node_embeddings))

# for k, word in enumerate(model.wv.index2word):
#         print(k, '    ', word)    
######################################################################################

