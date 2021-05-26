# IdentiBug: A tool for Model-Driven Bug Localization
Our tool IdentiBug for model-driven bug localization works based on the data stored in Neo4j graph database management.  

## Setting Up the Database
To set up the database needed to work with IdentiBug, you need to first configure it as the following:

***Note:*** this is the steps needed to proceed with the neo4j server version. So, for configuring the database and working with it, command prompt consule is to be used. The command prompt should be opened while using the database. So, after finishing using the database any time, the connection to server will be interuppted on closing the command prompt. As a result, it is needed to run start command to load the dataset any time and continue working. 

**(1) to download the Dump files for training or testing:**

./bin/neo4j-admin dump --database=neo4j --to=./backups/neo4j-eclipse.jdt.core_samples_2021-03-25.dump

**(2) to load the downloaded files:**

*Load command:*

     ./bin/neo4j-admin load --from=./backups/neo4j-eclipse.jdt.core_samples_2021-03-25.dump --database=neo4j --force
    
**(3) to start server:**

*Start command:*

      ./bin/neo4j.bat console
    
**(4) open database in server and set password:**

**(5) to download APOC Jar from the following url and copy it to plugins folder**

[Download Link](https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/)

**(6) to copy the Settings in conf/neo4j.conf**

    dbms.security.procedures.unrestricted=apoc.*
    
    dbms.memory.heap.initial_size=512m
    
    dbms.memory.heap.max_size=4G
    
    


