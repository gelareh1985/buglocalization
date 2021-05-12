import tensorflow as tf
import numpy as np
from tensorflow.keras.preprocessing.text import one_hot
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dense, Flatten
from tensorflow.keras.layers import Embedding

print("**********************************************************************************")
nodelist_path = "buglocalization/research/wordembedding_project/00001_bug_12000_version_2a9b25d23714b865b9b9713bbe18b653db291769.nodelist"
fname = "00001_bug_12000_version_2a9b25d23714b865b9b9713bbe18b653db291769.nodelist"

# define documents
docs = ['Well done!',
        'Good work',
        'Great effort',
        'nice work',
        'Excellent!',
        'Weak',
        'Poor effort!',
        'not good',
        'poor work',
        'Could have done better.']
# define class labels
labels = np.array([1, 1, 1, 1, 1, 0, 0, 0, 0, 0])


# integer encode the documents
vocab_size = 50
encoded_docs = [one_hot(d, vocab_size) for d in docs]
print(encoded_docs)

# integer encode the documents
vocab_size = 50
encoded_docs = [one_hot(d, vocab_size) for d in docs]
print(encoded_docs)


# pad documents to a max length of 4 words
max_length = 4
padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
print(padded_docs)

# pad documents to a max length of 4 words
max_length = 4
padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
print(padded_docs)

# define the model
model = Sequential()
model.add(Embedding(vocab_size, 8, input_length=max_length))
model.add(Flatten())
model.add(Dense(1, activation='sigmoid'))
# compile the model
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
# summarize the model
print(model.summary())
for layer in model.layers:
    print("input shape: ", layer.input_shape)
    print("output shape: ", layer.output_shape)

tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir="logs")
# fit the model
model.fit(padded_docs, labels, epochs=50, verbose=2,callbacks=[tensorboard_callback])

# evaluate the model
loss, accuracy = model.evaluate(padded_docs, labels, verbose=2)
print('Accuracy: %f' % (accuracy * 100))


# x = np.arange(12).reshape(1, 1, 6, 2)
# print(x)

# y = np.arange(12).reshape(1, 1, 2, 6)
# print(y)
# print(tf.keras.layers.Dot(axes=(3, 2))([x, y]))

