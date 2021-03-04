'''
Created on Nov 10, 2020

@author: Gelareh_mp
'''
from stellargraph import StellarGraph
from stellargraph import IndexedArray

from src.generate_dataset import MDEdataset

import os
import numpy as np
import pandas as pd
from nltk.corpus import stopwords

p3='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\evaluation\\'
p4='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\training\\'
nodelist_output_folder='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\results\\outputs_training\\'
#edgelist_output_folder='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\evaluation\\edgelist_outputs\\'

############################################################################################################
all_word_to_index=dict()
all_index_to_word=dict()
word_to_index=dict()
index_to_word=dict()
edge_list_dictionary=dict()
   
stop_words = set(stopwords.words('english'))

output_filepath1=nodelist_output_folder
fname=""
all_files_table=[]
for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    number_of_files=len(os.listdir(p3))
    if filename.endswith(".nodelist"):
        data_instance=MDEdataset(filepath,stop_words,output_filepath1,number_of_files,fname)
        table=data_instance.load_table()
        print('length of all files table: ', len(table))
        all_files_table.append(table)
       
data_instance.number_of_files=number_of_files

nodelist_dictionary_of_all_words=data_instance.generate_dictinoary_of_all_words(all_files_table,1,all_word_to_index,all_index_to_word)
print('length of dictionary: ', len(nodelist_dictionary_of_all_words))    
data_instance.output_filepath=output_filepath1+"dictionary_all_files_word_to_index.txt"      
data_instance.save_dictionary(data_instance.output_filepath,all_word_to_index)
data_instance.output_filepath=output_filepath1+"dictionary_all_files_index_to_word.txt"  
data_instance.save_dictionary(data_instance.output_filepath,all_index_to_word)


i=0  

output_filepath2=nodelist_output_folder
fname=""

list_of_node_ids=[]
list_of_nodes=[]

list_of_meta_types=[]

list_of_model_vectors=[]
list_of_bug_vectors=[]

list_of_list_columns=[]

for filename in os.listdir(p4):
    
        filepath=os.path.join(p4,filename)
        number_of_files=1 
        
        if filename.endswith(".nodelist"):
            
            data_instance2=MDEdataset(filepath,stop_words,output_filepath2,number_of_files,fname)
            
            table=data_instance2.load_table()
           
            data_instance2.number_of_files=number_of_files
            node_ids=data_instance2.generate_list_of_data(table,0,data_instance2.number_of_files) 
            nodes=data_instance2.generate_list_of_data(table,1,data_instance2.number_of_files)
               
            meta_types=data_instance2.generate_list_of_data(table,2,data_instance2.number_of_files)    
            
            list_of_node_ids.append(node_ids)
            list_of_nodes.append(nodes)
            list_of_meta_types.append(meta_types)
            
           
            bug_report_vector=np.zeros(len(nodelist_dictionary_of_all_words))
            data_instance2.generate_one_hot_vector(table,0,1,1,nodelist_dictionary_of_all_words,bug_report_vector)
            
            list_of_bug_vectors.append(bug_report_vector)
            
            model_vectors=[]
            found_words=[]
            for k in range(len(table)):
                model_vector=np.zeros(len(nodelist_dictionary_of_all_words))
                data_instance2.generate_one_hot_vector(table,k+1,k+2,1,nodelist_dictionary_of_all_words,model_vector) 
                model_vectors.append(model_vector)
            list_of_model_vectors.append(model_vectors) 
            
            
        elif filename.endswith(".edgelist"):
            data_instance3=MDEdataset(filepath,stop_words,output_filepath2,number_of_files,fname)
            data_column1,data_column2=data_instance3.load_edge_table()
            
            #print('length of dcol1: ',len(data_column1), 'length of dcol2: ', len(data_column2))
        #        list_of_list_column1.append(data_column1)   
        #        list_of_list_column2.append(data_column2)    
            list_of_list_columns.append([data_column1,data_column2])     
        i=i+1    

############################################################################################################
list_of_dataframes=[]
for edge in list_of_list_columns:
    edge_arr=np.asarray(edge,np.object) 
    #print('edges: \n',edge_arr)
    edges=pd.DataFrame({"source": edge_arr[0], "target": edge_arr[1]})
    list_of_dataframes.append(edges)


node_ids_features_array_list=[]
for i in range(len(list_of_node_ids)):
     
        node_id_array=np.array([list_of_node_ids[i]],dtype=np.object)
        #print('model array dimensions: ',node_ids_features_array)
        
        for dim in range(len(list_of_model_vectors[i])):
            #print('dim: ',dim)
            node_ids_features_array=np.array(list_of_model_vectors[i][dim],dtype=np.object)
            node_ids_features_array_list.append(node_ids_features_array)
        
        modelnode_feats=np.array([node_ids_features_array_list[i]],dtype=np.object)
        #print('model array info:    ',modelnode_feats.shape,'    ',node_id_array.shape)  
        
        model_node_array=IndexedArray(modelnode_feats,index=node_id_array) 
                 
        bug_node=np.array([["1"]],dtype=np.object)
        bug_node_features_array=np.array([list_of_bug_vectors[i]],dtype=np.object)
        print('bug array info:    ',bug_node_features_array.shape,'    ', bug_node.shape) 
        bug_node_array=IndexedArray(bug_node_features_array,index=bug_node) 
        
        print('*******************************************************************************')
        
        Gs1 = StellarGraph({"model nodes":model_node_array})
        Gs2 = StellarGraph({"bug nodes":bug_node_array})
        print(Gs1.info())
        #      print('--------------------------------------------------')
        print(Gs2.info())
#      #print('new dims info:    ','    (',len(list_of_node_ids),',',len(list_of_node_ids[0]),')', '    (',len(list_of_model_vectors),',',len(list_of_model_vectors[0]),')') 
     
#      Gs3 = StellarGraph({"model new nodes":model_node_array_new}) 
#      print('--------------------------------------------------')
#      print(Gs3.info())
#      list_of_graphs.append(Gs3)

#        node_id_array_new=np.array(list_of_node_ids[i],dtype=np.object)
#        modelnode_feats_new=np.array(list_of_model_vectors[i],dtype=np.object)
#        model_node_array_new=IndexedArray(modelnode_feats_new,index=node_id_array_new)   
#      edges=list_of_dataframes[i]
#      print('model array info:    ',modelnode_feats.shape,'    ',node_id_array.shape, '    ',edges.shape,'\n')  
#      Gs4=StellarGraph({"model nodes":model_node_array_new})
#      print(Gs4.info(),'\n')
############################################################################################################

