import os
from word_to_vec import generate_dictinoary_data



print("Hello World!")


p='D:\\files_MDEAI_original\\Datasets\\DataSet_20200925144220_with_tabs\\DataSet_20200925144220\\evaluation\\'
#path=r'D:\files_MDEAI_original\Datasets\DataSet_20200925144220_with_tabs\DataSet_20200925144220\training\00002_bug_12000_version_2c9cb94dc84956e5c7f0db27e02f01d02c3e4f02.nodelist'


def load_table(filepath):
    table_data=[]
    with open(filepath) as f:
         #content=f.read()
         #print(filename)
         #table_data.append(content)
         #print(content)
        for i, line in enumerate(f):
             info = line.strip().split('\t')
             table_data.append(info)
    return table_data
                          
   
def save_table(out_file):
    with open('tbl.txt','w') as fp:
        for row in out_file:
            fp.write("%s\n" % row)
            print("finished!")
            
            
tbl_list=[]
for filename in os.listdir(p):
        filepath=os.path.join(p,filename)
        if filename.endswith(".nodelist"):
           table=load_table(filepath)
           tbl_list.append(table)    
   
       
save_table(tbl_list)           
    

            
          