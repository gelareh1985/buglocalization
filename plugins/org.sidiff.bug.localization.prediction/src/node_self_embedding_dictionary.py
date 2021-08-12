import numpy as np
import pickle
import nltk


fpath = "D:/buglocalization_gelareh_home/saved files/"


def load_file(file_name):
    open_file = open(file_name, "rb")
    loaded_list = pickle.load(open_file)
    open_file.close()
    return loaded_list


def getNodeSelfEmbedding(nodeID, data_matrix): 
    
    return data_matrix[nodeID]


if __name__ == '__main__':
    
    """calculate sum of word embeddings for most similar words to each word in each node and then sum up all words in the node
    key : node id
    value: processed text
    """
    filename = fpath + 'data_matrix.pkl'
    word_index_train = load_file(filename)
    print('file loaded!')

    data_matrix_dict = {}     
    for key, value in word_index_train.items():
        k = value[0]
        data_matrix_dict[k] = value[1]
    print('data_matrix updated!')
    
    # an example of finding embedding from dictionary by nodeId
    nodeid = 31948  # 414807
    embedding_vec = getNodeSelfEmbedding(nodeid, data_matrix_dict)
    print('node id: ', nodeid, '    embedding: ', embedding_vec)
