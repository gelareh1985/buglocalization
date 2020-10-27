'''
Created on Oct 26, 2020

@author: Gelareh_mp
'''
import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
import pylab
import os

str_path1=r"D:\\MDEAI_Files_Original\\Datasets\\DataSet_20200925144220\\training\\00010_bug_12326_version_8879559d00cf4c7ff860cd544f1fd232ff4b2378.edgelist"
str_path2=r"D:\\MDEAI_Files_Original\\Datasets\\DataSet_20200925144220\\training\\00010_bug_12326_version_8879559d00cf4c7ff860cd544f1fd232ff4b2378.nodelist"

def netx_graph1():
    G = nx.Graph()
    G.add_edges_from(
        [('A', 'B'), ('A', 'C'), ('D', 'B'), ('E', 'C'), ('E', 'F'),
         ('B', 'H'), ('B', 'G'), ('B', 'F'), ('C', 'G')])
    
    val_map = {'A': 1.0,
               'D': 0.5714285714285714,
               'H': 0.0}
    
    values = [val_map.get(node, 0.25) for node in G.nodes()]
    
    nx.draw(G, cmap = plt.get_cmap('jet'), node_color = values)
    plt.show()

def netx_graph2():
    G = nx.DiGraph()
    G.add_edges_from(
        [('A', 'B'), ('A', 'C'), ('D', 'B'), ('E', 'C'), ('E', 'F'),
         ('B', 'H'), ('B', 'G'), ('B', 'F'), ('C', 'G')])
    
    val_map = {'A': 1.0,
               'D': 0.5714285714285714,
               'H': 0.0}
    
    values = [val_map.get(node, 0.25) for node in G.nodes()]
    
    # Specify the edges you want here
    red_edges = [('A', 'C'), ('E', 'C')]
    edge_colours = ['black' if not edge in red_edges else 'red'
                    for edge in G.edges()]
    black_edges = [edge for edge in G.edges() if edge not in red_edges]
    
    # Need to create a layout when doing
    # separate calls to draw nodes and edges
    pos = nx.spring_layout(G)
    nx.draw_networkx_nodes(G, pos, cmap=plt.get_cmap('jet'), 
                           node_color = values, node_size = 500)
    nx.draw_networkx_labels(G, pos)
    nx.draw_networkx_edges(G, pos, edgelist=red_edges, edge_color='r', arrows=True)
    nx.draw_networkx_edges(G, pos, edgelist=black_edges, arrows=False)
    plt.show()

def netx_graph3():
    G = nx.DiGraph()
    G.add_node("A")
    G.add_node("B")
    G.add_node("C")
    G.add_node("D")
    G.add_node("E")
    G.add_node("F")
    G.add_node("G")
    G.add_edge("A","B")
    G.add_edge("B","C")
    G.add_edge("C","E")
    G.add_edge("C","F")
    G.add_edge("D","E")
    G.add_edge("F","G")
    
    print(G.nodes())
    print(G.edges())
    
    pos = nx.spring_layout(G)
    
    nx.draw_networkx_nodes(G, pos)
    nx.draw_networkx_labels(G, pos)
    nx.draw_networkx_edges(G, pos, edge_color='r', arrows = True)
    
    plt.show()
    
def netx_graph4(array_file1):   
    G = nx.DiGraph()
#     i=0    
#     for k in range(0,len(array_from_file2)):
#         if i in range(0,len(array_from_file2[k])):
#             G.add_node(array_from_file2[k].item(i+1))
    a=np.array(['node1', 'node2', 'node3','node4','node5','node6','node7','node8'])
    for k in range(0,len(a)):
        G.add_node(a[k])
    i=0  
    for k in range(0,len(array_from_file1)):
        if i in range(0,len(array_from_file1[k])):
            G.add_edge(array_from_file1[k].item(i),array_from_file1[k].item(i+1))
    print(G.nodes()," keys: ",G.nodes.keys(), " values: ",G.nodes.values())
    print(G.edges())
    pos = nx.spring_layout(G)   
    nx.draw_networkx_nodes(G, pos)
    nx.draw_networkx_labels(G, pos)
    nx.draw_networkx_edges(G, pos, edge_color='r', arrows = True)
    plt.show()
     
def netx_graph5():
    G = nx.DiGraph()
#     G.add_nodes_from(['node1', 'node2', 'node3', 'node4', 'node5', 'node6', 'node7', 'node8'])
    G.add_nodes_from(['A', 'B', 'C', 'D', 'E', 'F'])
    G.add_edges_from([('A', 'B'),('C','D'),('G','D')], weight=1)
    G.add_edges_from([('D','A'),('D','E'),('B','D'),('D','E')], weight=2)
    G.add_edges_from([('B','C'),('E','F')], weight=3)
    G.add_edges_from([('C','F')], weight=4)   
    
    edge_labels=dict([((u,v,),d['weight'])
                     for u,v,d in G.edges(data=True)])
    red_edges = [('C','D'),('D','A')]
    edge_colors = ['black' if not edge in red_edges else 'red' for edge in G.edges()]
    
    pos=nx.spring_layout(G)
    nx.draw_networkx_edge_labels(G,pos,edge_labels=edge_labels)
    #nx.draw(G,pos, node_color = values, node_size=1500,edge_color=edge_colors,edge_cmap=plt.cm.Reds)
#     options = {
#     'node_color': 'blue',
#     'node_size': 100,
#     'width': 3,
#     'arrowstyle': '-|>',
#     'arrowsize': 12,
#      }
#     nx.draw_networkx(G, arrows=True, **options)
    edge_labels=dict([((u,v,),d['weight'])
                 for u,v,d in G.edges(data=True)])
    red_edges = [('C','D'),('D','A')]
    edge_colors = ['black' if not edge in red_edges else 'red' for edge in G.edges()]
    pos=nx.spring_layout(G)
    nx.draw_networkx_edge_labels(G,pos,edge_labels=edge_labels)
    nx.draw(G,pos, node_size=1500,edge_color=edge_colors)
    pylab.show()
    
def netx_graph6():

    # Define a graph
    G = nx.Graph()
    G.add_edges_from([(1,2,{'weight':10, 'val':0.1}),
                      (1,4,{'weight':30, 'val':0.3}),
                      (2,3,{'weight':50, 'val':0.5}),
                      (2,4,{'weight':60, 'val':0.6}),
                      (3,4,{'weight':80, 'val':0.8})])
    # generate positions for the nodes
    pos = nx.spring_layout(G, weight=None)
    
    # create the dictionary with the formatted labels
    edge_labels = {i[0:2]:'${}'.format(i[2]['weight']) for i in G.edges(data=True)}
    
    # create some longer node labels
    node_labels = {n:"this is node {}".format(n) for n in range(1,5)}
    
    
    # draw the graph
    nx.draw_networkx(G, pos=pos, with_labels=False)
    
    # draw the custom node labels
    shifted_pos = {k:[v[0],v[1]+.04] for k,v in pos.items()}
    node_label_handles = nx.draw_networkx_labels(G, pos=shifted_pos,
            labels=node_labels)
    
    # add a white bounding box behind the node labels
    [label.set_bbox(dict(facecolor='white', edgecolor='none')) for label in
            node_label_handles.values()]
    
    # add the custom egde labels
    nx.draw_networkx_edge_labels(G, pos=pos, edge_labels=edge_labels) 
    # Axes settings (make the spines invisible, remove all ticks and set title)
    ax = plt.gca()
    [sp.set_visible(False) for sp in ax.spines.values()]
    ax.set_xticks([])
    ax.set_yticks([])
    plt.show()  
    
# def netx_graph7():
#     G = nx.Graph()
#     a=np.array(['node1', 'node2', 'node3','node4','node5','node6','node7','node8'])
# #     for k in range(0,len(a)):
# #         G.add_node(a[k]) #{'weight':10, 'val':0.1}
#     option_info={'weight':[10 , 30 , 50 , 60 , 80 , 70 , 15 , 25 , 35 , 45 ,55 , 65 , 75],
#                  'val':[0.1,0.3,0.5,0.6,0.8,0.7,0.15,0.25,0.35,0.45,0.55,0.65,0.75]}
#     i=0  
#     for k in range(0,len(array_from_file1)):
#         if i in range(0,len(array_from_file1[k])):
#             G.add_edge(array_from_file1[k].item(i),array_from_file1[k].item(i+1),option_info.keys())
#     G.add_edges_from([(array_from_file1[k],array_from_file1[k].item(i+1),)])
#     pos = nx.spring_layout(G, weight=None)   
#######################################################################################################################     
head, tail = os.path.split("D:/MDEAI_Files_Original/Datasets/DataSet_20200925144220/training/00010_bug_12326_version_8879559d00cf4c7ff860cd544f1fd232ff4b2378.edgelist")
print("tail: ",tail, "   ;   " ,"head: ", head)
#  
array_from_file1 = np.loadtxt("00010_bug_12326_version_8879559d00cf4c7ff860cd544f1fd232ff4b2378.edgelist", dtype=str)
#print(array_from_file1, "pairs in each line: ", array_from_file1[0], array_from_file1[12])

# for x in np.nditer(array_from_file):
#     print("array elements: ",x)
i=0
for k in range(0,len(array_from_file1)):
    #for i in range(0,len(array_from_file[k])):
    if i in range(0,len(array_from_file1[k])):
        print("array pairs: ",array_from_file1[k], "    ", len(array_from_file1[k]), "array pairs zoom: ",array_from_file1[k].item(0), 
              array_from_file1[k].item(1), " array pairs new:",array_from_file1[k].item(i)," array pairs new:",array_from_file1[k].item(i+1))
     
        #i=i+1     
    
print(array_from_file1.item(0), " ",array_from_file1.item(1), " size: ", len(array_from_file1), np.nditer(array_from_file1).itersize,  np.nditer(array_from_file1).iterindex)

# for i in array_from_file:#.shape[0]:
#     #for j in array_from_file.shape[1]:
#     print("element: ", "[", i,"]")

#array_from_file2 = np.loadtxt("00010_bug_12326_version_8879559d00cf4c7ff860cd544f1fd232ff4b2378.nodelist", dtype=str)
#print(array_from_file2)    
# i=0
# for k in range(0,len(array_from_file2)):
#     if i in range(0,len(array_from_file2[k])):
#         print("array pairs: ",array_from_file2[k], "    ", len(array_from_file2[k]), "array pairs zoom: ",array_from_file2[k].item(0), 
#               array_from_file2[k].item(1), " array pairs new:",array_from_file2[k].item(i)," array pairs new:",array_from_file2[k].item(i+1))
#      
# a=np.array(['node1', 'node2', 'node3','node4','node5','node6','node7','node8'])
#print(a, a[0])
# for x in np.nditer(a):
#     print(a)

#netx_graph1()
#netx_graph2()
#netx_graph3() 
#netx_graph4(array_from_file1)
#netx_graph5()
netx_graph6()

