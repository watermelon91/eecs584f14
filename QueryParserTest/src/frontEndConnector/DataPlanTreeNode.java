package frontEndConnector;

import java.util.List;

import queryParser.QueryProcessingUtilities;

import com.sun.xml.internal.ws.util.StringUtils;

import binaryTree.LinkedBinaryTreeNode;

public class DataPlanTreeNode {
	
	private String[] attrs;
	private List<String[]> values;
	
	public class BlankAttributesException extends Exception{}
	public class NonMatchingAttrCountAndValueCountException extends Exception{}
	
	public DataPlanTreeNode(String[] _attrs, List<String[]> _values) throws NonMatchingAttrCountAndValueCountException, BlankAttributesException
	{
		if(_attrs == null || _attrs.length == 0)
		{
			throw new BlankAttributesException();
		}
		attrs = _attrs;
		
		if(_values != null)
		{
			if((_values.size() > 0) && (_attrs.length != _values.get(0).length))
			{
				throw new NonMatchingAttrCountAndValueCountException();
			}
			
			values = _values;
		}
		else
		{
			// it's ok for some table to not have the data
			values = null;
		}
	}
	
	public String[] getAttributes()
	{
		return attrs;
	}
	
	public List<String[]> getValues()
	{
		return values;
	}
	
	 @Override public String toString() {
		 final String SPLITTER = "\n";
		 
		 String[] formats = new String[attrs.length];
		 for(int i = 0; i < attrs.length; i++)
		 {
			 formats[i] = "%-" + attrs[i].length() + "s";
		 }
				 
		 String nodeFormattedStr = "";
		 for(int i = 0; i < attrs.length; i++)
		 {
			 nodeFormattedStr = nodeFormattedStr + " | " + String.format(formats[i], QueryProcessingUtilities.removeQuotes(attrs[i]));
		 }
		 nodeFormattedStr = nodeFormattedStr + SPLITTER;
		 
		 if(values == null)
		 {
			 return nodeFormattedStr;
		 }
		 
		 for(int i = 0; i < values.size(); i++)
		 {
			 String[] vals = values.get(i);
			 for(int j = 0; j < attrs.length; j++)
			 {
				 nodeFormattedStr = nodeFormattedStr + " | " + String.format(formats[j], vals[j]);
			 }
			 nodeFormattedStr = nodeFormattedStr + SPLITTER;
		 }
		 return nodeFormattedStr;
	 }
}
