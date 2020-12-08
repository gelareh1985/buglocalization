'''
Created on Dec 7, 2020

@author: Gelareh_mp
'''
archive_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset/00001_bug_12000_version_2a9b25d23714b865b9b9713bbe18b653db291769.7z"
file_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset/"

import libarchive
import io
import word_dictionary
import os
from nltk.tokenize import RegexpTokenizer
import re

from word_dictionary import WordDictionary

from nltk.corpus import stopwords

stop_words = set(stopwords.words('english'))


def read_archive(archive_path, factory):
    with libarchive.file_reader(archive_path) as reader:
            for entry in reader:
                if entry.name.endswith(".nodelist"):
                    data = b""
                    
                    for block in entry.get_blocks():
                        data += block
                    
                    yield factory(entry.name, io.BytesIO(data))

       
def load_text(file, buffer):
    return file, buffer.read().decode('UTF-8')


def load_file(file_path):
    tablelist = []
    with open(file_path) as f:
        # contents = f.read()
        for i, line in enumerate(f):
            info = line.strip().split('\t')
            tablelist.append(info[1])
            # print(info[1])
    f.close()
    return tablelist

# def get_words(text):
#     words_array=[]
#     tokenizer=RegexpTokenizer('[A-Za-z]+')
#     words=tokenizer.tokenize(text)
#     for word in words:
#         splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
#         for split_word in splitted:
#             split_word=split_word.lower()
#             words_array.append(split_word)
#     return words_array 


######################################################################
# reading from archieve
dictionary_from_archive = WordDictionary(stopwords=stop_words)

for file, text in read_archive(archive_path, load_text):
    print(file)
    
    for line in text.splitlines():
        columns = line.split("\t")
        
        # type column
        node_type = ""
        
        if (len(columns) >= 3):
            # [get text-label column][remove quotes]
            node_type = columns[2][1:-1]
               
        if node_type == "BugReportNode" or node_type == "BugReportCommentNode":
        
            # text-label column
            if (len(columns) >= 2):
                # [get text-label column][remove quotes]
                node_text = columns[1][1:-1]
                dictionary_from_archive.add_text(node_text)

    print('length of dictionary: ', len(dictionary_from_archive.get_dictionary()))   
    
    listpairs = []
    for key, value in dictionary_from_archive.get_dictionary().items():
        listpairs.append([key, value])
    print(listpairs)                

######################################################################
# reading from file
nodelists = []

for filename in os.listdir(file_path):
    if filename.endswith(".nodelist"):
        graph_filename = filename[:filename.rfind(".")]
        filepath = file_path + graph_filename + ".nodelist"
        file_number = graph_filename[0:graph_filename.find("_")]
        
        table = load_file(filepath)
        nodelists.append(table)
        print('Dictionary: ', file_number) 

dictionary_from_file = WordDictionary(stopwords=stop_words)     

for nodelist_index in range(len(nodelists)):  
    for row in nodelists[nodelist_index]:
        dictionary_from_file.add_text(row)

print('size of whole dictionary: ', len(dictionary_from_file.get_dictionary()))                            

listpairs = []
for key, value in dictionary_from_file.get_dictionary().items():
    listpairs.append([key, value])
print(listpairs)        
                                
