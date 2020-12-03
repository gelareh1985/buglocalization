'''
Created on Dec 3, 2020

@author: Gelareh_mp
'''
import os
from nltk.tokenize import RegexpTokenizer
import re
from nltk.corpus import stopwords
import pandas as pd

stop_words = set(stopwords.words('english'))

#dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset\buglocations_5000\all_files\buglocations/" 
dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset\buglocations_5000/"
save_path = "D:\\files_MDEAI_original\\Datasets\\buglocations_dataset\\saved files\\"

def generate_dictinoary_of_words(nodeslist,word_to_index,index_to_word):
    count =len(word_to_index)
    for nodelist in nodeslist:
        for row in nodelist:
            words=get_words(row)
            for word in words:
                if len(word) > 1 and word not in stop_words:
                    if(word not in word_to_index):
                        word_to_index.update ( {word : count})
                        index_to_word.update ( {count : word })
                        count  += 1

def get_words(text):
    words_array=[]
    tokenizer=RegexpTokenizer('[A-Za-z]+')
    words=tokenizer.tokenize(text)
    for word in words:
        splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
        for split_word in splitted:
            split_word=split_word.lower()
            words_array.append(split_word)
    return words_array 

def save_dictionary(dictionary_file_name,dictionary):
    with open(dictionary_file_name+".dictionary",'w') as f:
            for keys,values in dictionary.items():
                f.write(str(values)+'\t'+str(keys)+"\n") 
            
def load_nodes(node_list_path):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
    node_data = node_data.fillna("")
    return node_data


word_to_index=dict()
index_to_word=dict()

list_of_all_nodes_data=[]
for filename in os.listdir(dataset_path):
    if filename.endswith(".nodelist"):
        graph_filename = filename[:filename.rfind(".")]
        node_list_path = dataset_path + graph_filename + ".nodelist"
        graph_number = graph_filename[0:graph_filename.find("_")]

        # nodes:
        nodes_data = load_nodes(node_list_path)
        
        node_data_list=[]
        for index,row in nodes_data.iterrows(): 
            text = row["text"]
            node_data_list.append(text)
        
        list_of_all_nodes_data.append(node_data_list)   
         
        print("Graph: ", graph_number)

generate_dictinoary_of_words(list_of_all_nodes_data,word_to_index,index_to_word)
print("Length of complete dictionary: ", len(index_to_word))
 
dictionary_file_name=save_path+"complete_dict"
save_dictionary(dictionary_file_name,index_to_word)