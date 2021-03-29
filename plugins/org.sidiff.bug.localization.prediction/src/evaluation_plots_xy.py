'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import numpy as np
from IPython.display import display
import matplotlib.pyplot as plt


#plot_data_path=r"D:\buglocalization_gelareh_home\evaluations\charts and evaluation results\xy plots\trained_model_2021-03-04_02-06-04_train90_test10_layer300_k2_undirected_metrics/"
#plot_data_path=r"D:\buglocalization_gelareh_home\evaluations\charts and evaluation results\xy plots\trained_model_2021-03-04_02-06-04_train90_test10_layer300_metrics/"

plot_data_path=r"D:\buglocalization_gelareh_home\git\buglocalization\evaluation\eclipse.jdt.core\# trained_model_2021-03-13_16-16-02_lr-4_layer300_evaluation_new\evaluation_ranking_metrics/"
#plot_data_path=r"D:\buglocalization_gelareh_home\git\buglocalization\evaluation\eclipse.jdt.core\# trained_model_2021-03-13_16-16-02_lr-4_layer300_evaluation_new\evaluation_location_neighbours_diagram/"

def load_evaluation_results(path): 
    data_tables=[]
    table_names=[]
    for filename in os.listdir(path):
        
        #if filename.endswith("_topk.csv"):
        #if filename.endswith("_boxplot2.csv"): 
        #if filename.endswith("_0.csv"):
        #if filename.endswith("_test--k_neighbor_2.csv"):
        if filename.endswith("_test--map_mrr_topk.csv"):
            tbl_filename = filename[:filename.rfind(".")]
            tbl_path = path + tbl_filename + ".csv"
            tbl__data=pd.read_csv(tbl_path,sep=";",header=0)
            data_tables.append(tbl__data)
            table_names.append(filename)
            
    return data_tables

tables=load_evaluation_results(plot_data_path)

# #########################################################################################
# # 1st and 2nd Box_plots (MRR/MAP)
# #########################################################################################
# for table in tables:
#     display(table.loc[:,'MeanAveragePrecision':'MeanReciprocalRank'])
#     mrr_map=table.loc[:,'MeanAveragePrecision':'MeanReciprocalRank']
#     minimalranks=table.loc[:,'MinimalRank']
#     if len(mrr_map==4):
#         minimalrank_labels=['Whisker_top','Whisker_bottom','Quartile_top','Median']
#         x= minimalrank_labels
#         y1=mrr_map.loc[0,:].values.tolist()
#         y2=mrr_map.loc[1,:].values.tolist()
#         y3=mrr_map.loc[2,:].values.tolist()
#         y4=mrr_map.loc[3,:].values.tolist()
#         fig = plt.figure()
#         #plt.plot(y1,'r--',y2,'m--',y3,'y--',y4,'c--')
#         y1_plt=plt.plot(y1,'r--')
#         y2_plt=plt.plot(y2,'m--')
#         y3_plt=plt.plot(y3,'y--')
#         y4_plt=plt.plot(y4,'c--')
#         plt.legend([y1_plt[0],y2_plt[0],y3_plt[0],y4_plt[0]],['Whisker_top','Whisker_bottom','Quartile_top','Median'])
#         plt.xlabel('MAP/MRR')
#         plt.ylabel('Accuracy')
#         plt.show() 
#       
#         df=pd.DataFrame({"Curve_Whisker_Top":y1,
#                          "Curve_Whisker_Bottom":y2,
#                          "Curve_Quartile_Top":y3,
#                          "Curve_Median":y4})
#           
#         #df.set_axis(['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'], axis=0, inplace=True)
#         display("x-y plot data frame: ",df)
#         color=['r','m','y','c']
#         ax=df.plot(kind='line',color=color,title='MAP/MRR from Box-Plot (search_depth_k=2)', linestyle='dashed', marker='o', markersize=10)
#         ax.set(ylabel='Accuracy', xlabel = 'MAP/MRR')
#         plt.show()
#     print('average MAP:',mrr_map['MeanAveragePrecision'].mean())
#     print('average MRR:',mrr_map['MeanReciprocalRank'].mean())
# #########################################################################################
# # 1st and 2nd Box_plot (Top-K)
# #########################################################################################
for table in tables:
    display(table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35'])
    topk=table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35']
    minimalranks=table.loc[:,'MinimalRank']
    if len(topk==4):
        minimalrank_labels=['Whisker_top','Whisker_bottom','Quartile_top','Median']
        x= minimalrank_labels
        y1=topk.loc[0,:].values.tolist()
        y2=topk.loc[1,:].values.tolist()
        y3=topk.loc[2,:].values.tolist()
        y4=topk.loc[3,:].values.tolist()
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
        
        df=pd.DataFrame({"Curve_Whisker_Top":y1,
                         "Curve_Whisker_Bottom":y2,
                         "Curve_Quartile_Top":y3,
                         "Curve_Median":y4})
            
        df.set_axis(['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'], axis=0, inplace=True)
        display("x-y plot data frame: ",df)
        color=['r','m','y','c']
        ax=df.plot(kind='line',color=color,title='top-k from Box-Plot (search_depth_k=2)', linestyle='dashed', marker='o', markersize=10)
        ax.set(ylabel='Accuracy', xlabel = 'Top-K')
        plt.show()
          
        if len(topk==2):    
            minimalrank_labels=['With_Outliers','Without_Outliers','Quartile_top','Median']
            x= minimalrank_labels
            y1=topk.loc[0,:].values.tolist()
            y2=topk.loc[1,:].values.tolist() 
            
            df=pd.DataFrame({"IdentiBug":y1,
                     "IdentiBug (Filtering Outliers)":y2})
        
            df.set_axis(['K=1','K=5','K=10','K=15','K=20','K=25','K=30','K=35'], axis=0, inplace=True)
            display("x-y plot data frame: ",df)
            color=['r','m','y','c']
            ax=df.plot(kind='line',color=color,title='Top-K from Ranked List of Classifiers', linestyle='dashed', marker='o', markersize=10)
            ax.set(ylabel='Accuracy', xlabel = 'Top-K')
            plt.show()
             
            df2=df.copy()
            df2=df2.loc["K=1":"K=15",:]
            df3=pd.DataFrame({"KGBugLocator":[0.437,0.691,0.759,0.828],"CAST": [0.432,0.681,0.757,0.818]})
            df3.set_axis(['K=1','K=5','K=10','K=15'], axis=0, inplace=True)
            display(df3)
            df4=pd.concat([df2,df3],axis=1)
            display("x-y plot data frame: ",df4)
            color=['r','m','c','y','tab:orange']
            ax=df4.plot(kind='line',color=color,title='Top-K from Ranked List of Classifiers', linestyle='dashed', marker='o', markersize=10)
            ax.set(ylabel='Accuracy', xlabel = 'Top-K')
            plt.ylim(0.0,1.0)
            plt.show()
            
#########################################################################################
# diagram experiment for search_depth_k=2
#########################################################################################
# for table in tables:
#     table.set_axis(['DigramSize','TopkAccuracy@1','TopkAccuracy@5','TopkAccuracy@10','TopkAccuracy@15','TopkAccuracy@20',
#                       'TopkAccuracy@25','TopkAccuracy@30','TopkAccuracy@35'],inplace=True, axis=1)
#     display(table)
#     topk=table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35']
#     diagram_size=table.loc[:,'DigramSize']
#      
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
#         ax=df.plot(kind='line',color=color,title='top-k from Ranked List of Class Diagrams (search_depth=2)', linestyle='dashed', marker='o', markersize=8,figsize =(10, 10))
#         ax.set(ylabel='Accuracy', xlabel = 'Diagram Size')
#         plt.show()

#########################################################################################
# diagram experiment for search_depth_k=0 
#########################################################################################
# for table in tables:
#     table.set_axis(['DigramSize','TopkAccuracy@1','TopkAccuracy@5','TopkAccuracy@10','TopkAccuracy@15','TopkAccuracy@20',
#                       'TopkAccuracy@25','TopkAccuracy@30','TopkAccuracy@35'],inplace=True, axis=1)
#     display(table)
#     topk=table.loc[:,'TopkAccuracy@1':'TopkAccuracy@35']
#     diagram_size=table.loc[:,'DigramSize']
#      
#     y1=topk.loc[:,'TopkAccuracy@1'].values.tolist()
#     y2=topk.loc[:,'TopkAccuracy@5'].values.tolist()
#     y3=topk.loc[:,'TopkAccuracy@10'].values.tolist()
#     y4=topk.loc[:,'TopkAccuracy@15'].values.tolist()
#     y5=topk.loc[:,'TopkAccuracy@20'].values.tolist()
#     y6=topk.loc[:,'TopkAccuracy@25'].values.tolist()
#     y7=topk.loc[:,'TopkAccuracy@30'].values.tolist()
#     y8=topk.loc[:,'TopkAccuracy@35'].values.tolist()
#      
#     df=pd.DataFrame({"K=1":y1,
#                      "K=5":y2,
#                      "K=10":y3,
#                      "K=15":y4,
#                      "K=20":y5,
#                      "K=25":y6,
#                      "K=30":y7,
#                      "K=35":y8})
#  
#     display("x-y plot data frame: ",df) 
#     df_new=df.transpose()
#  
#     df_new.columns=["Diagram_Size=1"] 
#     display("transposed dataframe: ", df_new)
#     color=['r','m','y','c','b','tab:brown','tab:gray','tab:orange']
#     ax=df_new.plot(kind='line',color=color,title='top-k from Box-Plot (search_depth_k=0)', linestyle='dashed', marker='o', markersize=8,figsize =(10, 8))
#     ax.set(ylabel='Accuracy', xlabel = 'Top-K')
#     plt.show() 
#     
