import os
import numpy as np
import re
from nltk.tokenize import RegexpTokenizer
from collections import OrderedDict
from word_to_vec import generate_dictinoary_data

print("Hello World!")

global table_data
global p1
p1='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\evaluation\\'
p2='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\training\\'

#---------------------------------------------------------------

def load_table(filepath):
    table_data=[]
    with open(filepath) as f:
        for i, line in enumerate(f):
            info = line.strip().split('\t')
            info=info[1].replace('"',"")
            table_data.append(info)
            
    return table_data
   
def create_unique_set(list):   
    iterated_items = set()
    list=filter(None, list)
    for value in list: 
        
        if value not in iterated_items and value.isspace()==False: 
            iterated_items.add(value)
                   
    #print('length of set: ',len(iterated_items))
    return iterated_items

def save_file(save_path,list):
    with open(save_path,"w") as f:
         for line in list:
             f.write(line)                                  
    f.close() 

def generate_dictinoary(text):
    row_to_index=OrderedDict({index:i for i, index in enumerate(text)})
    vocab_size=len(row_to_index)  
    return row_to_index,vocab_size       

#---------------------------------------------------------------
            
text1=[]
for filename in os.listdir(p1):
    filepath=os.path.join(p1,filename)
    
    if filename.endswith(".nodelist"):
       table=load_table(filepath)
       for line in table:
            tokenizer=RegexpTokenizer('[A-Za-z]+')
            st=tokenizer.tokenize(line)
            strln=" ".join(st)+'\n'
            text1.append(strln)
            
print("length of all text: ",len(text1))
save_file("data_table.txt",text1)

list1=create_unique_set(text1)  
save_file("list1.txt",list1)

dictionary_list1,vocabulary_size1= generate_dictinoary(list1)
print("vocabulary_size1: ", vocabulary_size1)
    
with open("dictionary_file_corpus.txt",'w') as f:
    for keys,values in dictionary_list1.items():
        f.write(str(values)+'\t'+keys)
                    
i=1
list_of_vectors=[]
for filename in os.listdir(p2):
    
    filepath=os.path.join(p2,filename)
    if filename.endswith(".nodelist"):
        table2=load_table(filepath)
        text2=[]
        for line in table2:
            
            tokenizer=RegexpTokenizer('[A-Za-z]+')
            st=tokenizer.tokenize(line)
            strln=" ".join(st)+'\n'
            text2.append(strln)
   
        list2=create_unique_set(text2)
        print('length of file '+str(i)+': '+str(len(list2)))
        
        train_file_name="train_file_"+str(i)+".txt"
        save_file(train_file_name,list2)
        
        dictionary_list2,vocabulary_size2= generate_dictinoary(list2)
        print("vocabulary_size2: ", vocabulary_size2)
        
        encoded_l=np.zeros(vocabulary_size1,dtype=np.uint8)

        j=0
        sublist_vector=[]
        for line in dictionary_list1.keys():
            if line in dictionary_list2:
                encoded_l[j]=1
                sublist_vector.append(str(encoded_l[j]))
            else:
                encoded_l[j]=0
                sublist_vector.append(str(encoded_l[j]))
            j+=1
        list_of_vectors.append(sublist_vector)    

        encoded_file_name="encoded_file_"+str(i)+".txt"  
        np.savetxt(encoded_file_name,encoded_l, fmt="%d")
                
        dictionary_file_name="dictionary_file_"+str(i)+".txt"        
        with open(dictionary_file_name,'w') as f:
            for keys,values in dictionary_list2.items():
                f.write(str(values)+'\t'+keys) 
                
    i=i+1

print('length of list of vectors: ', len(list_of_vectors), '  length of sublist vector: ', len(sublist_vector))

with open('list_of_lists.txt','w') as f:
    for list_item1 in  list_of_vectors:
        for list_item2 in sublist_vector:
         f.write(list_item2+' ')

