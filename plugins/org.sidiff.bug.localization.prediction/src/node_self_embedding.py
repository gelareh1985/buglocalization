import sys
import logging
import logging.config

from buglocalization.textembedding.word_to_vector_dictionary import WordToVectorDictionary
import numpy as np
import pickle
import nltk
import re
# from gensim.models import KeyedVectors
from nltk.tokenize import word_tokenize
from nltk.tokenize import RegexpTokenizer
from nltk.corpus import brown
# from nltk.stem.wordnet import WordNetLemmatizer
# from gensim.models import Phrases

# import smart_open
from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Dense, Dot, Input, Embedding, Flatten, SpatialDropout1D, LSTM
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.preprocessing.text import one_hot
from tensorflow.keras.callbacks import TensorBoard
from tensorflow.keras import backend as K
from tensorflow.keras import optimizers

from py2neo import Graph
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration


# tokenizer = RegexpTokenizer('[A-Za-z]+')
# nltk.download('wordnet')
# nltk.download('punkt')
# nltk.download('averaged_perceptron_tagger')
# nltk.download('brown')
# nltk.download('universal_tagset')
fpath = "D:/buglocalization_gelareh_home/saved files/"


def tokenize_corpus(text):
    words_array = []
    tokenizer = RegexpTokenizer('[A-Za-z]+[A-Za-z]')
    for row in text:
        words = tokenizer.tokenize(row)
        words_array.append(words)
    return words_array


def process_text(text):
    words_array = []
    tokenizer = RegexpTokenizer('[A-Za-z]+[A-Za-z]')

    for row in text:
        if not row.isdecimal():
            words = tokenizer.tokenize(row)
        row_words = []
        for word in words:
            splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
            for split_word in splitted:
                # split_word = lemmatizer.lemmatize(split_word.strip().lower())
                split_word = split_word.strip().lower()

                # if len(split_word) > 1 and split_word not in stopwords:
                if len(split_word) > 1:
                    if split_word not in row_words:
                        row_words.append(split_word)
                    print(row_words)
        words_array.append(row_words)
    return words_array


def save_file(file_name, sample_list):
    open_file = open(file_name, "wb")
    pickle.dump(sample_list, open_file)
    open_file.close()


def load_file(file_name):
    open_file = open(file_name, "rb")
    loaded_list = pickle.load(open_file)
    open_file.close()
    return loaded_list


def put_embeddings_together(embeddings):
    all_embeddings = []
    for embedding_list in embeddings:
        for embedding in embedding_list:
            all_embeddings.append(embedding)
    return all_embeddings


def get_sum_of_word_embeddings(sum_vectors):
    vectors = []
    for vect in sum_vectors:
        if vect.size > 1:
            vectors.append(vect)
        else:
            pass
    return vectors

#def get_pos_tagged_corpus(dictionary):


def find_similar_words(text_corpus, model):
    all_similar_word_vectors = {}
    i = 0

    for row in text_corpus:

        similar_words = {}
        similar_word_vectors = []
        j = 0
        count = 0
        for word in row:
            # for word in row.strip().split():
            if word in model.index_to_key:
                if model[word] is not None:
                    # embeddings.append(model[word])
                    #key = model.key_to_index[word]
                    similar_words = model.most_similar(word)
                    similar_words_list = []
                    for pair in similar_words:
                        word_tagged_pos = nltk.pos_tag([pair[0]])
                        similar_words_list.append([pair[0], word_tagged_pos, pair[1]])
                    word_pos = nltk.pos_tag([word])
                    similar_words[j] = (word_pos, similar_words_list)
                    print('similar words in row ', i + 1, ' to word ', count + 1, ' (', word, ') found')
                    similar_word_vectors.append(similar_words[j])
            else:
                word_pos = nltk.pos_tag([word])
                similar_words[j] = (word_pos,'not_found')
                similar_word_vectors.append(similar_words[j])
                print('in row ', i + 1, ' word ', count + 1, ' (', word, ') not found')
            count = count + 1
        # calling **Logger** function
        # file_name = 'my_run_logs'
        # log_obj = Logger(file_name)
        # log_obj.critical("CRIC".format())
        # log_obj.error("ERR".format())
        # log_obj.warning("WARN".format())
        # log_obj.debug("debug".format())
        # log_obj.info("info".format())
               
        # closing file
        #log_obj.handlers.clear()
        all_similar_word_vectors[i] = similar_word_vectors
        i = i + 1
        j = j + 1
    return all_similar_word_vectors


def Logger(file_name):
    formatter = logging.Formatter(fmt='%(asctime)s %(module)s,line: %(lineno)d %(levelname)8s | %(message)s',
                                  datefmt='%Y/%m/%d %H:%M:%S')  # %I:%M:%S %p AM|PM format
    logging.basicConfig(filename='%s.log' % (file_name), format='%(asctime)s %(module)s,line: %(lineno)d %(levelname)8s | %(message)s',
                        datefmt='%Y/%m/%d %H:%M:%S', filemode='w', level=logging.INFO)
    log_obj = logging.getLogger()
    log_obj.setLevel(logging.DEBUG)
    # log_obj = logging.getLogger().addHandler(logging.StreamHandler())

    # console printer
    screen_handler = logging.StreamHandler(stream=sys.stdout)  # stream=sys.stdout is similar to normal print
    screen_handler.setFormatter(formatter)
    logging.getLogger().addHandler(screen_handler)

    log_obj.info("Logger object created successfully..")
    return log_obj


def get_embedding_vectors(input_matrix, model, embedding_matrix):

    i = 0
    for corpus_row in input_matrix.values():
        
        for row in corpus_row:
            if row[1] != 'not_found':
                similarwordsvector = []
                for similar_word_tuples in row[1]:
                    word = similar_word_tuples[0]
                    similarwordsvector.append(model[word])
                line_vect_Sum = sum_word_vectors(similarwordsvector)
                embedding_matrix[i] = line_vect_Sum
            else:
                print(row[0],row[1])    
        i = i + 1
    return embedding_matrix


def sum_word_vectors(vectors):
    # line_vect = []
    # for word in words:
    #     vect = model[word]
    #     line_vect.append(vect)
    # line_vect_sum = np.sum(line_vect, axis=0)
    line_vect_sum = np.sum(vectors, axis=0)
    if line_vect_sum.size > 1:
        return line_vect_sum


def get_part_of_speech(similars):
    tagged_pos = nltk.pos_tag(similars)
    return tagged_pos


def get_padded_sequences(t, max_length, docs):
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


def count_vectors(corpus_vectors):
    counter = 0
    for vect in corpus_vectors:
        if vect.size > 1:
            counter += 1
        else:
            pass
    return counter


if __name__ == '__main__':

    # # Database connection:
    # neo4j_configuration = Neo4jConfiguration(
    #     neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    #     neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    #     neo4j_user="neo4j",
    #     neo4j_password="password",
    # )

    # metamodel = MetaModelUML(neo4j_configuration=neo4j_configuration)
    # file_corpus = []
    # for metatype, properties in metamodel.get_type_to_properties().items():
    #     # print(type, properties)
    #     cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
    #         metatype=metatype
    #     )
    #     metamodel_nodes = metamodel.get_graph().run(cypher=cypher_str_command).to_table()
    #     for node in metamodel_nodes:
    #         # node comments (long text)
    #         # node (short text)
    #         for property in properties:
    #             nodex = node[0][property]
    #             if nodex is not None\
    #                     and type(nodex) != bool\
    #                     and type(nodex) != int\
    #                     and type(nodex) != float:
    #                 print(nodex)
    #                 if nodex not in file_corpus:
    #                     file_corpus.append(nodex)  # split to words - each word to vector - sowe

    word_to_vector_dictionary = WordToVectorDictionary()
    word_dictionary, dimension = word_to_vector_dictionary.dictionary()
    # print(len(word_dictionary), "    ", word_dictionary["compile"])

# ***********************************************************************************
    # Access vectors for specific words with a keyed lookup:
    # file_corpus = [['main compile close log file main'], ['fixed'], ['compile'], ['system exit finished'],
    #                ['return statement'], ['get result'], ['compile key compile'], ['print modifiers']]

    file_corpus_train = ['main compile close log file main',
                         'fixed',
                         '',
                         'compile',
                         'system exit finished',
                         'statement',
                         'get result',
                         'compile key compile',
                         'print modifiers',
                         'computer interface']

    file_corpus_test = ['main bug report comment log sample close', 'meta information']
# ***********************************************************************************
    print("\n\n")
    model = word_dictionary

    # brown_news_tagged = brown.tagged_words(categories='news', tagset='universal')
    # tag_fd = nltk.FreqDist(tag for (word, tag) in brown_news_tagged)
    # print(tag_fd.most_common())

# ***********************************************************************************
    #docs_train = file_corpus_train
    #docs_train = file_corpus
    docs_test = file_corpus_test

    # docs_train = process_text(docs_train)
    filename = fpath + '/file_corpus_train.pkl'
    # save_file(filename,docs_train)
    docs_train = load_file(filename)
    print(docs_train)
    
    """ prepare train and test data labels """
    #labels_train = np.array([1, 1, 1, 1, 1, 0, 0, 0, 0])
    labels_train = np.random.randint(2, size=len(docs_train))
    labels_test = np.array([1, 0])

    """ prepare tokenizer """
    max_length = 200
    t = Tokenizer()
    vocab_size_train, word_index_train, index_word_train, encoded_docs_train, padded_docs_train = get_padded_sequences(t, max_length, docs_train)
    #t.fit_on_texts(docs_train)
    #vocab_size_train = len(t.word_index) + 1
    
    filename = fpath + 'encoded_docs_train.pkl'
    #save_file(filename,encoded_docs_train)
    encoded_docs_train = load_file(filename)

    filename = fpath + 'padded_docs_train.pkl'
    #save_file(filename,padded_docs_train)
    padded_docs_train = load_file(filename)

    filename = fpath + 'word_index_train.pkl'
    #save_file(filename,word_index_train)
    word_index_train = load_file(filename)

    filename = fpath + 'index_word_train.pkl'
    #save_file(filename,index_word_train)
    index_word_train = load_file(filename)

    t = Tokenizer()
    vocab_size_test, word_index_test, index_word_test, encoded_docs_test, padded_docs_test = get_padded_sequences(t, max_length, docs_test)

    print("\n word2index dictionary (train data): ", word_index_train)
    print("\n index2word dictionary (train data): ", index_word_train)
    print("\n encoded docs (train data): ", encoded_docs_train)
    print("\n padded docs (train data): ", padded_docs_train)
    print("\n word2index dictionary (test data): ", word_index_test)
    print("\n index2word dictionary (test data): ", index_word_test)
    print("\n encoded docs (test data): ", encoded_docs_test)
    print("\n padded docs (test data): ", padded_docs_test)
    """ load the whole embedding into memory """
    
    print("document row 83509: ",docs_train[83509])
    print("document row 83510: ",docs_train[83510])
    print("document row 83511: ",docs_train[83511])

    #fcorpus = tokenize_corpus(text=file_corpus_train)
    #similar_words_matrix = find_similar_words(text_corpus=fcorpus, model=model)
    #similar_words_matrix = find_similar_words(text_corpus=docs_train, model=model)
    filename = fpath + 'similar_words_matrix.pkl'
    #save_file(filename, similar_words_matrix)
    
    similar_words_matrix = load_file(filename)
    #print(similar_words_matrix)
    vocab_size_train = len(similar_words_matrix) + 1
    embedding_matrix = np.zeros((vocab_size_train, 300))
    embeddings_train = get_embedding_vectors(input_matrix=similar_words_matrix, model=model, embedding_matrix=embedding_matrix)
    #print("\n\n found input words: ", len(found_embeddings_dict_train), "   ,   ", found_embeddings_dict_train)
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
    embeddings_arr = np.asarray(embeddings_train)
    weights = embeddings_arr

    embedding_matrix = weights
    # embeddings_arr = np.asarray(all_embeddings)
    # weights = embeddings_arr

    # embedding_matrix = weights
    model = Sequential()
    e = Embedding(vocab_size_train, 300, weights=[embedding_matrix], input_length=max_length, trainable=False)
    model.add(e)
    # model.add(Conv1D(filters=32, kernel_size=3, padding='same', activation='relu'))
    # model.add(MaxPooling1D(pool_size=2))
    # model.add(LSTM(100, dropout=0.2, recurrent_dropout=0.2))
    # model.add(Flatten())
    model.add(LSTM(100))
    """ activation: 'sigmoid', 'relu', 'softmax',... """
    model.add(Dense(1, activation='sigmoid'))
    # compile the model
    # model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    # opt = optimizers.Adam(learning_rate=0.001)
    # model.compile(optimizer=opt, loss='binary_crossentropy', metrics=['accuracy'])

    # optimizer = optimizers.SGD(lr=0.01)
    print("\n\n compile 1: ")
    model.compile(optimizer='Adam', loss='binary_crossentropy', metrics=['accuracy'])
    # summarize the model
    print("\n", model.summary())
    # fit the model
    tensorboard_callback = TensorBoard(log_dir="logs")
    model.fit(X_train, y_train, epochs=10, verbose=2, batch_size=64)
    # evaluate the model
    loss, accuracy = model.evaluate(X_test, y_test, verbose=2)
    print('\n Accuracy: %f' % (accuracy * 100))

    print("\n\n compile 2: ")
    model.compile(optimizer='sgd', loss='mean_squared_error', metrics=['accuracy'])
    model.fit(X_train, y_train, epochs=60, verbose=2, batch_size=64)
    loss, accuracy = model.evaluate(X_test, y_test, verbose=2)
    print('\n Accuracy: %f' % (accuracy * 100))
    print("\n\n")

    print("\n\n compile 3: ")
    opt = optimizers.RMSprop()
    model.compile(optimizer=opt, loss='binary_crossentropy', metrics=['accuracy'])
    model.fit(X_train, y_train, epochs=60, verbose=2, batch_size=64)
    loss, accuracy = model.evaluate(X_test, y_test, verbose=2)
    print('\n Accuracy: %f' % (accuracy * 100))

    predicted_text = model.predict(X_test)
    encoded_argmax = np.argmax(X_test, axis=1)
    predicted_classes = np.argmax(X_test, axis=-1)
    encoded_argmax = encoded_argmax.tolist()
    predicted_classes = predicted_classes.tolist()
    print(predicted_classes)
    print(encoded_argmax)

    """ Inferring words and indexes from predicted test texts """
    # (1) get each layer needed separately --> it's already done
    embeddings_layer1 = model.layers[0].get_weights()[0]
    print("\n\n get embedding layer1 from model: ", embeddings_layer1)

    words_embeddings_w2index = {w: embeddings_layer1[idx] for w, idx in word_index_train.items()}

    given_word = 'system'
    print("\n\n word embedding of the given word: ", words_embeddings_w2index[given_word])
    print("\n\n related index of the word embedding of the given word: ", word_index_train[given_word])

    # words_embeddings_index2word = {idx:embeddings[w] for w, idx in index_word.items()}
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
    predicted_label = labels_test[np.argmax(predicted_text)]
    predicted_text_retrieved = docs_test[predicted_label]
    print(outputs[2])
    print(predicted_text_retrieved)

    # y_classes = predicted_text.argmax(axis=-1)
    # encoded_docs_test = array(encoded_docs_test)
    # yhat = model.predict_classes(padded_docs_test)
    for sentence in docs_test:
        for word in sentence.strip().split():
            if word in word_index_train.keys():
                print("related index of the word embedding of the given word: ", word_index_train[word])
            else:
                print('the word ', word, 'not exist')
