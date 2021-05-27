from buglocalization.textembedding.word_to_vector_dictionary import WordToVectorDictionary
import numpy as np
import nltk
# from gensim.models import KeyedVectors
from nltk.tokenize import api, word_tokenize
from nltk.tokenize import RegexpTokenizer
from nltk.corpus import brown
# from nltk.stem.wordnet import WordNetLemmatizer
# from gensim.models import Phrases
from gensim.corpora import Dictionary
from gensim.models import FastText

# import smart_open
from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Dense, Dot, Input, Embedding, Flatten, SpatialDropout1D, LSTM
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.preprocessing.text import one_hot
from tensorflow.keras.callbacks import TensorBoard


tokenizer = RegexpTokenizer('[A-Za-z]+')
# nltk.download('wordnet')
# nltk.download('punkt')
# nltk.download('averaged_perceptron_tagger')
# nltk.download('brown')
# nltk.download('universal_tagset')


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
    # print("all embedding vectors: ", corpus_vects)
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


if __name__ == '__main__':

    word_to_vector_dictionary = WordToVectorDictionary()
    word_dictionary, dimension = word_to_vector_dictionary.dictionary()
    # print(len(word_dictionary), "    ", word_dictionary["compile"])

# ***********************************************************************************
    # Access vectors for specific words with a keyed lookup:
    file_corpus = [['main compile close log file main'], ['fixed'], ['compile'], ['system exit finished'],
                ['return statement'], ['get result'], ['compile key compile'], ['print modifiers']]

    # file_corpus_train = ['main compile close log file main',
    #             'fixed',
    #             'compile',
    #             'system exit finished',
    #             'statement',
    #             'get result',
    #             'compile key compile',
    #             'print modifiers',
    #             'computer interface']
# ***********************************************************************************
    print("\n\n")
    model = word_dictionary
    corpus_vects = []
    tagged_part_of_speech = []
    for corpus in file_corpus:

        line_vect = []
        for phrase in corpus:

            print('corpus', corpus, '    ', phrase.strip().split())
            text = word_tokenize(phrase)
            print('par-of-speech tagged text: ', nltk.pos_tag(text))
            tagged_pos = nltk.pos_tag(text)
            tagged_part_of_speech.append(tagged_pos)

            for word in phrase.strip().split():
                try:
                    vect = model[word]
                    line_vect.append(vect)

                except KeyError:
                    pass  # ignore...how to handle unseen words...?
        line_vect_Sum = np.sum(line_vect, axis=0)
        corpus_vects.append(line_vect_Sum)
    # print("all embedding vectors: ", corpus_vects)
    # print("text words dictionary: ", word_dict)

    print("\n similarity to word1:", model.similarity('main', 'key'))
    print("\n similarity to word2:", model.similarity('Statement', 'fixed'))
    print("\n most similar words to the word:", model.most_similar('Statement'))

    print('\n length of the sum vector embedding: ', len(corpus_vects), '\t', len(corpus_vects[0]))

    brown_news_tagged = brown.tagged_words(categories='news', tagset='universal')
    tag_fd = nltk.FreqDist(tag for (word, tag) in brown_news_tagged)
    print(tag_fd.most_common())

    corpus_vects_verb = []
    corpus_vects_noun = []
    corpus_vects_number = []
    corpus_vects_adjective = []
    corpus_vects_adverb = []
    corpus_vects_conjunction = []
    for tagged_pos in tagged_part_of_speech:
        line_vect_verb = []
        line_vect_noun = []
        line_vect_number = []
        line_vect_adjective = []
        line_vect_adverb = []
        line_vect_conjunction = []
        for pos_tuple in tagged_pos:
            try:
                if pos_tuple[1] == 'VERB':
                    word = pos_tuple[0]
                    vect = model[word]
                    line_vect_verb.append(vect)
                elif pos_tuple[1] == 'NOUN':
                    word = pos_tuple[0]
                    vect = model[word]
                    line_vect_noun.append(vect)
                elif pos_tuple[1] == 'NUM':
                    word = pos_tuple[0]
                    vect = model[word]
                    line_vect_number.append(vect)
                elif pos_tuple[1] == 'ADJ':
                    word = pos_tuple[0]
                    vect = model[word]
                    line_vect_adjective.append(vect)
                elif pos_tuple[1] == 'ADV':
                    word = pos_tuple[0]
                    vect = model[word]
                    line_vect_adverb.append(vect)            
                elif pos_tuple[1] == 'CONJ':
                    word = pos_tuple[0]
                    vect = model[word]
                    line_vect_conjunction.append(vect)
            except KeyError:
                pass  # ignore...how to handle unseen words...?
        line_vect_sum_verb = np.sum(line_vect_verb, axis=0)
        corpus_vects_verb.append(line_vect_sum_verb)

        line_vect_sum_noun = np.sum(line_vect_noun, axis=0)
        corpus_vects_noun.append(line_vect_sum_noun)

        line_vect_sum_number = np.sum(line_vect_number, axis=0)
        corpus_vects_number.append(line_vect_sum_number)

        line_vect_sum_adjective = np.sum(line_vect_adjective, axis=0)
        corpus_vects_adjective.append(line_vect_sum_adjective)

        line_vect_sum_adverb = np.sum(line_vect_adverb, axis=0)
        corpus_vects_adverb.append(line_vect_sum_adverb)

        line_vect_sum_conjunction = np.sum(line_vect_conjunction, axis=0)
        corpus_vects_conjunction.append(line_vect_sum_conjunction)
# ***********************************************************************************    
    # print("model vector 0: ",model.vectors[0])

    # keras_model = Sequential()
    # keras_model.add(layer)
    # keras_model.add(Embedding(vocab_size, 8, input_length=MAX_SEQUENCE_LENGTH)
    # keras_model.add(SpatialDropout1D(0.2))
    # keras_model.add(LSTM(100, dropout=0.2, recurrent_dropout=0.2))
    # keras_model.add(Dense(10, activation='softmax'))
    # keras_model.add(LSTM(64))
    # keras_model.add(Dense(64, activation='relu'))
   
