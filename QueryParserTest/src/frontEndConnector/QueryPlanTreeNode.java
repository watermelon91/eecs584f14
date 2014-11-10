package frontEndConnector;

import  java.lang.Math;

public class QueryPlanTreeNode {
	private String type;
	private String aliasSet;
	private String filter;
	private String inputTable;
	private String newTableName;
	private String joinCondition;
	private String outputAttrs;
	private AbbreviatedTreeNode abbrTreeNode;
	
	public class AbbreviatedTreeNode{
		private String LargeFontStr;
		private String SmallFontStr;
		
		public AbbreviatedTreeNode(String _Large, String _Small)
		{
			LargeFontStr = _Large;
			SmallFontStr = _Small;
		}
		
		public String getLargeFontStr()
		{
			return LargeFontStr;
		}
		
		public String getSmallFontStr()
		{
			return SmallFontStr;
		}
	}
	
	public QueryPlanTreeNode(
			String _type, 
			String _aliasSet, 
			String _filter,
			String _inputTable,
			String _newTableName,
			String _joinCondition,
			String _outputAttrs
			)
	{
		type = _type;
		aliasSet = _aliasSet;
		filter = _filter;
		inputTable = _inputTable;
		newTableName = _newTableName;
		joinCondition = _joinCondition;
		outputAttrs = _outputAttrs;
		
		abbrTreeNode = new AbbreviatedTreeNode(constructLargeFontString(), constructSmallFontString());
	}
	
	/*
	 * - only type, filter, inputTable, joinCondition matter in the abbreviated version
	 * - large font: type, inputTable
	 * - small font: filter, joinCondition
	 */
	private String constructSmallFontString()
	{
		String str = "";
		if(!filter.isEmpty())
		{
			str = str + filter;
		}
		if(!joinCondition.isEmpty())
		{
			str = str + "; " + joinCondition;
		}
		
		return str;
	}
	
	private String constructLargeFontString()
	{
		String str = "";
		if(!type.isEmpty())
		{
			str = str + type;
		}
		if(!inputTable.isEmpty())
		{
			str = str + "; " + inputTable;
		}
		
		return str;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getAliasSet()
	{
		return aliasSet;
	}
	
	public String getFilter()
	{
		return filter;
	}
	
	public String getInputTable()
	{
		return inputTable;
	}
	
	public String getNewTableName()
	{
		return newTableName;
	}
	
	public String getJoinCondition()
	{
		return joinCondition;
	}
	
	public String getOutputAttrs()
	{
		return outputAttrs;
	}
	
	public AbbreviatedTreeNode getAbbreviatedTreeNode()
	{
		return abbrTreeNode;
	}
	
	 @Override public String toString() {
		 final String SPLITTER = "\n";
		 //final String SPLITTER = "`";
		 
		 //String outputAttrFormatted = outputAttrs.substring(1, outputAttrs.indexOf(',')) + ", ...";
		 String outputAttrFormatted = outputAttrs.substring(1, outputAttrs.length()-2);
		 
		 String nodeFormattedStr = constructString("Type", type, SPLITTER) 
				 + constructString("Alias", aliasSet, SPLITTER) 
				 + constructString("Filter", filter, SPLITTER)
				 + constructString("Input", inputTable, SPLITTER)
				 + constructString("TempTable", newTableName, SPLITTER)
				 + constructString("JoinCond", joinCondition, SPLITTER)
				 + constructString("Output", outputAttrFormatted, SPLITTER);

		 return nodeFormattedStr;
	 }
	 
	 public interface Visitor 
	 {
		 void visit(QueryPlanTreeNode node);
	 }	
	 
	 private String constructString(String nodeLabel, String nodeAttr, String SPLITTER)
	 {

		 if(nodeAttr.isEmpty())
		 {
			 return "";
		 }
		 else
		 {
			 return cutOffLongString(nodeLabel + ": " + nodeAttr + SPLITTER);
		 }
	 }
	 
	 private String cutOffLongString(String input)
	 {
		 int MAX_LEN = 45;
		 if(input.length() <= MAX_LEN)
		 {
			 return input;
		 }
		 else
		 {
			 String multilineInput = "";
			 int i = 0;
			 for(; i < (int)Math.floor((input.length() * 1.0 / MAX_LEN)); i++)
			 {
				 multilineInput = multilineInput + input.substring(i * MAX_LEN, (i+1) * MAX_LEN) + "\n";
			 }
			 if(input.length() > i * MAX_LEN)
			 {
				 multilineInput = multilineInput + input.substring(i * MAX_LEN, input.length());
			 }
			 
			 return multilineInput;
		 }
	 }
	
	
}
