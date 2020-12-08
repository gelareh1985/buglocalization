import numpy as np
import pandas as pd
import stellargraph as sg
import os

from stellargraph.layer import GraphSAGE, link_classification
from stellargraph.mapper import GraphSAGELinkGenerator
from tensorflow import keras
from sklearn.model_selection import train_test_split
from IPython.display import display, HTML


#dataset_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\buglocations_1000/"
#feature_node_save_path=r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\saved files\nodelist_features/"

dataset_path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\smalltest/"

def load_dataset(dataset_path, log=False):

    # Collect all graphs from the given folder:
    dataset_nodes = []
    dataset_edges = []
    dataset_bug_locations = []
    
    feature_size = None
    
    for filename in os.listdir(dataset_path):
        if filename.endswith(".edgelist"):
            graph_filename = filename[:filename.rfind(".")]
            edge_list_path = dataset_path + graph_filename + ".edgelist"
            node_list_path = dataset_path + "/features/" + graph_filename + ".featurenodelist"
            graph_number = graph_filename[0:graph_filename.find("_")]
    
            if (feature_size == None):
                feature_size = get_feature_size(node_list_path)
    
            # nodes:
            nodes_data = load_nodes(node_list_path, feature_size, graph_number)
            dataset_nodes.append(nodes_data)
            
            # edges:
            edge_data = load_edges(edge_list_path, graph_number)
            dataset_edges.append(edge_data)
            
            # bug locations:
            bug_locations = extract_bug_locations(nodes_data)
            dataset_bug_locations.append(bug_locations)
            
            # remove bug location edges:
            remove_bug_location_edges(edge_data, bug_locations)
            
            if log:
                print("Graph: ", graph_number)
    
    if(not dataset_nodes or not dataset_edges or not dataset_bug_locations):
        raise Exception('No samples found')
    #df=dataset_nodes[["index","text", "type"]]
    return dataset_nodes, dataset_edges, dataset_bug_locations

# Assumes Tables: index, n-feature, tag

def get_feature_size(node_list_path):
    return len(pd.read_table(node_list_path).columns) - 2

def load_nodes(node_list_path, feature_size, graph_number):
    
    # Column names:
    node_data_columns = []
    node_data_columns.append("index")
    
    for feature_num in range(0, feature_size):
        node_data_columns.append("feature" + str(feature_num))
    
    node_data_columns.append("tag")
    
    # Load data:
    node_data = pd.read_table(node_list_path, names=node_data_columns)
    
    # Create index:
    node_data.set_index("index", inplace=True)
    node_data = node_data.rename(index=lambda index: add_prefix(graph_number, index))
    node_data = node_data.fillna("")
    
    return node_data


def load_edges(edge_list_path, graph_number):
    edge_list_col_names = ["source", "target"]
    edge_data = pd.read_table(edge_list_path, names=edge_list_col_names)
    
    edge_data['source'] = edge_data['source'].apply(lambda index: add_prefix(graph_number, index))
    edge_data['target'] = edge_data['target'].apply(lambda index: add_prefix(graph_number, index))
    edge_data = edge_data.rename(index=lambda index: add_prefix(graph_number, index))
    
    return edge_data


def add_prefix(prefix, index):
    return str(prefix) + "_" + str(index)


def extract_bug_locations(node_data):
    bug_locations = []
    bug_report_node = None
    
    for node_index, node_row in node_data.iterrows():
        tag = node_row["tag"]
        
        if (tag == "# REPORT"):
            bug_report_node = node_index
        elif (tag == "# LOCATION"):
            bug_location_node = node_index
            bug_locations.append([bug_report_node, bug_location_node])
    
    if (bug_report_node is None):
        raise Exception("Error: Bug report node not found!")
    
    # Remove the column 'tag' - it is not a numerical column:
    node_data.drop("tag", inplace=True, axis=1)
    return bug_locations


def remove_bug_location_edges(edge_data, bug_locations):
    bug_location_edges = []
    
    for bug_location in bug_locations:
        for edge_index, edge_row in edge_data.iterrows():
            if edge_row[0] == bug_location[0] and edge_row[1] == bug_location[1]:
                bug_location_edges.append(edge_index)
                break
        
    edge_data.drop(index=bug_location_edges, inplace=True)


def unpack(packed_list):
    unpacked_list = []
    
    for sublist in packed_list:
        unpacked_list.extend(sublist)
        
    return unpacked_list

###################################################################################################
# Create Training and Test Data:
###################################################################################################

# Collect all graphs from the given folder:
dataset_nodes, dataset_edges, dataset_bug_locations = load_dataset(dataset_path, log=True)

#display(dataset_nodes)

# TODO: Negative samples!
# TODO: Check this -> Use 50% from historic versions and 50% from newer versions
split_data = len(dataset_nodes) / 2

# Positive training set:
nodes_train = pd.concat(dataset_nodes[0:split_data])
edges_train = pd.concat(dataset_edges[0:split_data])
bug_locations_train = unpack(dataset_bug_locations[0:split_data])
edge_labels_train = np.ones(len(bug_locations_train))
 
# Positive test set:
nodes_test = pd.concat(dataset_nodes[split_data:len(dataset_nodes)])
edges_test = pd.concat(dataset_edges[split_data:len(dataset_nodes)])
bug_locations_test = unpack(dataset_bug_locations[split_data:len(dataset_nodes)])
edge_labels_test = np.ones(len(bug_locations_test))

# listreport=nodes_train[nodes_train.isin(['# REPORT'])].stack()
# display(listreport)
# nodes_train.replace(listreport,np.ones(len(listreport)))

# display('size of nodes_train:    [',nodes_train.size, 'x',nodes_train.ndim,']')
# display(nodes_train)
# display('size of edges_train:    [',edges_train.size, 'x',edges_train.ndim,']')
# display(edges_train)
# display(dataset_bug_locations)

# Create Stellar graphs:

# for nodes_row in nodes_train.iterrows():
#     for nodes_column in nodes_row:
#         if isinstance(nodes_column, pd.Series):
#             if nodes_column.eq("# REPORT").any():
#                 indexCol = 0
#                 
#                 for value in nodes_column:
#                     indexCol += 1
#                     if value == "# REPORT":
#                         print(indexCol)
#                         print(value)

#print(nodes_train.eq("# REPORT").any(1))
    

G_train = sg.StellarGraph(nodes_train, edges_train)
print(G_train.info())
G_test = sg.StellarGraph(nodes_train, edges_test)
print(G_test.info())

###################################################################################################
# Create AI Model:
###################################################################################################
# 
# batch_size = 20
# epochs = 20
# num_samples = [20, 10]
# 
# train_gen = GraphSAGELinkGenerator(G_train, batch_size, num_samples)
# train_flow = train_gen.flow(bug_locations_train, edge_labels_train, shuffle=True)
# 
# test_gen = GraphSAGELinkGenerator(G_test, batch_size, num_samples)
# test_flow = test_gen.flow(bug_locations_test, edge_labels_test)
# 
# layer_sizes = [20, 20]
# 
# graphsage = GraphSAGE(
#     layer_sizes=layer_sizes, generator=train_gen, bias=True, dropout=0.3
# )
# 
# # Build the model and expose input and output sockets of GraphSAGE model for link prediction
# x_inp, x_out = graphsage.in_out_tensors()
# 
# prediction = link_classification(
#     output_dim=1, output_act="relu", edge_embedding_method="ip"
# )(x_out)
# 
# model = keras.Model(inputs=x_inp, outputs=prediction)
# 
# model.compile(
#     optimizer=keras.optimizers.Adam(lr=1e-3),
#     loss=keras.losses.binary_crossentropy,
#     metrics=["acc"],
# )
# 
# ###################################################################################################
# # Train and Evaluate AI Model:
# ###################################################################################################
# 
# init_train_metrics = model.evaluate(train_flow)
# init_test_metrics = model.evaluate(test_flow)
# 
# print("\nTrain Set Metrics of the initial (untrained) model:")
# for name, val in zip(model.metrics_names, init_train_metrics):
#     print("\t{}: {:0.4f}".format(name, val))
# 
# print("\nTest Set Metrics of the initial (untrained) model:")
# for name, val in zip(model.metrics_names, init_test_metrics):
#     print("\t{}: {:0.4f}".format(name, val))
#     
# history = model.fit(train_flow, epochs=epochs, validation_data=test_flow, verbose=2)
# sg.utils.plot_history(history)
# 
# train_metrics = model.evaluate(train_flow)
# test_metrics = model.evaluate(test_flow)
# 
# print("\nTrain Set Metrics of the trained model:")
# for name, val in zip(model.metrics_names, train_metrics):
#     print("\t{}: {:0.4f}".format(name, val))
# 
# print("\nTest Set Metrics of the trained model:")
# for name, val in zip(model.metrics_names, test_metrics):
#     print("\t{}: {:0.4f}".format(name, val))
