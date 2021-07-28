'''
Created on Nov 30, 2020

@author: Gelareh_mp
'''
import pandas as pd
import os
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from IPython.display import display
from preprocess_word_dictionary import WordDictionary
import multiprocessing
from gensim.utils import grouper

#positve_samples_path =      r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\set_5000/positive/"
positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"
feature_node_save_path =    positve_samples_path + "/features/"
doc2vec_save_path=  positve_samples_path + "/doc2vec_models/" 
dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\dictionaries\main dictionaries/complete_dict_stopwords_removed.dictionary_shrinked.dictionary"


def dataset_to_vector(positve_samples_path, dictionary_words, log=False):
    dataset_nodes = []
    dataset_edges=[]
    dataset_features = []
    for filename in os.listdir(positve_samples_path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = positve_samples_path + graph_filename + ".nodelist"
            edge_list_path = positve_samples_path + graph_filename + ".edgelist"
            graph_number = graph_filename[0:graph_filename.find("_")]
    
            # nodes:
            nodes_data = load_nodes(node_list_path)
            nodes_features = node_to_vector(nodes_data, dictionary_words)
            dataset_nodes.append(nodes_data)
            dataset_features.append(nodes_features)
            # edges:
            edge_data = load_edges(edge_list_path, graph_number)
            dataset_edges.append(edge_data)
            
            # store node encoding:
#             Path(feature_node_save_path).mkdir(parents=True, exist_ok=True)
#             node_features_list_path = feature_node_save_path + graph_filename + ".featurenodelist"
#             nodes_features.to_csv(node_features_list_path,chunksize=1000, sep="\t", header=False, index=True) 
            
            if log:
                print("Graph: ", graph_number)
    return dataset_nodes,dataset_edges, dataset_features 

    
def load_nodes(node_list_path):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
    node_data = node_data.fillna("")
    return node_data

def load_edges(edge_list_path, graph_number):
    edge_list_col_names = ["source", "target"]
    edge_data = pd.read_table(edge_list_path, names=edge_list_col_names)
    
    edge_data['source'] = edge_data['source'].apply(lambda index: add_prefix(graph_number, index))
    edge_data['target'] = edge_data['target'].apply(lambda index: add_prefix(graph_number, index))
    edge_data = edge_data.rename(index=lambda index: add_prefix(graph_number, index))
    
    return edge_data

def add_prefix(prefix, index):
    return str(prefix) + "_" + str(index)

def node_to_vector(node_data, dictionary_words):
    dictionary_words_dict = dictionary_words.get_dictionary()
    
    node_feature_data = pd.DataFrame(index=node_data.index, columns=["__text__"])
    
    
    text = node_data["text"].values.tolist()
    list_words=[]
    for row in text:
        words = dictionary_words.text_to_words(row)
        row_words=[]
        for word in words:
            if (word in dictionary_words_dict):
                row_words.append(word)
        single_string_of_words=' '.join(row_words)   
        list_words.append([single_string_of_words])
    node_feature_data["__text__"]=list_words
    
    metatype=node_data["type"].values.tolist()
    list_type=[]
    list_tag=[]
    for typ_value in metatype:
        if(typ_value=="BugReportNode"):
            list_type.append(typ_value)
            list_tag.append("# REPORT")
        elif (typ_value!="BugReportNode"):
            list_type.append("")    
            list_tag.append("")
    node_feature_data["__type__"]=list_type
    
    location_tag=node_data["tag"].values.tolist()
    for location_tag_index in range(len(location_tag)):
        if(location_tag[location_tag_index] == "# LOCATION"):
            list_tag[location_tag_index]="# LOCATION"
    node_feature_data["__tag__"]=list_tag
    
    node_feature_data.fillna(value="", inplace=True)
            
    return node_feature_data


# *******************************************************************************************
# Initialize dictionaries:
dictionary_words = WordDictionary()
dictionary_words.load(dictionary_path)
print("Dictionary Words:", len(dictionary_words.get_dictionary()))

#dictionary_types = WordDictionary()
#filename_types = dictionary_path + "complete_version_of_dictionary.dictionary"
#dictionary_types.load(filename_types)
#print("Dictionary Types:", len(dictionary_types.get_dictionary()))
                 
# Encode all graphs from the given folder: #, dataset_features
dataset_nodes,dataset_edges,dataset_features = dataset_to_vector(positve_samples_path, dictionary_words, log=True)

for table_node in dataset_nodes:
    display(table_node)

list_corpus=[]

for table_feature in dataset_features:
    display(table_feature)
#     list_sets=[]
#     for  index, row in table_feature.iterrows():
#         positiveFile=row["text"]
#         list_sets.append(positiveFile)
#              
    list_corpus.append(table_feature["__text__"].values)
print('****************************************************************') 
#print('corpus list: ', list_corpus)
 
count_file=0
for file_corpus in list_corpus:
    #print('file corpus: ', len(file_corpus), '    ',file_corpus)
    new_corpus=list(grouper(file_corpus,chunksize=100)) #new_corpus[0]
    #print(new_corpus)
    positive_set = [word.split() for sentence in new_corpus[0] for word in sentence]
    taggedPositiveFiles = [TaggedDocument(sentence, ["positive"+str(i)])for i, sentence in enumerate(positive_set)]
    #list_positive_set.append(positive_set)
    print('tagged document: ', len(taggedPositiveFiles),'    ',taggedPositiveFiles,'\n')
    cores = multiprocessing.cpu_count()
    model = Doc2Vec(taggedPositiveFiles, dm=1,min_count = 1,alpha=0.065, min_alpha=0.065, hs=1,workers=cores,  window=5,vector_size=50)
    model.build_vocab(taggedPositiveFiles, update=True)
     
    model.train(taggedPositiveFiles,total_examples=len(taggedPositiveFiles),epochs=30)
    model.delete_temporary_training_data(keep_doctags_vectors=True, keep_inference=True)
    
#     node_features_list_path=doc2vec_save_path+'doc2vec_file_'+str(count_file)+'.model'
#     model.save(node_features_list_path)
    print('finished saving model ',str(count_file),' !')
    vocab=model.wv.index2word
    print('vocabulary',' , (length): ', len(vocab), '    ', vocab,'\n')

    count_file=count_file+1
    
# *********************** Infering vector from the embedding *******************
#     node_embeddings = model.wv.vectors  # numpy.ndarray of size number of nodes times embeddings dimensionality
#     #node_embeddings=model.docvecs.vectors
#     #print('node embeddings: ',node_embeddings)
#     node_embedding_size=model.wv.vector_size
#     print('node embedding size: ',node_embedding_size, '    length of embeddings: ', len(node_embeddings))
#            
    if 'compiler' in vocab:
        print("word exist in dictionary! ")
        wv=model.wv['compiler']
        similars=model.wv.most_similar(positive=[wv,])
        print('similar word vector: ',similars,'\n')
               
        print('vector to infer and find: ',positive_set[0],'\n')
        dv=model.docvecs["positive0"]
        #wv2=model.wv['main', 'compile', 'close', 'log', 'file']
        similars_docs=model.docvecs.most_similar(positive=[dv])
        print('similar word to doc vector: ',similars_docs,'\n')
               
        inferred_vector = model.infer_vector(positive_set[0])
        print('a tagged sample: ', taggedPositiveFiles[0].tags, '    ',taggedPositiveFiles[0].words,'\n')
        #inferred_vecs=[(doc.tags[0], model.infer_vector(doc.words, steps=20)) for doc in taggedPositiveFiles] 
        #print(len(inferred_vecs))
        sims_found=model.docvecs.most_similar(positive=[inferred_vector])
        print('similar doc vector: ', [sims_found],'\n')
        #print(sims_found[0][0],'    ')#, taggedPositiveFiles["positive0"]) 
#    print('****************************************************************') 

# ************ Rank list of Infering vector from the embedding ************
# ranks = []    
# second_ranks = []
# for model in list_models:
#     doc_id=0
#     for positive_set in list_positive_set:
#         inferred_vector = model.infer_vector(positive_set[doc_id])
#         #print('inferred vec: ',inferred_vector,'\n')
#         sims = model.docvecs.most_similar([inferred_vector], topn=len(model.docvecs))
#         print("similar words: ",sims,'    ','\n')
#         rank = [tagged_doc for tagged_doc, similarity in sims]
#         #print('rank: ',rank,'\n')
#         for docid, sim in sims:
#             if docid in taggedPositiveFiles[doc_id].tags:
#                 print('info of inferred vector and its similar words: ',docid,'    ', sim, '    ',taggedPositiveFiles[doc_id].words,'\n')
#                 rnk=rank.index(docid)
#         doc_id=doc_id+1
#         ranks.append(rnk)
#   
# second_ranks.append(sims[1])
# print('ranks: ',ranks)
# print('second ranks: ', second_ranks)

# node_features_list_path=positve_samples_path+'data_frames/data_frame_categorial_'+str(i)+'.csv'
# nodes_features=pd.dataframe()
# nodes_features.to_csv(node_features_list_path,chunksize=1000, sep="\t", header=False, index=True) 

# ******************** Loading Saved models and querying *****************
# node_features_list_path=feature_node_save_path+'feature_file_new_0.model'
# model = Doc2Vec.load(node_features_list_path)
# vocab=model.wv.index2word
# #     print('vocabulary',' , (length): ', len(vocab), '    ', vocab,'\n')
# if 'compiler' in vocab:
#     print("word exist in dictionary! ")
#     wv=model.wv['compiler']
#     similars=model.wv.most_similar(positive=[wv,])
#     print('similar word vector: ',similars,'\n')
#           
#     print('vector to infer and find: ',positive_set[0],'\n')
#     dv=model.docvecs["positive0"]
#     #wv2=model.wv['main', 'compile', 'close', 'log', 'file']
#     similars_docs=model.docvecs.most_similar(positive=[dv])
#     print('similar word to doc vector: ',similars_docs,'\n')
#           
#     inferred_vector = model.infer_vector(positive_set[0])
#     print('a tagged sample: ', taggedPositiveFiles[0].tags, '    ',taggedPositiveFiles[0].words,'\n')
#     #inferred_vecs=[(doc.tags[0], model.infer_vector(doc.words, steps=20)) for doc in taggedPositiveFiles] 
#     #print(len(inferred_vecs))
#     sims_found=model.docvecs.most_similar(positive=[inferred_vector])
#     print('similar doc vector: ', [sims_found],'\n')
#     #print(sims_found[0][0],'    ')#, taggedPositiveFiles["positive0"]) 
        
# print('Finished encoding nodes!')
