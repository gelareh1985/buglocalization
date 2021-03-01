from time import time


def t(start_time: float) -> str:
    return "{:.3f}".format(time() - start_time) + "s"
