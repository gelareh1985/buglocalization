'''
Created on Apr 9, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import buglocalization.evaluation.evaluation_util as load_data 
from IPython.display import display, HTML

path=r"D:\buglocalization_gelareh_home\evaluations\examples training or evaluation\data_examples/"

def load_json_file_content(filepath):
    list_json_tables=[]
    for filename in os.listdir(filepath):
        if filename.endswith("_nodes.json"):
            evaluation_filename = filename[:filename.rfind("_")]

            tbl_json_file = evaluation_filename + "_nodes.json"
            tbl_json_path = filepath + tbl_json_file
            tbl_json = pd.read_json(tbl_json_path)
            list_json_tables.append(tbl_json)
    return  list_json_tables        

def hover(hover_color="#ffff99"):
    return dict(selector="tr:hover",
                props=[("background-color", "%s" % hover_color)])

filters=None
at_leat_one_relevant=True
data=load_data.load_all_evaluation_results(path, filters, at_leat_one_relevant)
print("data: ",data)
print("type of data : ", type(data))
df=pd.DataFrame(data)
display("data frame: ",df)

df2=df.copy()
df3=df.copy()


df2=df2.loc[0:0,0:3]
col1=df2.loc[:,0].values.tolist()
col2=df2.loc[:,1].values.tolist()
col3=df2.loc[:,2].values.tolist()
col4=df2.loc[:,3].values.tolist()

lbl=["File: ","Information Content: ","File: ","Prediction Content: "]
d1=pd.DataFrame(lbl,columns=["Data"])
d2=pd.DataFrame([col1,col2,col3,col4],columns=["Bug Report"])
d3=pd.concat([d1,d2],axis=1)
#d3["Data"]=d3["Data"].str.pad(width=50,side='both')
d3.to_html("data_bugreport1.html",justify='justify-all',col_space="8pt",border="2pt",index=None)
 
 
 

df3=df3.loc[1:1,0:3]
col1=df3.loc[:,0].values.tolist()
col2=df3.loc[:,1].values.tolist()
col3=df3.loc[:,2].values.tolist()
col4=df3.loc[:,3].values.tolist()

lbl=["File: ","Information Content: ","File: ","Prediction Content: "]
d1=pd.DataFrame(lbl,columns=["Data"])
d2=pd.DataFrame([col1,col2,col3,col4],columns=["Bug Report"])
d3=pd.concat([d1,d2],axis=1)
d3.to_html("data_bugreport2.html",justify='justify-all',col_space="8pt",border="2pt",index=None)

json_tables=load_json_file_content(path)
i=1
for table in json_tables:
    display(table.columns.to_list())
    table=table.T
    display(table)
    #display(table.dropna())
    table.to_html("data_json"+str(i)+".html",justify='justify-all',border="2pt")
    i+=1
i=1
list_cols=[]
for table in json_tables:
    display(table.columns.to_list())
    display(table)
    for col in table.columns:
        each_col=table[col].values
        list_cols.append(each_col)
    #display(table.dropna())
    df_cols=pd.DataFrame(list_cols)
    df_cols.to_html("data_json_new"+str(i)+".html",justify='justify-all',border="2pt")
    i+=1    
########### style 1 :
# myhtml = dff2.style.set_properties(**{'font-size': '12pt', 'font-family': 'TimesNewRoman','border': '1px solid black'}).render()
# 
# with open('data_bugreport1_.html','w') as f:
#     f.write(myhtml)        
########### style 2 :    
# styles = [
#     hover(),
#     dict(selector="th", props=[("font-size", "12pt"),
#                                ("text-align", "center"),
#                                ('padding', "3em 6em")#,
#                                #('max-width', '600px')
#                                ])
#                               # ('border-collapse', 'separate'),
#                                #("border-spacing",  "200px 500px")])
# ]
# html = (dff2.style.set_table_styles(styles).render())
# with open('data_bugreport1__.html','w') as f:
#     f.write(html)     
        











