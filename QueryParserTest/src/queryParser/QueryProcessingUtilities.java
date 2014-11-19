package queryParser;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import queryReconstructor.PlanReducer;
import java.util.Map;

public class QueryProcessingUtilities {
	/*
	 * Utility class for miscellaneous query text processing  
	 */
	
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
	
	public static boolean conditionContainsAliases(String condition, JSONArray aliases) {
		// TODO: string literals
		boolean containsAlias = false;
		Iterator<String> it = aliases.iterator();
		while (it.hasNext()) {
			// possibly add \b for word boundary
			String alias = it.next();
			String regex = "\\b" + alias + "\\.";
			// so in here we should be only executing the replace all on sections of the condition that are not string literals.
			// okay, it shouldn't be THAT bad to implement - just go through and check for unescaped single quotes, and ignore everything in between pairs.
			// but let's get aggregates working first.
			if (condition.contains(regex)) {
				containsAlias = true;
			}
		}
		return containsAlias;
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
	
	public static String extractConditionsContainingAlias(JSONObject reducedNode, JSONArray aliases, PlanReducer pr) {
		String cond = "";
		String tmp = pr.getFilter(reducedNode);
		if (conditionContainsAliases(tmp, aliases)) {
			cond = combineAndConditions(cond, tmp);
			pr.setFilter(reducedNode, "");
		}
		return cond;
	}
	
	public static boolean filterContainsSubplan(String filter) {
		//TODO implement for real - this (like everything else) doesn't handle string literals
		if (filter.contains("SubPlan")) {
			return true;
		}
		return false;
	}
	
	public static String generateColumnNameForFunction(String function) {
		function = function.replaceAll("'", "").replaceAll(":", "").replaceAll("\\.", "_").replaceAll("\\*", "star").replaceAll("\\(", "_").replaceAll("\\)", "_").replaceAll(" ", "").replaceAll("\\\\", "_").replaceAll("__", "_");
		return function;
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
	
	public static JSONArray getFinalColumnNames(JSONArray columns) {
		JSONArray finalColNames = new JSONArray();
		
		Iterator<String> it = columns.iterator();
		while (it.hasNext()) {
			finalColNames.add(getFinalColumnName(it.next()));
		}
		
		return finalColNames;
	}
	
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
	
	public static String removeAllWrappingParentheses(String inStr) {
		while ((inStr.length() >= 2) && inStr.charAt(0) == '(' && inStr.charAt(inStr.length()-1) == ')')
		{
			inStr = inStr.substring(1, inStr.length()-1);
		}
		return inStr;		
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
	
	public static String splitOnDotAddUnderscore(String attr) {
		String[] ar = attr.split("\\.");
		if (ar.length == 2) {
			attr = ar[0] + "_" + ar[1];
		}
		return attr;
	}

	
	
	public static boolean searchJSONArrayForString(JSONArray ar, String search) {
		for (int i = 0; i < ar.size(); i++) {
			if (((String)ar.get(i)).equals(search)) {
				return true;
			}
		}
		return false;
	}


	
	public static String replaceSubplanNameWithQuery(String subplanName, String filter, String query) {
		// TODO: not robust to string literals!!!!
		
		filter = filter.replace(subplanName, query);
		return filter;
	}


	
	public static JSONArray renameAttributesSimple(JSONArray attrs) {
		JSONArray newAttrs = new JSONArray();
		Iterator<String> it = attrs.iterator();
		while (it.hasNext()) {		
			String attr = it.next();
			String[] attrParts = attr.split("\\.");
			String newAttr = "";
			// check alias, replace with name of child
			if (attrParts.length == 2) {
				newAttrs.add(attr + " as " + attrParts[0] + "_" + attrParts[1]);// + " as " + attrParts[0] + "_" + attrParts[1]);
			} else {
				newAttrs.add(attr);
			}
		}
		return newAttrs;
	}
	
	public static JSONArray replaceFunctionsWithColumnNames(JSONArray attrs, Map<String, String> functionToName) {
		JSONArray newAttrs = new JSONArray();
		Iterator<String> it = attrs.iterator();
		while (it.hasNext()) {		
			String attr = it.next();
			attr = removeAllWrappingParentheses(attr);
			if (functionToName.containsKey(attr)) {
				String newName = functionToName.get(attr);
				newAttrs.add(newName);
			} else {
				newAttrs.add(attr);
			}
		}
		return newAttrs;
	}
	
	/*
	public static void replaceAliasWithRenamedColumns(String text, JSONArray alias) {
		
	}
	*/

}
