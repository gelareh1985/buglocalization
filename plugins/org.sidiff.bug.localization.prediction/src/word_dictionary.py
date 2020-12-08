'''
Created on Dec 3, 2020

@author: Gelareh_mp
'''
from nltk.tokenize import RegexpTokenizer
import re


class WordDictionary:
    
    def __init__(self, stopwords={}, unescape=True):
        super(WordDictionary, self).__init__()
        self.stopwords = stopwords
        self.dictionary_words = {}
        self.unescape = unescape
    
    def load(self, filename):
        with open(filename) as f:
            for i, line in enumerate(f):
                columns = line.strip().split('\t')
                self.dictionary_words[columns[0]] = int(columns[1])
        f.close()
        
    def save(self, dictionary_file_name):
        with open(dictionary_file_name + ".dictionary", 'w') as f:
            for key, value in self.dictionary_words.items():
                f.write(str(key) + '\t' + str(value) + "\n") 
     
    def get_dictionary(self):
        return self.dictionary_words
    
    def add_label(self, label):
        dictionary_words_size = len(self.dictionary_words)
        self.dictionary_words[label] = dictionary_words_size;
        dictionary_words_size += 1
      
    def add_text(self, text):
        words = self.text_to_words(text)
        dictionary_words_size = len(self.dictionary_words)
        
        for word in words:
            if word not in self.dictionary_words:
                self.dictionary_words[word] = dictionary_words_size;
                dictionary_words_size += 1
                    
        return words

    def text_to_words(self, text):
        if self.unescape:
            text = text.encode().decode("unicode-escape", "ignore")
        words_array = []
        tokenizer = RegexpTokenizer('[A-Za-z]+')
        words = tokenizer.tokenize(text)
        for word in words:
            splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
            for split_word in splitted:
                split_word = split_word.lower()
                
                if len(split_word) > 1 and split_word not in self.stopwords:
                    words_array.append(split_word)
        return words_array 
