'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

import threading
from typing import Iterator, List, Optional, Union

from stellargraph import StellarGraph  # type: ignore

# ===============================================================================
# Common interface of all data set <-*--1-> sample implementations:
# ===============================================================================


class IDataSet:

    def __init__(self, is_negative: bool = False):
        self.bug_samples: List[IBugSample] = []
        self.is_negative: bool = is_negative

    def __len__(self) -> int:
        return len(self.bug_samples)

    def __getitem__(self, idx) -> IBugSample:
        return self.bug_samples[idx]

    def __iter__(self) -> Iterator[IBugSample]:
        return self.bug_samples.__iter__()


class IBugSample:

    def __init__(self, dataset: IDataSet, sample_id: str):
        self.lock = threading.Lock()
        self.dataset: IDataSet = dataset
        self.sample_id: str = sample_id
        self.location_samples: List[ILocationSample] = []

    def initialize(self, log_level: int = 0):
        """
        Will be lazely called to create all (not yet inistialized) "location samples".

        Args:
            log_level (int, optional): 0-100 provide more detailed logging. Defaults to 0 for no logging.
        """
        ...
        
    def uninitialize(self):
        ...

    def __len__(self) -> int:
        return len(self.location_samples)

    def __getitem__(self, idx) -> ILocationSample:
        return self.location_samples[idx]

    def __iter__(self) -> Iterator[ILocationSample]:
        return self.location_samples.__iter__()


class ILocationSample:

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        """
        Will be lazely called to initialize the provided data: label, graph, bug report, model location, is negative

        Args:
            bug_sample (IBugSample): The parent/corresponding bug report.
            log_level (int, optional): 0-100 provide more detailed logging. Defaults to 0 for no logging.
        """
        ...
        
    def uninitialize(self):
        ...

    def label(self) -> Optional[Union[float, int]]:
        """
        Returns:
            Optional[Union[float, int]]: The expected result for training, or the model element ID as "sample ID" for prediction.
        """
        return -1

    def graph(self) -> StellarGraph:
        """
        Returns:
            StellarGraph: The graph which will be used for sampling paths to neigbors form the given bug report and model element location.
        """
        raise NotImplementedError()

    def bug_report(self) -> Union[int, str]:
        """
        Returns:
            Union[int, str]: The node ID (in the given Stellar-Graph) of the corresponding bug report.
        """
        raise NotImplementedError()

    def model_location(self) -> Union[int, str]:
        """
        Returns:
            Union[int, str]: The node ID (in the given Stellar-Graph) of the corresponding model element.
        """
        raise NotImplementedError()

    def is_negative(self) -> Optional[bool]:
        """
        Returns:
            Optional[bool]: During training, true if it is a negative sample; false if it is a positive sampel.
        """
        raise NotImplementedError()


class LocationSampleBase(ILocationSample):

    def __init__(self, bug_report: Union[int, str], model_location: Union[int, str], label: float = None, is_negative: bool = False):
        self._bug_report: Union[int, str] = bug_report
        self._model_location: Union[int, str] = model_location
        self._label: Optional[float] = label
        self._is_negative: bool = is_negative

    def label(self) -> Optional[float]:
        return self._label

    def graph(self) -> StellarGraph:
        raise NotImplementedError()

    def bug_report(self) -> Union[int, str]:
        return self._bug_report

    def model_location(self) -> Union[int, str]:
        return self._model_location

    def is_negative(self) -> bool:
        return self._is_negative
