-------------------------- Code status--------------------------
[10/5]
Query parser:
    read in a JSON query plan object (from "EXPLAIN (FORMAT JSON) SELECT ...") from file
    supports retrieving the attributes in the current level query plan
    supports retrieving sub query plans
[10/25]
Query parser:
    added second constructor to support read in a JSON query plan from string
database connector:
    login, execute a SELECT query, logout
front end connector: 
    construct a connector
    getSampleData() on a table
    executeTestQuery()
    debugQuery() currently returns a complete query plan, instead of a reduced one (once the query reducer is done we should be able to return a reduced query plan)

-------------------------- TO-DO--------------------------
[listed from top-priority]
1. Try to reconstruct a valid query from the parsed query plan. If we can reconstruct a valid query from the query plan for a simple original query, we can keep working on extending the query parser & reconstruction to support more complex queries.

2. It's very likely that there are other attributes in the EXPLAIN output not captured in queryPlanAttrMapping in QueryParser.java. We can add them as we see more.


-------------------------- Folder structure --------------------------
1. QueryParserTest:
	- Contains QueryParser class in QueryParser.java.
    - QueryReconstructor class (to be implemented).
	- The main method is in QueryParserTest.java.
	- The github version is Eclipse version. 
	  Take out the java files in the QueryParserTest/src folder to compile on command line, but make sure to reference the json-simple-1.1.1.jar. Notice that the parser and the main function are in their own packages so we can easily take out the main function later.

2. TestingData:
	- i.e. testing data :)

3. PerlParserTrial:
	- Incomplete code used to explore the perl Postgres parser 