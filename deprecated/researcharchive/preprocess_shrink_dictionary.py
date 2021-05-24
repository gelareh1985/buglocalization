'''
Created on Dec 8, 2020

@author: Gelareh_mp
'''

dictionary_words_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary"
wordcount_bugreports_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\bug report count dictionaries/complete_word_dict_complete_bugreport_count_new.dictionary"
wordcount_modelnodes_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\model node count dictionaries/complete_word_dict_complete_modelnode_count_new.dictionary"


def limit_dictionary_by_word_frequence(wordfrequence, frequence_limit):
    word_frequence_dictionary = {}
    
    for word, frequence in wordfrequence.items():
        if frequence > frequence_limit:
            word_frequence_dictionary[word] = frequence
                            
    return word_frequence_dictionary

    
def limit_dictionary_by_word_length(dictionary, wordlength):
    word_length_dictionary = {}
          
    for word, value in dictionary.items():
        if (len(word) >= wordlength): 
            word_length_dictionary[word] = value
            
    return word_length_dictionary


def intersection_dictionary(wordcountA, wordcountB):
    intersection_dictionary = {}
    
    for wordA, frequenceA in wordcountA.items():  
        if wordA in wordcountB:
            frequenceB = wordcountB[wordA]
            intersection_dictionary[wordA] = frequenceA + frequenceB
    
    return intersection_dictionary


def merge_dictionaries(dictionaryA, dictionaryB):
    merge_dictionaries = dict(dictionaryA)
    merge_dictionaries.update(dictionaryB)
    return merge_dictionaries;

            
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
    path = dictionary_file_name + ".dictionary"
    
    with open(path, 'w') as f:
        for word in dictionary:
            f.write(str(word) + '\t' + str(word_index) + "\n")
            word_index += 1
            
    return path

    
dictionary_words = load_dict(dictionary_words_path)
wordfrequence_bugreports = load_dict(wordcount_bugreports_path)
wordfrequence_modelnodes = load_dict(wordcount_modelnodes_path)

high_frequence_modelnode = 200
high_frequence_bugreport = 200
high_frequence_shrinked = 5
limit_word_length = 1

print("Original Dictionary: ", len(dictionary_words))

high_frequence_modelnode_words = limit_dictionary_by_word_frequence(wordfrequence_modelnodes, high_frequence_modelnode)
print("High Frequency (", high_frequence_modelnode, ") Model Node Words:", len(high_frequence_modelnode_words))

high_frequence_bugreport_words = limit_dictionary_by_word_frequence(wordfrequence_bugreports, high_frequence_bugreport)
print("High Frequency (", high_frequence_bugreport, ") Bug Report Words:", len(high_frequence_bugreport_words))

intersecting_dictionary = intersection_dictionary(wordfrequence_bugreports, wordfrequence_modelnodes)
print("Model Nodes And Bug Report Intersecting Words: ", len(intersecting_dictionary))

# Compute Shrinked Dictionary
shrinked_dictionary = dict(intersecting_dictionary)
print("Shrinked Dictionary - Only Intersecting Bug Report And Model Node Words: ", len(shrinked_dictionary))

shrinked_dictionary = merge_dictionaries(shrinked_dictionary, high_frequence_modelnode_words)
print("Shrinked Dictionary - Added High Frequency Model Node Words: ", len(shrinked_dictionary))

shrinked_dictionary = merge_dictionaries(shrinked_dictionary, high_frequence_bugreport_words)
print("Shrinked Dictionary - Added High Frequency Bug Report Words: ", len(shrinked_dictionary))

shrinked_dictionary = intersection_dictionary(dictionary_words, shrinked_dictionary)
print("Shrinked Dictionary - Only Words Occurring In The Original Dictionary: ", len(shrinked_dictionary))

shrinked_dictionary = limit_dictionary_by_word_length(shrinked_dictionary, limit_word_length)
print("Shrinked Dictionary - Only Words Longer Than", limit_word_length, ": ", len(shrinked_dictionary))

shrinked_dictionary = limit_dictionary_by_word_frequence(shrinked_dictionary, high_frequence_shrinked)
print("Shrinked Dictionary - High Frequency (", high_frequence_shrinked, "): ", len(shrinked_dictionary))

# Save dictionary:
output_path = save(shrinked_dictionary, dictionary_words_path + "_shrinked")

print("Output Path:", output_path)

