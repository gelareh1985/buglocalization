import os
from pathlib import Path
from time import time
import datetime


def t(start_time: float) -> str:
    return "{:.3f}".format(time() - start_time) + "s"


def start_t() -> float:
    return time()


def get_project_folder() -> str:
    return str(Path(os.path.dirname(os.path.abspath(__file__))).parent.parent.parent)  # from this module location


def create_folder(folder_path: str) -> None:
    Path(folder_path).mkdir(parents=True, exist_ok=True)


def create_timestamp() -> str:
    return datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')