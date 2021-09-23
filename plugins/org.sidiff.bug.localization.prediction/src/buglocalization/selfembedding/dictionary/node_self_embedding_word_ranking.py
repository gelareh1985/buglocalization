import pickle
from typing import Dict, List, Tuple

import numpy as np
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.textembedding.text_utils import text_to_words
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from py2neo import Graph

# Database connection:
neo4j_configuration = Neo4jConfiguration(
    neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
    neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
    neo4j_user="neo4j",
    neo4j_password="password",
)

# Metamodel configuration:
metamodel = MetaModelUML(neo4j_configuration=neo4j_configuration)

# Word embedding dictionary configuration:
word_to_vector_dictionary: WordToVectorDictionary = WordToVectorDictionary()
embedding_dictionary, dictionary_words_length = word_to_vector_dictionary.dictionary()
missing_words = set()

# Output path of the node self embedding dictionary:
node_self_embedding_dictionary_path = 'D:/evaluation/eclipse.pde.ui/node_self_embedding.pkl'

# Dictionaries with ranking:
named_element_dictionary: Dict[str, int] = None
common_words_dictionary: Dict[str, int] = None
number_of_words = 20


def compute_node_self_embedding():
    global named_element_dictionary
    global common_words_dictionary

    named_element_dictionary, common_words_dictionary = compute_dictionaries()
    node_self_embedding_dictionary: Dict[int, np.ndarray] = compute_node_self_embedding_dictionary()

    save_file(node_self_embedding_dictionary_path, node_self_embedding_dictionary)
    
    print("Missing Words:", len(missing_words))
    print(missing_words)
    
    print("Node Embedding Finished!")


def compute_node_self_embedding_dictionary(unescape=True) -> Dict[int, np.ndarray]:
    
    node_self_embedding_dictionary: Dict[int, np.ndarray] = {}

    for metatype, properties in metamodel.get_type_to_properties().items():
        cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
            metatype=metatype
        )
        metamodel_nodes = metamodel.get_graph().run(cypher=cypher_str_command).to_table()
        
        print("Compute Node Self Embedding:", len(metamodel_nodes), "nodes of type", metatype)

        for node in metamodel_nodes:
            nodeID = node[0].identity
            words = []
            
            for property in properties:
                text = node[0][property]
                words = words + text_to_words(text, unescape)
                
            # NOTE: For Summe Of Word Embeddings the ordering of words does not matter!
            # Truncate list of words if the number of words is larger then the specified threshold.
            if len(words) > number_of_words:
                ranked_words = get_top_ranked_words(words)
            else:
                ranked_words = words

            node_self_embedding_vector = sum_of_word_embeddings(ranked_words)
            node_self_embedding_dictionary[nodeID] = node_self_embedding_vector

    return node_self_embedding_dictionary


def get_top_ranked_words(words: List[str]):
    ranked_named_elements: List[Tuple[int, str]] = []
    ranked_common_words: List[Tuple[int, str]] = []
    
    # Compute ranking of each word:
    for word in words:
        if word in named_element_dictionary:
            ranking = named_element_dictionary[word]
            ranked_named_elements.append((ranking, word))
        elif word in common_words_dictionary:
            ranking = common_words_dictionary[word]
            ranked_common_words.append((ranking, word))
            
    # Sort by ranking:
    ranked_named_elements.sort(key=lambda rank: rank[0], reverse=True)
    ranked_common_words.sort(key=lambda rank: rank[0], reverse=True)
    
    # Compute truncated, ranked list of words:
    ranked_words = ranked_named_elements + ranked_common_words
    ranked_words = ranked_words[:number_of_words]  # truncating
    ranked_words = [ranked_word[1] for ranked_word in ranked_words]  # mapping to words

    return ranked_words


def sum_of_word_embeddings(words: List[str]):

    # Encode words:
    feature_vector = np.zeros(dictionary_words_length, dtype=np.float32)

    for word in words:
        try:
            word_embedding = embedding_dictionary[word]
            feature_vector = np.sum((feature_vector, word_embedding), axis=0)
        except KeyError:
            missing_words.add(word)
            pass  # ignore...how to handle unseen words...?

    return feature_vector


def compute_dictionaries(unescape=True) -> Tuple[Dict[str, int], Dict[str, int]]:

    # Dictionary: Word -> Ranking (Number)
    named_element_dictionary: Dict[str, int] = {}  # Ranking by incident (incoming/outgoing) edges
    common_words_dictionary: Dict[str, int] = {}  # Ranking by frequence of words

    for metatype, properties in metamodel.get_type_to_properties().items():
        cypher_str_command = """MATCH (n:{metatype}) RETURN n, size((n)-->()) + size((n)<--()) AS edgecount""".format(
            metatype=metatype
        )
        metamodel_nodes = metamodel.get_graph().run(cypher=cypher_str_command).to_table()
        
        print("Compute Word Ranking:", len(metamodel_nodes), "nodes of type", metatype)

        for node in metamodel_nodes:
            for property in properties:
                text = node[0][property]
                edgecount = node[1]

                words = text_to_words(text, unescape)

                for word in words:
                    if property == 'name':
                        addWordToDictionary(word, named_element_dictionary, edgecount)
                    else:
                        addWordToDictionary(word, common_words_dictionary, 1)

    return named_element_dictionary, common_words_dictionary


def addWordToDictionary(word: str, dictionary: Dict[str, int], ranking: int):
    if word in dictionary:
        current_ranking = dictionary[word]
        dictionary[word] = current_ranking + ranking
    else:
        dictionary[word] = ranking


def save_file(file_name, sample_list):
    open_file = open(file_name, "wb")
    pickle.dump(sample_list, open_file)
    open_file.close()


def load_file(file_name):
    open_file = open(file_name, "rb")
    loaded_list = pickle.load(open_file)
    open_file.close()
    return loaded_list
