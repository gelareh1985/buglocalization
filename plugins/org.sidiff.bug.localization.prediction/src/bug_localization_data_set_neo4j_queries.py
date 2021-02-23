
# # Subgraph Cypher queries # #


def buggy_versions() -> str:
    return "MATCH (b:TracedBugReport)-[:modelLocations]->(c:Change)-[:location]->(e) RETURN DISTINCT b.__initial__version__ AS versions"


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


def path(edge_properties: str, start_variable: str, k=2, return_query: str=None) -> str:
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


def edges_no_dangling(source_variable: str, target_variable: str) -> str:
    # TODO: We might remove this check for performance improvement!?
    return by_version(source_variable) + ' AND ' + by_version(target_variable)


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


def property_value_in_version(meta_type_label: str, property_name: str) -> str:
    # $db_version: int
    return 'MATCH (n:' + meta_type_label + ') WHERE ' + by_version('n') + ' RETURN n.' + property_name

# # Full graph Cypher queries # #


def nodes_in_version(meta_type_label: str = '', node_ids: bool = False) -> str:
    if meta_type_label != '':
        meta_type_label = ':' + meta_type_label

    where = 'WHERE ' + by_version('n')

    if node_ids:
        where += ' AND ID(n) IN $node_ids'

    return 'MATCH (n' + meta_type_label + ') ' + where + ' RETURN ID(n) AS index, n AS nodes'


def random_nodes_in_version(count: int, meta_type_label: str = '') -> str:
    if meta_type_label != '':
        meta_type_label = ':' + meta_type_label
    return_query = 'RETURN n AS nodes, rand() AS r ORDER BY r LIMIT ' + str(count)
    return 'MATCH (n' + meta_type_label + ') WHERE ' + by_version('n') + ' ' + return_query


def edges_in_version(
        source_meta_type_label: str = '',
        edge_meta_type_label: str = '',
        target_meta_type_label: str = '',
        return_ids: bool = True) -> str:

    if source_meta_type_label != '':
        source_meta_type_label = ':' + source_meta_type_label
    if edge_meta_type_label != '':
        edge_meta_type_label = ':' + edge_meta_type_label
    if target_meta_type_label != '':
        target_meta_type_label = ':' + target_meta_type_label

    if return_ids:
        return_query = 'RETURN ID(r) AS index, ID(s) AS source, ID(t) AS target, r AS edges'
    else:
        return_query = 'RETURN ID(r) AS index, s AS source, t AS target, r AS edges'

    is_in_version = 'WHERE ' + by_version('r') + ' AND ' + edges_no_dangling('s', 't')
    match_query = 'MATCH (s' + source_meta_type_label + ')-[r' + edge_meta_type_label + ']->(t' + target_meta_type_label + ') ' + is_in_version

    return match_query + ' ' + return_query
