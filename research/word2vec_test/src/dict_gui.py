'''
Created on Oct 23, 2020

@author: Gelareh_mp
'''
from tkinter import *
import tkinter as ttk
from tkinter import filedialog
from tkinter.messagebox import showinfo

import configparser

from create_dict import get_file_data
from create_dict import generate_dictinoary_data
from create_dict import save_file_data
from create_dict import stopword_effect

root = Tk()
#root.geometry("500x500")
root.title("Word2Vec Demo!")

p=" "
variable1 = StringVar()  # Value saved here
variable2 = StringVar()
text1=[] 
text2=[]
str_txt=[]
checker1=True
checker2=True

group_1 = ttk.LabelFrame(root, padx=15, pady=10,text="Dictionary of Corpus")
group_1.pack(padx=10, pady=5)

def myClick1():
    global checker1
    global text1
    if checker1==True and len(text1)==0:
        str_path=p.replace('/', "\\\\")
        print(str_path)
        if p!="":
            #get_file_data(str_path,'no')
            text1 = get_file_data(str_path) 
            text1=stopword_effect(text1,stop_word_removal = 'no')  
            word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text1)
    
        print("vocab size: ",vocab_size,"\n",
              "length of corpus: ",length_of_corpus,"\n",
              "Corpus: ", corpus, "\n")
        
        with open('dict_corpus_with_stopwords.txt', 'w') as f:
            for item in corpus:
                f.write("%s\n" % item)
      
        config = configparser.ConfigParser()  
        # Make each dictionary a separate section in the configuration
        config['dict1'] = word_to_index
        config['dict2'] = index_to_word
        
        # Save the configuration to a file
        f = open('config_with_stopwords.ini', 'w')
        config.write(f)
        f.close()
        
        # Read the configuration from a file
#         config2 = configparser.ConfigParser()
#         config2.read('config_with_stopwords.ini')
        
        # ConfigParser objects are a lot like dictionaries, but if you really
        # want a dictionary you can ask it to convert a section to a dictionary
#         dictA = dict(config2['dict1'] )
#         dictB = dict(config2['dict2'] )
        
#         print(dictA)
#         print(dictB)
        #print(text1)
        #checker=True
        return text1   
    else: 
        showinfo("Hint:", "Already Clicked!")   
    

def myClick2():
    global checker2
    global text2
    if checker2==True and len(text2)==0:
        str_path=p.replace('/', "\\\\")
        print(str_path)
        
        if p!="":
            #get_file_data(str_path,'yes')
            text2 = get_file_data(str_path) 
            text2=stopword_effect(text2,stop_word_removal = 'yes')   
            word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text2)
    
        print("vocab size: ",vocab_size,"\n",
              "length of corpus: ",length_of_corpus,"\n",
              "Corpus: ", corpus, "\n")
        
        with open('dict_corpus_with_stopwords_removed.txt', 'w') as f:
            for item in corpus:
                f.write("%s\n" % item)
 
        config = configparser.ConfigParser()  
        # Make each dictionary a separate section in the configuration
        config['dict1'] = word_to_index
        config['dict2'] = index_to_word
        
        # Save the configuration to a file
        f = open('config_stopwords_removed.ini', 'w')
        config.write(f)
        f.close()
        
        # Read the configuration from a file
#         config2 = configparser.ConfigParser()
#         config2.read('config_stopwords_removed.ini')
        
        # ConfigParser objects are a lot like dictionaries, but if you really
        # want a dictionary you can ask it to convert a section to a dictionary
#         dictA = dict(config2['dict1'] )
#         dictB = dict(config2['dict2'] )
#         
#         print(dictA)
#         print(dictB)

        
        #print(text2)
        #checker=True 
        return text2
    else: 
        showinfo("Hint:", "Already Clicked!")   
#variable1=text1
#variable2=text2
#text1= variable1.get() 
#text2= variable2.get()  

def myClick3():
    global checker1
    if checker1==True:
        print(text1)
        save_file_data(text1, stop_word_removal = 'no')
        checker1=False 
    elif checker1==False:
        showinfo("Hint:", "Data exists!")
        
def myClick4():
    global checker2
    if checker2==True:
        print(text2)
        save_file_data(text2, stop_word_removal = 'yes')
        checker2=False 
    elif checker2==False:
        showinfo("Hint:", "Data exists!")
                
def fileDialog():
    global p
    filename = filedialog.askopenfilename(initialdir =  "/", title = "Select A File", filetype =
    (("txt files","*.txt"),("all files","*.*")) )
    label = ttk.Label(group_1, text = "")
    label.grid(column = 1, row = 2)
    label.configure(text = filename)
    p=filename
    return p

ttk.Button(group_1, text = "Browse a File: ", command=fileDialog).grid(row=0, column=0, sticky=W)
ttk.btn1=Button(group_1, text = "Data with stopwords",command=myClick1).grid(row=0, column=2, sticky=E)
ttk.btn2=Button(group_1, text = "Data without stopwords",command=myClick2).grid(row=2, column=2, sticky=E)
ttk.btn3=Button(group_1, text = "Save Data",command=myClick3).grid(row=0, column=3, sticky=E)
ttk.btn4=Button(group_1, text = "Save Data",command=myClick4).grid(row=2, column=3, sticky=E)

root.mainloop() 
