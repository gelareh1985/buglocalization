'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

tbl_shorted_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_shorted_data.csv"
tbl_90percent_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_full_90percent_data.csv"
tbl_45percent_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_full_45percent_data.csv"
tbl_k2_directed_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_full_k2_directed_data.csv"
tbl_k2_undirected_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results/boxplot_full_k2_undirected_data.csv"

# -----------------------------------------------------------------------------
table_shorted1=pd.read_csv(tbl_45percent_data_path,sep=";",header=0)
#a = table_shorted.filter(like='to_keep').values.T.ravel()
#b = table_shorted.filter(like='to_dropout').values.T.ravel()
#df = pd.DataFrame({'to_keep':a, 'to_dropout':b}, columns=['to_keep','to_dropout'])

# ******** example of box_plot for one model ******************
df=table_shorted1[["to_keep","to_dropout"]].drop_duplicates()
display(df)
data=pd.concat([df["to_keep"],df["to_dropout"]],axis=1)
data.plot(kind='box',title='tables to keep distribution', figsize=(10,8))
plt.show()
# ******** example of box_plot for all models ******************
table_shorted2=pd.read_csv(tbl_90percent_data_path,sep=";",header=0)
table_shorted3=pd.read_csv(tbl_k2_directed_data_path,sep=";",header=0)
table_shorted4=pd.read_csv(tbl_k2_undirected_data_path,sep=";",header=0)

df1=table_shorted1[["to_keep","to_dropout"]].drop_duplicates()
df1=df1.rename(columns={"to_keep":"modelA1","to_dropout":"modelA2"})
display(df1)
df2=table_shorted2[["to_keep","to_dropout"]].drop_duplicates()
df2=df2.rename(columns={"to_keep":"modelB1","to_dropout":"modelB2"})
display(df2)
df3=table_shorted3[["to_keep","to_dropout"]].drop_duplicates()
df3=df3.rename(columns={"to_keep":"modelC1","to_dropout":"modelC2"})
display(df3)
df4=table_shorted4[["to_keep","to_dropout"]].drop_duplicates()
df4=df4.rename(columns={"to_keep":"modelD1","to_dropout":"modelD2"})
display(df4)

data=pd.concat([df1["modelA1"],df1["modelA2"],df2["modelB1"],df2["modelB2"],
               df3["modelC1"],df3["modelC2"],df4["modelD1"],df4["modelD2"]],axis=1)
data.fillna(0,inplace=True)
display(data)
 
color = dict(boxes='DarkGreen', whiskers='DarkOrange',
                       medians='DarkBlue', caps='Gray') 

data.plot(kind='box',color=color,title='tables to keep distribution', figsize=(10,8),sym='r+')

green_patch = mpatches.Patch(color='green', label='The green data')
blue_patch = mpatches.Patch(color='blue', label='The blue data')
red_patch = mpatches.Patch(color='red', label='The red data')
orange_patch=mpatches.Patch(color='orange', label='The orange data')
plt.legend(handles=[green_patch,blue_patch,red_patch,orange_patch])
plt.show()

# data['a'] = np.arange(12)+1
# data['b'] = np.arange(14)+1
# data['c'] = np.arange(8)+1
# 
# color_dict = {'trt_a':'orange', 'trt_b':'blue', 'trt_c':'green'}
# controls = ['trt_a', 'trt_b', 'trt_c']

# fake data
d0 = [[4.5, 5, 6, 4],[4.5, 5, 6, 4]]
d1 = [[1, 2, 3, 3.3],[1, 2, 3, 3.3]]

# basic plot
bp0 = plt.boxplot(d0, patch_artist=True)
bp1 = plt.boxplot(d1, patch_artist=True)

for box in bp0['boxes']:
    # change outline color
    box.set(color='red', linewidth=2)
    # change fill color
    #box.set(facecolor = 'green' )
    # change hatch
    box.set(hatch = '/')

for box in bp1['boxes']:
    box.set(color='blue', linewidth=5)
    #box.set(facecolor = 'red' )

plt.show()


#fig, (ax1, ax2) = plt.subplots(nrows=1, ncols=1, figsize=(10, 8))
fig,ax=plt.subplots(figsize=(10, 8))

a1=data["modelA1"].values.tolist()
a2=data["modelB1"].values.tolist()
a3=data["modelC1"].values.tolist()
a4=data["modelD1"].values.tolist()
data_plot1=[a1,a2,a3,a4]
print("data for box_plot: ",data_plot1)
labels=["modelA1","modelB1","modelC1","modelD1"]
#boxplt1 = plt.boxplot(data_plot1, labels=labels,patch_artist=True) 
boxplt1 = ax.boxplot(data_plot1, labels=labels,patch_artist=True) 

colors = ['blue', 'green', 'purple', 'tan'] 
for patch, color in zip(boxplt1['boxes'], colors):
    patch.set_facecolor(color)
#plt.figure(figsize =(10, 8))     
plt.show()      

fig,ax=plt.subplots(figsize=(10, 8))

b1=df1["modelA2"].values.tolist()
b2=df2["modelB2"].values.tolist()
b3=df3["modelC2"].values.tolist()
b4=df4["modelD2"].values.tolist()

data_plot2=[b1,b2,b3,b4]
print("data for box_plot: ",data_plot1)
labels=["modelA2","modelB2","modelC2","modelD2"]
#boxplt2 = plt.boxplot(data_plot1, patch_artist=True) 
boxplt2 = ax.boxplot(data_plot2, labels=labels,patch_artist=True) 

colors = ['blue', 'green', 'purple', 'tan'] 
for patch, color in zip(boxplt2['boxes'], colors):
    patch.set_facecolor(color)
#plt.figure(figsize =(10, 8))  
plt.show()      

# box1 = plt.boxplot(data1, patch_artist=True)
#  
# #colors = ['blue', 'green', 'purple', 'tan', 'pink', 'red']
# colors = ['blue', 'green', 'purple', 'tan'] 
# for patch, color in zip(box1['boxes'], colors):
#     patch.set_facecolor(color)
#  
# plt.show()
# 
# box2 = plt.boxplot(data2, patch_artist=True)
#  
# #colors = ['blue', 'green', 'purple', 'tan', 'pink', 'red']
# colors = ['blue', 'green', 'purple', 'tan'] 
# for patch, color in zip(box2['boxes'], colors):
#     patch.set_facecolor(color)
#  
# plt.show()




# data=[table_shorted["to_keep"],table_shorted["to_dropout"]]
# plt.figure(figsize =(10, 7)) 
# # Creating axes instance 
# plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
# plt.show()
# 
# table_shorted["to_keep"].quantile([0.25,0.5,0.75])
# table_shorted["to_dropout"].quantile([0.25,0.5,0.75])
# 
# #table_shorted["to_keep"].plot(kind='box',title='tables to keep distribution', figsize=(10,8))
# plt.show()
# #table_shorted["to_dropout"].plot(kind='box',title='tables to keep distribution', figsize=(10,8))
# #data=[table_shorted["to_keep"],table_shorted["to_dropout"]]
# 
# data=pd.concat([table_shorted["to_keep"],table_shorted["to_dropout"]],axis=1)
# data.plot(kind='box',title='tables to keep distribution', figsize=(10,8))
# plt.show()
# -----------------------------------------------------------------------------

# # -----------------------------------------------------------------------------
# table_45percent=pd.read_csv(tbl_45percent_data_path,sep=";",header=0)
# display(table_45percent)
# 
# data=[table_45percent["to_keep"],table_45percent["to_dropout"]]
# plt.figure(figsize =(10, 7)) 
# # Creating axes instance 
# plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
# plt.show()
# # -----------------------------------------------------------------------------
# table_90percent=pd.read_csv(tbl_90percent_data_path,sep=";",header=0)
# display(table_90percent)
# 
# data=[table_90percent["to_keep"],table_90percent["to_dropout"]]
# plt.figure(figsize =(10, 7)) 
# # Creating axes instance 
# plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
# plt.show()
# # -----------------------------------------------------------------------------
# table_k2_directed=pd.read_csv(tbl_k2_directed_data_path,sep=";",header=0)
# display(table_k2_directed)
# 
# data=[table_k2_directed["to_keep"],table_k2_directed["to_dropout"]]
# plt.figure(figsize =(10, 7)) 
# # Creating axes instance 
# plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
# plt.show()
# # -----------------------------------------------------------------------------
# table_k2_undirected=pd.read_csv(tbl_k2_undirected_data_path,sep=";",header=0)
# display(table_k2_undirected)
# 
# data=[table_k2_undirected["to_keep"],table_k2_undirected["to_dropout"]]
# plt.figure(figsize =(10, 7)) 
# # Creating axes instance 
# plt.boxplot(data,patch_artist=True,labels=['to keep','to drop out'])
# plt.show()