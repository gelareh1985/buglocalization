'''
Created on Jan 13, 2021

@author: Gelareh_mp
'''
import numpy as np
import pandas as pd
from IPython.display import display

mylist1=[[('1.0','2.0','3.0','4.0')],[('5.0','6.0','7.0','8.0')],[('9.0','10.0','11.0','12.0')]]
print('initial list1: ',mylist1)

arr1=np.array(['1.0','2.0','3.0','4.0'])
arr2=np.array(['5.0','6.0','7.0','8.0'])
arr3=np.array(['9.0','10.0','11.0','12.0'])

mylist2=[[arr1],[arr2],[arr3]]
print('initial list2: ',mylist2)

print('tuple of my list1: ',tuple(mylist1))
print('tuple of my list2: ',tuple(mylist2))

display(pd.DataFrame(tuple(mylist1)))
display(pd.DataFrame(tuple(mylist2)))

arr1=np.asarray(mylist1)
arr2=np.asarray(mylist2)
print('shapes: ', arr1.shape , '    ', arr2.shape)
# display(pd.DataFrame(arr1))
# display(pd.DataFrame(arr2))

col_list=['col1','col2','col3','col4']

print('array element: ',tuple([mylist1[0],mylist1]))
print('tuples: ', tuple(mylist1[0]),'\n', tuple(mylist1[1]))

print('tuple union1: ', tuple(mylist1[0])+tuple(mylist1[1]))
print('tuple union2: ', list(tuple(mylist1[0])+tuple(mylist1[1])))
display(pd.DataFrame(list(tuple(mylist1[0])+tuple(mylist1[1]))))

print('\n DataFrame of Tuple lists: ')
df1_arr=np.array(list(tuple([mylist1[0],mylist1])))

#arr2=df1_arr.reshape(3,4)
#df1=pd.DataFrame(arr2)
#display(df1)
new_arr=np.array(tuple(mylist2))
print('array shape: ',new_arr.shape, '  ', new_arr.ndim, '  ', new_arr[0].size, '  ', len(new_arr.flat))
print(len(new_arr[0]), '    ',len(new_arr[0][0]), '    ', type(new_arr[0]), '    ',new_arr[0].shape)
print(new_arr[0], '    ',new_arr[0][0])
new_arr=new_arr.reshape(3,4)
df2=pd.DataFrame(new_arr)#,columns=col_list)
display(df2)

mylist3=list(np.array([ '0.18966675', '-0.03137207', '-0.11621094',  
                       '0.02246094','0.18966675', '-0.03137207', 
                       '-0.11621094',  '0.02246094','0.18966675',
                        '-0.03137207', '-0.11621094',  '0.02246094']))

print('initial list3: ',mylist3)
arr=np.array(mylist3)

#arr=arr.reshape(3,4)
arr2=np.vstack([mylist3,np.arange(12)])
print(arr2.shape)
df3=pd.DataFrame(arr2)#,columns=col_list)
display(df3)

