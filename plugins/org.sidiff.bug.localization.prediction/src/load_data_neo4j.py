import json
import pandas as pd
import numpy as np
import pickle
from py2neo import Graph
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from tensorflow.keras.preprocessing.text import Tokenizer
from nltk.tokenize import RegexpTokenizer
from buglocalization.textembedding.word_to_vector_dictionary import WordToVectorDictionary
import re
import nltk
from tensorflow.keras.preprocessing.sequence import pad_sequences


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
    
    i = 0
    for key, value in node_signatures.items():
        processed_text = process_text(value)
        print(key, '     ', processed_text)
        similar_word_vectors = []
        count = 0
        for word in processed_text:
            # for word in row.strip().split():
            if word in model.index_to_key:
                if model[word] is not None:
                    # embeddings.append(model[word])
                    #key = model.key_to_index[word]
                    similar_words = model.most_similar(word)
                    for pair in similar_words:
                        vec = model[pair[0]]
                        similar_word_vectors.append(vec)
            count = count + 1
        line_vect_Sum = sum_word_vectors(similar_word_vectors)
        embedding_matrix[i] = (key, line_vect_Sum)    
        i = i + 1
    return embedding_matrix


def sum_word_vectors(vectors):
    line_vect_sum = np.sum(vectors, axis=0)
    if line_vect_sum.size > 1:
        return line_vect_sum


############################################################################################

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

