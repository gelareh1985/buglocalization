# IdentiBug: A tool for Model-Driven Bug Localization
Our tool IdentiBug for model-driven bug localization works based on the data stored in Neo4j graph database management.  

## Setting Up the Database (Just for the first time)
To set up the database needed to work with IdentiBug, you need to first configure it as the following:

***Note:*** this is the steps needed to proceed with the neo4j server version. So, for configuring the database and working with it, command prompt consule is to be used. The command prompt should be opened while using the database. So, after finishing using the database any time, the connection to server will be interuppted on closing the command prompt. As a result, it is needed to run start command to load the dataset any time and continue working. 

***Note:*** Please first download the latest Neo4j Server Version (Community version) and then create a folder preferably named your dataset name. Then copy all the files from the downloaded neo4j to your dataset folder. Then, create a folder named 'backups' where you place your dataset dump file. Then, download APOC.jar and copy it in the dataset folder. Then, you need to update the configuration details. To do so, you should go to 'conf' folder and add configuration details (step3 in the following) to the beggining of the neo4j.conf like the following image. Then, just you need to start the server (only for the first time you want to create the dataset). So, for the next times you want to use the database, you just need to load it.

**(1) to download the Dump files for training or testing:**

./bin/neo4j-admin dump --database=neo4j --to=./backups/neo4j-eclipse.jdt.core_samples_2021-03-25.dump
    
**(2) to download APOC Jar from the following url and copy it to plugins folder**  [Download Link](https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/)

**(3) to copy the Settings in conf/neo4j.conf**

    dbms.security.procedures.unrestricted=apoc.*
    
    dbms.memory.heap.initial_size=512m
    
    dbms.memory.heap.max_size=4G
    
    dbms.allow_upgrade=true

**(4) to start server:**
**Open Terminal Window in the database folder**
*Start command:*

      ./bin/neo4j.bat console
    
**(5) open database in server and set password:**

## Loading the Database (For next times)
**(1) to load the downloaded files:**
**Open Terminal Window in the database folder**
*Load command:*

     ./bin/neo4j-admin load --from=./backups/neo4j-eclipse.jdt.core_samples_2021-03-25.dump --database=neo4j --force
     
***Note:*** Please note that if the database server does not work in a browser like Microsoft edge, try changing the brower and use another browser like: Google Chrome

## Requirements for running scripts:
To better run the python scripts, It is needed to create a virtual environment with the following settings.

**(1)**

*to create a virtual environment:*

     conda create --name tf-gpu
     
**(2)**

*to activate the environment:*

     conda activate tf-gpu
     
**(3)**

*to install a python version in the virtual environment:*

     conda install python=3.8
     
**(4)**

*to install cuda toolkit:*

     conda install -c anaconda cudatoolkit=10.1
     
**(5)**

*to install pip:*

    conda install pip
    
**(6)**

*to install a tensorflow version (an api for creating deep learning models):*

    pip install tensorflow==2.3
    
**(7)**

*to install stellargraph (an api for machine learning using graph structures):*

    pip install stellargraph
    
**(8)**

*it is optional to update all the possible packages if seeing any conflicts in the dependencies installed:*

    conda update --all
    
**(9)**

*to install gensim topic modeling for humans which is an api for large scale text processing with deep learning models, e.g., nlp models, semantic representation models, ...
[Website](https://radimrehurek.com/gensim/)*

    pip install gensim
    
**(10)**

*to install natural language processing api: [Website](https://www.nltk.org/)*

    possibility1: conda install nltk ---> if not working
    possibility2: pip install nltk
    
**(11)**

*to install neo4j graph database management api: [Website](https://neo4j.com/)*

    pip install py2neo
    
