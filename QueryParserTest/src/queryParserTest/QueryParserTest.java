package queryParserTest;

//import java.io.BufferedReader;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import QueryParser.QueryParser;

public class QueryParserTest {

	public static void main(String [ ] args) 
	{
		// input file containing the returned query plan
		String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1.txt";
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		JSONObject topLevelNode = qParser.toplevelNode;
		
		// get the attributes of topLevelNode, except nested plans
		System.out.println(qParser.getNodeType(topLevelNode));
		
		// get the 2nd level plan from the top level node
		JSONArray childrenNodes = qParser.getChildrenPlanNodes(topLevelNode);
		// iterate through all the children plans of the top level node
		// this can be repeated all the way to get the deepest level
		Iterator<JSONObject> citerator = childrenNodes.iterator();	
		while(citerator.hasNext())
		{
			JSONObject curChild = citerator.next();
			System.out.println(curChild.toJSONString());
		}
		
		// previous code
		/*BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
		String line = "", jsonString = "";
		while ((line = reader.readLine()) != null) {
		    jsonString = jsonString + line;
		}
		System.out.println(jsonString);*/
	}
}
