from buglocalization.textembedding.word_to_vector_dictionary import WordToVectorDictionary
import numpy as np
import pickle
import nltk
import re
# from gensim.models import KeyedVectors
#from nltk.tokenize import word_tokenize
from nltk.tokenize import RegexpTokenizer
#from nltk.corpus import brown
# from nltk.stem.wordnet import WordNetLemmatizer
# from gensim.models import Phrases

# import smart_open
from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Dense, Dot, Input, Embedding, Flatten, SpatialDropout1D, LSTM
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
#from tensorflow.keras.preprocessing.text import one_hot
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


def node_to_signature(node, properties, type):
    signature = ""
    for property in properties:
        node_property = node[property]
        if node_property is not None:
            node_property += " "
            signature += str(node_property)
            # signature = signature.rstrip()
            # signature = signature.lstrip()
    return signature


def tokenize_corpus(text):
    words_array = []
    tokenizer = RegexpTokenizer('[A-Za-z]+[A-Za-z]')
    for row in text:
        words = tokenizer.tokenize(row)
        words_array.append(words)
    return words_array


def process_text(text):
    if isinstance(text, list):
        words_array = []
        tokenizer = RegexpTokenizer('[A-Za-z]+[A-Za-z]')

        for row in text:
            if not row.isdecimal():
                words = tokenizer.tokenize(row)
            row_words = camel_case_processor(words)
            words_array.append(row_words)
        return words_array
    elif isinstance(text, str):
        tokenizer = RegexpTokenizer('[A-Za-z]+[A-Za-z]')
        if not text.isdecimal():
            words = tokenizer.tokenize(text)
        row_words = camel_case_processor(words)
        #print(row_words)
        return row_words


def camel_case_processor(words):
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
    return row_words


def SOWE_similar_words(node_signatures, model, embedding_matrix):

    data_matrix = {}

    i = 0
    for key, value in node_signatures.items():
        processed_text = process_text(value)
        print('node ', str(i), ' :', key, '     ', processed_text)

        if value != "":
            count = 0
            sum_vectors = []
            for word in processed_text:
                # for word in row.strip().split():
                similar_word_vectors = []
                if word in model.index_to_key:  # and model[word] is not None:
                    # embeddings.append(model[word])
                    #key = model.key_to_index[word]
                    similar_words = model.most_similar(word)

                    for pair in similar_words:
                        vec = model[pair[0]]
                        similar_word_vectors.append(vec)
                else:
                    similar_word_vectors.append(np.zeros(300))
                sowe_one_word = sum_word_vectors(similar_word_vectors)
                sum_vectors.append(sowe_one_word)
                count = count + 1
            
            sowe_all_vectors = sum_word_vectors(sum_vectors)
            embedding_matrix[i] = sowe_all_vectors
            data_matrix[i] = (key, sowe_all_vectors)
            
        else:
            embedding_matrix[i] = np.zeros(300)
            data_matrix[i] = (key, np.zeros(300))
        i = i + 1

    return embedding_matrix, data_matrix


def get_embedding_matrix(t, model, input_text, embedding_matrix):
    t.fit_on_texts(input_text)
    for word, i in t.word_index.items():
        similar_word_vectors = []
        if word in model.index_to_key:
            similar_words = model.most_similar(word)

            for pair in similar_words:
                vec = model[pair[0]]
                similar_word_vectors.append(vec)
            sowe_one_word = sum_word_vectors(similar_word_vectors)
        embedding_matrix[i] = sowe_one_word        
    return embedding_matrix


def save_file(file_name, sample_list):
    open_file = open(file_name, "wb")
    pickle.dump(sample_list, open_file)
    open_file.close()


def load_file(file_name):
    open_file = open(file_name, "rb")
    loaded_list = pickle.load(open_file)
    open_file.close()
    return loaded_list


def sum_word_vectors(vectors):
    vects_sum = np.sum(vectors, axis=0)
    return vects_sum


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

    # Database connection:
    neo4j_configuration = Neo4jConfiguration(
        neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
        neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
        neo4j_user="neo4j",
        neo4j_password="password",
    )

    metamodel = MetaModelUML(neo4j_configuration=neo4j_configuration)
    nodes_to_signatures = {}
    for metatype, properties in metamodel.get_type_to_properties().items():
        # print(type, properties)
        cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
            metatype=metatype
        )
        metamodel_nodes = metamodel.get_graph().run(cypher=cypher_str_command).to_table()

        for row in metamodel_nodes:
            # node comments (long text)
            # node (short text)
            node = row[0]
            signature = node_to_signature(node, properties, metatype)
            node_id = node.identity
            nodes_to_signatures[node_id] = signature

    print(nodes_to_signatures)
    print(len(nodes_to_signatures))

    # file_corpus_train = ['main compile close log file pde jdt',
    #                      '',
    #                      'camel case txt',
    #                      'compile',
    #                      'system exit finished',
    #                      'statement',
    #                      'get result',
    #                      'compile key compile',
    #                      'print modifiers',
    #                      'computer interface']
    
    # file_corpus_test = ['main bug report comment log sample close', 'meta information']

    # print('\n process 1: ',tokenize_corpus(file_corpus_train))
    # print('\n')
    # print('\n process 2:',process_text(file_corpus_train))

    # t = Tokenizer()
    # t.fit_on_texts(file_corpus_train)
    # print('\n process 3:',len(t.word_index), '  ', t.word_index)

    #print(process_text('compile key compile CamelCaseTxt'))

    # nodes_to_signatures small example  
    # nodes_to_signatures = {}
    # i = 0
    # for node in file_corpus_train:
    #     nodes_to_signatures[i] = node
    #     i = i + 1

    # docs_train = []
    # for key, value in nodes_to_signatures.items():
    #     #print(key,'     ',value)
    #     processed_text = process_text(value)
    #     print(key, '     ', processed_text)
    #     node_pair = (key, processed_text)
    #     docs_train.append(node_pair)
    vocab_size_train = len(nodes_to_signatures) + 1 
    embedding_matrix = np.zeros((vocab_size_train, 300))

    """calculate sum of word embeddings for most similar words to each word in each node and then sum up all words in the node
    key : node id
    value: processed text
    """

    print("Loading Pre-trained Model ... ")
    word_to_vector_dictionary = WordToVectorDictionary()
    word_dictionary, dimension = word_to_vector_dictionary.dictionary()
    print("Loading Pre-trained Model Finished ... ")

    model = word_dictionary

    embedding_matrix, data_matrix = SOWE_similar_words(nodes_to_signatures, model, embedding_matrix)
    
    filename = fpath + 'data_matrix.pkl'
    save_file(filename,data_matrix)
    #word_index_train = load_file(filename)

    