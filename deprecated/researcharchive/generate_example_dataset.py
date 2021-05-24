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
output_folder='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_metatyps\\evaluation\\initial outputs\\'
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
        for table_row in table_data:
            for row in table_row:
                for column in row:
                    f.write(str(column)+'\t')  
                f.write('\n')
            
def save_dictionary(dictionary_file_name,dictionary):
    with open(dictionary_file_name+".txt",'w') as f:
        for keys,values in dictionary.items():
            f.write(str(values)+'\t'+str(keys)+"\n")    


def generate_dictinoary_of_all_words(table,stop_words,column_number,word_to_index,index_to_word):
    count =len(word_to_index)
    
    for table_row in table:
        for row in table_row:
            words=get_words(row[column_number])
            for word in words:
                if len(word) > 1 and word not in stop_words:
                    if word_to_index.get(word) == None:
                        word_to_index.update ( {word : count})
                        index_to_word.update ( {count : word })
                        count  += 1
    return word_to_index

def generate_dictinoary_of_words(table,stop_words,column_number,word_to_index,index_to_word):
    count =len(word_to_index)
   
    for row in table:
        words=get_words(row[column_number])
        for word in words:
            if len(word) > 1 and word not in stop_words:
                if word_to_index.get(word) == None:
                    word_to_index.update ( {word : count})
                    index_to_word.update ( {count : word })
                    count  += 1
    return  word_to_index              
                       
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
    
def generate_list_of_data(table,column_number,number_of_files):    
    list_of_data=[]
    if (number_of_files>1):
        for table_row in table:
            for row in table_row:
                list_of_data.append(row[column_number])
    elif(number_of_files==1):
        for table_row in table:
            list_of_data.append(table_row)
                        
    return  list_of_data   

def save_list_of_data(filename, list_of_data):   
    with open(filename+".txt",'w') as f:    
        for row in list_of_data:
            f.write(str(row)+'\n')
#---------------------------------------------------------------
all_word_to_index=dict()
all_index_to_word=dict()
word_to_index=dict()
index_to_word=dict()

stop_words = set(stopwords.words('english'))

all_files_table=[]
for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    number_of_files=len(os.listdir(p3))
    if filename.endswith(".nodelist"):
        table=load_table(filepath)
        print('length of all files table: ', len(table))
        all_files_table.append(table)
        
        
print('length of all files table: ', len(all_files_table))
save_table(output_folder+'_test.txt',all_files_table)        

all_nodes=generate_list_of_data(all_files_table,1,number_of_files)          
all_meta_types=generate_list_of_data(all_files_table,2,number_of_files)          

list_data_name=output_folder+"list_of_" 
save_list_of_data(list_data_name+"nodes",all_nodes)  
save_list_of_data(list_data_name+"meta_types",all_meta_types) 
 
nodelist_dictionary_of_all_words=generate_dictinoary_of_all_words(all_files_table,stop_words,1,all_word_to_index,all_index_to_word)
      
dictionary_file_name=output_folder+"dictionary_all_files_"      
save_dictionary(dictionary_file_name+"word_to_index",all_word_to_index)
save_dictionary(dictionary_file_name+"index_to_word",all_index_to_word)

i=0  
j=0 
list_of_all_data=[]
for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    
    if filename.endswith(".nodelist"):
        table=load_table(filepath)
        nodelist_dictionary_of_words=generate_dictinoary_of_words(table,stop_words,1,word_to_index,index_to_word)  
        print('length of nodelist dictionary of words: ', len(nodelist_dictionary_of_words))
    
        nodes=generate_list_of_data(table,1,1)          
        meta_types=generate_list_of_data(table,2,1)    
        
        bug_report_vector=np.zeros(len(nodelist_dictionary_of_all_words))
        model_vector=np.zeros(len(nodelist_dictionary_of_all_words))
        #model_type_vector=np.zeros(len(nodelist_dictionary_of_words))
        generate_one_hot_vector(table,0,1,1,nodelist_dictionary_of_all_words,bug_report_vector)
        generate_one_hot_vector(table,1,len(table),1,nodelist_dictionary_of_all_words,model_vector) 
        #generate_one_hot_vector(table,1,len(table),1,meta_type_to_index,model_type_vector)
        print('bug report vector: ', bug_report_vector)
        print('model vector: ', model_vector)
        #print('model type vector: ', model_type_vector)
        encoded_file_name=output_folder+"bug_report_vector_encoded_file_"+str(i)+".txt" 
        save_data_vector(bug_report_vector,encoded_file_name)
        encoded_file_name=output_folder+"model_vector_encoded_file_"+str(i)+".txt"   
        save_data_vector(model_vector,encoded_file_name)
       
    list_of_vectors=[str(j),bug_report_vector,model_vector,nodes,meta_types]
    list_of_all_data.append(list)
    i=i+1    
    j=j+1    
  
