package queryParser;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class QueryProcessingUtilities {
	/*
	 * Utility class for miscellaneous query text processing  
	 */
	
	public static String removeParenthesis(String inStr)
	{
		if((inStr.length() >= 2) && inStr.charAt(0) == '(' && inStr.charAt(inStr.length()-1) == ')')
		{
			return inStr.substring(1, inStr.length()-1);
		}
		else
		{
			return inStr;
		}
	}
	
	
	public static JSONArray concatArrays(JSONArray a1, JSONArray a2) {
		JSONArray newAr = new JSONArray();
		Iterator<JSONObject> it = a1.iterator();	
		while(it.hasNext())
		{
			newAr.add(it.next());
		}
		
		it = a2.iterator();	
		while(it.hasNext())
		{
			newAr.add(it.next());
		}
 
		return newAr;
	}

}
