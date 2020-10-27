'''
Created on Oct 22, 2020

@author: Gelareh_mp
'''
from word_to_vec import generate_dictinoary_data
import re
from nltk.corpus import stopwords
from nltk.tokenize import RegexpTokenizer
import numpy as np

# path=r'D:\\MDEAI_Files_Original\\Datasets\\DataSet_20200925144220\\evaluation\\00001_bug_12000_version_2c9cb94dc84956e5c7f0db27e02f01d02c3e4f02.nodelist'


def generate_dictinoary_data(text):
    word_to_index= dict()
    index_to_word = dict()
    corpus = []
    count = 0
    vocab_size = 0
    
    for row in text:
        for word in row.split():
            word = word.lower()
            corpus.append(word)
            if word_to_index.get(word) == None:
                word_to_index.update ( {word : count})
                index_to_word.update ( {count : word })
                count  += 1
    vocab_size = len(word_to_index)
    length_of_corpus = len(corpus)
    
    return word_to_index,index_to_word,corpus,vocab_size,length_of_corpus

def get_file_data(path):
    table_data=load_table(path)
    file_contents = []
    for row in table_data:
#         file_contents = f.read()
        #tokenizer = RegexpTokenizer('\w+|\$[\d\.]+|\S+')
        tokenizer=RegexpTokenizer('[A-Za-z]+')
        file_contents.extend(tokenizer.tokenize(row[1]))
       
    return file_contents  

def load_table(path):
    table_data = []
    with open(path) as fp:
        for i,line in enumerate(fp):
            info = line.strip().split('\t')
            table_data.append(info) 
        
    return table_data
  
def stopword_effect(file_contents,stop_word_removal = 'no'): 
    line1 = ''
    line2= ''
    text=[]
    stop_words = set(stopwords.words('english'))
    for words in file_contents:
        #print('Words: ',words)
        if stop_word_removal == 'yes': 
            if len(words) > 1 and words not in stop_words:
                line1 = line1 + ' ' + words
        else:
            if len(words) > 1 :
                line2 = line2 + ' ' + words
    if stop_word_removal == 'yes':    
        text.append(line1) 
    elif stop_word_removal == 'no': 
        text.append(line2)  
    return text
          
def save_file_data(text,stop_word_removal = 'no'):   
    
    for words in text:    
        if stop_word_removal == 'yes':
            appendFile = open('text_corpus_stopwords_removed.txt','a') 
            appendFile.write(words+" ") 
            appendFile.close()         
        elif stop_word_removal == 'no':    
           
            appendFile = open('text_corpus_with_stopwords.txt','a') 
            appendFile.write(words+" ") 
            appendFile.close()  
            
            
        