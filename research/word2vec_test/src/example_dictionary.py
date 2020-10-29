import os
import numpy as np
from word_to_vec import generate_dictinoary_data
from docutils.utils.math.math2html import file


print("Hello World!")

global p
p='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\evaluation\\'
#path=r'D:\files_MDEAI_original\Datasets\DataSet_20200925144220_with_tabs\DataSet_20200925144220\evaluation\all_table_data.txt'
global temp_file
global table_data
global strline
global text
#global filename
#global filepath

file_with_unique_lines="uniqueset.txt"

def load_table(filepath):
    table_data=[]
    with open(filepath) as f:
         #print(filename)
         #table_data.append(content)
         #print(content)
        for i, line in enumerate(f):
            info = line.strip().split('\t')
            table_data.append(info)
            #print(line)
            
    return table_data
                                  

temp_file="table_of_string.txt"
text=[]
for filename in os.listdir(p):
    filepath=os.path.join(p,filename)
    if filename.endswith(".nodelist"):
       
       with open(temp_file,"a")as f: 
           table=load_table(filepath)
           for line in table:
               #print('table lines: \t',line[0],'\t', line[1])
               strline='{}{}\n'.format(line[0],line[1])
               #print(strline)
               text.append(strline)
               f.write(strline)
            #print("Finished collecting data from all files!")        
            #return text 

print("length of all text: ",len(text))

        
iterated_items = set()
for value in text: 
    if value not in iterated_items: 
        if len(value)!=0: 
            iterated_items.add(value)
               
print('length of set: ',len(iterated_items))
#   
# with open(file_with_unique_lines,"a") as f:
#       #print(txtset)
#       for l in iterated_items: 
#           f.write(l)
#                 