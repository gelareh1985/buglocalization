'''
Created on Jan 14, 2021

@author: Gelareh_mp
'''
import concurrent.futures  # type: ignore
from pathlib import Path

from gensim.models import KeyedVectors  # type: ignore

from bug_localization_data_set import DataSet, DataSetBugSample
import numpy as np  # type: ignore
import pandas as pd  # type: ignore
from word_dictionary import WordDictionary

positve_samples_path = r"C:\Users\manue\git\buglocalization\research\org.sidiff.bug.localization.dataset.domain.eclipse\datasets\eclipse.jdt.core\DataSet_20201123160235\positivesamples/"
negative_samples_path = r"C:\Users\manue\git\buglocalization\research\org.sidiff.bug.localization.dataset.domain.eclipse\datasets\eclipse.jdt.core\DataSet_20201123160235\negativesamples/"

feature_node_save_folder = "/features/"
type_dictionary_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\type_dictionary.dictionary"
pretrained_dictionary_path = r"C:\Users\manue\git\buglocalization\plugins\org.sidiff.bug.localization.embedding.properties\testdata\GoogleNews-vectors-negative300.bin"


class DatasetPropertyEmbedding:
    
    def __init__(self, node_property_embedding):
        self.node_feature_embedding = node_property_embedding
    
    def dataset_to_vector(self, positve_samples_path:str, negative_samples_path:str, log:bool=False):
        
        # Collect all graphs from the given folder:
        dataset = DataSet(positve_samples_path, negative_samples_path)
       
        with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
            for positive_bug_sample in dataset.positive_bug_samples:
                # self.sample_embedding(positve_samples_path, positive_bug_sample, log)
                executor.submit(self.sample_embedding, positve_samples_path, positive_bug_sample, log)
            for negative_bug_sample in dataset.negative_bug_samples:
                # self.sample_embedding(negative_samples_path, negative_bug_sample, log)
                executor.submit(self.sample_embedding, negative_samples_path, negative_bug_sample, log)
                    
    def sample_embedding(self, path:str, bug_sample:DataSetBugSample, log):
        bug_sample.load()
        
        # Encoded nodes:
        node_feature_data = pd.DataFrame(columns=self.create_column_names())
        filtered_nodes = set()
        
        for index, node in bug_sample.nodes.iterrows():
            if self.filter_node(node):
                filtered_nodes.add(index)
            else:
                feature_vector, tag = self.node_feature_embedding.node_to_vector(node)
                feature_vector_data = feature_vector.tolist()
                feature_vector_data.append(tag)
                
                node_feature_data.loc[index] = feature_vector_data

        # Store node encoding:
        Path(path + feature_node_save_folder).mkdir(parents=True, exist_ok=True)
        node_features_list_path = path + feature_node_save_folder + bug_sample.name + ".featurenodelist"
        
        # TODO: Make the storage format configurable...
        # node_feature_data.to_csv(node_features_list_path, sep="\t", header=False, index=True) 
        node_feature_data.to_pickle(path=node_features_list_path, compression="gzip")
        
        # Store edges:
        self.remove_node_edges(filtered_nodes, bug_sample.edges)
        edge_list_path = path + feature_node_save_folder + bug_sample.name + ".edgelist"
        bug_sample.edges.to_csv(edge_list_path, sep="\t")
        
        if log:
            print("Graph: ", bug_sample.number)
    
    # TODO: Use DataSet class
    def create_column_names(self):
        column_names = []
        
        for column in range(self.node_feature_embedding.get_dimension()):
            column_names.append(str("feature" + str(column)))
        
        column_names.append("tag")
        return column_names
    
    # FIXME: "unnamed" Literal... model elements
    def filter_node(self, node) -> bool:
        meta_type = node["type"]
        
        if meta_type == "LiteralString" or meta_type == "LiteralInteger" or meta_type == "LiteralBoolean" or meta_type == "LiteralUnlimitedNatural" or meta_type == "LiteralReal":
            return True
            
        return False
    
    def remove_node_edges(self, removed_nodes, edges):
        if removed_nodes:
            removed_edges = edges.loc[edges["source"].isin(removed_nodes) | edges["target"].isin(removed_nodes)]
            edges.drop(removed_edges.index, inplace=True)


class NodePropertyEmbedding:
    
    def __init__(self, dictionary_words, dictionary_words_length, dictionary_types, dictionary_types_length):
        self.dictionary_words = dictionary_words
        self.dictionary_words_length = dictionary_words_length
        self.dictionary_types = dictionary_types
        self.dictionary_types_length = dictionary_types_length
        # self.vector_dimension = dictionary_words_length + dictionary_types_length    # TODO: Make conversion configurable
        self.vector_dimension = dictionary_words_length
        
    def get_dimension(self):
        return self.vector_dimension

    def node_to_vector(self, node):
        text_vector = self.text_to_vector(node)
        # type_vector = self.metatype_to_vector(node)                                    # TODO: Make conversion configurable
        # feature_vector = np.concatenate((text_vector, type_vector), axis=None)
        feature_vector = text_vector
        
        tag = self.get_tag(node)
        
        return feature_vector, tag

    def text_to_vector(self, node):
        
        # Encode words:
        feature_vector = np.zeros(dictionary_words_length)
        
        text = node["text"]
        words = dictionary_types.text_to_words(text)  # TODO: Refactoring: move text_to_words function
        
        for word in words:
            try:
                word_embedding = dictionary_words[word]
                feature_vector = np.sum((feature_vector, word_embedding), axis=0) 
            except KeyError:   
                pass  # ignore...how to handle unseen words...?
                
        return feature_vector
    
    def metatype_to_vector(self, node):
        dictionary_types_dict = dictionary_types.get_dictionary()
        feature_vector = np.zeros(len(dictionary_types_dict))
                     
        # Encode meta-types:
        meta_type = node["type"]
         
        if meta_type in dictionary_types_dict:
            feature_vector[dictionary_types_dict[meta_type]] = 1
                 
        return feature_vector
   
    def get_tag(self, node): 
        
        # Is locations?
        tag = node["tag"]
        
        if tag is not None and tag:
            return tag
         
        # Mark bug report node:
        meta_type = node["type"]
         
        if meta_type == "BugReportNode":
            return "# REPORT"

        if meta_type == "BugReportCommentNode":
            return "# COMMENT"
        
        return None

if __name__ == '__main__':

    #===========================================================================
    # Load type dictionary:
    #===========================================================================
    dictionary_types = WordDictionary()
    dictionary_types.load(type_dictionary_path)
    
    print("Dictionary Types:", len(dictionary_types.get_dictionary()))
    
    #===========================================================================
    # Load Pretrained Dictionary:
    #===========================================================================
    print('Begin Loading Pretrained Dictionary Model ...')
    
    dictionary_words = KeyedVectors.load_word2vec_format(pretrained_dictionary_path, binary=True)
    dictionary_words_length = 300
    
    print('Finished Loading Pretrained Dictionary Model!\n')
    
    #===========================================================================
    # Encode all graphs from the given folder:
    #===========================================================================
    node_property_embedding = NodePropertyEmbedding(
        dictionary_words, dictionary_words_length,
        dictionary_types, len(dictionary_types.get_dictionary()))
    dataset_property_embedding = DatasetPropertyEmbedding(node_property_embedding)
    dataset_property_embedding.dataset_to_vector(positve_samples_path, negative_samples_path, log=True)
    
    print('Finished encoding nodes!')
