'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import os
import pandas as pd
import matplotlib.pyplot as plt

plot_data_path_jdt = r"C:/Users/gelareh/git/buglocalization/evaluation/SMC21/eclipse.jdt.core/eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation/"
plot_data_path_pde = r"C:/Users/gelareh/git/buglocalization/evaluation/SMC21/eclipse.pde.ui/eclipse.pde.ui_data-2021-04-09_model-2021-04-12_evaluation/"


def load_evaluation_results(path, flag):
    data_tables_jdt = []
    data_tables_jdt_core = []
    data_tables_pde = []
    data_tables_diagram_size_jdt = []
    data_tables_diagram_size_jdt_core = []
    data_tables_diagram_size_pde = []

    for filename in os.listdir(path):

        if filename.endswith("_diagrams-map_mrr_topk.csv"):

            if (flag == "JDT"):
                tbl_path = read_path(filename, path)
                tbl__data = pd.read_csv(tbl_path, sep=";", header=0)
                data_tables_jdt.append([tbl__data, flag])
            elif (flag == "PDE"):
                tbl_path = read_path(filename, path)
                tbl__data = pd.read_csv(tbl_path, sep=";", header=0)
                data_tables_pde.append([tbl__data, flag])
        elif filename.endswith("_diagrams-map_mrr_topk-jdt_core.csv"):
            if (flag == "JDT Core"):
                tbl_path = read_path(filename, path)
                tbl__data = pd.read_csv(tbl_path, sep=";", header=0)
                data_tables_jdt_core.append([tbl__data, flag])
        elif filename.endswith("_diagrams-k_neighbor_2.csv"):
            if (flag == "JDT"):
                tbl_path = read_path(filename, path)
                tbl__data = pd.read_csv(tbl_path, sep=";", header=0)
                data_tables_diagram_size_jdt.append([tbl__data, flag])
            elif (flag == "PDE"):
                tbl_path = read_path(filename, path)
                tbl__data = pd.read_csv(tbl_path, sep=";", header=0)
                data_tables_diagram_size_pde.append([tbl__data, flag])
        elif filename.endswith("_diagrams-k_neighbor_2_core.csv"):
            if (flag == "JDT Core"):
                tbl_path = read_path(filename, path)
                tbl__data = pd.read_csv(tbl_path, sep=";", header=0)
                data_tables_diagram_size_jdt_core.append([tbl__data, flag])

    if flag == "JDT":
        return data_tables_jdt, data_tables_diagram_size_jdt
    elif flag == "JDT Core":
        return data_tables_jdt_core, data_tables_diagram_size_jdt_core
    elif flag == "PDE":
        return data_tables_pde, data_tables_diagram_size_pde


def read_path(filename, path):
    tbl_filename = filename[:filename.rfind(".")]
    tbl_path = path + tbl_filename + ".csv"
    return tbl_path


def plot_topk(data_tables_jdt, data_tables_jdt_core, data_tables_pde, indx):
    list_jdt = []
    for table in data_tables_jdt:
        topk_data_jdt = table[0].loc[:, 'TopkAccuracy@1':'TopkAccuracy@15']
        print("jdt table: ", topk_data_jdt)
        y1 = topk_data_jdt.loc[indx, :].values.tolist()
        list_jdt.append([y1, table[1]])

    list_jdt_core = []
    for table in data_tables_jdt_core:
        topk_data_jdt_core = table[0].loc[:, 'TopkAccuracy@1':'TopkAccuracy@15']
        print("jdt core table: ", topk_data_jdt_core)
        y1 = topk_data_jdt_core.loc[indx, :].values.tolist()
        list_jdt_core.append([y1, table[1]])

    list_pde = []
    for table in data_tables_pde:
        topk_data_pde = table[0].loc[:, 'TopkAccuracy@1':'TopkAccuracy@15']
        print("pde table: ", topk_data_pde)
        y1 = topk_data_pde.loc[indx, :].values.tolist()
        list_pde.append([y1, table[1]])

    print("jdt: "+str(list_jdt), "\n", "pde"+str(list_pde))

    flag_jdt_str = "IdentiBug (Project "+list_jdt[0][1]+")"
    df1 = pd.DataFrame({flag_jdt_str: list_jdt[0][0]})
    df1.set_axis(['K=1', 'K=5', 'K=10', 'K=15'], axis=0, inplace=True)
    print(df1)

    flag_jdt_core_str = "IdentiBug (Project "+list_jdt_core[0][1]+")"
    df11 = pd.DataFrame({flag_jdt_core_str: list_jdt_core[0][0]})
    df11.set_axis(['K=1', 'K=5', 'K=10', 'K=15'], axis=0, inplace=True)
    print(df11)

    flag_pde_str = "IdentiBug (Project "+list_pde[0][1]+")"
    df2 = pd.DataFrame({flag_pde_str: list_pde[0][0]})
    df2.set_axis(['K=1', 'K=5', 'K=10', 'K=15'], axis=0, inplace=True)
    print(df2)

    df3 = pd.concat([df1, df11, df2], axis=1)
    print("concatenated data-frame: ", df3)

    df_other_methods = pd.DataFrame({"KGBugLocator": [0.437, 0.691, 0.759, 0.828], "CAST": [0.432, 0.681, 0.757, 0.818]})
    df_other_methods.set_axis(['K=1', 'K=5', 'K=10', 'K=15'], axis=0, inplace=True)
    df4 = pd.concat([df3, df_other_methods], axis=1)
    print("x-y plot data frame: ", df4)

    color = ['r', 'm', 'c', 'y', 'tab:orange']
    if indx == 0:
        title_plot = 'Top-K from Ranked List of Class Diagrams'
    elif indx == 1:
        title_plot = 'Top-K from Ranked List of Class Diagrams (Without Outliers)'
    ax = df4.plot(kind='line', color=color, title=title_plot, linestyle='dashed', marker='o', markersize=10)
    ax.set(ylabel='Accuracy', xlabel='Top-K')
    plt.ylim(0.0, 1.0)
    plt.show()


def plot_diagram_size(tables):
    for table in tables:
        table[0].set_axis(['DigramSize', 'TopkAccuracy@1', 'TopkAccuracy@5', 'TopkAccuracy@10', 'TopkAccuracy@15', 'TopkAccuracy@20',
                          'TopkAccuracy@25', 'TopkAccuracy@30', 'TopkAccuracy@35'], inplace=True, axis=1)
        print(table[0])
        topk = table[0].loc[0:10, 'TopkAccuracy@1':'TopkAccuracy@35']
        diagram_size = table[0].loc[0:10, 'DigramSize']

        xaxis_labels = diagram_size.values.tolist()
        x = xaxis_labels
        y1 = topk.loc[:, 'TopkAccuracy@1'].values.tolist()
        y2 = topk.loc[:, 'TopkAccuracy@5'].values.tolist()
        y3 = topk.loc[:, 'TopkAccuracy@10'].values.tolist()
        y4 = topk.loc[:, 'TopkAccuracy@15'].values.tolist()
        y5 = topk.loc[:, 'TopkAccuracy@20'].values.tolist()
        y6 = topk.loc[:, 'TopkAccuracy@25'].values.tolist()
        y7 = topk.loc[:, 'TopkAccuracy@30'].values.tolist()
        y8 = topk.loc[:, 'TopkAccuracy@35'].values.tolist()

        df = pd.DataFrame({"K=1": y1,
                           "K=5": y2,
                          "K=10": y3,
                           "K=15": y4,
                           "K=20": y5,
                           "K=25": y6,
                           "K=30": y7,
                           "K=35": y8})
        df.set_axis(x, axis=0, inplace=True)
        print("x-y plot data frame: ", df)
        color = ['r', 'm', 'y', 'c', 'b', 'tab:brown', 'tab:gray', 'tab:orange']
        flag_str = "top-k from Ranked List of Class Diagrams of Project "+table[1]+" (search_depth=2)"
        ax = df.plot(kind='line', color=color, title=flag_str, linestyle='dashed', marker='o', markersize=8)
        ax.set(ylabel='Accuracy', xlabel='Diagram Size')
        plt.ylim(0.0, 1.0)
        plt.show()

def plot_map_mrr(tables):
    for table in tables:
        print(table[0].loc[:,'MeanAveragePrecision':'MeanReciprocalRank'])
        mrr_map = table[0].loc[:, 'MeanAveragePrecision':'MeanReciprocalRank']
        minimalranks=table[0].loc[:,'MinimalRank']

        minimalrank_labels=['Whisker_top','Whisker_bottom','Quartile_top','Median']
        x= minimalrank_labels
        y1=mrr_map.loc[0,:].values.tolist()
        y2=mrr_map.loc[1,:].values.tolist()
        y3=mrr_map.loc[2,:].values.tolist()
        y4=mrr_map.loc[3,:].values.tolist()
        df = pd.DataFrame({"Curve_Whisker_Top": y1,
                           "Curve_Whisker_Bottom": y2,
                           "Curve_Quartile_Top": y3,
                           "Curve_Median": y4,
                            })
        df.set_axis(x, axis=1, inplace=True)
        print("x-y plot data frame: ", df)
        color = ['r', 'm', 'y', 'c', 'b', 'tab:brown', 'tab:gray', 'tab:orange']
        flag_str = "map_mrr from Class Diagrams of Project "+table[1]+" (search_depth=2)"
        ax = df.plot(kind='line', color=color, title=flag_str, linestyle='dashed', marker='o', markersize=8)
        ax.set(ylabel='Accuracy', xlabel='MAP/MRR')
        plt.ylim(0.0, 1.0)
        plt.show()

# #########################################################################################
# # Final results plot Top-k for SMC paper
# #########################################################################################
data_tables_jdt, data_tables_diagram_size_jdt = load_evaluation_results(plot_data_path_jdt, flag="JDT")
data_tables_jdt_core, data_tables_diagram_size_jdt_core = load_evaluation_results(plot_data_path_jdt, flag="JDT Core")
data_tables_pde, data_tables_diagram_size_pde = load_evaluation_results(plot_data_path_pde, flag="PDE")

#plot_topk(data_tables_jdt,data_tables_pde,indx=0)
plot_topk(data_tables_jdt, data_tables_jdt_core, data_tables_pde, indx=0)
plot_topk(data_tables_jdt, data_tables_jdt_core, data_tables_pde, indx=1)  # without OutLiers

plot_diagram_size(tables=data_tables_diagram_size_jdt)
plot_diagram_size(tables=data_tables_diagram_size_jdt_core)
plot_diagram_size(tables=data_tables_diagram_size_pde)

plot_map_mrr(tables=data_tables_jdt)
plot_map_mrr(tables=data_tables_jdt_core)
plot_map_mrr(tables=data_tables_pde)
