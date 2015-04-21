2PLDDB README
Copyright (c) 2015
Author: Yidan(Evan) Zheng

SQLite Database
---------------------------------------------------------------------------------------------------
1. The program will create an sqlite database if there isn't one. Make sure SQLite is installed in your machine. 
2. The database is called "g2pl.db". It will be created in the current directory where you started your program. 
2. The database contains the following table. Note that you do not need to create it yourself. The program will create for you. 
	CREATE TABLE `g2pl_database` (
		`item` VARCHAR(50) PRIMARY KEY NOT NULL,
		`value` INT(11) NOT NULL
	);

Linux
---------------------------------------------------------------------------------------------------
0. You need to install Java RMI, SQLite.
1. You need to add JDBC to classpath permanently. In bash, add the following line to .bashrc:
	export CLASSPATH=$CLASSPATH:{project-path}/2PLDDB/lib/sqlite-jdbc-3.8.7.jar
2. Under the project root directory (2PLDDB), run the following command to compile the project:
	javac -d bin/ -cp src/ src/g2pl/systems/*.java
4. cd to bin/ directory, run the following command to start the central site:
	java g2pl.systems.CentralSite
5. Open another console, run the following command to start other site:
	java g2pl.systems.OtherSite {path-of-transactions-file}
6. Open other consoles to start additional copies of other sites.


Windows
------------------------------------------------------------------------------------------------------
1. You need to install Java RMI, SQLite.
2. You need to add JDBC to classpath permanently.
3. Compile the project (same as Linux).
4. Run the central site (same as Linux).
5. Run other site (same as Linux).
6. Run additional copies of other sites (same as Linux).
