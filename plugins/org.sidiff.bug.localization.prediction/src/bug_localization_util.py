'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from nltk.tokenize import RegexpTokenizer  # type: ignore
from time import time
from typing import List
import re


def t(start_time: float) -> str:
    return "{:.3f}".format(time() - start_time) + "s"


def text_to_words(text: str, stopwords={}, unescape: bool = True) -> List[str]:
    if unescape:
        text = text.encode().decode("unicode-escape", "ignore")
    words_array = []
    tokenizer = RegexpTokenizer('[A-Za-z]+')
    words = tokenizer.tokenize(text)
    for word in words:
        splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
        for split_word in splitted:
            split_word = split_word.lower()

            if len(split_word) > 1 and split_word not in stopwords:
                words_array.append(split_word)
    return words_array


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
        self.dictionary_words[label] = dictionary_words_size
        dictionary_words_size += 1

    def add_text(self, text):
        words = text_to_words(text, self.stopwords, self.unescape)
        dictionary_words_size = len(self.dictionary_words)

        for word in words:
            if word not in self.dictionary_words:
                self.dictionary_words[word] = dictionary_words_size
                dictionary_words_size += 1

        return words, self.dictionary_words
