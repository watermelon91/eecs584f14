package queryParser;

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
}
