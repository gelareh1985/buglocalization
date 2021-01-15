'''
Created on Dec 25, 2020

@author: Gelareh_mp
'''
import pandas as pd
import numpy as np
from numpy import argmax
from sklearn.preprocessing import OneHotEncoder
from sklearn.preprocessing import LabelEncoder
from IPython.display import display

from nltk.corpus import stopwords

stop_words = set(stopwords.words('english'))

positve_samples_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/"
dictionary_path =           r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/positive/complete_dictionary_set_smalltest_positive.dictionary"

def one_hot_encode(value,invert_index):
    labelencoder = LabelEncoder() 
    integer_encoded = labelencoder.fit_transform(value) 
    print('label encoder shape: ', integer_encoded.shape)
    print('label encoder: ', integer_encoded)

    onehot_encoder = OneHotEncoder(sparse=False)
    integer_encoded = integer_encoded.reshape(len(integer_encoded), 1)
    onehot_encoded = onehot_encoder.fit_transform(integer_encoded)
    print('one_hot encoder shape: ',onehot_encoded.shape)
    print('one_hot encoder: ',onehot_encoded)
    
    # invert first example
    inverted = labelencoder.inverse_transform([argmax(onehot_encoded[invert_index, :])])
    print('inverted value by index ', str(invert_index), ' : ' ,inverted)
    return onehot_encoded
#####################################################################
col_data=('main','compile','does','not','close','log','file')
df = pd.DataFrame(col_data, columns=['Col1'])
df['Col1'] = df['Col1'].astype('category') 
df['Col1_Cat'] = df['Col1'].cat.codes 
display(df)

print('\n')

labelencoder = LabelEncoder() 
df['Col1_Cat'] = labelencoder.fit_transform(df['Col1']) 
display(df)

print('\n')

df2=df.copy()
enc = OneHotEncoder(handle_unknown='ignore') 
enc_df = pd.DataFrame(enc.fit_transform(df2[['Col1_Cat']]).toarray())
df2 = df2.join(enc_df) 
display(df2)

print('\n')


col_data=('main','compile','does','not','close','log','file')
df3 = pd.DataFrame(col_data, columns=['Col1'])
dum_df = pd.get_dummies(df3, columns=["Col1"], prefix=["Type_is"] ) # generate binary values using get_dummies
df3 = df3.join(dum_df) 
display(df3) 
display(df3.shape)

print('\n')

inverted = labelencoder.inverse_transform([argmax(enc_df[6])])
print(inverted)  

print('\n ************************************************************** \n')

col_data1=('main','compile','does','not','close','log','file')
col_data2=('main','compile','does','not','close','','')
df = pd.DataFrame(col_data1, columns=['Col1'])
df['Col2']=col_data2
display(df)

print('\n')

values1 = np.array(df['Col1'])
print('values shape: ', values1.shape)
print('values: ', values1)

onehot_encoded1=one_hot_encode(values1, 0)
print('\n')

print('display a row of one_hot  encoded by index: ',onehot_encoded1[1, :])

print('\n ************************************************************** \n')

values2 = np.array(df['Col2'])
print('values shape: ', values2.shape)
print('values: ', values2)

onehot_encoded2=one_hot_encode(values2, 4)
print('\n')

print('display a row of one_hot  encoded by index: ', onehot_encoded2[1, :])
print('sum of vectors col1: ',np.add.reduce(onehot_encoded1))
print('sum of vectors col2: ',np.add.reduce(onehot_encoded2))

