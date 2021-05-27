from numpy import array, asarray, argmax, zeros
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Embedding, Flatten, LSTM, Conv1D, MaxPooling1D
from tensorflow.keras.callbacks import TensorBoard
from gensim.models import KeyedVectors
from tensorflow.keras import backend as K

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

def get_padded_sequences(t,max_length,docs):
    t.fit_on_texts(docs)
    vocab_size = len(t.word_index) + 1
    # word2index dictionary on tokenized input text:
    word_index = t.word_index
    index_word = t.index_word
    # integer encode the documents
    encoded_docs = t.texts_to_sequences(docs)
    # pad documents to a max length of 10 words
    padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
    return vocab_size, word_index, index_word, encoded_docs, padded_docs

if __name__ == '__main__':
    # for multi-class classification, to encode labels like: (class1: 1), (class2:2), (class3: 3), ...
    # define class labels (two-classes)
    # multi-class (four-classes)
    #labels = array(['1', '4', '3', '1', '2', '3', '4', '2', '1'])
    # for infering trained data and retrieving text by word and index:
    # (1) get each layer needed separately --> it's already done
    # (2) looping through layers of model and save to list --> it's already done 
    # (3) using keras backend library --> it's already done

    # Different DNN models used:
    """ model 1:
    model = Sequential()
    e = Embedding(vocab_size, 300, weights=[embedding_matrix], input_length=10, trainable=False)
    model.add(e)
    model.add(Flatten())
    model.add(Dense(1, activation='sigmoid'))
    """
    # +++++++++++++++++++++++++++++++
    """ model 2:   
    model = Sequential()
    e = Embedding(vocab_size, 300, weights=[embedding_matrix], input_length=10, trainable=False)
    model.add(e)
    model.add(LSTM(100)) # model.add(LSTM(100, dropout=0.2, recurrent_dropout=0.2))
    model.add(Dense(1, activation='sigmoid'))
    """
    # +++++++++++++++++++++++++++++++
    """ model 3: using Convolutional and LSTM layers
    model = Sequential()
    e = Embedding(vocab_size, 300, weights=[embedding_matrix], input_length=10, trainable=False)
    model.add(e)
    model.add(Conv1D(filters=32, kernel_size=3, padding='same', activation='relu'))
    model.add(MaxPooling1D(pool_size=2))
    model.add(LSTM(100)) # model.add(LSTM(100, dropout=0.2, recurrent_dropout=0.2))
    model.add(Dense(1, activation='sigmoid'))
    """ 
    # +++++++++++++++++++++++++++++++
    file_corpus_train = ['main compile close log file main',
                   'fixed',
                   'compile',
                   'system exit finished',
                   'statement',
                   'get result',
                   'compile key compile',
                   'print modifiers',
                   'computer interface']

    file_corpus_test = ['main bug report comment log sample close', 'meta information']
    docs_train = file_corpus_train
    docs_test = file_corpus_test
    
    """ prepare train and test data labels """
    labels_train = array([1, 1, 1, 1, 1, 0, 0, 0, 0])
    labels_test = array([1, 0])

    max_length = 10

    """ prepare tokenizer """
    t = Tokenizer()
    vocab_size_train, word_index_train, index_word_train, encoded_docs_train, padded_docs_train = get_padded_sequences(t,max_length,docs_train)
    t = Tokenizer()
    vocab_size_test, word_index_test, index_word_test, encoded_docs_test, padded_docs_test = get_padded_sequences(t,max_length,docs_test)
   
    print("\n word2index dictionary (train data): ", word_index_train)
    print("\n index2word dictionary (train data): ", index_word_train)  
    print("\n encoded docs (train data): ", encoded_docs_train) 
    print("\n padded docs (train data): ", padded_docs_train)
    print("\n word2index dictionary (test data): ", word_index_test)
    print("\n index2word dictionary (test data): ", index_word_test)  
    print("\n encoded docs (test data): ", encoded_docs_test) 
    print("\n padded docs (test data): ", padded_docs_test)
    """ load the whole embedding into memory """ 
    model = KeyedVectors.load_word2vec_format(pretrained_dict_path, binary=True)

    embedding_matrix = zeros((vocab_size_train, 300))
    embeddings_train, found_embeddings_dict_train = get_embedding_vectors(dictionary=word_index_train, model=model, embedding_matrix=embedding_matrix)
    print("\n\n found input words: ", len(found_embeddings_dict_train), "   ,   ", found_embeddings_dict_train)
    print("\n found input embeddings: ", len(embeddings_train))

    """ prepare train and test data """
    X_train = padded_docs_train
    y_train = labels_train
    X_test = padded_docs_test
    y_test = labels_test

    """  preparing keras first embedding layer of the model (word2vec: local/pretrained) """
    """ if using all vectors from pretrained dictionary """ 
    # weights = model.vectors
    # print("weights shape: ", weights.shape)

    index_to_key_dict = model.index_to_key
    key_to_index_dict = model.key_to_index
    
    """ if using just limited vectors found by keys and values from pretrained dictionary """
    embeddings_arr = asarray(embeddings_train)
    weights = embeddings_arr

    embedding_matrix = weights
    model = Sequential()
    e = Embedding(vocab_size_train, 300, weights=[embedding_matrix], input_length=10, trainable=False)
    model.add(e)
    model.add(Conv1D(filters=32, kernel_size=3, padding='same', activation='relu'))
    model.add(MaxPooling1D(pool_size=2))
    model.add(LSTM(100, dropout=0.2, recurrent_dropout=0.2))
    #model.add(Flatten())
    #model.add(LSTM(100))
    model.add(Dense(1, activation='sigmoid'))
    # compile the model
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    # summarize the model
    print("\n", model.summary())

    # fit the model
    tensorboard_callback = TensorBoard(log_dir="logs")
    model.fit(X_train, y_train, epochs=50, verbose=2, callbacks=[tensorboard_callback])

    # evaluate the model
    loss, accuracy = model.evaluate(X_test, y_test, verbose=2)
    print('\n Accuracy: %f' % (accuracy * 100))
    
    """ Inferring words and indexes from predicted test texts """
    # (1) get each layer needed separately --> it's already done
    embeddings_layer1 = model.layers[0].get_weights()[0]
    print("\n\n get embedding layer1 from model: ", embeddings_layer1)

    words_embeddings_w2index = {w: embeddings_layer1[idx] for w, idx in word_index_train.items()}

    given_word = 'system'
    print("\n\n word embedding of the given word: ", words_embeddings_w2index[given_word])
    print("\n\n related index of the word embedding of the given word: ", word_index_train[given_word])

    #words_embeddings_index2word = {idx:embeddings[w] for w, idx in index_word.items()}
    given_index = 10
    words_embeddings_index2word = {}
    for idx, w in index_word_train.items():
        words_embeddings_index2word.update({idx: words_embeddings_w2index[w]})
    print("\n\n word embedding of the given index: ", words_embeddings_index2word[given_index])
    print("\n\n related word of the word embedding of the given index: ", index_word_train[given_index])

    # (2) looping through layers of model and save to list --> it's already done 
    outputs = []
    for layer in model.layers:
        keras_function = K.function([model.input], [layer.output])
        outputs.append(keras_function([padded_docs_train, labels_train]))
    # print(outputs)
    # predict model with test data:

    predicted_text = model.predict(padded_docs_test, verbose=0)
    predicted_label = labels_test[argmax(predicted_text)]
    predicted_text_retrieved = docs_test[predicted_label]
    print(outputs[2])
    print(predicted_text_retrieved)

    #y_classes = predicted_text.argmax(axis=-1)
    #encoded_docs_test = array(encoded_docs_test)
    #yhat = model.predict_classes(padded_docs_test)
    for sentence in docs_test:
        for word in sentence.strip().split():
            if word in word_index_train.keys():
                print("related index of the word embedding of the given word: ", word_index_train[word])
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
