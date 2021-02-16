'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

from typing import List, Optional, Union, Iterator

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
        self.dataset: IDataSet = dataset
        self.sample_id: str = sample_id
        self.location_samples: List[ILocationSample] = []

    def initialize(self, log_level: int = 0):
        ...

    def __len__(self) -> int:
        return len(self.location_samples)

    def __getitem__(self, idx) -> ILocationSample:
        return self.location_samples[idx]

    def __iter__(self) -> Iterator[ILocationSample]:
        return self.location_samples.__iter__()


class ILocationSample:

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        ...

    def label(self) -> Optional[Union[float, int]]:
        return -1

    def graph(self) -> StellarGraph:
        raise NotImplementedError()

    def bug_report(self) -> Union[int, str]:
        raise NotImplementedError()

    def model_location(self) -> Union[int, str]:
        raise NotImplementedError()

    def is_negative(self) -> Optional[bool]:
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
