import numpy as np
import pandas as pd
import stellargraph as sg
import os

from stellargraph.layer import GraphSAGE, link_classification
from stellargraph.mapper import GraphSAGELinkGenerator
from tensorflow import keras

dictionary = {"parameter" : 0, "shadowing" : 1, "nested" : 2}  # dummy

dataset_path = r"D:\files_MDEAI_original\Datasets\buglocations_dataset/" 


def load_dataset(dataset_path, dictionary, log=False):

    # Collect all graphs from the given folder:
    dataset_nodes = []
    dataset_edges = []
    dataset_bug_locations = []
    
    for filename in os.listdir(dataset_path):
        if filename.endswith(".nodelist"):
            graph_filename = filename[:filename.rfind(".")]
            node_list_path = dataset_path + graph_filename + ".nodelist"
            edge_list_path = dataset_path + graph_filename + ".edgelist"
            graph_number = graph_filename[0:graph_filename.find("_")]
    
            # nodes:
            nodes_data = load_nodes(node_list_path, graph_number)
            nodes_features = node_to_vector(nodes_data, dictionary)
            dataset_nodes.append(nodes_features)
            
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
            
    return dataset_nodes, dataset_edges, dataset_bug_locations


def load_nodes(node_list_path, graph_number):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(node_list_path, names=node_list_col_names, index_col="index")
    node_data = node_data.rename(index=lambda index: add_prefix(graph_number, index))
    node_data = node_data.fillna("")
    return node_data


def node_to_vector(node_data, dictionary):
    node_feature_data = pd.DataFrame(index=node_data.index, columns=dictionary)
    
    # Initialize with 0:
    for column in node_feature_data.columns:
        node_feature_data[column].values[:] = 0
    
    # Encode words:
    for index, row in node_data.iterrows():
        feature_row = node_feature_data.loc[index]
        
        text = row["text"]
        words = text_to_words(text)
        
        for word in words:
            if word in dictionary:
                feature_row[dictionary[word]] = 1
            
    return node_feature_data


def text_to_words(text):
    return text.split(" ")


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
        type_name = node_row["type"]
        tag = node_row["tag"]
        
        if (type_name == "BugReportNode"):
            bug_report_node = node_index
        elif (type_name == "BugReportCommentNode"):
            pass
        elif (tag == "# LOCATION"):
            bug_location_node = node_index
            bug_locations.append([bug_report_node, bug_location_node])
        else:
            # Assuming that the data set starts with the bug report and its locations.
            break  # optimization
        
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
dataset_nodes, dataset_edges, dataset_bug_locations = load_dataset(dataset_path, dictionary, log=True)

# Positive training set:
nodes_train = pd.concat(dataset_nodes[1::2]) # split 50% odd
edges_train = pd.concat(dataset_edges[1::2]) # split 50% odd
bug_locations_train = unpack(dataset_bug_locations[1::2]) # split 50% odd
edge_labels_train = np.ones(len(bug_locations_train))

# Positive test set:
nodes_test = pd.concat(dataset_nodes[::2]) # split 50% even
edges_test = pd.concat(dataset_edges[::2]) # split 50% even
bug_locations_test = unpack(dataset_bug_locations[::2]) # split 50% even
edge_labels_test = np.ones(len(bug_locations_test))

# Create Stellar graphs:       
G_train = sg.StellarGraph(nodes_train, edges_train)
G_test = sg.StellarGraph(nodes_test, edges_test)

###################################################################################################
# Create AI Model:
###################################################################################################

batch_size = 20
epochs = 20
num_samples = [20, 10]

train_gen = GraphSAGELinkGenerator(G_train, batch_size, num_samples)
train_flow = train_gen.flow(bug_locations_train, edge_labels_train, shuffle=True)

test_gen = GraphSAGELinkGenerator(G_test, batch_size, num_samples)
test_flow = test_gen.flow(bug_locations_test, edge_labels_test)

layer_sizes = [20, 20]

graphsage = GraphSAGE(
    layer_sizes=layer_sizes, generator=train_gen, bias=True, dropout=0.3
)

# Build the model and expose input and output sockets of GraphSAGE model for link prediction
x_inp, x_out = graphsage.in_out_tensors()

prediction = link_classification(
    output_dim=1, output_act="relu", edge_embedding_method="ip"
)(x_out)

model = keras.Model(inputs=x_inp, outputs=prediction)

model.compile(
    optimizer=keras.optimizers.Adam(lr=1e-3),
    loss=keras.losses.binary_crossentropy,
    metrics=["acc"],
)

###################################################################################################
# Train and Evaluate AI Model:
###################################################################################################

init_train_metrics = model.evaluate(train_flow)
init_test_metrics = model.evaluate(test_flow)

print("\nTrain Set Metrics of the initial (untrained) model:")
for name, val in zip(model.metrics_names, init_train_metrics):
    print("\t{}: {:0.4f}".format(name, val))

print("\nTest Set Metrics of the initial (untrained) model:")
for name, val in zip(model.metrics_names, init_test_metrics):
    print("\t{}: {:0.4f}".format(name, val))
    
history = model.fit(train_flow, epochs=epochs, validation_data=test_flow, verbose=2)
sg.utils.plot_history(history)

train_metrics = model.evaluate(train_flow)
test_metrics = model.evaluate(test_flow)

print("\nTrain Set Metrics of the trained model:")
for name, val in zip(model.metrics_names, train_metrics):
    print("\t{}: {:0.4f}".format(name, val))

print("\nTest Set Metrics of the trained model:")
for name, val in zip(model.metrics_names, test_metrics):
    print("\t{}: {:0.4f}".format(name, val))
