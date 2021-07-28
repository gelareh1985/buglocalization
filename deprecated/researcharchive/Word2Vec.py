from tensorflow.keras import Model
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dot, Dense, Embedding, Flatten


class Word2Vec(Model):

    def __init__(self, vocab_size, embedding_dim):

        super(Word2Vec, self).__init__()
        #self.vocab_size = vocab_size
        #self.embedding_dim = embedding_dim
        self.target_embedding = Embedding(vocab_size,
                                          embedding_dim,
                                          input_length=1,
                                          name="w2v_embedding")
        self.context_embedding = Embedding(vocab_size,
                                           embedding_dim,
                                           input_length=5)
        self.dots = Dot(axes=(3, 2))
        self.flatten = Flatten()

    def call(self, pair):
        target, context = pair
        word_emb = self.target_embedding(target)
        context_emb = self.context_embedding(context)
        dots = self.dots([context_emb, word_emb])
        return self.flatten(dots)

    # def model(self):
    #     x=tf.keras.Input(shape=((self.vocab_size, self.embedding_dim)))
    #     return tf.keras.Model(inputs=[x],outputs=self.call(x))
