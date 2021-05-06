'''
Created on Feb 28, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display

#tables_path = r"D:\buglocalization_gelareh_home\eclipse.jdt.core_2021-02-27_15-39-33/"
#tables_path = r"D:\buglocalization_gelareh_home\eclipse.jdt.core_2021-03-04_02-06-04_train90_test10_layer300\eclipse.jdt.core_2021-03-04_02-06-04/"
#tables_path = r"D:\buglocalization_gelareh_home\eclipse.jdt.core_evaluation_2021-03-04_02-06-04_k2_undirected/"
tables_path = r"D:\buglocalization_gelareh_home\eclipse.jdt.core_evaluation_2021-03-04_02-06-04_k2_directed/"

def load_evaluation_results(path):
    
    tables_predicted=[]
    tables_info=[]
    table_names=[]
    for filename in os.listdir(path):
        
        if filename.endswith("_prediction.csv"):
            tbl_predicted_filename = filename[:filename.rfind(".")]
            tbl_predicted_path = path + tbl_predicted_filename + ".csv"
            tbl_predicted_data=pd.read_csv(tbl_predicted_path,sep=';',header=0)
            tables_predicted.append(tbl_predicted_data)
            table_names.append(filename)
            
        elif filename.endswith("_info.csv"): 
            tbl_info_filename = filename[:filename.rfind(".")]
            tbl_info_path = path + tbl_info_filename + ".csv"
            tbl_info_data=pd.read_csv(tbl_info_path,sep=';',header=0)
            tables_info.append(tbl_info_data)
            
    return table_names, tables_predicted, tables_info

def mean_reciprocal_rank(rs):
    """Score is reciprocal of the rank of the first relevant item
    First element is 'rank 1'.  Relevance is binary (nonzero is relevant).
    Example from http://en.wikipedia.org/wiki/Mean_reciprocal_rank
    >>> rs = [[0, 0, 1], [0, 1, 0], [1, 0, 0]]
    >>> mean_reciprocal_rank(rs)
    0.61111111111111105
    >>> rs = np.array([[0, 0, 0], [0, 1, 0], [1, 0, 0]])
    >>> mean_reciprocal_rank(rs)
    0.5
    >>> rs = [[0, 0, 0, 1], [1, 0, 0], [1, 0, 0]]
    >>> mean_reciprocal_rank(rs)
    0.75
    Args:
        rs: Iterator of relevance scores (list or numpy) in rank order
            (first element is the first item)
    Returns:
        Mean reciprocal rank
    """
    rs = (np.asarray(r).nonzero()[0] for r in rs)
    return np.mean([1. / (r[0] + 1) if r.size else 0. for r in rs])

def precision_at_k(r, k):
    """Score is precision @ k
    Relevance is binary (nonzero is relevant).
    >>> r = [0, 0, 1]
    >>> precision_at_k(r, 1)
    0.0
    >>> precision_at_k(r, 2)
    0.0
    >>> precision_at_k(r, 3)
    0.33333333333333331
    >>> precision_at_k(r, 4)
    Traceback (most recent call last):
        File "<stdin>", line 1, in ?
    ValueError: Relevance score length < k
    Args:
        r: Relevance scores (list or numpy) in rank order
            (first element is the first item)
    Returns:
        Precision @ k
    Raises:
        ValueError: len(r) must be >= k
    """
    assert k >= 1
    r = np.asarray(r)[:k] != 0
    if r.size != k:
        raise ValueError('Relevance score length < k')
    return np.mean(r)

def ranking_precision_score(y_true, y_score, k=10):
    """Precision at rank k
    Parameters
    ----------
    y_true : array-like, shape = [n_samples]
        Ground truth (true relevance labels).
    y_score : array-like, shape = [n_samples]
        Predicted scores.
    k : int
        Rank.
    Returns
    -------
    precision @k : float
    """
    unique_y = np.unique(y_true)

    if len(unique_y) > 2:
        raise ValueError("Only supported for two relevance levels.")
    elif len(unique_y) ==2:
        pos_label = unique_y[1]
        n_pos = np.sum(y_true == pos_label)
    elif len(unique_y)==1:    
        pos_label = unique_y[0]
        n_pos = np.sum(y_true == pos_label)
        
    order = np.argsort(y_score)[::-1]
    y_true = np.take(y_true, order[:k])
    n_relevant = np.sum(y_true == pos_label)

    # Divide by min(n_pos, k) such that the best achievable score is always 1.0.
    return float(n_relevant) / min(n_pos, k)
# *************************************************************************
tblnames,tables_predicted, tables_info=load_evaluation_results(tables_path)

for idx in range(len(tables_predicted)):
    display("prediction data: ",tables_predicted[idx])
    display("info data: ",tables_info[idx]) 
#percentage=[60, 65, 70, 75, 80]

list_mean_precision=[]
list_mrr_rank_vals=[]
list_labels_for_topk=[]
list_scores_for_topk=[]
p=0.80
row_num=249
table_no=0
list_to_dropout=[]
total_locations=[]
total_classes=[]
for table in tables_predicted:
    ######### Dataframe define size #########
    df1=table.loc[:]
    indx_bug_loc_0=df1.loc[df1.IsLocation==0].index.tolist()  # for mean reciprocal ranks
    indx_bug_loc_1=df1.loc[df1.IsLocation==1].index.tolist()
    
    indx_class_locs=df1.loc[df1.MetaType=="['Class']"].index.tolist()
    total_locations.append(indx_bug_loc_1)
    total_classes.append(indx_class_locs)
    
    tbls=df1.loc[df1["IsLocation"]==1].index.tolist()
    if(len(tbls)==0):
        list_to_dropout.append(table_no)
    df2=df1.copy()
    if(len(indx_bug_loc_1)==0 and df2.loc[indx_bug_loc_0[0],"Prediction"] > p):
        df2.loc[indx_bug_loc_0[0],"IsLocation"]=1
        
    list_mrr_rank_vals.append(df2["IsLocation"].values.tolist())
    ######### Dataframe for MAP: #########
    df3=df1.copy()
    df3=df3.loc[df3["Prediction"]>p]
    list_mean_precision.append(df3["Prediction"].mean()) 
 
    ######## Dataframe for TOPK: #########
    df4=df1.copy()
    list_true_topk=df4.loc[df4.Prediction > p].index.tolist()
    df4.loc[list_true_topk,"IsLocation"]=1
    lblst=df4["IsLocation"].values.tolist()
    list_labels_for_topk.append(lblst)
    scorlst=df4["Prediction"].values.tolist()
    list_scores_for_topk.append(scorlst)
    table_no=table_no+1
# ************************************************************************* 
print("number of tables to be ignored: ",len(list_to_dropout),'\n',list_to_dropout ) 
for tbl_no in list_to_dropout:
    print(tblnames[tbl_no])        
df_MAP=pd.DataFrame(list_mean_precision, columns=["mean_precision"])
display(df_MAP)
     
map_val=df_MAP.mean() 
print("MAP: ", map_val)
   
mrr_val=mean_reciprocal_rank(list_mrr_rank_vals)
print("MRR: ", mrr_val) 
# rs = [[0, 0, 1], [0, 1, 0], [1, 0, 0]]
# print(mean_reciprocal_rank(rs))  
listk=[1,5,10,15]
topk_final_acc_list=[]
p_atk_final_acc_list=[]
for k in listk:
    list_topk=[]
    list_p_atk=[]
    for i in range(len(list_labels_for_topk)):
        y_true_labels=np.asarray(list_labels_for_topk[i])
        y_scores=np.asarray(list_scores_for_topk[i])
        bool_list_labels = list(map(bool,list_labels_for_topk[i]))
        #bool_list_scores = list(map(bool,list_scores_for_topk[i]))
        try:
            topk_val=ranking_precision_score(bool_list_labels, y_scores, k)
            list_topk.append(topk_val)
            p_atk=precision_at_k(y_true_labels,k)
            list_p_atk.append(p_atk)
        except ValueError:
            pass
    acc_k=sum(list_topk)/len(list_topk)  
    topk_final_acc_list.append(acc_k)
    acc_at_k=sum(list_p_atk)/len(list_p_atk) 
    p_atk_final_acc_list.append(acc_at_k)
  
i=0
for acc in topk_final_acc_list:   
    print("Top k= ", listk[i], "(accuracy):",acc)
    i=i+1
i=0    
for acc in p_atk_final_acc_list:
    print("Precision at k= (", listk[i], "), (accuracy):",acc)     
    i=i+1
 
#print("number of locations for each file (list): ", total_locations)
sum_locs_per_file_list=[]
for num_locs_file in total_locations:
    sum_locs_per_file=sum(num_locs_file)
    sum_locs_per_file_list.append(sum_locs_per_file)
#print("sum of all of locations for each file: ", sum_locs_per_file_list)    
sum_all_location=sum(sum_locs_per_file_list)
print("sum of all of locations for all files: ", sum_all_location)        
    
    
#print("number of classes for each file (list): ", total_classes)
sum_classes_per_file_list=[]
for num_classes_file in total_classes:
    sum_classes_per_file=sum(num_classes_file)
    sum_classes_per_file_list.append(sum_classes_per_file)
#print("sum of all of classes for each file: ", sum_classes_per_file_list)    
sum_all_classes=sum(sum_classes_per_file_list)
print("sum of all of classes for all files: ", sum_all_classes) 
