FROM continuumio/miniconda3:latest
WORKDIR /notebooks

RUN conda install python=3.8
RUN conda install -c anaconda cudatoolkit=10.1
RUN conda install pip
RUN pip install tensorflow==2.3
RUN pip install stellargraph
RUN conda install nltk
RUN conda install -c conda-forge py2neo
RUN conda install -c conda-forge jupyterlab