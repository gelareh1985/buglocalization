'''
Created on Nov 5, 2020

@author: Gelareh_mp
'''
import os
import numpy as np
import re
from nltk.tokenize import RegexpTokenizer
from collections import OrderedDict

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
   
def create_unique_set(list):   
    iterated_items = []
    #list=filter(None, list)
    for i,line in enumerate(list): 
        #print(value)
        info = line.strip().split('\t')
        #if info in iterated_items:
        if(len(info)==3):
           l2=info[0]+'\t'+info[1]+'\t'+info[2]+'\n'
           if l2 not in iterated_items:
               iterated_items.append(l2)
        elif(len(info)==2): 
           l1=info[0]+'\t'+info[1]+'\n'
           if l1 not in iterated_items:
               iterated_items.append(l1)  
                        
    print('length of list: ', len(list), 'length of unique list: ',len(iterated_items))
    return iterated_items

def save_file(save_path,list):
    with open(save_path,"w") as f:
         for line in list:
             f.write(line)                                  
    f.close() 

def generate_dictinoary(text):
    #row_to_index=OrderedDict({index:i for i, index in enumerate(text)})
    count=0
    vocab_size=0
    row_to_index=OrderedDict()
    
    for index in text:
        row_to_index.update({index : count})  
        count=count+1                   
    vocab_size=len(row_to_index)  
    return row_to_index,vocab_size       

#---------------------------------------------------------------
            
text1=[]
for filename in os.listdir(p3):
    filepath=os.path.join(p3,filename)
    
    if filename.endswith(".nodelist"):
       table=load_table(filepath)
       for line in table:
       #if(line[0]!=""):
            tokenizer=RegexpTokenizer('[A-Za-z]+')
            #print(line)
            st=tokenizer.tokenize(line[0].lower())
            strln=" ".join(st)
            l1=strln+'\t'+line[2]+'\n'
            if(len(line)==6):
               l2=strln+'\t'+line[2]+'\t'+line[4]+'\n'
               text1.append(l2)
            else: 
               text1.append(l1)  
                

#print("length of all text: ",len(text1))
save_file("data_table.txt",text1)

list1=create_unique_set(text1)  
save_file("list1.txt",list1)

dictionary_list1,vocabulary_size1= generate_dictinoary(list1)
print("vocabulary_size1: ", vocabulary_size1)
    
  
with open("dictionary_file_corpus.txt",'w') as f:
    for keys,values in dictionary_list1.items():
        #print(str(values)+'\t'+keys)
        f.write(str(values)+'\t'+keys)
                    
i=1
list_of_vectors=[]
data_vector=[]
location_vec=[] 
for filename in os.listdir(p4):
    filepath=os.path.join(p4,filename)
    
    if filename.endswith(".nodelist"):
        table2=load_table(filepath)
        
        text2=[]
        for line in table2:
        #if(line[0]!=""):
            tokenizer=RegexpTokenizer('[A-Za-z]+')
            st=tokenizer.tokenize(line[0].lower())
            strln=" ".join(st)
            l1=strln+'\t'+line[2]+'\n'
            if(len(line)==6):
               l2=strln+'\t'+line[2]+'\t'+line[4]+'\n'
               text2.append(l2)
            else: 
               text2.append(l1)  
                      
        list2=create_unique_set(text2)
        print('length of file '+str(i)+': '+str(len(list2)))
        
        train_file_name="train_file_"+str(i)+".txt"
        save_file(train_file_name,text2)
        
        dictionary_list2,vocabulary_size2= generate_dictinoary(list2)
        print("vocabulary_size2: ", vocabulary_size2)
        
        encoded_l=np.zeros(vocabulary_size1,dtype=np.uint8)

        j=0
        
        for key,value in dictionary_list1.items():
            if key in dictionary_list2:
                encoded_l[j]=1
                
                data_type_str=['BugReportNode', 'Model', 'Operation', 'Class','Package', 'Property','Parameter' ]
                str_loc="# LOCATION"
                for type_str in data_type_str:
                
                    if (str_loc in key) and ( type_str in key) :
                        data_vector.append(' {'+str(encoded_l[j])+' '+str(value)+' '+"True"+'} '+type_str)  
                        location_vec.append(' {'+str(encoded_l[j])+' '+str(value)+' '+"True"+'} ') 
                    else:
                        data_vector.append(' {'+str(encoded_l[j])+' '+str(value)+' '+"False"+'} '+type_str) 
                        location_vec.append(' {'+str(encoded_l[j])+' '+str(value)+' '+"False"+'} ')  
                    
                   
            else:
                encoded_l[j]=0
                data_vector.append(str(encoded_l[j]))

            j+=1

        

        encoded_file_name="encoded_file_"+str(i)+".txt"  
        np.savetxt(encoded_file_name,encoded_l, fmt="%d")
                
               
        dictionary_file_name="dictionary_file_"+str(i)+".txt"        
        with open(dictionary_file_name,'w') as f:
            for keys,values in dictionary_list2.items():
                f.write(str(values)+'\t'+keys) 
        
        list_of_vectors.append(data_vector)        
    i=i+1

with open('data_vector_file.txt','w') as f:
    for line in data_vector:
        f.write(line)

with open('location_vector_file.txt','w') as f:
    for line in location_vec:
        f.write(line)
    
print('length of list of vectors: ', len(list_of_vectors), '  length of location vector: ', len(location_vec))

# for itm in location_vec:
#     print(itm+'\t')

with open('contents_file.txt','w') as f:
    str_l=[]
    for list_item1 in  list_of_vectors:
        for list_item2 in data_vector:
            
            str_l.append(str(list_item2)+"\t")
            #print(str_l)       
     
    for line in str_l:
        line='[ '+line + ' ]'
        f.write(line)
            
