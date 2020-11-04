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
                        
    print('length of list: ', len(list), 'length of set: ',len(iterated_items))
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

dictionary_list1,vocabulary_size1= generate_dictinoary(text1)
print("vocabulary_size1: ", vocabulary_size1)
    
with open("dictionary_file_corpus.txt",'w') as f:
    for keys,values in dictionary_list1.items():
        f.write(str(values)+'\t'+keys)
                    
i=1
list_of_vectors=[]

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
        num_vector=[]
         
        for line in dictionary_list1.keys():
            if line in dictionary_list2:
                encoded_l[j]=1
                if line.find("# LOCATION")==-1:
                  num_vector.append('{'+str(encoded_l[j])+' '+"True"+'}')   
                else:
                  num_vector.append('{'+str(encoded_l[j])+' '+"False"+'}')  
            else:
                encoded_l[j]=0
                num_vector.append(str(encoded_l[j]))
           
            j+=1

        

        encoded_file_name="encoded_file_"+str(i)+".txt"  
        np.savetxt(encoded_file_name,encoded_l, fmt="%d")
                
               
        dictionary_file_name="dictionary_file_"+str(i)+".txt"        
        with open(dictionary_file_name,'w') as f:
            for keys,values in dictionary_list2.items():
                f.write(str(values)+'\t'+keys) 
        
        #sublist_vector= num_vector + location_vector
        #sublist_vector.append(num_vector)
        list_of_vectors.append(num_vector)        
    i=i+1

# print('length of list of vectors: ', len(list_of_vectors), '  length of sublist vector: ', len(sublist_vector))

with open('contents_file.txt','w') as f:
    
    for list_item1 in  list_of_vectors:
        count=0
        for list_item2 in num_vector:
            
            str_l=str(list_item2)+"    "
            f.write(str_l)
            
