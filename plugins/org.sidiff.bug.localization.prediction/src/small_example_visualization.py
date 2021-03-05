'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
import matplotlib.pyplot as plt

tbl_shorted_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_shorted_data.csv"
tbl_90percent_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_full2_90percent_data.csv"
tbl_45percent_version1_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_full1_version1_45percent_data.csv"

# -----------------------------------------------------------------------------
table_shorted=pd.read_csv(tbl_shorted_data_path,sep=";",header=0)
display(table_shorted)

data=[table_shorted["to_keep"],table_shorted["to_dropout"]]
plt.figure(figsize =(10, 7)) 
# Creating axes instance 
plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
plt.show()
# -----------------------------------------------------------------------------
table_90percent=pd.read_csv(tbl_shorted_data_path,sep=";",header=0)
display(table_90percent)

data=[table_90percent["to_keep"],table_90percent["to_dropout"]]
plt.figure(figsize =(10, 7)) 
# Creating axes instance 
plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
plt.show()
# -----------------------------------------------------------------------------
table_45percent_version1=pd.read_csv(tbl_shorted_data_path,sep=";",header=0)
display(table_45percent_version1)

data=[table_45percent_version1["to_keep"],table_45percent_version1["to_dropout"]]
plt.figure(figsize =(10, 7)) 
# Creating axes instance 
plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
plt.show()
# -----------------------------------------------------------------------------