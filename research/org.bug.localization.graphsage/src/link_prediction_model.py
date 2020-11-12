'''
Created on Nov 10, 2020

@author: Gelareh_mp
'''

#import stellargraph as sg
from stellargraph import StellarGraph
from stellargraph import IndexedArray

import numpy as np
import pandas as pd

def load_table(filepath):
    data_column1=[]
    data_column2=[]
    list=[]
    with open(filepath) as f:
        for j,line in enumerate(f):
                info = line.strip().split('\t')
                #info=[info[0],info[1]]
                data_column1.append(str(info[0]))
                data_column2.append(str(info[1]))
                info2=[info[0],info[1]]
                list.append(info2)
    return  data_column1,data_column2, list

##################################################################
filepath='data.edgelist'

data_column1,data_column2,list=load_table(filepath)

pair1=np.array(len(data_column1),dtype=object)
pair2=np.array(len(data_column2),dtype=object)

pair1=np.unique(data_column1)
pair2=np.unique(data_column1)

print('list pairs shape: ', pairs_array.shape)
print('length of pair 1: '+str(len(pair1)))
print('length of pair 2: '+str(len(pair2)))
print('pair 1: ',pair1)          
print('pair 2: ',pair2)

          
feature_array= np.random.rand(len(pair1), 2)
          
ind_arr1=IndexedArray(feature_array,index= pair1) 
ind_arr2=IndexedArray(index= pair2) 
          
square_numeric_edges = pd.DataFrame(
    {"source": pair1, "target": pair2}
) 
     
square_numeric = StellarGraph(ind_arr1, square_numeric_edges)
print(square_numeric.info())


