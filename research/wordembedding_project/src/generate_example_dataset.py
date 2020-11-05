'''
Created on Nov 5, 2020

@author: Gelareh_mp
'''
import os
import numpy as np
from nltk.tokenize import RegexpTokenizer

print("Hello World!")

global table_data
global p1
global p3
#p1='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\evaluation\\'
#p2='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\training\\'
p3='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\evaluation\\'
p4='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\training\\'
#---------------------------------------------------------------

def load_table(filepath):
    table_data=[]
    #metatype_data=[]
    with open(filepath) as f:
        for i, line in enumerate(f):
            info = line.strip().split('\t')
           
            info1=info[1].replace('"',"")
            info2=info[2].replace('"',"") 
            
            if (len(info)==4):
                info3=info[3]
                arr_line=[info1,'\t', info2,'\t', info3, '\n']
                table_data.append(arr_line)
            elif(len(info)==3):
                arr_line=[info1,'\t', info2,'\n'] 
                table_data.append(arr_line)
               
    return table_data
   
                        

def save_dictionary(dictionary_file_name,dictionary):
    with open(dictionary_file_name+".txt",'w') as f:
        for keys,values in dictionary.items():
            f.write(str(values)+'\t'+str(keys)+"\n")    


def generate_dictinoary_data(table,column_number,word_to_index,index_to_word):
    count =len(word_to_index)
    
    for row in table:
        tokenizer=RegexpTokenizer('[A-Za-z]+')
        words=tokenizer.tokenize(row[column_number])
        for word in words:
            word = word.lower()
            if word_to_index.get(word) == None:
                word_to_index.update ( {word : count})
                index_to_word.update ( {count : word })
                count  += 1

def generate_one_hot_vector(table,row_number,column_number,dictionary,vector,offset):   
    index=offset
    for row_index in range(row_number,len(table)):
        row=table[row_index]
        tokenizer=RegexpTokenizer('[A-Za-z]+')
        words=tokenizer.tokenize(row[column_number])
        for word in words:
            word = word.lower()
            word_index=dictionary.get(word)+offset
            vector[word_index]=1

#---------------------------------------------------------------
word_to_index=dict()
index_to_word=dict()
meta_type_to_index=dict()
index_to_meta_type=dict()

for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    if filename.endswith(".nodelist"):
        table=load_table(filepath)
       
        generate_dictinoary_data(table,1,word_to_index,index_to_word)
        generate_dictinoary_data(table,2,meta_type_to_index,index_to_meta_type)  
   
dictionary_file_name="dictionary_file_"        
save_dictionary(dictionary_file_name+"word_to_index",word_to_index)
save_dictionary(dictionary_file_name+"index_to_word",index_to_word)
save_dictionary(dictionary_file_name+"meta_type_to_index",meta_type_to_index) 
save_dictionary(dictionary_file_name+"index_to_meta_type",index_to_meta_type)           
   
for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    
    if filename.endswith(".nodelist"):
       
        table=load_table(filepath)
        vector=np.zeros(len(word_to_index)+len(meta_type_to_index),dtype=np.uint8)
        generate_one_hot_vector(table,1,1,word_to_index,vector,0)
        generate_one_hot_vector(table,1,2,meta_type_to_index,vector,len(word_to_index))  
           
