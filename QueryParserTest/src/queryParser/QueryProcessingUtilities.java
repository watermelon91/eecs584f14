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
	
	public static String removeSquareParenthesis(String inStr)
	{
		if((inStr.length() >= 2) && inStr.charAt(0) == '[' && inStr.charAt(inStr.length()-1) == ']')
		{
			return inStr.substring(1, inStr.length()-1);
		}
		else
		{
			return inStr;
		}
	}
	
	public static String removeQuotes(String inStr)
	{
		if((inStr.length() >= 2) && inStr.charAt(0) == '"' && inStr.charAt(inStr.length()-1) == '"')
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
	
	
	public static boolean searchJSONArrayForString(JSONArray ar, String search) {
		for (int i = 0; i < ar.size(); i++) {
			if (((String)ar.get(i)).equals(search)) {
				return true;
			}
		}
		return false;
	}

	public static JSONArray getFinalColumnNames(JSONArray columns) {
		JSONArray finalColNames = new JSONArray();
		
		Iterator<String> it = columns.iterator();
		while (it.hasNext()) {
			finalColNames.add(getFinalColumnName(it.next()));
		}
		
		return finalColNames;
	}
	
	public static String getFinalColumnName(String origColName) {
		String finalCol = "";
		if (origColName.contains(" as ")) {
			String[] ar = origColName.split(" as ");
			finalCol = ar[1];
		} else {
			finalCol = origColName;
		}
		
		return finalCol;
	}
	
	public static String combineAndConditions(String cond1, String cond2) {
		String finalCond = "";
		
		if (!cond1.equals("")) {
			finalCond = cond1;
			if (!cond2.equals("")) {
				finalCond = finalCond + " and " + cond2;
			}
		} else {
			finalCond = cond2;
		}
		return finalCond;
	}
}
