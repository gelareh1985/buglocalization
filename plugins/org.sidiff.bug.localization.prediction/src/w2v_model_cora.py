'''
Created on Jan 26, 2021

@author: Gelareh_mp
'''
import io
import itertools
import os
import numpy as np
import re
import string
import tensorflow as tf
import tqdm
from nltk.tokenize import RegexpTokenizer

from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Activation, Dense, Dot, Embedding, Flatten, GlobalAveragePooling1D, Reshape
from tensorflow.keras.layers.experimental.preprocessing import TextVectorization
from keras.layers import dot

import pandas as pd

from stellargraph import datasets
from IPython.display import display, HTML

# ************************************************************************************

class Word2Vec(Model):
    def __init__(self,vocab_size, embedding_dim):
        super(Word2Vec, self).__init__()
        self.target_embedding = Embedding(vocab_size, 
                                        embedding_dim,
                                        input_length=1,
                                        name="w2v_embedding", )
        self.context_embedding = Embedding(vocab_size, 
                                         embedding_dim, 
                                         input_length=num_ns+1)
        self.dots = Dot(axes=(3,2))
        self.flatten = Flatten()
    
    def call(self, pair):
        target, context = pair
        we = self.target_embedding(target)
        ce = self.context_embedding(context)
        dots = self.dots([ce, we])
        return self.flatten(dots)

class LogisticEndpoint(tf.keras.layers.Layer):
    def __init__(self, name=None):
        super(LogisticEndpoint, self).__init__(name=name)
        self.loss_fn = tf.keras.losses.BinaryCrossentropy(from_logits=True)
        self.accuracy_fn = tf.keras.metrics.BinaryAccuracy()

    def call(self, targets, logits, sample_weights=None):
        # Compute the training-time loss value and add it
        # to the layer using `self.add_loss()`.
        loss = self.loss_fn(targets, logits, sample_weights)
        self.add_loss(loss)

        # Log accuracy as a metric and add it
        # to the layer using `self.add_metric()`.
        acc = self.accuracy_fn(targets, logits, sample_weights)
        self.add_metric(acc, name="accuracy")

        # Return the inference-time prediction tensor (for `.predict()`).
        return tf.nn.softmax(logits)

def custom_loss(x_logit, y_true):
    return tf.nn.sigmoid_cross_entropy_with_logits(logits=x_logit, labels=y_true)

# Generates skip-gram pairs with negative sampling for a list of sequences
# (int-encoded sentences) based on window size, number of negative samples
# and vocabulary size.
def generate_training_data(sequences, window_size, num_ns, vocab_size, seed):
    # Elements of each training example are appended to these lists.
    targets, contexts, labels = [], [], []
    
    # Build the sampling table for vocab_size tokens.
    sampling_table = tf.keras.preprocessing.sequence.make_sampling_table(size=vocab_size)
    print('new sampling table (length): ',len(sampling_table),sampling_table)
    # Iterate over all sequences (sentences) in dataset.
    for sequence in tqdm.tqdm(sequences):
    
        # Generate positive skip-gram pairs for a sequence (sentence).
        positive_skip_grams, _ = tf.keras.preprocessing.sequence.skipgrams(
              sequence, 
              vocabulary_size=vocab_size,
              sampling_table=sampling_table,
              window_size=window_size,
              negative_samples=0)
    
        # Iterate over each positive skip-gram pair to produce training examples 
        # with positive context word and negative samples.
        for target_word, context_word in positive_skip_grams:
            context_class = tf.expand_dims(tf.constant([context_word], dtype="int64"), 1)
            negative_sampling_candidates, _, _ = tf.random.log_uniform_candidate_sampler(
              true_classes=context_class,
              num_true=1, 
              num_sampled=num_ns, 
              unique=True, 
              range_max=vocab_size, 
              seed=seed, name="negative_sampling")
    
            # Build context and label vectors (for one target word)
            negative_sampling_candidates = tf.expand_dims(negative_sampling_candidates, 1)
        
            context = tf.concat([context_class, negative_sampling_candidates], 0)
            label = tf.constant([1] + [0]*num_ns, dtype="int64")
        
            # Append each element from the training example to global lists.
            targets.append(target_word)
            contexts.append(context)
            labels.append(label)

    return targets, contexts, labels

# We create a custom standardization function to lowercase the text and 
# remove punctuation.
def custom_standardization(input_data):
    lowercase = tf.strings.lower(input_data)
    return tf.strings.regex_replace(lowercase,
                                  '[%s]' % re.escape(string.punctuation), '')

def vectorize_text(text):
    text = tf.expand_dims(text, -1)
    return tf.squeeze(vectorize_layer(text))
    
# ************************************************************************************
# ************* Initial tests of w2vec: (skip-gram) model using keras ****************
# ************************************************************************************
num_ns = 4
SEED=0
AUTOTUNE=tf.data.experimental.AUTOTUNE

dataset = datasets.Cora()
display(HTML(dataset.description))
G, node_subjects = dataset.load(subject_as_feature=True)
print(G.info())

nodes=G.nodes()
node_feats=G.node_features(nodes)

list_nodes=[]
for node in G.nodes():
    list_nodes.append([str(node)])

df_feats=pd.DataFrame(node_feats,dtype=str)
df_cora=pd.DataFrame(list_nodes,columns=['Node_ID'],dtype=str)
df_cora['Node_Subject']=node_subjects.values

concatenated_df=pd.concat([df_cora,df_feats],axis=1)
print('\n Concatenated Dataframe: ')
display(concatenated_df)

docs=concatenated_df.loc[:len(concatenated_df),'Node_Subject'].values

print(docs.shape, '', type(docs))

text_col=docs.tolist()
list_tokens=[]
for row in text_col:
    tokenizer = RegexpTokenizer('[A-Za-z]+')
    words = tokenizer.tokenize(row.lower())
    for word in words:
        list_tokens.append(word) 
print('words (length): ',len(list_tokens), '\n', list_tokens)

vocab, index = {}, 1 # start indexing from 1
vocab['<pad>'] = 0 # add a padding token 
for token in list_tokens:
    if token not in vocab: 
        vocab[token] = index
        index += 1
vocab_size = len(vocab)
print('vocabulary (word2index): ',vocab_size, '    ',vocab)
    
inverse_vocab = {index: token for token, index in vocab.items()}
print('vocabulary (index2word): ',inverse_vocab)

### Vectorize the sentences based on dictionary indexes
example_sequence = [vocab[word] for word in list_tokens]
print(example_sequence)

### Generate skip-grams from sentences
window_size = 2
positive_skip_grams, _ = tf.keras.preprocessing.sequence.skipgrams(
      example_sequence, 
      vocabulary_size=vocab_size,
      window_size=window_size,
      negative_samples=0)
print(len(positive_skip_grams))

### Few positive skip-grams 
for target, context in positive_skip_grams[:5]:
    print(f"({target}, {context}): ({inverse_vocab[target]}, {inverse_vocab[context]})")
# Get target and context words for one positive skip-gram.
target_word, context_word = positive_skip_grams[0]

# Set the number of negative samples per positive context. 

context_class = tf.reshape(tf.constant(context_word, dtype="int64"), (1, 1))
negative_sampling_candidates, _, _ = tf.random.log_uniform_candidate_sampler(
    true_classes=context_class, # class that should be sampled as 'positive'
    num_true=1, # each positive skip-gram has 1 positive context class
    num_sampled=num_ns, # number of negative context words to sample
    unique=True, # all the negative samples should be unique
    range_max=vocab_size, # pick index of the samples from [0, vocab_size]
    seed=SEED, # seed for reproducibility
    name="negative_sampling" # name of this operation
)
print(negative_sampling_candidates)
print([inverse_vocab[index.numpy()] for index in negative_sampling_candidates])

# Add a dimension so you can use concatenation (on the next step).
negative_sampling_candidates = tf.expand_dims(negative_sampling_candidates, 1)

# Concat positive context word with negative sampled words.
context = tf.concat([context_class, negative_sampling_candidates], 0)

# Label first context word as 1 (positive) followed by num_ns 0s (negative).
label = tf.constant([1] + [0]*num_ns, dtype="int64") 

# Reshape target to shape (1,) and context and label to (num_ns+1,).
target = tf.squeeze(target_word)
context = tf.squeeze(context)
label =  tf.squeeze(label)

print(f"target_index    : {target}")
print(f"target_word     : {inverse_vocab[target_word]}")
print(f"context_indices : {context}")
print(f"context_words   : {[inverse_vocab[c.numpy()] for c in context]}")
print(f"label           : {label}")

print(f"target  :", target)
print(f"context :", context )
print(f"label   :", label )

sampling_table = tf.keras.preprocessing.sequence.make_sampling_table(size=vocab_size)
print('sampling table (length): ',len(sampling_table),'    ',sampling_table)

print('finished initial tests for creating skip-gram model on cora dataset !\n')
# ************************************************************************************
file_path=r'D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest\text_sample.txt'

with open(file_path) as f: 
    lines = f.read().splitlines()
text= []   
for line in lines[:20]:
    print(line)
    text.append(line)
  
text_ds = tf.data.TextLineDataset(file_path).filter(lambda x: tf.cast(tf.strings.length(x), bool))
print('\n',type(text_ds), 'object of text line dataset: ',text_ds)  

list_tokens=[]
for row in text:
    tokenizer = RegexpTokenizer('[A-Za-z]+')
    words = tokenizer.tokenize(row.lower())
    for word in words:
        list_tokens.append(word) 
print('words (length): ',len(list_tokens), '\n', list_tokens)

vocab_new, index = {}, 1 # start indexing from 1
vocab_new['<pad>'] = 0 # add a padding token 
for token in list_tokens:
    if token not in vocab_new: 
        vocab_new[token] = index
        index += 1
vocab_new_size = len(vocab_new)
print('vocabulary (word2index): ',vocab_new_size, ' vocabulary: ', vocab_new)

 
# Define the vocabulary size and number of words in a sequence.
sequence_length = 10
  
# Use the text vectorization layer to normalize, split, and map strings to
# integers. Set output_sequence_length length to pad all samples to same length.
vectorize_layer = TextVectorization(
    standardize=custom_standardization,
    max_tokens=vocab_new_size,
    output_mode='int',
    output_sequence_length=sequence_length)  
 
vectorize_layer.adapt(text_ds.batch(vocab_new_size//2))
# Save the created vocabulary for reference.
inverse_vocab = vectorize_layer.get_vocabulary()
print(inverse_vocab[:20])


# Vectorize the data in text_ds.
text_vector_ds = text_ds.batch(vocab_new_size//2).prefetch(AUTOTUNE).map(vectorize_layer).unbatch()

sequences = list(text_vector_ds.as_numpy_iterator())
print(len(sequences))

for seq in sequences[:5]:
    print(f"{seq} => {[inverse_vocab[i] for i in seq]}")
  
targets, contexts, labels = generate_training_data(
    sequences=sequences, 
    window_size=2, 
    num_ns=4, 
    vocab_size=vocab_new_size, 
    seed=SEED)
print('training data dimensions: ',len(targets), len(contexts), len(labels))
  
BATCH_SIZE = vocab_new_size//2
BUFFER_SIZE = 10000
dataset = tf.data.Dataset.from_tensor_slices(((targets, contexts), labels))
print('dataset from tensor slices: ',dataset)
dataset = dataset.shuffle(BUFFER_SIZE).batch(BATCH_SIZE, drop_remainder=True)
print('dataset shuffle: ',dataset)
  
dataset = dataset.cache().prefetch(buffer_size=BUFFER_SIZE)
print('dataset cached: ',dataset)

# dataset=list(dataset.as_numpy_iterator())
# print(type(dataset), dataset)

i=0
for element in dataset:
    print('element ',str(i),' :',element)
    i=i+1

# embedding_dim = 128
# model=Sequential()
# target_embedding= Embedding(vocab_size, 
#                             embedding_dim,
#                             input_length=1,
#                             name="w2v_embedding", )
# context_embedding= Embedding(vocab_size, 
#                              embedding_dim, 
#                              input_length=num_ns+1)                           
# inputs = tf.keras.Input((55,), name="inputs")
# logits = tf.keras.layers.Dense(1)(inputs)
# targets = tf.keras.Input((1,), name="targets")
# sample_weight = tf.keras.Input((1,), name="sample_weight")
# preds = LogisticEndpoint()(logits, targets, sample_weight)
# model = tf.keras.Model([inputs, targets, sample_weight], preds)
 
# data = {
#     "inputs": np.random.random((55,)),
#     "targets": np.random.random((1, )),
#     "sample_weight": np.random.random((1, )),
# }

# model.compile(optimizer='adam',
#             loss=tf.keras.losses.CategoricalCrossentropy(from_logits=True),
#             metrics=['accuracy'])
#  
# 
# tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir="logs")
# model.fit(dataset, epochs=20,callbacks=[tensorboard_callback]) 


#X,Y=dataset
#print('X: ', Y, '    Y: ', Y)
# print('\n pairs: ',dataset.element_spec)
# i=0
# for element in dataset.element_spec:
#     print('element tensor shape',str(i),' : ',np.shape(element))
#     print('element ',str(i),' : ',element)
#     i=i+1
#inputs=tf.keras.Input(shape=())
# word2vec.compile(optimizer='adam',
#               loss=tf.keras.losses.CategoricalCrossentropy(from_logits=True),
#               metrics=['accuracy'])
# 
# tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir="logs")




#word2vec.fit(dataset, epochs=20)#,callbacks=[tensorboard_callback])

