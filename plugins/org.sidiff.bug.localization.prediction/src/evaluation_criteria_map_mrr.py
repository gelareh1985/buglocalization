'''
Created on Feb 28, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display

#pd.options.mode.chained_assignment = None

tables_path = r"D:\buglocalization_gelareh_home\eclipse.jdt.core_2021-02-27_15-39-33/"

def load_evaluation_results(path):
    
    tables_predicted=[]
    tables_info=[]
    
    for filename in os.listdir(path):
        
        if filename.endswith("_prediction.csv"):
            tbl_predicted_filename = filename[:filename.rfind(".")]
            tbl_predicted_path = path + tbl_predicted_filename + ".csv"
            tbl_predicted_data=pd.read_csv(tbl_predicted_path,sep=';',header=0)
            tables_predicted.append(tbl_predicted_data)
            
        elif filename.endswith("_info.csv"): 
            tbl_info_filename = filename[:filename.rfind(".")]
            tbl_info_path = path + tbl_info_filename + ".csv"
            tbl_info_data=pd.read_csv(tbl_info_path,sep=';',header=0)
            tables_info.append(tbl_info_data)
            
    return tables_predicted, tables_info

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

def get_data_matrixes(n_rows,tabls_predicted,list_p):

    list_mean_precision=[]
    list_mrr_rank_vals=[]
    list_labels_for_topk=[]
    list_scores_for_topk=[]
    for p in list_p:
        l_mean_precision=[]
        l_mrr_rank_vals=[]
        l_labels_for_topk=[]
        l_scores_for_topk=[]
        for table in tabls_predicted:
            
            ######### Data frame define size ########
            df1=table.loc[0:n_rows,:]
            #df2=df1.copy()
            #df3=df1.copy()
             
            indx_bug_loc_0=df1.loc[df1.IsLocation==0].index.tolist()  # for mean reciprocal ranks
            indx_bug_loc_1=df1.loc[df1.IsLocation==1].index.tolist()
         
             
            if(len(indx_bug_loc_1)==0 and df1.loc[indx_bug_loc_0[0],"Prediction"] > p):
                df1.loc[indx_bug_loc_0[0],"IsLocation"]=1
                
            l_mrr_rank_vals.append(df1["IsLocation"].values.tolist())
             
            ########## Data frame for MAP: ##########
            df2=table.loc[0:n_rows]
            selected_prec=df2.loc[df2.Prediction > p]
            mean_prec=selected_prec["Prediction"].mean()
            l_mean_precision.append(mean_prec) 
         
            ######### Data frame for TOPK: ##########
            df3=table.loc[0:n_rows]
            list_true_topk=df3.loc[df3.Prediction > p].index.tolist()
            for i in list_true_topk:
                df3.loc[list_true_topk[i],"IsLocation"]=1
            
            #df3.IsLocation[df3.Prediction > p] = 1 
            
             
            lblst=df3["IsLocation"].values.tolist()
            l_labels_for_topk.append(lblst)
            scorlst=df3["Prediction"].values.tolist()
            l_scores_for_topk.append(scorlst)
            
            list_mean_precision.append(l_mean_precision)
            list_mrr_rank_vals.append(l_mrr_rank_vals)  
            list_labels_for_topk.append(l_labels_for_topk)
            list_scores_for_topk.append(l_scores_for_topk)
    return list_mean_precision,list_mrr_rank_vals,list_labels_for_topk,list_scores_for_topk
# *************************************************************************  

tables_predicted, tables_info=load_evaluation_results(tables_path)

for idx in range(len(tables_predicted)):
    display("prediction data: ",tables_predicted[idx])
    display("info data: ",tables_info[idx]) 
    
# *************************************************************************   

percentage_list=[0.60, 0.65, 0.70, 0.75, 0.80]
num_rows=99
mean_precision_data,mrr_rank_vals_data,labels_for_topk_data,scores_for_topk_data=get_data_matrixes(num_rows,tables_predicted,percentage_list)

# df.replace(np.nan, 0, inplace=True)

df_MAP=pd.DataFrame(mean_precision_data)
#df_MAP.fillna(0,inplace=True)
#df_MAP.replace(np.nan, 0, inplace=True)
display(df_MAP)

map_val_list=[]
for i in range(len(percentage_list)):
    map_val=df_MAP.iloc[i].mean()
    print("MAP for prediction data greater than (p=",percentage_list[i],")", map_val)
    map_val_list.append(map_val) 

df_MRR=pd.DataFrame(mrr_rank_vals_data)
#df_MRR.fillna(0)
#df_MRR.replace(np.nan, 0, inplace=True)
display(df_MRR)

mrr_val_list=[]
for i in range(len(percentage_list)):
    mrr_vects=df_MRR.iloc[i].values.tolist()
    mrr_val=mean_reciprocal_rank(mrr_vects)
    print("MRR for prediction data greater than (p=",percentage_list[i],")", mrr_val) 
    mrr_val_list.append(mrr_val)
    i=i+1
# rs = [[0, 0, 1], [0, 1, 0], [1, 0, 0]]
# print(mean_reciprocal_rank(rs))

#print("Results for predictions greater than P=", p, "%  :")

df_TOPK_lbls=pd.DataFrame(labels_for_topk_data)
#df_TOPK_lbls.fillna(0)
#df_TOPK_lbls.replace(np.nan, 0, inplace=True)
display(df_TOPK_lbls)

lbls_topk_list=[]
for i in range(len(percentage_list)):
    topk_lbls=df_TOPK_lbls.iloc[i].values.tolist()
    lbls_topk_list.append(topk_lbls)

df_TOPK_scores=pd.DataFrame(scores_for_topk_data)
#df_TOPK_lbls.fillna(0)
#df_TOPK_scores.replace(np.nan, 0, inplace=True)
display(df_TOPK_scores)

scores_topk_list=[]
for i in range(len(percentage_list)):
    topk_scores=df_TOPK_scores.iloc[i].values.tolist()
    scores_topk_list.append(topk_scores)

topk_final_acc_list_for_p=[]
for row in range(len(lbls_topk_list)):   
    print("TOPK for prediction data greater than (p=",percentage_list[row],"):")
    listk=[1,5,10,15]
    topk_final_acc_list=[]
    
    for k in listk:
        
        list_topk=[]
        list_p_atk=[]
        row_of_lbls=lbls_topk_list[row]
        row_of_scores=scores_topk_list[row]
        
        for i in range(len(row_of_lbls)):
            
            y_true_labels=np.asarray(row_of_lbls[i])
            y_scores=np.asarray(row_of_scores[i])
            bool_list_labels = list(map(bool,row_of_lbls[i]))
            
            try:
                topk_val=ranking_precision_score(bool_list_labels, y_scores, k)
                list_topk.append(topk_val)
                p_atk=precision_at_k(y_true_labels,k)
                list_p_atk.append(p_atk)
            except ValueError:
                pass
            
        acc_k=sum(list_topk)/len(list_topk)  
        topk_final_acc_list.append(acc_k)
        
        print("Top k= ", k, "(accuracy):",acc_k)
    
    topk_final_acc_list_for_p.append(topk_final_acc_list)  
      
print("Precision at k= (", listk, "), (accuracy):",list_p_atk)    




