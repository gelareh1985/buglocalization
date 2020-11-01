import os
import numpy as np
import re
from nltk.tokenize import RegexpTokenizer
import pickle

from word_to_vec import generate_dictinoary_data

print("Hello World!")

global p
p='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\evaluation\\'
global table_data

#---------------------------------------------------------------

def load_table(filepath):
    table_data=[]
    with open(filepath) as f:
        for i, line in enumerate(f):
            info = line.strip().split('\t')
            table_data.append(info)
            #print(info)
            
    return table_data
   
def create_unique_set(list):   
    iterated_items = set()
    for value in list: 
        if value not in iterated_items and len(value)!=0: 
            iterated_items.add(value)
                   
    print('length of set: ',len(iterated_items))
    return iterated_items

def save_file(save_path,list):
    with open(save_path,"w") as f:
         for line in list:
             f.write(line)                                  

def generate_dictinoary(text):
    row_to_index=dict()
    count=0
    vocab_size=0
    for row in text:
        row=row.lower()
        if row_to_index.get(row)==None:
            row_to_index.update({row:count})
            count+=1
    vocab_size=len(row_to_index)
    return row_to_index, vocab_size        

#---------------------------------------------------------------
            
table_dt="data_table.txt"
text1=[]

for filename in os.listdir(p):
    filepath=os.path.join(p,filename)
    
    if filename.endswith(".nodelist"):
       table=load_table(filepath)
       
       for line in table:
           tokenizer=RegexpTokenizer('[A-Za-z]+')
           st=tokenizer.tokenize(line[1])

           strline=''
           for l in st:
               strline=strline+' '+l
           text1.append(strline+'\n')
               
 
print("length of all text: ",len(text1))
save_file(table_dt,text1)

txtfile1="list1.txt"
list1=create_unique_set(text1)  
save_file(txtfile1,list1)
 

row_to_index,vocab_size= generate_dictinoary(list1)

dict_file = open("data.pkl", "wb")
pickle.dump(row_to_index, dict_file) #save dictionary data
dict_file.close()

# dict_file = open("data.pkl", "rb")
# output = pickle.load(dict_file) # load dictionary data
# print(output)
 
file_sample='00002_bug_12000_version_2c9cb94dc84956e5c7f0db27e02f01d02c3e4f02.nodelist'
txtfile2="list2.txt"
text2=[]

table2=load_table(file_sample)
for line in table2:
    
    tokenizer=RegexpTokenizer('[A-Za-z]+')
    st=tokenizer.tokenize(line[1])

    strline=''
    for l in st:
        strline=strline+' '+l
    text2.append(strline+'\n')
   
list2=create_unique_set(text2)
save_file(txtfile2,list2)


              