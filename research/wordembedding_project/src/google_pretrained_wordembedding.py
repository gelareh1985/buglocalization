from numpy import array
from numpy import asarray
from numpy import argmax
from numpy import zeros
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Embedding
from tensorflow.keras.callbacks import TensorBoard
from gensim.models import KeyedVectors
from tensorflow.keras import backend as K
# from tensorflow.keras import np_utils.probas_to_classes

pretrained_dict_path = "D:/buglocalization_gelareh_home/GoogleNews-vectors-negative300.bin"


def get_embedding_vectors(dictionary, model, embedding_matrix):
    embeddings = []

    found_embeddings_from_pretrained_model = {}
    for word, i in dictionary.items():
        if word in model.index_to_key:
            if model[word] is not None:
                embeddings.append(model[word])
                key = model.key_to_index[word]
                embedding_matrix[i] = model[word]
                found_embeddings_from_pretrained_model[key] = word

    return embedding_matrix, found_embeddings_from_pretrained_model


if __name__ == '__main__':
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
    #labels = array(['class1', 'class1', 'class1', 'class1', 'class1', 'class2', 'class2', 'class2', 'class2'])
    # prepare tokenizer
    t = Tokenizer()
    t.fit_on_texts(docs)
    vocab_size = len(t.word_index) + 1
    # word2index dictionary on tokenized input text:
    word_index = t.word_index
    index_word = t.index_word
    print("\n word2index dictionary: ", word_index)
    print("\n index2word dictionary: ", index_word)
    # integer encode the documents
    encoded_docs = t.texts_to_sequences(docs)
    print("\n encoded docs: ", encoded_docs)
    # pad documents to a max length of 10 words
    max_length = 10
    padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
    print("\n padded docs: ", padded_docs)
    # load the whole embedding into memory
    model = KeyedVectors.load_word2vec_format(pretrained_dict_path, binary=True)
    # embeddings_index = dict()
    # for line in f:
    #     values = line.split()
    #     word = values[0]
    #     coefs = asarray(values[1:], dtype='float32')
    #     embeddings_index[word] = coefs
    # f.close()
    # print('Loaded %s word vectors.' % len(embeddings_index))
    embedding_matrix = zeros((vocab_size, 300))
    embeddings_train, found_embeddings_dict_train = get_embedding_vectors(dictionary=word_index, model=model, embedding_matrix=embedding_matrix)
    print("\n\n found input words: ", len(found_embeddings_dict_train), "   ,   ", found_embeddings_dict_train)
    print("\n found input embeddings: ", len(embeddings_train))

    #weights = model.vectors
    embeddings_arr = asarray(embeddings_train)
    weights = embeddings_arr
    print("weights shape: ", weights.shape)
    index_to_key_dict = model.index_to_key
    key_to_index_dict = model.key_to_index

    # print("\n word2vec matrix: ", weights)
    # wv_layer = Embedding(input_dim=weights.shape[0],
    #                     output_dim=weights.shape[1],
    #                     weights=[weights],
    #                     input_length=MAX_SEQUENCE_LENGTH,
    #                     trainable=False)
    embedding_matrix = weights
    model = Sequential()
    e = Embedding(vocab_size, 300, weights=[embedding_matrix], input_length=10, trainable=False)
    model.add(e)
    model.add(Flatten())
    model.add(Dense(1, activation='sigmoid'))
    # compile the model
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    # summarize the model
    print("\n", model.summary())
    # fit the model
    tensorboard_callback = TensorBoard(log_dir="logs")
    model.fit(padded_docs, labels, epochs=50, verbose=2, callbacks=[tensorboard_callback])
    # evaluate the model
    loss, accuracy = model.evaluate(padded_docs, labels, verbose=2)

    print('\n Accuracy: %f' % (accuracy * 100))

    embeddings_layer1 = model.layers[0].get_weights()[0]
    print("\n\n get embedding layer1 from model: ", embeddings_layer1)

    words_embeddings_w2index = {w: embeddings_layer1[idx] for w, idx in word_index.items()}

    given_word = 'system'
    print("\n\n word embedding of the given word: ", words_embeddings_w2index[given_word])
    print("\n\n related index of the word embedding of the given word: ", word_index[given_word])

    #words_embeddings_index2word = {idx:embeddings[w] for w, idx in index_word.items()}
    given_index = 10
    words_embeddings_index2word = {}
    for idx, w in index_word.items():
        words_embeddings_index2word.update({idx: words_embeddings_w2index[w]})
    print("\n\n word embedding of the given index: ", words_embeddings_index2word[given_index])
    print("\n\n related word of the word embedding of the given index: ", index_word[given_index])

    # inp = model.input                                           # input placeholder
    # outputs = [layer.output for layer in model.layers]          # all layer outputs
    # functor = K.function([inp, K.learning_phase()], outputs )   # evaluation function

    # embedding_matrix = zeros((vocab_size, 300))
    # # Testing
    # #test = np.random.random(input_shape)[np.newaxis,...]
    # layer_outs = functor([test, 1.])
    # print(layer_outs)

    outputs = []
    for layer in model.layers:
        keras_function = K.function([model.input], [layer.output])
        outputs.append(keras_function([padded_docs, labels]))
    # print(outputs)
    # predict model with test data:
    sample_text = ['main bug report comment log sample close', 'meta information']
    docs_test = sample_text
    # define class labels
    labels_test = array([1, 0])
    #labels = array(['class1', 'class2'])
    # prepare tokenizer
    t_test = Tokenizer()
    t_test.fit_on_texts(docs_test)
    vocab_size_test = len(t_test.word_index) + 1
    # word2index dictionary on tokenized input text:
    word_index_test = t_test.word_index
    index_word_test = t_test.index_word
    print("\n word2index dictionary: ", word_index_test)
    print("\n index2word dictionary: ", index_word_test)
    # integer encode the documents
    encoded_docs_test = t_test.texts_to_sequences(docs_test)
    print("\n encoded docs: ", encoded_docs_test)
    # pad documents to a max length of 10 words

    max_length = 10
    padded_docs_test = pad_sequences(encoded_docs_test, maxlen=max_length, padding='post')
    print("\n padded docs: ", padded_docs_test)
    # evaluate the model
    loss_test, accuracy_test = model.evaluate(padded_docs_test, labels_test, verbose=2)

    print('\n Accuracy: %f' % (accuracy_test * 100))
    predicted_text = model.predict(padded_docs_test, verbose=0)
    predicted_label = labels[argmax(predicted_text)]
    predicted_text_retrieved = sample_text[predicted_label]
    print(outputs[2])
    print(predicted_text_retrieved)

    #y_classes = predicted_text.argmax(axis=-1)
    #encoded_docs_test = array(encoded_docs_test)
    #yhat = model.predict_classes(padded_docs_test)
    for sentence in sample_text:
        for word in sentence.strip().split():
            if word in word_index.keys():
                print("related index of the word embedding of the given word: ", word_index[word])
            else:
                print('the word ', word, 'not exist')
    # for word, index in t_test.word_index.items():
    #     if index == yhat:
    #         print(word)
    #y_classess = tensorflow.keras.np_utils.probas_to_classes(y_proba)
    #encoded_argmax = argmax(encoded_docs)

    # t.sequences_to_texts(encoded_docs)
    # text = t.sequences_to_texts(y_classes)
    # print("predicted text: ",text)
