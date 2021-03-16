'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
import matplotlib.pyplot as plt


plot_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results\xy plots\trained_model_2021-03-04_02-06-04_train90_test10_layer300_k2_undirected_metrics/"
#plot_data_path=r"D:\buglocalization_gelareh_home\charts and evaluation results\xy plots\trained_model_2021-03-04_02-06-04_train90_test10_layer300_metrics/"

def load_evaluation_results(path): 
    data_tables=[]
    table_names=[]
    for filename in os.listdir(path):
        
        #if filename.endswith("_topk.csv"):
        if filename.endswith("_boxplot1.csv"):
        #if filename.endswith("_2.csv"):
            tbl_filename = filename[:filename.rfind(".")]
            tbl_path = path + tbl_filename + ".csv"
            tbl__data=pd.read_csv(tbl_path,sep=";",header=0)
            data_tables.append(tbl__data)
            table_names.append(filename)
            
    return data_tables

tables=load_evaluation_results(plot_data_path)

#########################################################################################
# 1st and 2nd Box_plots (MRR/MAP)
#########################################################################################
for table in tables:
    display(table.loc[:,'MeanAveragePrecision':'MeanReciprocalRank'])
    mrr_map=table.loc[:,'MeanAveragePrecision':'MeanReciprocalRank']
    minimalranks=table.loc[:,'MinimalRank']
    if len(mrr_map==4):
        minimalrank_labels=['Whisker_top','Whisker_bottom','Quartile_top','Median']
        x= minimalrank_labels
        y1=mrr_map.loc[0,:].values.tolist()
        y2=mrr_map.loc[1,:].values.tolist()
        y3=mrr_map.loc[2,:].values.tolist()
        y4=mrr_map.loc[3,:].values.tolist()
        fig = plt.figure()
        #plt.plot(y1,'r--',y2,'m--',y3,'y--',y4,'c--')
        y1_plt=plt.plot(y1,'r--')
        y2_plt=plt.plot(y2,'m--')
        y3_plt=plt.plot(y3,'y--')
        y4_plt=plt.plot(y4,'c--')
        plt.legend([y1_plt[0],y2_plt[0],y3_plt[0],y4_plt[0]],['Whisker_top','Whisker_bottom','Quartile_top','Median'])
        plt.xlabel('MAP/MRR')
        plt.ylabel('Accuracy')
        plt.show() 
     
        df=pd.DataFrame({"Curve_Whisker_Top":y1,
                         "Curve_Whisker_Bottom":y2,
                         "Curve_Quartile_Top":y3,
                         "Curve_Median":y4})
         
        #df.set_axis(['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'], axis=0, inplace=True)
        display("x-y plot data frame: ",df)
        color=['r','m','y','c']
        ax=df.plot(kind='line',color=color,title='MAP/MRR from 1st Box-Plot', linestyle='dashed', marker='o', markersize=10)
        ax.set(ylabel='Accuracy', xlabel = 'MAP/MRR')
        plt.show()
    print('average MAP:',mrr_map['MeanAveragePrecision'].mean())
    print('average MRR:',mrr_map['MeanReciprocalRank'].mean())
#########################################################################################
# 2nd Box_plot 
#########################################################################################
# for table in tables:
#     display(table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35'])
#     topk=table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35']
#     minimalranks=table.loc[:,'MinimalRank']
#     if len(topk==4):
#         minimalrank_labels=['Whisker_top','Whisker_bottom','Quartile_top','Median']
#         x= minimalrank_labels
#         y1=topk.loc[0,:].values.tolist()
#         y2=topk.loc[1,:].values.tolist()
#         y3=topk.loc[2,:].values.tolist()
#         y4=topk.loc[3,:].values.tolist()
#         fig = plt.figure()
#         #plt.plot(y1,'r--',y2,'m--',y3,'y--',y4,'c--')
#         y1_plt=plt.plot(y1,'r--')
#         y2_plt=plt.plot(y2,'m--')
#         y3_plt=plt.plot(y3,'y--')
#         y4_plt=plt.plot(y4,'c--')
#         plt.legend([y1_plt[0],y2_plt[0],y3_plt[0],y4_plt[0]],['Whisker_top','Whisker_bottom','Quartile_top','Median'])
#         plt.xlabel('Top-K')
#         plt.ylabel('Accuracy')
#         plt.show() 
#     
#         df=pd.DataFrame({"Curve_Whisker_Top":y1,
#                          "Curve_Whisker_Bottom":y2,
#                          "Curve_Quartile_Top":y3,
#                          "Curve_Median":y4})
#         
#         df.set_axis(['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'], axis=0, inplace=True)
#         display("x-y plot data frame: ",df)
#         color=['r','m','y','c']
#         ax=df.plot(kind='line',color=color,title='top-k from 1st Box-Plot', linestyle='dashed', marker='o', markersize=10)
#         ax.set(ylabel='Accuracy', xlabel = 'Top-K')
#         plt.show()
       
#         if len(topk==2):    
#             minimalrank_labels=['Whisker_top','Whisker_bottom','Quartile_top','Median']
#             x= minimalrank_labels
#             y1=topk.loc[0,:].values.tolist()
#             y2=topk.loc[1,:].values.tolist() 
#         
#             df=pd.DataFrame({"Curve_Whisker_Top":y1,
#                      "Curve_Whisker_Bottom":y2})
#     
#             df.set_axis(['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'], axis=0, inplace=True)
#             display("x-y plot data frame: ",df)
#             color=['r','m','y','c']
#             ax=df.plot(kind='line',color=color,title='top-k from 2nd Box-Plot', linestyle='dashed', marker='o', markersize=10)
#             ax.set(ylabel='Accuracy', xlabel = 'Top-K')
#             plt.show()

#########################################################################################
# 2nd Box_plot with diagram size
#########################################################################################
# for table in tables:
#     table.set_axis(['DigramSize','TopkAccuracy@1','TopkAccuracy@5','TopkAccuracy@10','TopkAccuracy@15','TopkAccuracy@20',
#                       'TopkAccuracy@25','TopkAccuracy@30','TopkAccuracy@35'],inplace=True, axis=1)
#     display(table)
#     topk=table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35']
#     diagram_size=table.loc[:,'DigramSize']
#     if len(topk==100):
#         xaxis_labels=diagram_size.values.tolist()
#         x= xaxis_labels
#         y1=topk.loc[:,'TopkAccuracy@1'].values.tolist()
#         y2=topk.loc[:,'TopkAccuracy@5'].values.tolist()
#         y3=topk.loc[:,'TopkAccuracy@10'].values.tolist()
#         y4=topk.loc[:,'TopkAccuracy@15'].values.tolist()
#         y5=topk.loc[:,'TopkAccuracy@20'].values.tolist()
#         y6=topk.loc[:,'TopkAccuracy@25'].values.tolist()
#         y7=topk.loc[:,'TopkAccuracy@30'].values.tolist()
#         y8=topk.loc[:,'TopkAccuracy@35'].values.tolist()
#         fig = plt.figure()
#         #plt.plot(y1,'r--',y2,'m--',y3,'y--',y4,'c--')
#         y1_plt=plt.plot(y1,'r--')
#         y2_plt=plt.plot(y2,'m--')
#         y3_plt=plt.plot(y3,'y--')
#         y4_plt=plt.plot(y4,'c--')
#         y5_plt=plt.plot(y5,'b--')
#         y6_plt=plt.plot(y6,'k--')
#         y7_plt=plt.plot(y7,'c--')
#         y8_plt=plt.plot(y8,'g--')###'#17becf'
#         plt.legend([y1_plt[0],y2_plt[0],y3_plt[0],y4_plt[0],y5_plt[0],y6_plt[0],y7_plt[0],y8_plt[0]],
#                    ['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'])
#         plt.xlabel('Top-K')
#         plt.ylabel('Accuracy')
#         plt.show() 
#      
#         df=pd.DataFrame({"K=1":y1,
#                          "K=5":y2,
#                          "K=10":y3,
#                          "K=15":y4,
#                          "K=20":y5,
#                          "K=25":y6,
#                          "K=30":y7,
#                          "K=35":y8})
#         #np.arange(1,100) 
#         df.set_axis(x, axis=0, inplace=True)
#         display("x-y plot data frame: ",df)
#         color=['r','m','y','c','b','tab:brown','tab:gray','tab:orange']
#         ax=df.plot(kind='line',color=color,title='top-k from 2nd Box-Plot', linestyle='dashed', marker='o', markersize=8,figsize =(10, 8))
#         ax.set(ylabel='Accuracy', xlabel = 'Diagram Size')
#         plt.show()
    