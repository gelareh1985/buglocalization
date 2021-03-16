'''
Created on Mar 16, 2021

@author: Gelareh_mp
'''
import pandas as pd
import numpy as np
from IPython.display import display

#data_path1 = r"D:\buglocalization_gelareh_home\trained_models\eclipse.jdt.core_2021-02-27_15-39-33/7729_bug545475_28f53155d592e8d12991fab6d60706a44adb05e0_prediction.csv"
data_path2=  r"D:\buglocalization_gelareh_home\trained_models\eclipse.jdt.core_evaluation_2021-03-04_02-06-04_k2_undirected/8534_bug568959_cf59ba3e76b87457d01068e4c3e0381b29e8920a_prediction.csv"
data_path1=r"D:\buglocalization_gelareh_home\trained_models\eclipse.jdt.core_evaluation_2021-03-04_02-06-04_k2_undirected/7729_bug545475_28f53155d592e8d12991fab6d60706a44adb05e0_prediction.csv"

bugreport_table_oldest_version=pd.read_csv(data_path1,sep=';',header=0)
bugreport_table_newest_version=pd.read_csv(data_path2,sep=';',header=0)

display(bugreport_table_oldest_version)
display(bugreport_table_newest_version)

display(np.unique(bugreport_table_oldest_version["MetaType"]))

#bugreport_table_oldest_version["MetaType"]
#bugreport_table_newest_version["MetaType"]

#indx_bug_loc_1=bugreport_table_oldest_version.loc[bugreport_table_oldest_version.IsLocation==1]
indx_bug_loc_1=bugreport_table_newest_version.loc[bugreport_table_newest_version.IsLocation==1]

loc_metatype_classes=bugreport_table_oldest_version.loc[bugreport_table_oldest_version.MetaType=="['Class']"].values.tolist()
loc_metatype_datatypes=bugreport_table_oldest_version.loc[bugreport_table_oldest_version.MetaType=="['DataType']"].index.tolist() 
loc_metatype_enumerations=bugreport_table_oldest_version.loc[bugreport_table_oldest_version.MetaType=="['Enumeration']"].index.tolist() 
loc_metatype_interfaces=bugreport_table_oldest_version.loc[bugreport_table_oldest_version.MetaType=="['Interface']"].index.tolist()

print(loc_metatype_classes)
print(loc_metatype_datatypes)
print(loc_metatype_enumerations)
print(loc_metatype_interfaces)
#if(not indx_bug_loc_1.empty):
#    display(indx_bug_loc_1.groupby('MetaType').count())
#display(bugreport_table_oldest_version.groupby('MetaType').count())
if(not indx_bug_loc_1.empty):
    display(indx_bug_loc_1.groupby('MetaType').count())
display(bugreport_table_newest_version.groupby('MetaType').count())
