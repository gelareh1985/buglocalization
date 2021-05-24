import numpy as np
# import nltk
# from gensim.models import KeyedVectors
from nltk.tokenize import RegexpTokenizer
from gensim.corpora import Dictionary
from gensim.test.utils import common_texts
from gensim.models import KeyedVectors
from gensim.models import Word2Vec

from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Bidirectional, Dense, Dot, Input, Embedding, Flatten, SpatialDropout1D, LSTM
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.preprocessing.text import one_hot
from tensorflow.keras.callbacks import TensorBoard


tokenizer = RegexpTokenizer('[A-Za-z]+')
# nltk.download('wordnet')
text_sample_path = r"C:/Users/gelareh/git/buglocalization/plugins/org.sidiff.bug.localization.prediction/data/sample_text.txt"
pretrained_dict_path = r"D:/buglocalization_gelareh_home/GoogleNews-vectors-negative300.bin"


def get_text(file_corpus):
    texts = []
    #str_texts = ""
    for row in file_corpus:
        for text in row:
            texts.append(text)
            text += "\n"
            #str_texts += text
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


def get_embedding_vectors(dictionary, model):
    embeddings = []
    found_embeddings_from_pretrained_model = {}
    for key, value in dictionary.items():
        #print(key, "  ,  ", value)
        if value in model.index_to_key:
            embeddings.append(model[value])
            key = model.key_to_index[value]
            found_embeddings_from_pretrained_model[key] = value
            #print("index of pretrained dictionary: ",model.key_to_index[value])
    return embeddings, found_embeddings_from_pretrained_model

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

if __name__ == '__main__':
    # Load vectors directly from the file
    model = KeyedVectors.load_word2vec_format(pretrained_dict_path, binary=True)
    # ***********************************************************************************
    # Access vectors for specific words with a keyed lookup:
    file_corpus = [['main compile close log file main'], ['fixed'], ['compile'], ['system exit finished'],
                   ['statement'], ['get result'], ['compile key compile'], ['print modifiers'], ['computer interface']]
    
    print("\n\n")
    # pretrained embeddings keydvectors
    corpus_vects = []
    for corpus in file_corpus:
        line_vect = []
        for phrase in corpus:
            print('corpus', corpus, '    ', phrase.strip().split())
            for word in phrase.strip().split():
                try:
                    vect = model[word]
                    line_vect.append(vect)

                except KeyError:
                    pass  # ignore...how to handle unseen words...?
        line_vect_Sum = np.sum(line_vect, axis=0)
        corpus_vects.append(line_vect_Sum)
    #print("all embedding vectors: ", corpus_vects)

    print("\n\n similarity to word1:", model.similarity('main', 'key'))
    print("\n similarity to word2:", model.similarity('Statement', 'fixed'))
    print("\n most similar words to the word:", model.most_similar('Statement'))
    print('\n length of the sum vector embedding: ', len(corpus_vects), '\t', len(corpus_vects[0]))

    # # ***********************************************************************************
    # # making keras model of pad sequences (sequential) first way
    # # ***********************************************************************************
    texts = get_text(file_corpus)
    docs_train = text_to_words(texts)
    print("\n\n docs_train: ", docs_train)

    dictionary_train = Dictionary(docs_train)
    print("\n documents dictionary: ", dictionary_train.token2id)
    # find file corpus embeddings from the pretrained data (input training data)
    embeddings_train, found_embeddings_dict_train = get_embedding_vectors(dictionary=dictionary_train, model=model)
    print("\n\n found input words: ", len(found_embeddings_dict_train), "   ,   ", found_embeddings_dict_train)
    print("\n found input embeddings: ", len(embeddings_train))

    # find file corpus embeddings from the pretrained data (sample testing data)
    sample_text = [['main bug report comment log sample close'], ['meta information']]
    texts = get_text(sample_text)
    docs_test = text_to_words(texts)
    print("\n\n docs_test: ", docs_test)

    dictionary_test = Dictionary(docs_test)
    print("\n test documents dictionary: ", dictionary_test.token2id)
    # Bag-of-words representation of the documents.
    # corpus = [dictionary_train.doc2bow(doc) for doc in docs_train]

    embeddings_test, found_embeddings_dict_test = get_embedding_vectors(dictionary=dictionary_test, model=model)
    print("\n\n found test words: ", len(found_embeddings_dict_test), "   ,   ", found_embeddings_dict_test)
    print("\nfound test embeddings: ", len(embeddings_test))

    sample_dictionary_train = found_pretrained_embeddings(found_embeddings_dict_train)
    vocab_train = sample_dictionary_train

    sample_dictionary_test = found_pretrained_embeddings(found_embeddings_dict_test)
    vocab_test = sample_dictionary_test     
    labels = np.array([1, 1, 1, 0, 1, 0, 0, 1, 0])
    
    # +++++++
    MAX_SEQUENCE_LENGTH = 10
    padded_docs_train = get_pad_sequences(file_corpus, sample_dictionary_train, MAX_SEQUENCE_LENGTH)

    padded_docs_test = get_pad_sequences(sample_text, sample_dictionary_test, MAX_SEQUENCE_LENGTH)

    print('Shape of data tensor:', padded_docs_train.shape)
    print('Shape of label tensor:', labels.shape)

    #weights = model.vectors
    embeddings_arr = np.asarray(embeddings_train)
    weights = embeddings_arr
    print("weights shape: ", weights.shape)
    index_to_key = model.index_to_key
    
    print("\n word2vec matrix: ", weights)
    wv_layer = Embedding(input_dim=weights.shape[0],
                     output_dim=weights.shape[1],
                     weights=[weights],
                     input_length=MAX_SEQUENCE_LENGTH,
                     trainable=False)
    # layer = Embedding(
    #     input_dim=weights.shape[0],
    #     output_dim=weights.shape[1],
    #     weights=[weights],
    #     input_length=10,
    #     trainable=False,
    # )
    # layer.mask_zero = True  # No need for a masking layer

    mymodel = Sequential()
    mymodel.add(wv_layer)  # your embedding layer
    # mymodel.add(Bidirectional(
    #     LSTM(64, dropout=0.5, recurrent_dropout=0.4, return_sequences=True))
    # )
    mymodel.add(Flatten())
    mymodel.add(Dense(1, activation='sigmoid')) 
    mymodel.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    print(mymodel.summary())