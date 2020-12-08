'''
Created on Dec 7, 2020

@author: Gelareh_mp
'''
import os
from nltk.corpus import stopwords

dictionary_path =  r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/"

stop_words = set(stopwords.words('english'))

def merge_wordcount(dict_path,dictionary):
    with open(dict_path) as f:
        #contents = f.read()
        for i, line in enumerate(f):
            info = line.strip().split('\t')
            dictionary[info[0]]=info[1]
    f.close()
    return dictionary
    
def save_dictionaries(dictionary_file_name,list_of_dicts):
    output_dict=dict() 
    dictionary_words_size = len(output_dict)
    
    for dictionary in list_of_dicts:  
        for key, value in dictionary.items():
            if key not in output_dict and key not in stop_words:
                output_dict.update({key:dictionary_words_size})
                dictionary_words_size += 1
    
    print('size of whole dictionary: ',len(output_dict))
    
    with open(dictionary_file_name + ".dictionary", 'w') as f:
        for key, value in output_dict.items():   
            f.write(str(key) + '\t' + str(value) + "\n") 
    f.close()
    
#####################################################################################    
    
complete_dictionary=dict()
list_dictionaries=[]
for filename in os.listdir(dictionary_path):
        if filename.endswith(".dictionary"):
            graph_filename = filename[:filename.rfind(".")]
            dict_list_path = dictionary_path  + graph_filename + ".dictionary"
            dict_number = graph_filename[0:graph_filename.find("_")]
            
            dictionary=merge_wordcount(dict_list_path,complete_dictionary)
            list_dictionaries.append(dictionary)
            print('Dictionary: ',dict_number)
    
filename=dictionary_path+"complete_dict_stopwords_removed"    
save_dictionaries(filename,list_dictionaries)


