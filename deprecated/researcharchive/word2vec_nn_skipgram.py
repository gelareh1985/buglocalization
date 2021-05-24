import re
import string

import tensorflow as tf
import tqdm
from tensorflow.keras import Model, Sequential
from tensorflow.keras.layers import Dense, Dot, Embedding, Flatten
from tensorflow.keras.layers.experimental.preprocessing import \
    TextVectorization

import Word2Vec

##########################################################################################
# ******************************* constants and variables ********************************
##########################################################################################

BATCH_SIZE = 32
BUFFER_SIZE = 1000
SEED = 42
AUTOTUNE = tf.data.experimental.AUTOTUNE
NUM_NEG_SAMPLES = 4
VOCABULARY_SIZE = 4096
sequence_length = 10
EMBEDDING_DIMENSION = 128

nodelist_path = "buglocalization/research/wordembedding_project/00001_bug_12000_version_2a9b25d23714b865b9b9713bbe18b653db291769.nodelist"
fname = "00001_bug_12000_version_2a9b25d23714b865b9b9713bbe18b653db291769.nodelist"

##########################################################################################
# *************************************** Functions **************************************
##########################################################################################
# Generates skip-gram pairs with negative sampling for a list of sequences
# (int-encoded sentences) based on window size, number of negative samples
# and vocabulary size.


def generate_training_data(sequences, window_size, num_s, vocab_size, seed):
    # Elements of each training example are appended to these lists.
    targets, contexts, labels = [], [], []

    # Build the sampling table for vocab_size tokens.
    sampling_table = tf.keras.preprocessing.sequence.make_sampling_table(vocab_size)

    # Iterate over all sequences (sentences) in dataset.
    for sequence in tqdm.tqdm(sequences):

        # Generate positive skip-gram pairs for a sequence (sentence).
        positive_skip_grams, _ = tf.keras.preprocessing.sequence.skipgrams(
            sequence,
            vocabulary_size=vocab_size,
            sampling_table=sampling_table,
            window_size=window_size,
            negative_samples=0)

        # Iterate over each positive skip-gram pair to produce training examples
        # with positive context word and negative samples.
        for target_word, context_word in positive_skip_grams:
            context_class = tf.expand_dims(tf.constant([context_word], dtype="int64"), 1)
            negative_sampling_candidates, _, _ = tf.random.log_uniform_candidate_sampler(
                true_classes=context_class,
                num_true=1,
                num_sampled=num_s,
                unique=True,
                range_max=vocab_size,
                seed=SEED,
                name="negative_sampling")

            # Build context and label vectors (for one target word)
            negative_sampling_candidates = tf.expand_dims(negative_sampling_candidates, 1)

            context = tf.concat([context_class, negative_sampling_candidates], 0)
            label = tf.constant([1] + [0] * num_s, dtype="int64")

            # Append each element from the training example to global lists.
            targets.append(target_word)
            contexts.append(context)
            labels.append(label)

    return targets, contexts, labels

# Now, create a custom standardization function to lowercase the text and
# remove punctuation.


def custom_standardization(input_data):
    lowercase = tf.strings.lower(input_data)
    return tf.strings.regex_replace(lowercase, '[%s]' % re.escape(string.punctuation), '')


def custom_loss(x_logit, y_true):
    return tf.nn.sigmoid_cross_entropy_with_logits(logits=x_logit, labels=y_true)


def save_file(weights, vocab):
    list_words = []
    list_vectors = []
    for index, word in enumerate(vocab):
        if index == 0:
            continue  # skip 0, it's padding.
        vec = weights[index]
        list_vectors.append(vec)
        list_words.append(word)
    return list_vectors, list_words
##########################################################################################
# *************************************** Main Program ***********************************
##########################################################################################


#path_to_file = tf.keras.utils.get_file('shakespeare.txt', 'https://storage.googleapis.com/download.tensorflow.org/data/shakespeare.txt')
path_to_file = tf.keras.utils.get_file(fname, origin="C:/Users/gelareh/.keras/datasets")

print("type of textfile: ",type(path_to_file))

with open(path_to_file) as f:
    lines = f.read().splitlines()
datatxt = []
for line in lines[:20]:
    #print(line)
    datatxt.append(line)
print(datatxt)    
print("**********************************************************************************")
text_ds = tf.data.TextLineDataset(path_to_file).filter(lambda x: tf.cast(tf.strings.length(x), bool))
print("text data: ", text_ds)
# Define the vocabulary size and number of words in a sequence.
# Use the text vectorization layer to normalize, split, and map strings to
# integers. Set output_sequence_length length to pad all samples to same length.
vectorize_layer = TextVectorization(
    standardize=custom_standardization,
    max_tokens=VOCABULARY_SIZE,
    output_mode='int',
    output_sequence_length=sequence_length)

vectorize_layer.adapt(text_ds.batch(BATCH_SIZE))
# Save the created vocabulary for reference.
inverse_vocab = vectorize_layer.get_vocabulary()
print(inverse_vocab[:20])
# Vectorize the data in text_ds.
text_vector_ds = text_ds.batch(BATCH_SIZE).prefetch(AUTOTUNE).map(vectorize_layer).unbatch()

sequences = list(text_vector_ds.as_numpy_iterator())
print(len(sequences))
for seq in sequences[:5]:
    print(f"{seq} => {[inverse_vocab[i] for i in seq]}")

targets, contexts, labels = generate_training_data(
    sequences=sequences,
    window_size=2,
    num_s=NUM_NEG_SAMPLES,
    vocab_size=VOCABULARY_SIZE,
    seed=SEED)
print(len(targets), len(contexts), len(labels))

mydataset = tf.data.Dataset.from_tensor_slices(((targets, contexts), labels))
mydataset = mydataset.shuffle(BUFFER_SIZE).batch(BATCH_SIZE, drop_remainder=True)
print("my dataset: ", mydataset)
mydataset = mydataset.cache().prefetch(buffer_size=AUTOTUNE)
print("my dataset: ", mydataset)
#print(mydataset._input_dataset.element_spec[0])
#dt1 = mydataset._input_dataset._flat_shapes[0]
#dt2 = mydataset._input_dataset._flat_shapes[1]
#dt3 = mydataset._input_dataset._flat_shapes[2]
#print()

# ***********************************************************************
word2vec = Word2Vec.Word2Vec(vocab_size=VOCABULARY_SIZE, embedding_dim=EMBEDDING_DIMENSION)
print("word2vec (subclass output): ",word2vec)
word2vec.compile(optimizer='adam',
                 loss=tf.keras.losses.CategoricalCrossentropy(from_logits=True),
                 metrics=['accuracy'])
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir="logs")
word2vec.fit(mydataset, epochs=20, callbacks=[tensorboard_callback]) 

print("************************************************")
print("\n\n word2vec model summary: ",word2vec.summary())

##########################################################################################
# ***************************** Embedding Lookup and Analysis ****************************
##########################################################################################

weights = word2vec.get_layer('w2v_embedding').get_weights()[0]
print("length of all word2vec weights: (dimension) ",weights.shape, type(weights),"\n",weights)

layer2 = word2vec.get_layer('embedding').get_weights()[0]
print("layer2 weights: (dimension) ",layer2.shape, type(layer2),"\n",layer2)
vocab = vectorize_layer.get_vocabulary()
print("vocabulary: (length/words)",len(vocab))
#out_v = io.open('buglocalization/research/wordembedding_project/vectors.tsv', 'rw', encoding='utf-8')
#out_m = io.open('buglocalization/research/wordembedding_project/metadata.tsv', 'rw', encoding='utf-8')

file_vectors = "./buglocalization/research/wordembedding_project/vectors.tsv"
file_metadata = "./buglocalization/research/wordembedding_project/metadata.tsv"

vectors, words = save_file(weights, vocab)

with open(file_vectors,"w") as f:
    f.write('\t'.join([str(x) for x in vectors]) + "\n")

with open(file_metadata,"w") as f:
    f.write('\t'.join([str(x) for x in words]) + "\n")

text = "compile"
#word2vec.predict([text])
# out_v.write('\t'.join([str(x) for x in vec]) + "\n")
# out_m.write(word + "\n")
# out_v.close()
# out_m.close()
