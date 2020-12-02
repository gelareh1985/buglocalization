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

class MDEdataset():
    def __init__(self,filepath,stop_words,output_filepath,number_of_files,file_name):
        super(MDEdataset, self).__init__()
        
        self.filepath=filepath
        self.stop_words=stop_words
        self.output_filepath=output_filepath
        self.number_of_files=number_of_files
        self.file_name=file_name
     
    def get_output_filepath(self):
        return self.output_filepath

    def set_output_filepath(self, value):
        self.output_filepath = value

    def get_number_of_files(self):
        return self.number_of_files

    def set_number_of_files(self, value):
        self.number_of_files = value
        
    def gte_file_name(self):
        return self.file_name

    def set_file_name(self, value):
        self.file_name = value
     
    def load_edge_table(self):
        data_column1=[]
        data_column2=[]
        #list=[]
        with open(self.filepath) as f:
            for j,line in enumerate(f):
                info = line.strip().split('\t')
                #info=[info[0],info[1]]
                data_column1.append(str(info[0]))
                data_column2.append(str(info[1]))
                #info2=[info[0],info[1]]
                #list.append(info2)
        return  data_column1,data_column2

        
    def load_table(self):
        table_data=[]
        with open(self.filepath) as f:
            for j,line in enumerate(f):
                info = line.strip().split('\t')
                for i in range(0,len(info)):
                    if(info[i][0]=='"' and info[i][-1]=='"'):
                        info[i]=info[i][1:-1]
                table_data.append(info) 
        return table_data
       
    def save_table(self, fname,table_data):
        with open(fname,'w') as f:
            for table_row in table_data:
                for row in table_row:
                    for column in row:
                        f.write(str(column)+'\t')  
                    f.write('\n')
                
    def save_dictionary(self,fname,dictionary):
        with open(fname,'w') as f:
            for keys,values in dictionary.items():
                f.write(str(values)+'\t'+str(keys)+"\n")    
                
    def generate_dictinoary_of_all_words(self,table,column_number,all_word_to_index,all_index_to_word):
        count =len(all_word_to_index)
        #print('stop words: ',self.stop_words)
        for table_row in table:
            for row in table_row:
                words=self.get_words(row[column_number])
                for word in words:
                    if len(word) > 1 and word not in self.stop_words:
                        if all_word_to_index.get(word) == None:
                            all_word_to_index.update ( {word : count})
                            all_index_to_word.update ( {count : word })
                            count  += 1
        return all_word_to_index
    
    def generate_dictinoary_of_words(self,table,column_number,word_to_index,index_to_word):
        count =len(word_to_index)
        #print('stop words: ',self.stop_words)
        for row in table:
            words=self.get_words(row[column_number])
            for word in words:
                if len(word) > 1 and word not in self.stop_words:
                    if word_to_index.get(word) == None:
                        word_to_index.update ( {word : count})
                        index_to_word.update ( {count : word })
                        count  += 1
        return  word_to_index              
                           
    def generate_one_hot_vector(self,table,row_start,row_end,column_number,dictionary,vector):   
        for row_index in range(row_start,row_end):
            if row_index <len(table):
                row=table[row_index]
                words=self.get_words(row[column_number])
                
                for word in words:
                    if word in dictionary:
                        word_index=dictionary.get(word)
                        vector[word_index]=1
        return vector
                    
    def get_words(self,text):
        words_array=[]
        tokenizer=RegexpTokenizer('[A-Za-z]+')
        words=tokenizer.tokenize(text)
        for word in words:
            splitted = re.sub('([A-Z][a-z]+)', r' \1', re.sub('([A-Z]+)', r' \1', word)).split()
            for split_word in splitted:
                split_word=split_word.lower()
                words_array.append(split_word)
        return words_array 
    
    def save_data_vector(self,vector,fname):  
        np.savetxt(fname,vector, fmt="%d")     
        
    def generate_list_of_data(self,table,column_number,num_of_files):    
        list_of_data=[]
        if (self.number_of_files>1):
            for table_row in table:
                for row in table_row:
                    list_of_data.append(row[column_number])
        elif(self.number_of_files==1):
            for table_row in table:
                list_of_data.append(table_row[column_number])
                            
        return  list_of_data   
    
    def save_list_of_data(self,fname,list_of_data):
          
        with open(fname,'w') as f:    
            for row in list_of_data:
                f.write(str(row)+'\n')
            