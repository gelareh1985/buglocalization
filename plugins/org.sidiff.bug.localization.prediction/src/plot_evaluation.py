'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
import matplotlib.pyplot as plt


plot_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/"


def load_evaluation_results(path): 
    data_tables=[]
    table_names=[]
    for filename in os.listdir(path):
        
        if filename.endswith("_data.csv"):
            tbl_filename = filename[:filename.rfind(".")]
            tbl_path = path + tbl_filename + ".csv"
            tbl__data=pd.read_csv(tbl_path,sep=";",header=0)
            data_tables.append(tbl__data)
            table_names.append(filename)
            
    return data_tables

tables=load_evaluation_results(plot_data_path)
for table in tables:
    display(table)
    print(table["to_keep"].quantile([0.25,0.5,0.75]))
    table["to_keep"].plot(kind='box',title='tables to keep distribution', figsize=(10,8))
    #plt.show()
    data=[table["to_keep"],table["to_dropout"]]
    
    fig,ax=plt.subplots()
    ax.set_title('Multiple Samples with Different sizes')
    ax.boxplot(data)
    plt.show()
    
    fig = plt.figure(figsize =(10, 7)) 
    # Creating axes instance 
    plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
    plt.show()

    