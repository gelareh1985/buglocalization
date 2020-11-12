'''
Created on Nov 5, 2020

@author: Gelareh_mp
'''
import os
import numpy as np
from nltk.tokenize import RegexpTokenizer
from nltk.corpus import stopwords
import re

print("Hello World!")


#p1='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\evaluation\\'
#p2='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\training\\'
p3='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\evaluation\\'
p4='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\training\\'
output_folder='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\evaluation\\set_output2\\'
#---------------------------------------------------------------

def load_table(filepath):
    table_data=[]
    with open(filepath) as f:
        for j,line in enumerate(f):
            info = line.strip().split('\t')
            for i in range(0,len(info)):
                if(info[i][0]=='"' and info[i][-1]=='"'):
                    info[i]=info[i][1:-1]
            table_data.append(info) 
    return table_data
   
def save_table(filepath, table_data):
    with open(filepath,'w') as f:
        for row in table_data:
            for column in row:
                f.write(column+'\t')  
            f.write('\n')
            
def save_dictionary(dictionary_file_name,dictionary):
    with open(dictionary_file_name+".txt",'w') as f:
        for keys,values in dictionary.items():
            f.write(str(values)+'\t'+str(keys)+"\n")    


def generate_dictinoary_data(table,stop_words,column_number,word_to_index,index_to_word):
    count =len(word_to_index)
    
    for row in table:
        words=get_words(row[column_number])
        for word in words:
            if len(word) > 1 and word not in stop_words:
                if word_to_index.get(word) == None:
                    word_to_index.update ( {word : count})
                    index_to_word.update ( {count : word })
                    count  += 1

def generate_one_hot_vector(table,row_start,row_end,column_number,dictionary,vector):   
    for row_index in range(row_start,row_end):
        row=table[row_index]
        words=get_words(row[column_number])
       
        for word in words:
            if word in dictionary:
                word_index=dictionary.get(word)
                
                vector[word_index]=1
def get_words(text):
    words_array=[]
    tokenizer=RegexpTokenizer('[A-Za-z]+')
    words=tokenizer.tokenize(text)
    for word in words:
        splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
        for split_word in splitted:
            split_word=split_word.lower()
            words_array.append(split_word)
    return words_array 

def save_data_vector(vector,file_name):  
    np.savetxt(file_name,vector, fmt="%d")     
    
#---------------------------------------------------------------
word_to_index=dict()
index_to_word=dict()
meta_type_to_index=dict()
index_to_meta_type=dict()

stop_words = set(stopwords.words('english'))

for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    if filename.endswith(".nodelist"):
        table=load_table(filepath)
        
        save_table(filepath+'_test',table)
        
        generate_dictinoary_data(table,stop_words,1,word_to_index,index_to_word)
        generate_dictinoary_data(table,stop_words,2,meta_type_to_index,index_to_meta_type)  
   
dictionary_file_name="dictionary_file_"        
save_dictionary(dictionary_file_name+"word_to_index",word_to_index)
save_dictionary(dictionary_file_name+"index_to_word",index_to_word)
save_dictionary(dictionary_file_name+"meta_type_to_index",meta_type_to_index) 
save_dictionary(dictionary_file_name+"index_to_meta_type",index_to_meta_type)           

i=0    
for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
     
    if filename.endswith(".nodelist"):
        
        table=load_table(filepath)
        bug_report_vector=np.zeros(len(word_to_index))
        model_vector=np.zeros(len(word_to_index))
        model_type_vector=np.zeros(len(meta_type_to_index))
        generate_one_hot_vector(table,0,0,1,word_to_index,bug_report_vector)
        generate_one_hot_vector(table,1,len(table),1,word_to_index,model_vector) 
        generate_one_hot_vector(table,1,len(table),1,meta_type_to_index,model_type_vector)
        print('bug report vector: ', bug_report_vector)
        print('model vector: ', model_vector)
        print('model type vector: ', model_type_vector)
#         break    
        encoded_file_name=output_folder+"bug_report_vector_encoded_file_"+str(i)+".txt" 
        save_data_vector(bug_report_vector,encoded_file_name)
        encoded_file_name=output_folder+"model_vector_encoded_file_"+str(i)+".txt"   
        save_data_vector(model_vector,encoded_file_name)
        encoded_file_name=output_folder+"model_type_vector_encoded_file_"+str(i)+".txt" 
        save_data_vector(model_type_vector,encoded_file_name)    
    i=i+1    
        