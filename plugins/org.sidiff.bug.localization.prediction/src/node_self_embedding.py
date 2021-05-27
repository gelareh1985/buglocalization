from buglocalization.textembedding.word_to_vector_dictionary import WordToVectorDictionary
import numpy as np
# import nltk
# from gensim.models import KeyedVectors
from nltk.tokenize import RegexpTokenizer
# from nltk.stem.wordnet import WordNetLemmatizer
# from gensim.models import Phrases
from gensim.corpora import Dictionary
from gensim.models import FastText

# import smart_open
from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Dense, Dot,Input, Embedding, Flatten, SpatialDropout1D, LSTM
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.preprocessing.text import one_hot
from tensorflow.keras.callbacks import TensorBoard


tokenizer = RegexpTokenizer('[A-Za-z]+')
#nltk.download('wordnet')


def get_sum_of_word_embeddings(file_corpus, model):
    # pretrained embeddings keydvectors
    corpus_vects = []
    file_corpus = list(file_corpus)
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
    return corpus_vects


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

# def extract_documents(url='https://cs.nyu.edu/~roweis/data/nips12raw_str602.tgz'):
#     with smart_open.open(url, "rb") as file:
#         with tarfile.open(fileobj=file) as tar:
#             for member in tar.getmembers():
#                 if member.isfile() and re.search(r'nipstxt/nips\d+/\d+\.txt', member.name):
#                     member_bytes = tar.extractfile(member).read()
#                     yield member_bytes.decode('utf-8', errors='replace')


text_sample_path = r"C:/Users/gelareh/git/buglocalization/plugins/org.sidiff.bug.localization.prediction/data/sample_text.txt"

if __name__ == '__main__':
     
    # docs = list(extract_documents())
    # print(len(docs))
    # print("sample docs: ",docs[0][:500]) 

    word_to_vector_dictionary = WordToVectorDictionary()
    word_dictionary, dimension = word_to_vector_dictionary.dictionary()
    #print(len(word_dictionary), "    ", word_dictionary["compile"])

# ***********************************************************************************
    # Access vectors for specific words with a keyed lookup:
    file_corpus = [['main compile close log file main'], ['fixed'], ['compile'], ['system exit finished'],
                ['ReturnStatement'], ['getResult'], ['compile key compile'], ['printModifiers']]
    
    #print("texts: ",str_texts)
    print("docs ",docs)

    #print("text string: ",str_texts)
    #print("text to words: ",text_to_words(str_texts))
# ***********************************************************************************
    # lemmatizer = WordNetLemmatizer()
    # docs = [[lemmatizer.lemmatize(token) for token in doc] for doc in docs]
    # print("letmaatizer output: ", docs)
# ***********************************************************************************
    # # Add bigrams and trigrams to docs (only ones that appear 20 times or more).
    # bigram = Phrases(docs, min_count=20)
    # for idx in range(len(docs)):
    #     for token in bigram[docs[idx]]:
    #         if '_' in token:
    #             # Token is a bigram, add to document.
    #             docs[idx].append(token)
    # print("bigrams and trigrams output: ", docs)            
# ***********************************************************************************    
    # Create a dictionary representation of the documents.
    dictionary = Dictionary(docs)
    #print("documents dictionary: ", dictionary.token2id)

    # # Filter out words that occur less than 20 documents, or more than 50% of the documents.
    # dictionary.filter_extremes(no_below=20, no_above=0.5)
# ***********************************************************************************
    # Bag-of-words representation of the documents.
    corpus = [dictionary.doc2bow(doc) for doc in docs]
    print("bag-of-words corpus: ",corpus)
# ***********************************************************************************
    model = word_dictionary

    # corpus_vects = []
    # for corpus in file_corpus:
        
    #     line_vect = []
    #     for phrase in corpus:
            
    #         print('corpus', corpus, '    ', phrase.strip().split())
    #         for word in phrase.strip().split():
    #             try:
    #                 vect = model[word]
    #                 line_vect.append(vect)

    #             except KeyError:
    #                 pass  # ignore...how to handle unseen words...?
    #     line_vect_Sum = np.sum(line_vect, axis=0)
    #     corpus_vects.append(line_vect_Sum)
    # # print("all embedding vectors: ", corpus_vects)
    # # print("text words dictionary: ", word_dict)

    # print("similarity to word1:", model.similarity('main', 'key'))
    # print("similarity to word2:", model.similarity('Statement', 'fixed'))
    # print("most similar words to the word:", model.most_similar('Statement'))

    # print('length of the sum vector embedding: ', len(corpus_vects), '\t', len(corpus_vects[0]))
# ***********************************************************************************    
    embeddings = []
    values = []
    for key, value in dictionary.items():
        #print(key, "  ,  ", value)
        if value in model.index_to_key:
            embeddings.append(model[value])
            values.append((key,value))

    print("found values: ", len(values), "   ,   ",values)
    print("found embeddings: ", len(embeddings))
    
    #print("model vector 0: ",model.vectors[0])

    #keras_model = Sequential()
    #keras_model.add(layer)
    #keras_model.add(Embedding(vocab_size, 8, input_length=MAX_SEQUENCE_LENGTH)
    #keras_model.add(SpatialDropout1D(0.2))
    #keras_model.add(LSTM(100, dropout=0.2, recurrent_dropout=0.2))
    #keras_model.add(Dense(10, activation='softmax'))
    #keras_model.add(LSTM(64))
    #keras_model.add(Dense(64, activation='relu'))
   
# ***********************************************************************************    
    docs_sample = ['main compile close log file main',
         'fixed',
         'compile', 
         'system exit finished',
         'ReturnStatement',
         'getResult', 
         'compile key compile', 
         'printModifiers']
    #dictionary = Dictionary([docs_sample])

    #print("length of dictionary: ",len(dictionary))
    
    # for key, value in dictionary.items():
    #     print(key, "    ", value)  
    print("dictionary (words and indexes): ",dictionary.token2id)
    # category1:1, category2:2, category3:3, category4:4
    #labels = np.array([1, 1, 1, 4, 2, 3, 3,4])
    labels = np.array([1, 1, 1, 0, 1, 0, 0, 1])
# ************************************************************************************    
    # integer encode the documents: method1
    vocab_size = len(dictionary)
    #encoded_docs = [one_hot(d, vocab_size) for d in docs_sample]

    #print("encoded docs: ",encoded_docs)
# ************************************************************************************   
    # # integer encode the documents: method2
    # t = Tokenizer()
    # t.fit_on_texts(docs)
    # encoded_docs = t.texts_to_sequences(docs)
    # vocab_size = len(t.word_index) + 1
    # print("encoded docs: ",encoded_docs)
# ************************************************************************************   
    # pad documents to a max length of 4 words
    max_length = 5
    for idx in range(len(corpus)):
        for encoded_doc_row in corpus[idx]:
            print(encoded_doc_row)

    #padded_docs = pad_sequences(encoded_docs, maxlen=max_length, padding='post')
    #print("padded docs: ",padded_docs)
# ************************************************************************************   
    # keras_model = Sequential()
    # keras_model.add(Embedding(vocab_size, 8, input_length=max_length))
    # keras_model.add(Dense(1))
    # keras_model.add(Flatten())
    # keras_model.add(Dense(1, activation='sigmoid'))
    
    # keras_model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    # #keras_model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
    # print(keras_model.summary())
    
    # tensorboard_callback = TensorBoard(log_dir="logs")
    # # fit the model
    # keras_model.fit(padded_docs, labels, epochs=50, verbose=2, callbacks=[tensorboard_callback])

    # # evaluate the model
    # loss, accuracy = keras_model.evaluate(padded_docs, labels, verbose=2)
    # print('Accuracy: %f' % (accuracy * 100))

    # output = keras_model.predict(padded_docs)
    # print(output)
