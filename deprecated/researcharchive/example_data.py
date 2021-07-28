'''
Created on Jan 13, 2021

@author: Gelareh_mp
'''
import numpy as np
from numpy.lib.function_base import vectorize
import pandas as pd
import matplotlib.pyplot as plt
from nltk.tokenize import WordPunctTokenizer, RegexpTokenizer
from string import punctuation, ascii_lowercase
from gensim.models import Word2Vec
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.layers import Flatten, Dense, Input, LSTM, Embedding, Dropout, SpatialDropout1D, Bidirectional
from tensorflow.keras import Model, Sequential
from tensorflow.keras.optimizers import Adam

def get_text(file_corpus):
    texts = []
    # str_texts = ""
    for row in file_corpus:
        for text in row:
            texts.append(text)
            text += "\n"
            # str_texts += text
    return texts


def text_to_words(docs):
    # Split the documents into tokens.
    tokenizer = RegexpTokenizer(r'\w+')
    for idx in range(len(docs)):
        docs[idx] = docs[idx].lower()  # Convert to lowercase.
        docs[idx] = tokenizer.tokenize(docs[idx])  # Split into words.

    # Remove numbers, but not words that contain numbers.
    docs = [[token for token in doc if not token.isnumeric()] for doc in docs]

    # Remove words that are only one character.
    docs = [[token for token in doc if len(token) > 1] for doc in docs]
    return docs


def get_pad_sequences(corpus, dictionary, max_length):
    new_X_train = []
    for idx in range(len(corpus)):
        for row in corpus[idx]:
            temp_text_sent = []
            for word in row.strip().split():
                try:
                    temp_text_sent.append(dictionary[word])
                except KeyError:
                    pass  # ignore...how to handle unseen words...?

            # Add the padding for each sentence. Here I am padding with 0
            temp_text_sent += [0] * (max_length - len(temp_text_sent))
            new_X_train.append(temp_text_sent)

    encoded_docs = np.asarray(new_X_train)
    #print("padded sequences: ", encoded_docs)
    padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
    print("padded docs: ", padded_docs)
    return padded_docs


def found_pretrained_embeddings(found_embeddings):
    sample_dictionary = {}
    for key, value in found_embeddings.items():
        sample_dictionary[value] = key
    print('sample dictionary: ', sample_dictionary)
    return sample_dictionary
# mylist1=[[('1.0','2.0','3.0','4.0')],[('5.0','6.0','7.0','8.0')],[('9.0','10.0','11.0','12.0')]]
# print('initial list1: ',mylist1)

# arr1=np.array(['1.0','2.0','3.0','4.0'])
# arr2=np.array(['5.0','6.0','7.0','8.0'])
# arr3=np.array(['9.0','10.0','11.0','12.0'])

# mylist2=[[arr1],[arr2],[arr3]]
# print('initial list2: ',mylist2)

# print('tuple of my list1: ',tuple(mylist1))
# print('tuple of my list2: ',tuple(mylist2))

# display(pd.DataFrame(tuple(mylist1)))
# display(pd.DataFrame(tuple(mylist2)))

# arr1=np.asarray(mylist1)
# arr2=np.asarray(mylist2)
# print('shapes: ', arr1.shape , '    ', arr2.shape)
# # display(pd.DataFrame(arr1))
# # display(pd.DataFrame(arr2))

# col_list=['col1','col2','col3','col4']

# print('array element: ',tuple([mylist1[0],mylist1]))
# print('tuples: ', tuple(mylist1[0]),'\n', tuple(mylist1[1]))

# print('tuple union1: ', tuple(mylist1[0])+tuple(mylist1[1]))
# print('tuple union2: ', list(tuple(mylist1[0])+tuple(mylist1[1])))
# display(pd.DataFrame(list(tuple(mylist1[0])+tuple(mylist1[1]))))

# print('\n DataFrame of Tuple lists: ')
# df1_arr=np.array(list(tuple([mylist1[0],mylist1])))

# #arr2=df1_arr.reshape(3,4)
# #df1=pd.DataFrame(arr2)
# #display(df1)
# new_arr=np.array(tuple(mylist2))
# print('array shape: ',new_arr.shape, '  ', new_arr.ndim, '  ', new_arr[0].size, '  ', len(new_arr.flat))
# print(len(new_arr[0]), '    ',len(new_arr[0][0]), '    ', type(new_arr[0]), '    ',new_arr[0].shape)
# print(new_arr[0], '    ',new_arr[0][0])
# new_arr=new_arr.reshape(3,4)
# df2=pd.DataFrame(new_arr)#,columns=col_list)
# display(df2)

# mylist3=list(np.array([ '0.18966675', '-0.03137207', '-0.11621094',
#                        '0.02246094','0.18966675', '-0.03137207',
#                        '-0.11621094',  '0.02246094','0.18966675',
#                         '-0.03137207', '-0.11621094',  '0.02246094']))

# print('initial list3: ',mylist3)
# arr=np.array(mylist3)


# #arr=arr.reshape(3,4)
# arr2=np.vstack([mylist3,np.arange(12)])
# print(arr2.shape)
# df3=pd.DataFrame(arr2)#,columns=col_list)
# display(df3)
# *************************************************************************************************
file_corpus = [['main compile close log file main'], ['fixed'], ['compile'], ['system exit finished'],
               ['statement'], ['get result'], ['compile key compile'], ['print modifiers'], ['computer interface']]
sample_text = [['main bug report comment log sample close'], ['meta information']]
found_embeddings_dict_train = {398: 'close', 20597: 'compile', 2281: 'file', 6408: 'log', 828: 'main', 2649: 'fixed', 4270: 'exit',
                               750: 'finished', 273: 'system', 489: 'statement', 91: 'get', 610: 'result', 562: 'key',
                               141876: 'modifiers', 3472: 'print', 1279: 'computer', 6763: 'interface'}
found_embeddings_dict_test = {398: 'close', 20597: 'compile', 2281: 'file', 6408: 'log', 828: 'main'}


# *************************************************************************************************
# TRAIN_DATA_FILE = file_corpus
# TEST_DATA_FILE = sample_text
# train_df = pd.DataFrame(TRAIN_DATA_FILE, columns=["comment_text"])
# test_df = pd.DataFrame(TEST_DATA_FILE, columns=["comment_text"])
# train_df.head(5)
########################################
# process texts in datasets
########################################
print('Processing text dataset')

# setup tokenizer
tokenizer = WordPunctTokenizer()

sample_dictionary_train = found_pretrained_embeddings(found_embeddings_dict_train)
vocab_train = sample_dictionary_train

sample_dictionary_test = found_pretrained_embeddings(found_embeddings_dict_test)
vocab_test = sample_dictionary_test

texts = get_text(file_corpus)
docs_train = text_to_words(texts)
print("\n\n docs_train: ", docs_train)

texts = get_text(sample_text)
docs_test = text_to_words(texts)
print("\n\n docs_test: ", docs_test)

print("\n The vocabulary contains {} unique tokens".format(len(vocab_train)))
model = Word2Vec(min_count=1, vector_size=100)
model.build_vocab(docs_train)
model.train(docs_train, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors
model.train(docs_test, total_examples=model.corpus_count, epochs=model.epochs)  # train word vectors

word_vectors = model.wv

print(model.wv.most_similar_cosmul(positive=['main', 'log'], negative=['finished']))

MAX_NB_WORDS = len(vocab_train)

list_classes = ["class1", "class2", "class3", "class4", "class1", "class2", "class4", "class2", "class3"]
list_classes = []
list_classes.append(np.array([1, 1, 1, 0, 1, 0, 0, 1, 0]))   
list_classes.append(np.array([1, 0, 0, 0, 1, 0, 1, 0, 0]))   
list_classes.append(np.array([0, 1, 0, 0, 1, 0, 0, 0, 0]))   
list_classes.append(np.array([1, 0, 1, 0, 1, 0, 0, 1, 0]))   
list_classes.append(np.array([1, 0, 1, 0, 0, 0, 1, 1, 0]))   
list_classes.append(np.array([0, 0, 1, 0, 1, 0, 1, 1, 0]))   
list_classes.append(np.array([1, 1, 0, 0, 1, 1, 0, 0, 1]))   
list_classes.append(np.array([1, 0, 0, 0, 1, 0, 1, 1, 0]))   
list_classes.append(np.array([0, 1, 1, 0, 0, 0, 0, 1, 1]))

y = np.asarray(list_classes)

sample_dictionary_train_copy = {}
i = 0
for key, value in sample_dictionary_train.items():
    sample_dictionary_train_copy[key] = i
    i = i + 1
MAX_SEQUENCE_LENGTH = 10
padded_docs_train = get_pad_sequences(file_corpus, sample_dictionary_train_copy, MAX_SEQUENCE_LENGTH)
# data = padded_docs_train
print('Shape of data tensor:', padded_docs_train.shape)
print('Shape of label tensor:', y.shape)

#padded_docs_test = get_pad_sequences(sample_text, sample_dictionary_test, MAX_SEQUENCE_LENGTH)
#print('Shape of test_data tensor:', padded_docs_test.shape)
# ++++++++++++++++++
# WV_DIM = 100
# nb_words = min(MAX_NB_WORDS, len(vocab_train))
# word_index = sample_dictionary_train

# index_word = {}
# for key, value in word_index.items():
#     index_word[value] = key

# # we initialize the matrix with random numbers
# wv_matrix = (np.random.rand(nb_words, WV_DIM + 1) - 0.5) / 5.0
# #x = np.empty(shape=(17,17,100))
# #wv_matrix = np.array(nb_words, WV_DIM)
# wv_matrix_dictionary = dict()
# i = 0
# for word, index in word_index.items():
#     try:
#         embedding_vector = word_vectors[word]
#         # words not found in embedding index will be all-zeros.
#         wv_matrix[i][0] = int(index)
#         wv_matrix[i][1:WV_DIM + 1] = embedding_vector
#         wv_matrix_dictionary.update({index:embedding_vector})
#         wv_matrix[i] = embedding_vector
#     except:
#         pass
#     i += 1
# print("\n word2vec matrix: ", wv_matrix)

# list_w2v = []
# for key, value in wv_matrix_dictionary.items():
#     list_w2v.append([key, value])
# print("\n\n",list_w2v)
# print("\n\n",np.asarray(list_w2v))
# ++++++++++++++++++
word_index = sample_dictionary_train
index_word = {}
for key, value in word_index.items():
    index_word[value] = key

WV_DIM = 100
nb_words = min(MAX_NB_WORDS, len(index_word))
# we initialize the matrix with random numbers
wv_matrix = (np.random.rand(nb_words, WV_DIM) - 0.5) / 5.0
for word, i in sample_dictionary_train_copy.items():
    if i >= MAX_NB_WORDS:
        continue
    try:
        embedding_vector = word_vectors[word]
        # words not found in embedding index will be all-zeros.
        wv_matrix[i] = embedding_vector
    except:
        pass 
#data = list(wv_matrix.items())
# wv_array = np.array(data)
#wv_matrix = np.asarray(list_w2v)
wv_layer = Embedding(len(sample_dictionary_train_copy),
                     WV_DIM,
                     weights=[wv_matrix],
                     input_length=MAX_SEQUENCE_LENGTH,
                     trainable=False)

# # Inputs
# data_input = Input(shape=(MAX_SEQUENCE_LENGTH,), dtype='int32')
# embedded_sequences = wv_layer(data_input)

# # biGRU
# embedded_sequences = SpatialDropout1D(0.2)(embedded_sequences)
# x = Bidirectional(LSTM(64, return_sequences=False))(embedded_sequences)

# # Output
# x = Dropout(0.2)(x)
# x = BatchNormalization()(x)
# preds = Dense(9, activation='sigmoid')(x)

# # build the model
# model = Model(inputs=[data_input], outputs=preds)
# model.compile(loss='binary_crossentropy',
#               optimizer=Adam(lr=0.001, clipnorm=.25, beta_1=0.7, beta_2=0.99),
#               metrics=[])

# model.fit([data], y, validation_split=0.1, epochs=10, batch_size=256, shuffle=True)

model = Sequential()
model.add(wv_layer)  # your embedding layer
# mymodel.add(Bidirectional(
#     LSTM(64, dropout=0.5, recurrent_dropout=0.4, return_sequences=True))
# )
model.add(Flatten())
model.add(Dense(1, activation='sigmoid'))

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
print(model.summary())

labels = np.array([1,1,1,1,1,0,0,0,0])
print(padded_docs_train)
model.fit(padded_docs_train, labels, epochs=50, verbose=0)

# embeddings = model.layers[0].get_weights()[0]
# print("get embeddings from model: ", embeddings)

# i = 0
# for embedding in embeddings:
#     index_float = embedding[:1].tolist()
#     index_integer = index_float[0]
#     index_integer = int(index_integer)
#     if index_integer in word_index.values():

#         print("embedding (index_pretrained dictionary): ",index_integer, " local w2v_matrix index: ",i, " for word: ", index_word[index_integer])
#         print("embedding vector: ",embedding[i + 1:101])
#     i += 1
#words_embeddings = {w:embeddings[idx] for w, idx in word_index.items()}

#print(words_embeddings['system'])



