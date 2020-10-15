# from IPython.display import display
# import pandas as pd
# import numpy as np
# 
# df = pd.DataFrame(np.random.randn(6,4), index=pd.date_range('20150101', periods=6), columns=list('ABCD'))
# df2 = pd.DataFrame(np.random.randn(6,4), index=pd.date_range('20150101', periods=6), columns=list('WXYZ'))
# 
# 
# df.head()
# df2.head()
# 
# display(df)
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
# from sklearn.manifold import TSNE
# import pandas as pd
# import re
# import os

# from nltk.corpus import stopwords

from word_to_vec import generate_dictinoary_data
from word_to_vec import generate_training_data
from word_to_vec import train
from word_to_vec import word_similarity_scatter_plot
from word_to_vec import plot_epoch_loss
from word_to_vec import get_file_data

path_file=r'D:\\workspaces\\buglocalization\\word2vec_test\\src\\jef_archer.txt'
output_text =r'D:\\workspaces\\buglocalization\\word2vec_test\\src\\jef_without stopwords.txt'


# text = get_file_data(path_file,'yes')
# # text_file = open(output_text, "w")
# # text_file.write(text)
# # text_file.close()
# with open(output_text, 'w') as f:
#     for item in text:
#         f.write("%s\n" % item)
print("Hello World!")

