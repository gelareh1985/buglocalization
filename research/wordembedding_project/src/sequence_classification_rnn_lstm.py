
# LSTM for sequence classification in the IMDB dataset
import numpy as np
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.datasets import imdb
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Embedding, LSTM, SimpleRNN
from tensorflow.keras.preprocessing import sequence
from gensim.models import KeyedVectors
from tensorflow.keras.callbacks import TensorBoard

pretrained_dict_path = "D:/buglocalization_gelareh_home/datasets/GoogleNews-vectors-negative300.bin"

# fix random seed for reproducibility
np.random.seed(7)
# load the dataset but only keep the top n words, zero the rest
top_words = 200
(X_train, y_train), (X_test, y_test) = imdb.load_data(num_words=top_words)
print(X_train[5])
print(y_train[5])
vocab_word_to_index = imdb.get_word_index()

vocab_index_to_word = {}
for key, value in vocab_word_to_index.items():
    vocab_index_to_word[value] = key

t = Tokenizer()
t.word_index
# truncate and pad input sequences
max_review_length = 100
X_train = sequence.pad_sequences(X_train, maxlen=max_review_length)
X_test = sequence.pad_sequences(X_test, maxlen=max_review_length)
# create the model
embedding_vecor_length = 32
model = Sequential()
model.add(Embedding(top_words, embedding_vecor_length, input_length=max_review_length))
model.add(LSTM(50))
model.add(Dense(1, activation='sigmoid'))
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
print(model.summary())
model.fit(X_train, y_train, epochs=1, batch_size=64)
# Final evaluation of the model
scores = model.evaluate(X_test, y_test, verbose=0)
print("Accuracy: %.2f%%" % (scores[1] * 100))

predicted_text = model.predict(X_test)
# predicted_classes = predicted_text.argmax(axis=-1)
# encoded_argmax = predicted_text.argmax(axis=1)

# encoded_argmax = np.argmax(X_test, axis=1)
# output_sequence = sequence.pad_sequences(encoded_argmax[0])
# text = Tokenizer.sequences_to_texts([output_sequence])

encoded_argmax = np.argmax(X_test, axis=1)
predicted_classes = np.argmax(X_test, axis=-1)
encoded_argmax = encoded_argmax.tolist()
predicted_classes = predicted_classes.tolist()
print(predicted_classes)
print(encoded_argmax)

predicted_words = {}
for index in encoded_argmax:
    if index in vocab_index_to_word.keys():
        predicted_words[index] = vocab_index_to_word[index]

print(len(predicted_words))



