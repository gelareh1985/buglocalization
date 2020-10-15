'''
Created on Sep 8, 2020

@author: Gelareh_mp
'''
from tkinter import *
import tkinter as ttk
#import Tkinter as tk
#from tkinter import ttk
from tkinter import filedialog
from matplotlib.backends.backend_tkagg import (FigureCanvasTkAgg,NavigationToolbar2Tk)
from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas
from matplotlib.backend_bases import key_press_handler
from matplotlib.figure import Figure
import io
import re
from PIL import ImageTk, Image

import numpy as np
import matplotlib.pyplot as plt

import analysis_small_corpus as analys1
import analysis_big_corpus as analys2

root = Tk()
#root.geometry("500x500")
root.title("Word2Vec Demo!")

p=" "

variable1 = StringVar()  # Value saved here
 
text=[]

group_1 = ttk.LabelFrame(root, padx=15, pady=10,text="Analysis Corpus Example")
group_1.pack(padx=10, pady=5)

 
ttk.Label(group_1, text="Insert Text: ").grid(row=0)
e=ttk.Entry(group_1,textvariable=variable1).grid(row=0, column=1, sticky=W)
  
def myClick1():
      
    if len(variable1.get())!=0:
        text.append(variable1.get())
        print(text)
        analys1.sketch_analysis1(text)
        #text.remove(variable1.get())
        #e.delete(0, END)
        
def myClick2():
      
    if len(variable1.get())!=0:
        text.append(variable1.get())
        print(text)
        analys1.sketch_analysis2(text)
        #text.remove(variable1.get())
        #e.delete(0, END)

        
def myClick3():
      
    if len(variable1.get())!=0:
        text.append(variable1.get())
        print(text)
        analys1.sketch_analysis3(text)
        
def myClick_print():
      
    if len(variable1.get())!=0:
        text.append(variable1.get())
        print(text)
        analys1.show_trained_data(text)


ttk.btn1=Button(group_1, text = "Dim Plot and Loss Plot",command=myClick1).grid(row=0, column=2, sticky=E)
ttk.btn3=Button(group_1, text = "Win Size Plot and Loss Plot",command=myClick2).grid(row=2, column=2, sticky=E)
ttk.btn5=Button(group_1, text = "Epoch Plot and Loss Plot",command=myClick3).grid(row=3, column=2, sticky=E)
ttk.btn6=Button(group_1, text = "Trained Data Example",command=myClick_print).grid(row=1, column=1, sticky=E)


group_2 = ttk.LabelFrame(root, padx=15, pady=10,text="Analysis Corpus Example")
group_2.pack(padx=10, pady=5)


   
def myClick4():
    str_path=p.replace('/', "\\\\")
    print(str_path)
    if p!="":
        analys2.sketch_analysis_B1(str_path)
        
def myClick5():
    str_path=p.replace('/', "\\\\")
    print(str_path)
    if p!="":
        analys2.sketch_analysis_B2(str_path)

        
def myClick6():
    str_path=p.replace('/', "\\\\")
    print(str_path)
    if p!="":
        analys2.sketch_analysis_B3(str_path)

def myClick7():
    str_path=p.replace('/', "\\\\")
    print(str_path)
    if p!="":
        analys2.sketch_analysis_B4(str_path)


def myClick8():
    str_path=p.replace('/', "\\\\")
    print(str_path)
    if p!="":
        analys2.sketch_analysis_B5(str_path)


def fileDialog():
    global p
    filename = filedialog.askopenfilename(initialdir =  "/", title = "Select A File", filetype =
    (("txt files","*.txt"),("all files","*.*")) )
    label = ttk.Label(group_2, text = "")
    label.grid(column = 1, row = 2)
    label.configure(text = filename)
    p=filename
    return p

    
ttk.Button(group_2, text = "Browse a File: ", command=fileDialog).grid(row=0, column=0, sticky=W)
ttk.btn1=Button(group_2, text = "With Stop word and Loss Plot",command=myClick4).grid(row=0, column=2, sticky=E)
ttk.btn2=Button(group_2, text = "Without Stop word and Loss Plot",command=myClick5).grid(row=2, column=2, sticky=E)
ttk.btn3=Button(group_2, text = "Dim Plot and Loss Plot",command=myClick6).grid(row=3, column=2, sticky=E)
ttk.btn4=Button(group_2, text = "Win Size Plot and Loss Plot",command=myClick7).grid(row=4, column=2, sticky=E)
ttk.btn5=Button(group_2, text = "Final Experiment",command=myClick8).grid(row=5, column=2, sticky=E)

root.mainloop()      


