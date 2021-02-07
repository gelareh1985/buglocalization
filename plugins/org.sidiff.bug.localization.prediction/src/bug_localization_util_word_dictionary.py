'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
from bug_localization_util import text_to_words


class WordDictionary:
    
    def __init__(self, stopwords={}, unescape=True):
        super(WordDictionary, self).__init__()
        self.stopwords = stopwords
        self.dictionary_words = {}
        self.unescape = unescape
    
    def load(self, filename):
        with open(filename) as f:
            for i, line in enumerate(f):  # @UnusedVariable
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
        words = text_to_words(text, self.stopwords, self.unescape)
        dictionary_words_size = len(self.dictionary_words)
        
        for word in words:
            if word not in self.dictionary_words:
                self.dictionary_words[word] = dictionary_words_size;
                dictionary_words_size += 1
                    
        return words, self.dictionary_words
