'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''

#%%

import os
import sys
from pathlib import Path

# WORKAROUND: Initialize PYTHONPATH with src folder for Jupyter extension:
if str(Path(os.path.dirname(os.path.abspath(__file__)))) not in sys.path:
    sys.path.append(str(Path(os.path.dirname(os.path.abspath(__file__)))))

# SOWE of truncated, ranked words:
from buglocalization.selfembedding.dictionary.node_self_embedding_word_ranking import compute_node_self_embedding

# SOWE of similar words:
# from buglocalization.selfembedding.dictionary.node_self_embedding_similar_words import compute_node_self_embedding

# Check the stored embeddings:
# from buglocalization.selfembedding.dictionary.node_self_embedding_dictionary_test import compute_node_self_embedding

if __name__ == '__main__':
    compute_node_self_embedding()
