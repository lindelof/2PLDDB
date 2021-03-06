2PLDDB README
Copyright (c) 2015
Author: Yidan(Evan) Zheng

The 2PLDDB project is a centralized two phase locking database system. Please refer to the following link to know its design, algorithms and other useful information:
https://github.com/lindelof/2PLDDB/blob/master/documentation.pdf

Linux
----------------------------------------------------------------------------
0. You need to install Java RMI, SQLite.
1. You need to add JDBC to classpath permanently. In bash, add the following line to .bashrc:
	export CLASSPATH=$CLASSPATH:{project-path}/2PLDDB/lib/sqlite-jdbc-3.8.7.jar
2. Under the project root directory (2PLDDB), run the following command to compile the project:
	javac -d bin/ -cp src/ src/g2pl/systems/*.java
4. cd to bin/ directory, run the following command to start the central site:
	java g2pl.systems.CentralSite
5. Open another console, run the following command to start other site:
	java g2pl.systems.OtherSite {path-of-transactions-file}
	For Example: java g2pl.systems.OtherSite testcases_cmd/transactions_few_1.txt
6. Open other consoles to start additional copies of other sites.


Windows
----------------------------------------------------------------------------
1. You need to install Java RMI, SQLite.
2. You need to add JDBC to classpath permanently.
3. Compile the project (same as Linux).
4. Run the central site (same as Linux).
5. Run other site (same as Linux).
6. Run additional copies of other sites (same as Linux).

SQLite Database
----------------------------------------------------------------------------
1. The program will create an sqlite database if there isn't one. Make sure SQLite is installed in your machine. 
2. The database is called "g2pl.db". It will be created in the current directory where you started your program. 
3. The database contains the following table. Note that you DO NOT need to create it yourself. The program will automatically create for you. 
	CREATE TABLE `g2pl_database` (
		`item` VARCHAR(50) PRIMARY KEY NOT NULL,
		`value` INT(11) NOT NULL
	);
4. You can connect to the database to see the variable values.	

How to Install SQLite
----------------------------------------------------------------------------
(Linux) Today, almost all the flavors of Linux OS are being shipped with SQLite. So you just issue the following command to check if you already have SQLite installed on your machine or not.
	$sqlite3
	SQLite version 3.7.15.2 2013-01-09 11:53:05
	Enter ".help" for instructions
	Enter SQL statements terminated with a ";"
	sqlite>
	(type .quit to quit)
If you do not see above result, then it means you do not have SQLite installed on your Linux machine. For Unbuntu, type in the following command:
	sudo apt-get install sqlite3 libsqlite3-dev
Or you can follow the following steps to install SQLite:
(1). Go to SQLite download page and download sqlite-autoconf-*.tar.gz from source code section: http://www.sqlite.org/download.html
(2). Follow the following steps:
	$tar xvfz sqlite-autoconf-3071502.tar.gz
	$cd sqlite-autoconf-3071502
	$./configure --prefix=/usr/local
	$make
	$make install
(3). Verify installation by running sqlite3

(Windows) To install SQLite in Windows, do the following:
(1). Go to SQLite download page, and download precompiled binaries from Windows section: http://www.sqlite.org/download.html
(2). you will need to download sqlite-shell-win32-*.zip and sqlite-dll-win32-*.zip zipped files.
(3). Create a folder C:\>sqlite and unzip above two zipped files in this folder which will give you sqlite3.def, sqlite3.dll and sqlite3.exe files.
(4). Add C:\>sqlite in your PATH environment variable and finally go to the command prompt and issue sqlite3 command, which should display a result something as below.


How To Access SQLite
----------------------------------------------------------------------------
1. sqlite3 g2pl.db // connect to database called g2pl.db
2. select * from g2pl_database; // print the table
3. .quit // DO NOT forget to quit after viewing data values, OTHERWISE sites cannot write because the database is holden by sqlite3.exe

Test Cases
----------------------------------------------------------------------------
There are several testing histories to bomb the other sites. They are under 2PLDDB/bin directory. The transaction files are under 2PLDDB/bin/testcases_cmd directory. They have names like transactions_<description>_<number>.txt. Tag "few" means there are few transactions in this test case. Tag "long" means the transactions in this test case have many operations. Tag 'many' means there are many transactions in this test case. In order to simulate parallel running of multiple processes, some scripts are written.

----Test Cases In Linux----
These bash scripts are under 2PLDDB/bin/testcases_bash. Please start the central site first. Then open multiple consoles and try to start other sites in the same time. Note that you have to kill these Java processes if you want to restart the server. Otherwise, a port will be blocked by existing processes.

----Test Cases In Windows----
These batch scripts are under 2PLDDB/bin/testcases_cmd directory. Start the central site first. Then select the other sites you want to run in parallel, press enter.
