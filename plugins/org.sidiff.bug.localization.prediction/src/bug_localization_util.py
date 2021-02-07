'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

import re
from typing import List

from gensim.models import KeyedVectors  # type: ignore
from nltk.tokenize import RegexpTokenizer  # type: ignore


def text_to_words(text:str, stopwords={}, unescape:bool=True) -> List[str]:
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
