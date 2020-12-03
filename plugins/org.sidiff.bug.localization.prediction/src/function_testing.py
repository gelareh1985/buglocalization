'''
Created on Dec 3, 2020

@author: Gelareh_mp
'''
from nltk.tokenize import RegexpTokenizer
import re
from nltk.corpus import stopwords

stop_words = set(stopwords.words('english'))

def text_to_words(text):
    words_array=[]
    tokenizer=RegexpTokenizer('[A-Za-z]+')
    words=tokenizer.tokenize(text)
    for word in words:
        splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
        for split_word in splitted:
            split_word=split_word.lower()
            if len(split_word) > 1 and split_word not in stop_words:
                words_array.append(split_word)
    return words_array 

str_test=b'"This definitely needs to be improved, \/P2.\n- Add jdt.annotation_2 bundle to both the projects.\n- Create classes C1 and C2 in P1 and P2 respectively with the following content:\n\tMap<String, String> map; \/\/ field\n\t{\n\t\tmap.get(null);\n\t}\n- Press Ctrl+Shift+T I agree.\n\nJust to double check:\n\nThose errors"'
print(str_test.decode("utf-8"))