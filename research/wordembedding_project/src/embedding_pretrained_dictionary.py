'''
Created on Jan 12, 2021

@author: Gelareh_mp
'''
from gensim.models import KeyedVectors
import numpy as np

path1 = r"D:/buglocalization_gelareh_home/GoogleNews-vectors-negative300.bin"
# path2 = r"D:/files_MDEAI_original\Data_sets/glove.6B.50d.txt"

# Load vectors directly from the file
model = KeyedVectors.load_word2vec_format(path1, binary=True)

# Access vectors for specific words with a keyed lookup:
file_corpus = [['main compile close log file'],['fixed'],['compile'],['system exit finished']] 

corpus_vects = []
for corpus in file_corpus:
    line_vect = []
    for phrase in corpus:
        print('corpus', corpus, '    ', phrase.strip().split())
        for word in phrase.strip().split():
            try:
                vect = model[word]
                line_vect.append(vect)
                    
            except KeyError:   
                pass  # ignore...how to handle unseen words...?
    line_vect_Sum = np.sum(line_vect, axis=0) 
    corpus_vects.append(line_vect_Sum)           

# vectors = [model[x] for x in "This is some text I am processing now".split(' ')]
# print(model.similarity('straightforward','easy'))
# print(model.similarity('simple','impossible'))
# print(model.most_similar('simple'))
print('length of the sum vector embedding: ',len(corpus_vects),'\t',len(corpus_vects[0]))
