from numpy import array
from numpy import asarray
from numpy import zeros
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Embedding
from tensorflow.keras.callbacks import TensorBoard
# 
pretrained_dict_path = "D:/buglocalization_gelareh_home/glove.6B.100d.txt"
# define documents
# docs = ['Well done!',
#         'Good work',
#         'Great effort',
#         'nice work',
#         'Excellent!',
#         'Weak',
#         'Poor effort!',
#         'not good',
#         'poor work',
#         'Could have done better.']

file_corpus = ['main compile close log file main', 
               'fixed',
               'compile',
               'system exit finished',
               'statement',
               'get result',
               'compile key compile',
               'print modifiers',
               'computer interface']

docs = file_corpus            
# define class labels
labels = array([1, 1, 1, 1, 1, 0, 0, 0, 0])
# prepare tokenizer
t = Tokenizer()
t.fit_on_texts(docs)
vocab_size = len(t.word_index) + 1
# word2index dictionary on tokenized input text: 
word_index = t.word_index
index_word = t.index_word
print("\n word2index dictionary: ",word_index)
print("\n index2word dictionary: ",index_word)
# integer encode the documents
encoded_docs = t.texts_to_sequences(docs)
print("\n encoded docs: ",encoded_docs)
# pad documents to a max length of 10 words
max_length = 10
padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
print("\n padded docs",padded_docs)
# load the whole embedding into memory
embeddings_index = dict()
f = open(pretrained_dict_path,encoding="utf8")
for line in f:
    values = line.split()
    word = values[0]
    coefs = asarray(values[1:], dtype='float32')
    embeddings_index[word] = coefs
f.close()
print('\n Loaded %s word vectors.' % len(embeddings_index))
# create a weight matrix for words in training docs
embedding_matrix = zeros((vocab_size, 100))
for word, i in t.word_index.items():
    embedding_vector = embeddings_index.get(word)
    if embedding_vector is not None:
        embedding_matrix[i] = embedding_vector
# define model
model = Sequential()
e = Embedding(vocab_size, 100, weights=[embedding_matrix], input_length=10, trainable=False)
model.add(e)
model.add(Flatten())
model.add(Dense(1, activation='sigmoid'))
# compile the model
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
# summarize the model
print("\n",model.summary())
# fit the model
tensorboard_callback = TensorBoard(log_dir="logs")
model.fit(padded_docs, labels, epochs=50, verbose=2, callbacks=[tensorboard_callback])
# evaluate the model
loss, accuracy = model.evaluate(padded_docs, labels, verbose=2)

print('\n Accuracy: %f' % (accuracy * 100))

embeddings = model.layers[0].get_weights()[0]
print("\n\n get embeddings from model: ", embeddings)

words_embeddings_w2index = {w:embeddings[idx] for w, idx in word_index.items()}

given_word = 'system'
print("\n\n word embedding of the given word: ",words_embeddings_w2index[given_word])
print("\n\n related index of the word embedding of the given word: ",word_index[given_word])

#words_embeddings_index2word = {idx:embeddings[w] for w, idx in index_word.items()}
given_index = 10
words_embeddings_index2word = {}
for idx, w in index_word.items():
    words_embeddings_index2word.update({idx:words_embeddings_w2index[w]})
print("\n\n word embedding of the given index: ",words_embeddings_index2word[given_index])
print("\n\n related word of the word embedding of the given index: ",index_word[given_index])

