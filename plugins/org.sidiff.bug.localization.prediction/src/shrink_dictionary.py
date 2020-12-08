'''
Created on Dec 8, 2020

@author: Gelareh_mp
'''

dictionary_words_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary"
wordcount_bugreports_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\bug report count dictionaries/complete_word_dict_complete_bugreport_count_new.dictionary"
wordcount_modelnodes_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\model node count dictionaries/complete_word_dict_complete_modelnode_count_new.dictionary"

def shrink_dictionary(dictionary_words, wordcount_bugreports, wordcount_modelnodes, frequence_limit_bug_report, frequence_limit_model_nodes):
    for bugreport_word, bugreport_count in wordcount_bugreports.items():
        
        # remove the words if the counting from bug reports is below the given frequence_limit
        # keep the word if it occurred in the model node dictionary 
#        if bugreport_count <= frequence_limit and bugreport_word not in wordcount_modelnodes:
#            dictionary_words.pop(bugreport_word, None)

        if bugreport_count <= frequence_limit_bug_report:
            dictionary_words.pop(bugreport_word, None)
    
    for modelnode_word, modelnode_count in wordcount_modelnodes.items():  
        if modelnode_count <= frequence_limit_model_nodes:
            dictionary_words.pop(modelnode_word, None)
            
    for dictionary_word, dictionary_word_index in list(dictionary_words.items()):
        if (len(dictionary_word) <= 2): 
            del dictionary_words[dictionary_word]
        
            
def load_dict(filename):
    dictionary = {}
    
    with open(filename) as f:
        for i, line in enumerate(f):
            columns = line.strip().split('\t')
            dictionary[columns[0]] = int(columns[1])
    f.close()
    return dictionary

def save(dictionary, dictionary_file_name):
    word_index = 0
    
    with open(dictionary_file_name + ".dictionary", 'w') as f:
        for key, value in dictionary.items():
            f.write(str(key) + '\t' + str(word_index) + "\n")
            word_index += 1
    
dictionary_words = load_dict(dictionary_words_path)
wordcount_bugreports = load_dict(wordcount_bugreports_path)
wordcount_modelnodes = load_dict(wordcount_modelnodes_path)

print(len(dictionary_words))
shrink_dictionary(dictionary_words, wordcount_bugreports, wordcount_modelnodes, 5, 2)
print(len(dictionary_words))

save(dictionary_words, dictionary_words_path + "_shrinked")

