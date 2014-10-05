-------------------------- Code status + TO-DO --------------------------
[10/5]
Query parser:
    read in a JSON query plan object (from "EXPLAIN (FORMAT JSON) SELECT ...") from file
    supports retrieving the attributes in the current level query plan
    supports retrieving sub query plans

TO-DO (listed from top-priority):
1. Try to reconstruct a valid query from the parsed query plan. If we can reconstruct a valid query from the query plan for a simple original query, we can keep working on extending the query parser & reconstruction to support more complex queries.
2. Postgres EXPLAIN doesn't support JSON object with VERBOSE flag. So if we want the verbose flag (with includes "Output" attribute -- the returned column names of a query plan), we will need to either parse the raw EXPLAIN returned text, or we can still use the JSON object we have now and then parse the raw text to extract "Output" only.



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