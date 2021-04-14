'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

from __future__ import annotations  # FIXME: Currently not supported by PyDev

import threading
from typing import Callable, Iterator, List, Optional, Union

from stellargraph import StellarGraph  # type: ignore

# ===============================================================================
# Common interface of all data set <-*--1-> sample implementations:
# ===============================================================================


class IDataSet:

    def __init__(self, is_negative: bool = False):
        self.lock = threading.Condition()
        self.bug_samples: List[IBugSample] = []
        self.is_negative: bool = is_negative
        
    def __getstate__(self):
        # Return a copy for parallel multiprocessing:
        self.lock.acquire()
        state = dict(self.__dict__)
        self.lock.release()
        
        # Locks can not be pickled:
        if 'lock' in state:
            state['lock'] = None
            
        return state

    def __len__(self) -> int:
        return len(self.bug_samples)

    def __getitem__(self, idx) -> IBugSample:
        return self.bug_samples[idx]

    def __iter__(self) -> Iterator[IBugSample]:
        return self.bug_samples.__iter__()


class IBugSample:

    def __init__(self, dataset: IDataSet, sample_id: str, version: int):
        self.lock: Optional[CountingLock] = None
        self.dataset: IDataSet = dataset
        self.sample_id: str = sample_id
        self.version = version
        self.location_samples: List[ILocationSample] = []
        
    def __getstate__(self):
    
        # Return a copy for parallel multiprocessing:
        self._lock().lock.acquire()
        state = dict(self.__dict__)
        self._lock().lock.release()
        
        # Locks can not be pickled:
        if 'lock' in state:
            state['lock'] = None
        
        return state
        
    def _lock(self):
        if self.lock is None:
            self.lock = CountingLock()
        return self.lock

    def initialize(self, log_level: int = 0):
        """
        Will be lazely called to create all (not yet inistialized) "location samples".

        Args:
            log_level (int, optional): 0-100 provide more detailed logging. Defaults to 0 for no logging.
        """
        # Only the first accessing thread will call initialize:
        self._lock().count_up(lambda: self._initialize(log_level))

    def _initialize(self, log_level: int = 0):
        # TODO: To be implemented by clients
        ...

    def uninitialize(self):
        """
        Finally called to free memory for garbadge collections.
        """
        # Only the last accessing thread will call uninitialize:
        self._lock().count_down(lambda: self._uninitialize())

    def _uninitialize(self):
        # TODO: To be implemented by clients
        ...

    def __len__(self) -> int:
        return len(self.location_samples)

    def __getitem__(self, idx) -> ILocationSample:
        return self.location_samples[idx]

    def __iter__(self) -> Iterator[ILocationSample]:
        return self.location_samples.__iter__()


class ILocationSample:

    def __init__(self) -> None:
        self.lock: Optional[CountingLock] = None
        
    def __getstate__(self):
        
        # Return a copy for parallel multiprocessing:
        self._lock().lock.acquire()
        state = dict(self.__dict__)
        self._lock().lock.release()
        
        # Locks can not be pickled:
        if 'lock' in state:
            state['lock'] = None
            
        return state
        
    def _lock(self):
        if self.lock is None:
            self.lock = CountingLock()
        return self.lock

    def initialize(self, bug_sample: IBugSample, log_level: int = 0):
        """
        Will be lazely called to initialize the provided data: label, graph, bug report, model location, is negative

        Args:
            bug_sample (IBugSample): The parent/corresponding bug report.
            log_level (int, optional): 0-100 provide more detailed logging. Defaults to 0 for no logging.
        """
        # Only the first accessing thread will call initialize:
        self._lock().count_up(lambda: self._initialize(bug_sample, log_level))

    def _initialize(self, bug_sample: IBugSample, log_level: int = 0):
        # TODO: To be implemented by clients
        ...

    def uninitialize(self):
        """
        Finally called to free memory for garbadge collections.
        """
        # Only the last accessing thread will call uninitialize:
        self._lock().count_down(lambda: self._uninitialize())

    def _uninitialize(self):
        # TODO: To be implemented by clients
        ...

    def label(self) -> Optional[Union[float, int]]:
        """
        Returns:
            Optional[Union[float, int]]: The expected result for training, or the model element ID as "sample ID" for prediction.
        """
        return -1

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


class CountingLock(object):

    def __init__(self, count=0):
        self.count = count
        self.lock = threading.Condition()

    def count_down(self, on_unlock: Callable):
        self.lock.acquire()
        self.count -= 1
        if self.count == 0:
            on_unlock()
        if self.count <= 0:
            self.lock.notifyAll()
        self.lock.release()

    def count_up(self, on_first_lock: Callable):
        self.lock.acquire()
        self.count += 1
        if self.count == 1:
            on_first_lock()
        self.lock.release()

    def wait(self, timeout: float = None):
        self.lock.acquire()
        while self.count > 0:
            self.lock.wait(timeout)
        self.lock.release()
