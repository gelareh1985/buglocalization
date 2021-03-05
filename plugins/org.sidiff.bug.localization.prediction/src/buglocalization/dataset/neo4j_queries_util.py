from py2neo import Graph, Node

import buglocalization.dataset.neo4j_queries as query


def get_label(node: Node) -> str:
    return ':'.join(node.labels)


def get_neo4j_node_id(graph: Graph, model_element_id: str, db_version: int) -> int:
    """
    Args:
        model_element_id (str): The XMI-ID of the model element.
        db_version (int): The version number in the database.

    Returns:
        (int): The resolved node ID in the database.
    """
    db_version_parrameter = {'db_version': db_version}
    model_element = graph.run(query.nodes_in_version(model_element_id=model_element_id), db_version_parrameter).to_data_frame()

    assert len(model_element.index) == 1, 'ID not unique in version.'
    neo4j_id = model_element.nodes.iloc[0].identity

    return neo4j_id
