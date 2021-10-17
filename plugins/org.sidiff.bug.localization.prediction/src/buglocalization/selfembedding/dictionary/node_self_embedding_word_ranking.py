'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

import pickle
from os import path
from typing import Any, Dict, List, Set, Tuple, Union

import numpy as np
import pandas as pd
from buglocalization.dataset.neo4j_data_set import Neo4jConfiguration
from buglocalization.metamodel.meta_model import MetaModel
from buglocalization.metamodel.meta_model_uml import MetaModelUML
from buglocalization.textembedding.text_utils import text_to_words
from buglocalization.textembedding.word_to_vector_dictionary import \
    WordToVectorDictionary
from nltk.corpus import stopwords as nltk_stopwords


def compute_node_self_embedding():
    number_of_words: int = -1  # Limit of words per embedding, or -1 to turn off
    average_word_embedding: bool = False  # True: Average Sum Of Word Embedding, False: Sum Of Word Embedding
    node_signature: bool = True  # Compute node signature based on type
    
    node_self_embedding_dictionary_path: str = 'D:/evaluation/eclipse.pde.ui/node_self_embedding.pkl'
    missing_words_translation_dictionary_path: str = 'D:/evaluation/eclipse.pde.ui/node_self_embedding_resolved_missing_words.pkl'

    # Database connection:
    neo4j_configuration = Neo4jConfiguration(
        neo4j_host="localhost",  # or within docker compose the name of docker container "neo4j"
        neo4j_port=7687,  # port of Neo4j bold port: 7687, 11003
        neo4j_user="neo4j",
        neo4j_password="password",
    )

    # Model configuration:
    metamodel = MetaModelUML(neo4j_configuration=neo4j_configuration)

    NodeSelfEmbeddingWordRanking(number_of_words, average_word_embedding, 
                                 node_signature, metamodel,
                                 node_self_embedding_dictionary_path,
                                 missing_words_translation_dictionary_path)


class NodeSelfEmbeddingWordRanking:

    def __init__(self, number_of_words: int, average_word_embedding: bool, 
                 node_signature: bool, metamodel: MetaModel,
                 node_self_embedding_dictionary_path: str,
                 missing_words_translation_dictionary_path: str):

        self.unescape: bool = True
        self.average_word_embedding: bool = average_word_embedding
        self.node_signature: bool = node_signature

        self.named_element_dictionary: Dict[str, int] = {}
        self.common_words_dictionary: Dict[str, int] = {}
        self.number_of_words: int = number_of_words  # Limit of words per embedding
        self.truncated_words: List[int] = []

        # Metamodel configuration:
        self.metamodel = metamodel

        # Word embedding dictionary configuration:
        self.word_to_vector_dictionary: WordToVectorDictionary = WordToVectorDictionary()
        self.embedding_dictionary, self.dictionary_words_length = self.word_to_vector_dictionary.dictionary()
        self.stopwords = set(nltk_stopwords.words('english'))

        # Handle words missing in embedding dictionary:
        self.missing_words: Set[str] = set()
        self.missing_words_translation_dictionary = {}
        self.missing_words_translation_dictionary_path = missing_words_translation_dictionary_path

        # Output path of the node self embedding dictionary:
        self.node_self_embedding_dictionary_path = node_self_embedding_dictionary_path

        # Load/Compute missing words
        if path.exists(self.missing_words_translation_dictionary_path):
            self.missing_words_translation_dictionary = self.load_file(self.missing_words_translation_dictionary_path)
        else:
            print("Resolving Missing Words...")
            self.missing_words_translation_dictionary = self.search_and_resolve_missing_words()

            # Manual translations:
            self.missing_words_translation_dictionary["pde"] = ["platform", "development", "environment"]
            self.missing_words_translation_dictionary["jdt"] = ["java", "developer", "toolkit"]
            self.missing_words_translation_dictionary["jre"] = ["java", "runtime", "environment"]

            self.save_file(self.missing_words_translation_dictionary_path, self.missing_words_translation_dictionary)
            print("Missing Resolved Words:", self.missing_words_translation_dictionary)

        # Compute word ranking:
        if self.number_of_words != -1:
            self.named_element_dictionary, self.common_words_dictionary = self.compute_dictionaries()
        
        # Compute and save node self embedding:
        self.node_self_embedding_dictionary: Dict[int, np.ndarray] = self.compute_node_self_embedding_dictionary()

        self.save_file(self.node_self_embedding_dictionary_path, self.node_self_embedding_dictionary)

        print("Missing Words:", len(self.missing_words))
        print(self.missing_words)

        truncated_words_df = pd.DataFrame(self.truncated_words, columns=['truncated_words'])
        print("Truncated Words:", truncated_words_df.describe())

        if self.truncated_words:
            truncated_words_df.boxplot(column=['truncated_words'], showfliers=False)

        print("Node Embedding Finished!")

    def compute_node_self_embedding_dictionary(self) -> Dict[int, np.ndarray]:

        node_self_embedding_dictionary: Dict[int, np.ndarray] = {}

        for metatype, properties in self.metamodel.get_type_to_properties().items():
            cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
                metatype=metatype
            )
            metamodel_nodes = self.metamodel.get_graph().run(cypher=cypher_str_command).to_table()

            print("Compute Node Self Embedding:", len(metamodel_nodes), "nodes of type", metatype)
            counter = 0

            for node in metamodel_nodes:
                counter += 1
                
                if counter % 100 == 0:
                    print("> Progress:", counter, end='\r')
                
                nodeID: int = node[0].identity
                words = []

                for property in properties:
                    text = node[0][property]
                    words = words + text_to_words(text, self.unescape)

                # NOTE: For Summe Of Word Embeddings the ordering of words does not matter!
                # Truncate list of words if the number of words is larger then the specified threshold.
                if self.number_of_words != -1 and len(words) > self.number_of_words:
                    ranked_words = self.get_top_ranked_words(words)
                    # print("Selected", len(ranked_words), "of", len(words), "words.")
                else:
                    ranked_words = words
                
                if self.node_signature:
                    signature_words = self.metamodel.get_signature(node[0])
                    
                    for signature_word in signature_words:
                        ranked_words = ranked_words + text_to_words(signature_word)

                node_self_embedding_vector = self.sum_of_word_embeddings(ranked_words)
                node_self_embedding_dictionary[nodeID] = node_self_embedding_vector

        print("\n")
        return node_self_embedding_dictionary

    def get_top_ranked_words(self, words: List[str]):
        ranked_named_elements: List[Tuple[int, str]] = []
        ranked_common_words: List[Tuple[int, str]] = []

        # Compute ranking of each word:
        for word in words:
            if word in self.named_element_dictionary:
                ranking = self.named_element_dictionary[word]
                ranked_named_elements.append((ranking, word))
            elif word in self.common_words_dictionary:
                ranking = self.common_words_dictionary[word]
                ranked_common_words.append((ranking, word))

        # Sort by ranking:
        ranked_named_elements.sort(key=lambda rank: rank[0], reverse=True)
        ranked_common_words.sort(key=lambda rank: rank[0], reverse=True)

        # Remove stopwords from common words:
        ranked_common_words_cleaned = [ranked_word for ranked_word in ranked_common_words if ranked_word[1] not in self.stopwords]

        # if len(ranked_common_words_cleaned) < len(ranked_common_words):
        #     print("Removed Stopwords", len(ranked_common_words) - len(ranked_common_words_cleaned))

        ranked_common_words = ranked_common_words_cleaned

        # Compute truncated, ranked list of words:
        ranked_words = ranked_named_elements + ranked_common_words

        if len(ranked_words) > self.number_of_words:
            # print("Truncated words:", len(ranked_words) - number_of_words)
            self.truncated_words.append(len(ranked_words) - self.number_of_words)

        ranked_words = ranked_words[:self.number_of_words]  # truncating
        ranked_words = [ranked_word[1] for ranked_word in ranked_words]  # mapping to words

        return ranked_words

    def sum_of_word_embeddings(self, words: List[str]) -> np.ndarray:

        # Encode words:
        feature_vectors = []

        for word in words:
            try:
                word_embedding = self.embedding_dictionary[word]
                feature_vectors.append(word_embedding)
            except KeyError:
                # Check for resolution:
                if word in self.missing_words_translation_dictionary:
                    for resolved_word in self.missing_words_translation_dictionary[word]:
                        # Filter single characters:
                        if len(resolved_word) > 1:
                            try:
                                word_embedding = self.embedding_dictionary[resolved_word]
                                feature_vectors.append(word_embedding)
                            except KeyError:
                                # should actually not happen
                                self.missing_words.add(word)
                else:
                    # ignore...how to handle unseen words...?
                    self.missing_words.add(word)

        if feature_vectors:
            if self.average_word_embedding:
                # Average word embedding:
                return np.mean(feature_vectors, axis=0)
            else:
                # Sum of word embeddings:
                return np.sum(feature_vectors, axis=0)  # type: ignore
        else:
            # "Empty" embedding:
            return np.zeros(self.dictionary_words_length, dtype=np.float32)

    def compute_dictionaries(self) -> Tuple[Dict[str, int], Dict[str, int]]:

        # Dictionary: Word -> Ranking (Number)
        named_element_dictionary: Dict[str, int] = {}  # Ranking by incident (incoming/outgoing) edges
        common_words_dictionary: Dict[str, int] = {}  # Ranking by frequence of words

        for metatype, properties in self.metamodel.get_type_to_properties().items():
            cypher_str_command = """MATCH (n:{metatype}) RETURN n, size((n)-->()) + size((n)<--()) AS edgecount""".format(
                metatype=metatype
            )
            metamodel_nodes = self.metamodel.get_graph().run(cypher=cypher_str_command).to_table()

            print("Compute Word Ranking:", len(metamodel_nodes), "nodes of type", metatype)

            for node in metamodel_nodes:
                for property in properties:
                    text = node[0][property]
                    edgecount = node[1]

                    words = text_to_words(text, self.unescape)

                    for word in words:
                        if property == 'name':
                            self.addWordToDictionary(word, named_element_dictionary, edgecount)
                        else:
                            self.addWordToDictionary(word, common_words_dictionary, 1)

        return named_element_dictionary, common_words_dictionary

    def addWordToDictionary(self, word: str, dictionary: Dict[str, int], ranking: int):
        if word in dictionary:
            current_ranking = dictionary[word]
            dictionary[word] = current_ranking + ranking
        else:
            dictionary[word] = ranking

    def search_and_resolve_missing_words(self) -> Dict[str, List[str]]:
        missing_words_unresolved = set()

        for metatype, properties in self.metamodel.get_type_to_properties().items():
            cypher_str_command = """MATCH (n:{metatype}) RETURN n""".format(
                metatype=metatype
            )
            metamodel_nodes = self.metamodel.get_graph().run(cypher=cypher_str_command).to_table()

            print("Resolve Missing Words:", len(metamodel_nodes), "nodes of type", metatype)

            for node in metamodel_nodes:
                for property in properties:
                    text = node[0][property]
                    words = text_to_words(text, self.unescape)

                    for word in words:
                        if word not in self.embedding_dictionary:
                            missing_words_unresolved.add(word)

        missing_words_translation_dictionary = self.resolve_missing_words(missing_words_unresolved)
        return missing_words_translation_dictionary

    def resolve_missing_words(self, missing_words: Set[str]) -> Dict[str, List[str]]:
        resolved_words = {}

        # Try to split compound words:
        # Minimze the number of splittings: 'thewidget': ['thew', 'id', 'get'],
        # Rank different splittings by position in (sorted by frequence) embedding dictionary: 'changeset': ['changes', 'et']
        # Search from right to left: 'junit': ['thew', 'id', 'get'],  ['juni', 't']
        for missing_word in missing_words:
            if missing_word in resolved_words:
                continue

            compound_words_forward = []
            compound_word_offset_forward = 0
            ranking_forward = 0

            #
            # Scan forward:
            #
            while compound_word_offset_forward < len(missing_word):
                max_compound_word, max_compound_word_ends = self.resolve_missing_compound_word(
                    missing_word, compound_word_offset_forward)

                if max_compound_word:
                    compound_words_forward.append(max_compound_word)
                    compound_word_offset_forward = max_compound_word_ends
                    ranking_forward += self.embedding_dictionary.vocab[max_compound_word].index  # type: ignore
                else:
                    break

            # Only if word is fully recognized.
            # Accept single missing characters.
            allow_missing_characters_forward = 1

            if compound_word_offset_forward < len(missing_word) - allow_missing_characters_forward:
                compound_words_forward = []

            #
            # Scan reversed:
            #
            compound_words_reversed = []
            compound_word_offset_reversed = len(missing_word)
            ranking_reversed = 0

            while compound_word_offset_reversed > 0:
                max_compound_word_reversed, max_compound_word_starts = self.resolve_missing_compound_word_reversed(
                    missing_word, compound_word_offset_reversed)

                if max_compound_word_reversed:
                    compound_words_reversed.append(max_compound_word_reversed)
                    compound_word_offset_reversed = max_compound_word_starts
                    ranking_reversed += self.embedding_dictionary.vocab[max_compound_word_reversed].index  # type: ignore
                else:
                    break

            # Only if word is fully recognized.
            # Accept single missing characters.
            allow_missing_characters_reversed = 1

            if compound_word_offset_reversed > 0 + allow_missing_characters_reversed:
                compound_words_forward = []

            # Compare forward and reverse resolution:
            compound_words = []

            # Resolution found?
            if not compound_words_reversed:
                compound_words = compound_words_forward
            elif not compound_words_forward:
                compound_words = compound_words_reversed
            else:
                # Minimze the number of splittings:
                if len(compound_words_reversed) < len(compound_words_forward):
                    compound_words = compound_words_reversed
                elif len(compound_words_reversed) > len(compound_words_forward):
                    compound_words = compound_words_forward
                else:
                    # Avg. rank of splittings:
                    if ranking_reversed / len(compound_words_reversed) < ranking_forward / len(compound_words_forward):
                        compound_words = compound_words_reversed
                    else:
                        compound_words = compound_words_forward

            # Resolution found?
            if compound_words:
                resolved_words[missing_word] = compound_words

        return resolved_words

    def resolve_missing_compound_word(self, missing_word: str, compound_offset: int = -1, reversed: bool = False) -> Tuple[Union[None, str], int]:
        max_compound_word = None
        max_compound_word_offset = 0

        if compound_offset == -1:
            compound_offset = 0

        for idx in range(len(missing_word)):
            start_inclusive = compound_offset
            end_exlusive = idx + 1

            if missing_word[start_inclusive:end_exlusive] in self.embedding_dictionary:
                max_compound_word = missing_word[start_inclusive:end_exlusive]
                max_compound_word_offset = end_exlusive

        return max_compound_word, max_compound_word_offset

    def resolve_missing_compound_word_reversed(self, missing_word: str, compound_offset: int = -1) -> Tuple[Union[None, str], int]:
        max_compound_word = None
        max_compound_word_offset = 0

        if compound_offset == -1:
            compound_offset = len(missing_word)

        for idx in range(len(missing_word)):
            start_inclusive = compound_offset - idx - 1
            end_exlusive = compound_offset

            if missing_word[start_inclusive:end_exlusive] in self.embedding_dictionary:
                max_compound_word = missing_word[start_inclusive:end_exlusive]
                max_compound_word_offset = start_inclusive

        return max_compound_word, max_compound_word_offset

    def save_file(self, file_name: str, sample_list: Any):
        open_file = open(file_name, "wb")
        pickle.dump(sample_list, open_file)
        open_file.close()

    def load_file(self, file_name: str):
        open_file = open(file_name, "rb")
        loaded_list = pickle.load(open_file)
        open_file.close()
        return loaded_list
