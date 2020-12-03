'''
Created on Dec 3, 2020

@author: Gelareh_mp
'''
from nltk.tokenize import RegexpTokenizer
import re

def text_to_words(text,unescape=True):
    if unescape:
        text = text.encode().decode("unicode-escape")
    words_array=[]
    tokenizer=RegexpTokenizer('[A-Za-z]+')
    words=tokenizer.tokenize(text)
    for word in words:
        splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
        for split_word in splitted:
            split_word=split_word.lower()
            words_array.append(split_word)
    return words_array 