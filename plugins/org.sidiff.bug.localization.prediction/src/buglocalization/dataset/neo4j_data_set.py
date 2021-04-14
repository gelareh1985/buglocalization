'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations

from typing import Any, Dict, List, Optional, Set, Tuple

from buglocalization.dataset import neo4j_queries as query
from buglocalization.dataset import neo4j_queries_util as query_util
from buglocalization.dataset.data_set import (IBugSample, IDataSet,
                                              ILocationSample)
from buglocalization.metamodel.meta_model import MetaModel
from pandas.core.frame import DataFrame
from py2neo import Graph, Node

# ===============================================================================
# Neo4j Data Connector:
# ===============================================================================
# -- NOTE: Do not save any "raw" Neo4j Node or Edge objects in the data set.
#    These objects store a reference to py2neo.internal.connectors.BoltConnector
#    which is not pickable and causing problems with multi-processing. Also see
#    __getstate__() makes the intentionally stored connection invisible to pickle.
# ===============================================================================


class Neo4jConfiguration:

    def __init__(self,
                 neo4j_host: str = 'localhost',
                 neo4j_port: int = 7687,
                 neo4j_user: str = None,
                 neo4j_password: str = None):
        """
        The configuration parameters for connection to Neo4j graph database.

        Args:
            neo4j_host (str): Neo4j host address. Defaults to localhost.
            neo4j_port (int, optional): Neo4j connection port. Defaults to bolt port 7687.
            neo4j_user (str, optional): Neo4j user name for connection. Defaults to None.
            neo4j_password (str, optional): Password for connection. Defaults to None.
        """
        self.neo4j_host: str = neo4j_host
        self.neo4j_port: Optional[int] = neo4j_port
        self.neo4j_user: Optional[str] = neo4j_user
        self.neo4j_password: Optional[str] = neo4j_password


class DataSetNeo4j(IDataSet):

    # https://py2neo.org/v4/database.html
    # https://stellargraph.readthedocs.io/en/stable/demos/basics/loading-saving-neo4j.html

    def __init__(self,
                 meta_model: MetaModel,
                 neo4j_config: Neo4jConfiguration,
                 is_negative: bool = False,
                 list_bug_samples: bool = True):

        super().__init__(is_negative)

        # Opened connection and read bug samlpes:
        self.neo4j_config: Neo4jConfiguration = neo4j_config
        self.connectionCounter: int = 0
        self.neo4j_graph: Optional[Graph] = None
        self.connectNeo4j()

        # Meta-model configuration
        self.meta_model: MetaModel = meta_model

        # Load all (uninitialized) bug samples:
        if list_bug_samples:
            self.list_samples()

    def __getstate__(self):
        
        # Close connection to allow multiprocessing i.e., each process needs its own connnection.
        self.lock.acquire()
        self.closeNeo4j()
        self.lock.release()

        # Do not expose Ne4j connection (for multiprocessing) which is not pickable.
        state = super().__getstate__()

        # Check again if connection is actually "closed" for exposed state:
        if 'neo4j_graph' in state:
            state['neo4j_graph'] = None
            
        return state

    def connectNeo4j(self) -> Graph:
        if self.connectionCounter > 2000:
            self.closeNeo4j()  # prevent resource leaks
            self.connectionCounter = 0
        else:
            self.connectionCounter += 1

        if self.neo4j_graph is None:
            self.neo4j_graph = Graph(host=self.neo4j_config.neo4j_host, port=self.neo4j_config.neo4j_port,
                                     user=self.neo4j_config.neo4j_user, password=self.neo4j_config.neo4j_password)
        return self.neo4j_graph

    def closeNeo4j(self):
        # https://stackoverflow.com/questions/59138809/connection-pool-life-cycle-of-py2neo-graph-would-the-connections-be-released-wh
        # graph.database.connector.close()
        # Database.forget_all()
        self.neo4j_graph = None

    def list_samples(self):
        for db_version in self.run_query(query.buggy_versions())['versions']:
            self.bug_samples.append(self.create_sample(db_version))

    def create_sample(self, db_version: int) -> 'BugSampleNeo4j':
        return BugSampleNeo4j(self, db_version)

    # # Send query to the Neo4j database and parse the result to a Pandas data frame #

    def run_query(self, query: str, parameters: Dict[str, Any] = None, index: str = None) -> DataFrame:
        df = self.connectNeo4j().run(query, parameters).to_data_frame()
        if not df.empty and index is not None:
            df.set_index(index, inplace=True)
        return df

    def run_query_to_table(self, query: str, parameters: Dict[str, Any] = None) -> List[Tuple[Any]]:
        return self.connectNeo4j().run(query, parameters).to_table()

    def run_query_value(self, query: str, parameters: Dict[str, Any] = None) -> Any:
        result = self.run_query(query, parameters)

        if len(result) > 0:
            return result.iloc[0, 0]


class BugSampleNeo4j(IBugSample):
    dataset: DataSetNeo4j

    def __init__(self, dataset: DataSetNeo4j, db_version: int):
        super().__init__(dataset, "version:" + str(db_version), db_version)
        self.edge_columns = ['source', 'target']

        # Bug report and locations:
        self.bug_report: int = -1
        self.bug_locations: Set[Tuple[int, str]] = set()  # node ID -> meta-type
        
    # # Load node pairs from Neo4j # #

    def load_bug_report(self):

        # Bug report node:
        bug_report_node_frame = self.run_query_by_version(
            query.nodes_in_version('TracedBugReport', returns='RETURN ID(n) AS index'))
        assert len(bug_report_node_frame.index) == 1
        return int(bug_report_node_frame.index[0])

    def load_bug_locations(self, locate_by_container: int):
        bug_locations: Set[Tuple[int, str]] = set()
        bug_location_edges = self.run_query_by_version(
            query.edges_in_version('Change', 'location', return_nodes=True))

        for edge_idx, edge in bug_location_edges.iterrows():
            # location edges point at model elements:
            bug_location: Node = edge['target_node']
            bug_locations.add((bug_location.identity, query_util.get_label(bug_location)))

        # Find container of bug location if the type is not in specified location:
        if locate_by_container > 0:
            bug_locations = self.locate_bug_locations_by_container(bug_locations, locate_by_container)

        return bug_locations

    def locate_bug_locations_by_container(self, bug_locations: Set[Tuple[int, str]], locate_by_container: int) -> Set[Tuple[int, str]]:
        bug_locations_by_container: Set[Tuple[int, str]] = set()  # Set eliminated duplicted id, type tuples.
        bug_location_types: Set[str] = self.dataset.meta_model.get_bug_location_types()

        query_parent_node = query.path(query.edge_containment(True), 'b', k=2, return_query='RETURN DISTINCT a AS parents')
        query_parent_node_parameter: Dict[str, List[int]] = {'node_ids': []}

        for model_location, bug_location_type in bug_locations:
            if bug_location_type in bug_location_types:
                bug_locations_by_container.add((model_location, bug_location_type))
            else:
                query_parent_node_parameter['node_ids'] = [model_location]
                parent_nodes = self.run_query_by_version(
                    query_parent_node, query_parent_node_parameter, set_index=False)

                if not parent_nodes.empty:
                    for parent_node in parent_nodes['parents']:
                        if query_util.get_label(parent_node) in bug_location_types:
                            bug_locations_by_container.add((parent_node.identity, query_util.get_label(parent_node)))
                            break
                else:
                    print("WARNING: No parent found for node ID", model_location)

        return bug_locations_by_container

    def run_query_by_version(self, query: str, parameters: Dict[str, Any] = None, set_index: bool = True) -> DataFrame:
        default_parameter = {'db_version': self.version}

        if parameters is not None:
            for parameter_name, value in parameters.items():
                default_parameter[parameter_name] = value

        df = self.dataset.run_query(query, default_parameter, index="index" if set_index else None)
        return df
    
    def run_query_by_version_value(self, query: str, parameters: Dict[str, Any] = None, set_index: bool = True) -> DataFrame:
        default_parameter = {'db_version': self.version}

        if parameters is not None:
            for parameter_name, value in parameters.items():
                default_parameter[parameter_name] = value

        df = self.dataset.run_query_value(query, default_parameter)
        return df


class LocationSampleNeo4j(ILocationSample):

    def __init__(self, model_location: int, bug_report: int, label: int):
        super().__init__()

        self._label: int = label
        self._model_location: int = model_location
        self._bug_report: int = bug_report

    def label(self) -> int:
        return self._label

    def is_negative(self) -> bool:
        return self._label == 0

    def bug_report(self) -> int:
        return self._bug_report

    def model_location(self) -> int:
        return self._model_location
