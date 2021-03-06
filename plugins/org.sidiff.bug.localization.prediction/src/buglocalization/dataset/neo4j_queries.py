
# # Subgraph Cypher queries # #


from typing import List


def buggy_versions() -> str:
    match = 'MATCH (b:TracedBugReport)-[:modelLocations]->(c:Change)-[:location]->(e)'
    returns = ' RETURN DISTINCT b.__initial__version__ AS versions ORDER BY versions'
    return match + returns


def version(returns: str = 'RETURN v') -> str:
    return 'MATCH (v:TracedVersion) ' + where_version('v') + ' ' + returns


def library_elements(label: str) -> str:
    """
    Args:
        type (str): a type label

    Returns:
        str: All nodes of the given type that were created as "external" library element, e.g., Java types like String, Integer.
    """
    return 'MATCH (l:' + label + ') WHERE ' + is_library_element('l') + ' RETURN ID(l) AS nodes'


def is_library_element(variable: str) -> str:
    return variable + '.__model__element__id__ STARTS WITH "library/"'


def edge_containment(is_value: bool) -> str:
    if is_value:
        return '__containment__:true'
    else:
        return '__containment__:false'


def edge_container(is_value: bool) -> str:
    if is_value:
        return '__container__:true'
    else:
        return '__container__:false'


def edges_to_parent_nodes(k=2) -> str:
    return path(edge_containment(True), 'b', k)


def edges_to_child_nodes(k=2) -> str:
    return path(edge_containment(True), 'a', k)


def outgoing_cross_tree_edges(k=2) -> str:
    return path(edge_containment(False) + ', ' + edge_container(False), 'a', k)


def incoming_cross_tree_edges(k=1) -> str:
    return path(edge_containment(False) + ', ' + edge_container(False), 'b', k)


def path(edge_properties: str, start_variable: str, k=2, return_query: str = None) -> str:
    """
    Args:
        edge_properties (str): The properties on the edges to be matched. Noted nameA:valueX, nameb:valueY,...
        start_variable (str): 'a' means traverses outgoing edges, 'b' means traverses incoming edges
        k (int, optional): The maximum distance from the start node. Defaults to 2.
        return_query (str, optional): Custom return statement. Defaults to None.

    Returns:
        [str]: The path with node IDs as 'source' and 'target' columns.
    """
    input_nodes = 'UNWIND $node_ids AS node_id '
    node_path_in_version = 'ID(' + start_variable + ')= node_id AND ' + edges_no_dangling('a', 'b')
    edge_path_in_version = 'UNWIND [edge IN relationships(path) WHERE ' + by_version('edge') + '] AS edge'

    if return_query is None:
        return_query = 'RETURN DISTINCT ID(edge) AS index, ID(a) AS source, ID(b) as target'

    path_filter = 'WHERE ' + node_path_in_version + ' WITH DISTINCT a, b, path ' + edge_path_in_version + ' ' + return_query
    return input_nodes + ' MATCH path=(a)-[*0..' + str(k) + ' {' + edge_properties + '}]->(b) ' + path_filter


def subgraph_k(k: int, node_id: int, labels_mask: List[str] = None, labels_blacklist: List[str] = None, undirected: bool = False) -> str:
    """
    Args:
        k (int): Hops from the started node (traversed edges).
        node_id (int): The start node ID
        labels_mask (str, optional): Finally, filters the subgraph by the given label. Defaults to None.
        labels_blacklist (str, optional): Node labels that should not be on any path.
        undirected (bool): True if no relationship direction should be used; False to follow only outgoing relationships.

    Returns:
        str: The subgraph nodes, without the start node.
    """
    dircetion = '' if undirected else '>'
    query = 'MATCH p0=(k0)-[e0]-' + dircetion + '(k1) WHERE ID(k0) = ' + str(node_id)
    query += ' AND ' + by_version_path('k0', 'e0', 'k1')
    if labels_blacklist is not None:
        query += ' AND NOT ' + label_match(labels_blacklist, 'k1')
    query += ' WITH k1, p0'

    for distance in range(1, k):
        current_path = 'p' + str(distance)
        current_node = 'k' + str(distance)
        current_edge = 'e' + str(distance) 
        next_node = 'k' + str(distance + 1)
        
        query += ' MATCH ' + current_path + '=(' + current_node + ')-[' + current_edge + ']-' + dircetion + '(' + next_node + ')'
        query += ' WHERE ' + by_version_path(current_node, current_edge, next_node) 
        if labels_blacklist is not None:
            query += ' AND NOT ' + label_match(labels_blacklist, next_node)
        query += ' WITH ' + next_node
        
        for paths in range(0, distance + 1):
            query += ', p' + str(paths)

    query += ' WITH NODES(p0)'

    for distance in range(1, k):
        query += ' + NODES(p' + str(distance) + ')'

    query += ' AS nodes UNWIND nodes AS n'
    
    # Filter by label:
    if labels_mask is not None:
        query += ' WITH n WHERE ' + label_match(labels_mask, 'n') 
    
    query += ' RETURN DISTINCT ID(n) AS index, n AS nodes'
    return query


def label_match(labels: List[str], variable: str):
    query = ''
    
    if labels:
        query += '('
        for label_idx in range(len(labels)):
            if (label_idx > 0):
                query += ' OR'
            query += ' "' + labels[label_idx] + '" IN LABELS(' + variable + ')'
        query += ')'
        
    return query


def edges_no_dangling(source_variable: str, target_variable: str) -> str:
    # TODO: We might remove this check for performance improvement!?
    return by_version(source_variable) + ' AND ' + by_version(target_variable)


def by_version_path(variableA: str, edge: str, variableB: str) -> str:
    return by_version(variableA) + ' AND ' + by_version(edge) + ' AND ' + by_version(variableB)


def by_version(variable: str) -> str:
    created_in_version = variable + '.__initial__version__ <= $db_version'
    removed_in_version = variable + '.__last__version__ >= $db_version'
    existing_in_latest_version = 'NOT EXISTS(' + variable + '.__last__version__)'
    return created_in_version + ' AND ' + '(' + removed_in_version + ' OR ' + existing_in_latest_version + ')'


def where_version(variable: str) -> str:
    return 'WHERE ' + by_version(variable)

# # Some Cypher queries for testing # #


def node_by_id(node_id: int) -> str:
    return 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' RETURN n'


def nodes_by_ids(return_query: str) -> str:
    # $node_ids: List[int]
    return 'MATCH (n) WHERE ID(n) IN $node_ids ' + return_query


def edge_by_id(edge_id: int) -> str:
    return 'MATCH (s)-[r]-(t) WHERE ID(r)=' + str(edge_id) + ' RETURN s, r, t'


def initial_repo_version(node_id: int) -> str:
    node_version = 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' WITH n.__initial__version__ AS version'
    version_node = 'MATCH (vn:TracedVersion {__initial__version__:version}) RETURN vn.modelVersionID'
    return node_version + ' ' + version_node


def last_repo_version(node_id: int) -> str:
    node_version = 'MATCH (n) WHERE ID(n)=' + str(node_id) + ' WITH n.__last__version__ AS version'
    version_node = 'MATCH (vn:TracedVersion {__last__version__:version}) RETURN vn.modelVersionID'
    return node_version + ' ' + version_node

# # Read version information Cypher query # #


def db_version_by_code_repo_version() -> str:
    return 'MATCH (n:TracedVersion {modelVersionID:$repo_version}) RETURN n.__initial__version__'


def db_version_by_model_repo_version() -> str:
    return 'MATCH (n:TracedVersion {codeVersionID:$repo_version}) RETURN n.__initial__version__'


def code_repo_version_by_db_version() -> str:
    return 'MATCH (n:TracedVersion {__initial__version__:$db_version}) RETURN n.codeVersionID'


def model_repo_version_by_db_version() -> str:
    return 'MATCH (n:TracedVersion {__initial__version__:$db_version}) RETURN n.modelVersionID'

# # Read property/attribute Cypher query # #


def property_value_in_version(labels: str, property_name: str) -> str:
    # $db_version: int
    return 'MATCH (n:' + labels + ') WHERE ' + by_version('n') + ' RETURN n.' + property_name

# # Full graph Cypher queries # #


def nodes_by_type(labels: str) -> str:
    return 'MATCH (n:' + labels + ') RETURN ID(n) AS index, n as nodes'


def nodes_in_version(labels: str = '', model_element_id: str = '') -> str:
    # $db_version: int
    if labels != '':
        labels = ':' + labels
    if model_element_id != '':
        model_element_id = ' { __model__element__id__: "' + model_element_id + '"}'
    return 'MATCH (n' + labels + model_element_id + ') WHERE ' + by_version('n') + ' RETURN ID(n) AS index, n AS nodes'


def nodes_by_types_in_version(labels: List[str], by_node_id: bool) -> str:
    # $db_version: int
    match = ''
    returns = ' RETURN ID(n) AS index, n AS nodes'

    for label_idx in range(len(labels)):
        if label_idx > 0:
            match += returns + ' UNION '
        match += 'MATCH (n:' + labels[label_idx] + ') '
        match += where_version('n')
        if by_node_id:
            match += ' AND ID(n) IN $node_ids'

    return match + returns


def random_nodes_in_version(count: int, labels: str = '', filter_library_elements: bool = True) -> str:
    if labels != '':
        labels = ':' + labels
    return_query = 'RETURN n AS nodes, rand() AS r ORDER BY r LIMIT ' + str(count)
    return 'MATCH (n' + labels + ') WHERE ' + by_version('n') + ' AND NOT ' + is_library_element('n') + ' ' + return_query


def edges_in_version(
        source_labels: str = '',
        edge_labels: str = '',
        target_labels: str = '',
        return_nodes: bool = False) -> str:

    if source_labels != '':
        source_labels = ':' + source_labels
    if edge_labels != '':
        edge_labels = ':' + edge_labels
    if target_labels != '':
        target_labels = ':' + target_labels

    if return_nodes:
        return_query = 'RETURN ID(r) AS index, ID(s) AS source, s AS source_node, ID(t) AS target, t AS target_node'
    else:
        return_query = 'RETURN ID(r) AS index, ID(s) AS source, ID(t) AS target'

    is_in_version = 'WHERE ' + by_version('r') + ' AND ' + edges_no_dangling('s', 't')
    match_query = 'MATCH (s' + source_labels + ')-[r' + edge_labels + ']->(t' + target_labels + ') ' + is_in_version

    return match_query + ' ' + return_query


def edges_from_nodes_in_version() -> str:
    return 'MATCH (a)-[e]->(b) WHERE ID(a) IN $node_ids AND ID(b) IN $node_ids AND ' + by_version('e') + ' RETURN ID(e) as index, ID(a) AS source, ID(b) AS target'  # noqa: E501
