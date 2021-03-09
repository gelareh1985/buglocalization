import os
from typing import Generator, List, Tuple

import pandas as pd


def load_evaluation_results(path: str) -> Generator[Tuple[str, pd.DataFrame, str, pd.DataFrame], None, None]:
    for filename in os.listdir(path):
        if filename.endswith("_info.csv"):
            evaluation_filename = filename[:filename.rfind("_")]

            tbl_info_file = evaluation_filename + "_info.csv"
            tbl_info_path = path + tbl_info_file
            tbl_info = pd.read_csv(tbl_info_path, sep=';', header=0)
            
            tbl_predicted_file = evaluation_filename + "_prediction.csv"
            tbl_predicted_path = path + tbl_predicted_file
            tbl_predicted = pd.read_csv(tbl_predicted_path, sep=';', header=0)

            yield tbl_info_file, tbl_info, tbl_predicted_file, tbl_predicted
            
            
def load_all_evaluation_results(path: str) -> List[Tuple[str, pd.DataFrame, str, pd.DataFrame]]:
    return list(load_evaluation_results(path))
         
            
def get_expected_locations(tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    return tbl_predicted_data[tbl_predicted_data.IsLocation == 1].copy()


def get_ranking_of_subgraph(subgraph_k: pd.DataFrame, tbl_predicted_data: pd.DataFrame) -> pd.DataFrame:
    # subgraph_k -> index -> node IDs
    return tbl_predicted_data[tbl_predicted_data.DatabaseNodeID.isin(subgraph_k.index)]
    