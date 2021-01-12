'''
Created on Jan 12, 2021

@author: Gelareh_mp
'''
from gensim.models import KeyedVectors
import numpy as np
import gensim.downloader as api
from gensim.models.word2vec import Word2Vec

path1= r"D:\files_MDEAI_original\Data_sets\GoogleNews-vectors-negative300.bin"
path2= r"D:\files_MDEAI_original\Data_sets\glove.6B.50d.txt"
dictionary_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary_shrinked.dictionary"

# Load vectors directly from the file
model = KeyedVectors.load_word2vec_format(path1, binary=True)

# Access vectors for specific words with a keyed lookup:
vectorA = model['easy']
vectorB = model['hallo']
vectorC = model['world']

# see the shape of the vector (300,)
print(vectorA.shape)
print(vectorA)
#print(model.most_similar('easy'))

print(vectorB.shape)
print(vectorB)

print(vectorC.shape)
print(vectorC)

vectorSum = np.sum((vectorA, vectorB, vectorC), axis=0)
print(vectorSum)

# vectorSum = np.sum((vectorA, vectorB, vectorC), axis=0)
# print(model.similarity(vectorSum))

# see the shape of the vector (300,)
print('vector shape: ',vectorA.shape)

# Processing sentences is not as simple as with Spacy:
vectors = [model[x] for x in "This is some text I am processing with Spacy".split(' ')]
print(model.similarity('straightforward','easy'))
print(model.similarity('simple','impossible'))
print(model.most_similar('simple'))


# Access vectors for specific words with a keyed lookup:
vectors = []

try:
    vectorA = model['kasdjklasjdlkasjdklsajdkasjdlknsnndj']
    vectors.append(vectorA)
except KeyError:
    pass # ignore...how to handle unseen words...?

try:
    vectorB = model['hallo']
    vectors.append(vectorB)
except KeyError:
    pass # ignore...how to handle unseen words...?

try:
    vectorC = model['world']
    vectors.append(vectorC)
except KeyError:
    pass # ignore...how to handle unseen words...?

# see the shape of the vector (300,)
vectorSum = np.sum(vectors, axis=0)
print(vectorSum)


