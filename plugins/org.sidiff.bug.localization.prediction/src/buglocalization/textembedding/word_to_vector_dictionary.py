import os
from pathlib import Path
from threading import Semaphore
from typing import Dict, Tuple

import numpy as np
from gensim.models import KeyedVectors

plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent.parent
pretrained_dictionary_path: str = str(os.path.join(plugin_directory, 'data/'))

pretrained_dictionary_name = "GoogleNews-vectors-negative300.bin"
pretrained_dictionary_normalized_name = "GoogleNews-vectors-gensim-normed300.bin"
pretrained_dictionary_feature_size = 300

# Dictionary:
# https://code.google.com/archive/p/word2vec/
# https://drive.google.com/file/d/0B7XkCwpI5KDYNlNUTTlSS21pQmM/edit

# Shared Memory:
# https://stackoverflow.com/questions/42986405/how-to-speed-up-gensim-word2vec-model-load-time/43067907
# https://tomassetti.me/creating-a-reverse-dictionary/
# https://stackoverflow.com/questions/51616074/sharing-memory-for-gensims-keyedvectors-objects-between-docker-containers


class WordToVectorDictionary:

    def dimension(self) -> int:
        return pretrained_dictionary_feature_size

    def dictionary(self) -> Tuple[Dict[str, np.ndarray], int]:
        """
        Returns:
            Tuple[Dict[str, np.ndarray], int]: The dictionary shared in the same memory for all processes, and the word vector size.
        """
        model = KeyedVectors.load(str(Path(pretrained_dictionary_path + pretrained_dictionary_normalized_name)), mmap='r')
        model.syn0norm = model.syn0  # prevent recalc of normed vectors
        model.vectors_norm = model.vectors
        word_vector_size = model.wv.vectors.shape[1]
        return model, word_vector_size


def load_shared_dictionary():
    """
    Loads the normalized dictionary into the shared memory to be accessibly by other processes.
    """
    model = KeyedVectors.load(str(Path(pretrained_dictionary_path + pretrained_dictionary_normalized_name)), mmap='r')
    model.syn0norm = model.syn0  # prevent recalc of normed vectors
    model.vectors_norm = model.vectors
    model.most_similar('stuff')  # any word will do: just to page all in
    Semaphore(0).acquire()  # just hang until process killed


def save_normalized_dictionary():
    """
    Precompute L2-normalized vectors.
    If replace is set, forget the original vectors and only keep the normalized ones = saves lots of memory!
    Note that you cannot continue training after doing a replace. 
    The model becomes effectively read-only = you can call most_similar, similarity etc., but not train.
    """
    model = KeyedVectors.load_word2vec_format(str(Path(pretrained_dictionary_path + "/" + pretrained_dictionary_name)), binary=True)
    model.init_sims(replace=True)
    model.save(str(Path(pretrained_dictionary_path + "/" + pretrained_dictionary_normalized_name)))


if __name__ == '__main__':

    # Check if normalized dictionary exists; otherwise create it?
    if not Path(pretrained_dictionary_path + "/" + pretrained_dictionary_normalized_name).is_file():
        print("Normalize dictionary:", str(Path(pretrained_dictionary_path + "/" + pretrained_dictionary_normalized_name)))
        save_normalized_dictionary()

    # Load the dictionary:
    load_shared_dictionary()
    print("Shared dictionary loaded. Read by access_shared_dictionary()")
