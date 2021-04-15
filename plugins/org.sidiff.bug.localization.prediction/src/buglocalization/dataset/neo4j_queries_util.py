from typing import List
import buglocalization.dataset.neo4j_queries as query
import pandas as pd
from buglocalization.metamodel.meta_model import MetaModel
from py2neo import Graph, Node


def get_label(node: Node) -> str:
    """
    Args:
        node (Node): A Neo4j node.

    Returns:
        str: The labels of the node as a single string.
    """
    return ':'.join(node.labels)


def get_neo4j_node_id(graph: Graph, model_element_id: str, db_version: int) -> int:
    """
    Args:
        graph (Graph): The Neo4j graph connection.
        model_element_id (str): The XMI-ID of the model element.
        db_version (int): The version number in the database.

    Returns:
        (int): The resolved node ID in the database.
    """
    db_version_parameter = {'db_version': db_version}
    model_element = graph.run(query.nodes_in_version(model_element_id=model_element_id), db_version_parameter).to_data_frame()

    assert len(model_element.index) == 1, 'ID not unique in version.'
    neo4j_id = model_element.nodes.iloc[0].identity

    return neo4j_id


def subgraph_k(
        graph: Graph, neo4j_id: int, db_version: int, k: int, undirected: bool,
        meta_model: MetaModel, labels_mask: List[str] = None) -> pd.DataFrame:
    """
    Args:
        graph (Graph): The Neo4j graph connection.
        neo4j_id (int): The node ID in the Neo4j database
        db_version (int): The model version.
        k (int): The distance of the subgraph's nodes from the given start node.
        directed (bool): True if no relationship direction should be used; False to follow only outgoing relationships.
        meta_model (MetaModel): The meta-model definition of the subgraph.
        labels_mask (str, optional): Finally, filters the subgraph by the given label. Defaults to None.

    Returns:
        pd.DataFrame: The computed subgraph with distance k from the given node.
    """

    # Do not include paths that go into the SystemModel (wrapper), e.g., changes/bug locations:
    subgraph_query = query.neighborhood(k=k,
                                        labels_mask=labels_mask,
                                        undirected=undirected)
    db_version_parameter = {'node_id': neo4j_id, 'db_version': db_version}
    subgraph = graph.run(subgraph_query, db_version_parameter).to_data_frame()

    if not subgraph.empty:
        subgraph.set_index('index', inplace=True)

    return subgraph
