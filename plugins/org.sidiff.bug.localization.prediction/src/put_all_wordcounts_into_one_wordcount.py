'''
Created on Dec 7, 2020

@author: Gelareh_mp
'''
import os
from nltk.corpus import stopwords

dictionary_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\model node count dictionaries/"


def merge_wordcount(dict_path, complete_wordcount):
    with open(dict_path) as f:
        for i, line in enumerate(f):
            columns = line.strip().split('\t')
            word = columns[0]
            wordcount = int(columns[1])
            
            if word in complete_wordcount:
                complete_wordcount[word] = wordcount + complete_wordcount[word]
            else:
                complete_wordcount[word] = wordcount
                
    f.close()
    
def save_dictionaries(dictionary_file_name, complete_wordcount):
    print('size of whole dictionary: ', len(complete_wordcount))
    
    with open(dictionary_file_name + ".dictionary", 'w') as f:
        for key, value in complete_wordcount.items():   
            f.write(str(key) + '\t' + str(value) + "\n") 
    f.close()
    
#####################################################################################    

    
complete_wordcount = dict()

for filename in os.listdir(dictionary_path):
        if filename.endswith(".dictionary"):
            graph_filename = filename[:filename.rfind(".")]
            dict_list_path = dictionary_path + graph_filename + ".dictionary"
            dict_number = graph_filename[0:graph_filename.find("_")]
            
            merge_wordcount(dict_list_path, complete_wordcount)
            print('Dictionary: ', dict_number)
    
filename = dictionary_path + "complete_dict_stopwords_removed"    
save_dictionaries(filename, complete_wordcount)

