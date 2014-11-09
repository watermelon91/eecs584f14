package queryParserTest;

//import java.io.BufferedReader;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import binaryTree.LinkedBinaryTreeNode;
//import binaryTree.LinkedBinaryTreeNode;
import queryParser.QueryParser;
import queryReconstructor.QueryReconstructor;
import frontEndConnector.FrontEndConnector;
//import frontEndConnector.QueryPlanTreeNode;
import frontEndConnector.QueryPlanTreeNode;

public class QueryParserTest {

	// example usage of QueryParser class
	public static void main(String [ ] args) throws Exception 
	{
		// Example of how UI can call the db
		// Example1: connecting the db
		System.out.println("\n---------Example 1---------");

		//FrontEndConnector UIConnector = new FrontEndConnector("eecs484.eecs.umich.edu", "uniqname", "uniqname", "pwd");
		FrontEndConnector UIConnector = new FrontEndConnector("127.0.0.1", "eecs584", "postgres", "pwd");
		
		String rst = UIConnector.initializeSQLConnection();
		if(rst=="")
		{
			System.out.println("Connection to db established...");
		}
		else
		{
			System.out.println(rst);
		}
		
		// Example2: getSampleData()
		System.out.println("\n---------Example 2---------");
		List<String[]> sampleDataRst = UIConnector.getSampleData("users").data;
		//String[] sampleDataRstArray = sampleDataRst.split(",");
		for(int i = 0; i < sampleDataRst.size(); i++)
		{
			System.out.println(sampleDataRst.get(i).toString());
		}
		
		// Example3: executeTestQuery()
		System.out.println("\n---------Example 3---------");
		List<String[]> testQueryRst = UIConnector.executeTestQuery("SELECT * FROM users WHERE user_id < 5;").data;
		//String[] testQueryRstArray = sampleDataRst.split(",");
		for(int i = 0; i < testQueryRst.size(); i++)
		{
			System.out.println(testQueryRst.get(i).toString());
		}
		
		// Example 4: get query plan
		System.out.println("\n---------Example 4---------");
		LinkedBinaryTreeNode<QueryPlanTreeNode>  queryPlanTopLevelNode = UIConnector.debugQuery("select * from hrecords h, users u where h.user_id = u.user_id;");
		//System.out.println(queryPlanTopLevelNode.toString());
		
		// Example5: close the db connection
		System.out.println("\n---------Example 5---------");
		UIConnector.closeDBConnection();
		if(rst=="")
		{
			System.out.println("Connection to db closed.");
		}
		else
		{
			System.out.println(rst);
		}
				
		// Example of query parser class
		System.out.println("\n---------Example of query parser class---------");
		// input file containing the returned query plan
		String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1_verbose.txt";
//		String inputFilePath = "/afs/umich.edu/user/d/a/daneliza/dwtemp/F14/eecs584/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		JSONObject topLevelNode = qParser.topLevelNode;
		
		// get the output attributes
		JSONArray outputAttrs = qParser.getOutputAttributes(topLevelNode);
		Iterator<String> oiterator = outputAttrs.iterator();
		System.out.print("Output attribtues: ");
		while(oiterator.hasNext())
		{
			String curAttr = oiterator.next();
			System.out.print(curAttr.toString() + " ");
		}
		System.out.print("\n");
		
		// get some individual attributes of a node, except nested plans
		// if some attributes don't exist in the node, the function returns a blank string ""
		System.out.println("Node Type: " + qParser.getNodeType(topLevelNode));
		System.out.println("Alias: " +  qParser.getAlias(topLevelNode));
		System.out.println("Parent Relationship: " + qParser.getParentRelationship(topLevelNode));
		System.out.println("Hash Condition: " + qParser.getHashCond(topLevelNode));
		System.out.println("Join Type: " + qParser.getJoinType(topLevelNode));
		System.out.println("Schema: " + qParser.getSchema(topLevelNode));
		System.out.println("---------end of one node-----------");
		
		// get the 2nd level plan from the top level node
		JSONArray childrenNodes = qParser.getChildrenPlanNodes(topLevelNode);
		// iterate through all the children plans of the top level node
		Iterator<JSONObject> citerator = childrenNodes.iterator();	
		while(citerator.hasNext())
		{
			JSONObject curChild = citerator.next();	
			
			System.out.println(curChild.toJSONString());
			
			// print some attributes of this node
			System.out.println("Node Type: " + qParser.getNodeType(curChild));
			System.out.println("Alias: " + qParser.getAlias(curChild)); 
			System.out.println("Parent Relationship: " + qParser.getParentRelationship(curChild));
			System.out.println("Hash Condition: " + qParser.getHashCond(curChild));
			System.out.println("Join Type: " + qParser.getJoinType(curChild));
			System.out.println("Schema: " + qParser.getSchema(curChild));
			System.out.println("---------end of one node-----------");
		}
		// you can keep iterating all the way to the deepest level by repeatedly getting the children plan of curChild
		
		// previous code -- ignore
		/*BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
		String line = "", jsonString = "";
		while ((line = reader.readLine()) != null) {
		    jsonString = jsonString + line;
		}
		System.out.println(jsonString);*/
	}
}
