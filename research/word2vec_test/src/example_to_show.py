from IPython.display import display
import pandas as pd
import numpy as np

df = pd.DataFrame(np.random.randn(6,4), index=pd.date_range('20150101', periods=6), columns=list('ABCD'))
df2 = pd.DataFrame(np.random.randn(6,4), index=pd.date_range('20150101', periods=6), columns=list('WXYZ'))


df.head()
df2.head()

display(df)