import numpy as np
from gensim.models.doc2vec import Doc2Vec, TaggedDocument

print('*************************************************************************') 
print('*************************  Pre_process Data  ****************************')
print('*************************************************************************') 
# -------------------------------------------------------------------------------
# Pre_process Data: (requirement for creating doc2vec model)
# -------------------------------------------------------------------------------
vocabulary_words=["I","have","a","pet","They","she", "has","no","sky","green","yellow","blue"]
file = [["I have a pet"], ["They have a pet"], ["she has no pet currently"]]
print('initial file: ',file,'\n')

list_row=[]
for row_idx in range(len(file)):
    row_arr=np.array(vocabulary_words)
    i=0
    for word in vocabulary_words:
        for text_word in file[row_idx]:
            #print('word: ', word, '    file row: ', text_word.split())
            if word in text_word.strip().split():
                row_arr[i]=str(word)
            elif word not in text_word.split():
                row_arr[i]=" "  
            i=i+1    
    list_row.append(row_arr)
print('new file rows (list of arrays): \n', list_row,'\n')

wordslist=[]
for arr in list_row:
    lst_wrds=arr.tolist()
    wordslst=[]
    strl=""
    for words in lst_wrds:
        #single_string_of_words=''.join(words)
        strl=strl+words+" "
    wordslst.append(strl)
    print('length of list: ', len(strl), '    ', wordslst,'\n')
    wordslist.append(wordslst) 

print('new file rows (list of lists): \n', wordslist,'\n')

#print('word lists new: ',wordslist_new)    

positiveFile=[wordslist[0]]
negativeFile=[wordslist[1]]

print('row1: \n', len(positiveFile),'    ',positiveFile,'\n')
print('row2: \n', len(negativeFile),'    ',negativeFile,'\n')
 
positive_set = [word.split() for sentence in positiveFile for word in sentence]
negative_set = [word.split() for sentence in negativeFile for word in sentence]
total = positive_set + negative_set
print('length of positive: ', len(positive_set),'    ', 'length of negative: ', len(negative_set),'\n')
print('length of total: ', len(total),'\n')
 
print('*************************************************************************')  
print('**********************  Create tagged document  *************************') 
print('*************************************************************************') 
# -------------------------------------------------------------------------------
# Create tagged document: (requirement for creating doc2vec model)
# -------------------------------------------------------------------------------
  
taggedPositiveFiles = [TaggedDocument(sentence, ["positive"+str(i)])for i, sentence in enumerate(positive_set)]
taggedNegativeFiles = [TaggedDocument(sentence, ["negative"+str(i)])for i, sentence in enumerate(negative_set)]

print('tagged document1: ', len(taggedPositiveFiles),'    ',taggedPositiveFiles,'\n')
print('tagged document2: ', len(taggedNegativeFiles),'    ',taggedNegativeFiles,'\n')

#model=Doc2Vec(taggedPositiveFiles, min_count = 1, workers=1, vector_size=len(vocabulary_words))

print('a tagged sample: ', taggedPositiveFiles[0][0], '    ',taggedPositiveFiles[0][1],'\n')
print('a tagged sample: ', taggedPositiveFiles[0].tags, '    ',taggedPositiveFiles[0].words,'\n')

totalTagged = taggedNegativeFiles + taggedPositiveFiles

print('*************************************************************************')  
print('********************  Create doc embedding model  ***********************')
print('*************************************************************************') 
# -------------------------------------------------------------------------------
# Create doc embedding model 
# -------------------------------------------------------------------------------  
model = Doc2Vec(totalTagged, min_count = 1, workers=1, vector_size=len(vocabulary_words))
model.build_vocab(totalTagged, update=True)
model.train(totalTagged,total_examples=1, epochs=1)
print(model.docvecs["positive0"],'\n')
print(model.docvecs["negative0"],'\n')
vocab=model.wv.index2word
print('vocabulary',' , (length): ', len(vocab), '    ', vocab,'\n')

# TODO: update dictionary list to dict object 

for k, word in enumerate(model.wv.index2word):
    print(k, '    ', word)  

print('*************************************************************************')
print('********************  Get vectors and infer them  ***********************')
print('*************************************************************************') 
# -------------------------------------------------------------------------------
# Get vectors and infer them from created doc embedding model 
# -------------------------------------------------------------------------------
wv=model.wv['have']
similars=model.wv.most_similar(positive=[wv,])
print('sample word vector: ', wv , '    ' ,similars,'\n')
print('infer and find: ',positiveFile[0],'\n')
inferred_vector = model.infer_vector(positiveFile[0])
sims_found=model.docvecs.most_similar(positive=[inferred_vector])
print('sample doc vector: ', model.docvecs["positive0"] , '    ' ,[sims_found[0]],'\n')
print(sims_found[0][0],'    ')#, taggedPositiveFiles["positive0"])

print('*************************************************************************')
print('**************  Get ranked list of the inferred vector  *****************')
print('*************************************************************************') 
# -------------------------------------------------------------------------------
# Get ranked list of the inferred vector from created doc embedding model 
# -------------------------------------------------------------------------------
ranks = []    
second_ranks = []
for doc_id in range(len(positiveFile)):
    inferred_vector = model.infer_vector(positiveFile[doc_id])
    print('inferred vec: ',inferred_vector,'\n')
    sims = model.docvecs.most_similar([inferred_vector], topn=len(model.docvecs))
    print("similar words: ",sims,'    ',positiveFile[doc_id],'\n')
    rank = [tagged_doc for tagged_doc, similarity in sims]
    print('rank: ',rank,'\n')
    for docid, sim in sims:
        if docid in taggedPositiveFiles[doc_id].tags:
            print('info of inferred vector and its similar words: ',docid,'    ', sim, '    ',taggedPositiveFiles[doc_id].words,'\n')
            rnk=rank.index(docid)
    ranks.append(rnk)
 
second_ranks.append(sims[1])
print('ranks: ',ranks)
print('second ranks: ', second_ranks)

print('*************************************************************************')
print('************************  Save and Load model  **************************')
print('*************************************************************************')
# -------------------------------------------------------------------------------
# Save and Load model
# -------------------------------------------------------------------------------
path= r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"
fname=path+'my_doc2vec_model'
model.save(fname)
model = Doc2Vec.load(fname)

wv=model.wv['have']
similars=model.wv.most_similar(positive=[wv,])
print('sample word vector: ', wv , '    ' ,similars)
