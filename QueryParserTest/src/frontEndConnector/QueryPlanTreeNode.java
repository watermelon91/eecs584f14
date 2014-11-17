package frontEndConnector;

import  java.lang.Math;
import java.util.Arrays;

public class QueryPlanTreeNode {
	private String type;
	private String aliasSet;
	private String filter;
	private String inputTable;
	private String newTableName;
	private String joinCondition;
	private String outputAttrs;
	private AbbreviatedTreeNode abbrTreeNode;
	private DataPlanTreeNode dataNode;
	
	public class AbbreviatedTreeNode{
		private String LargeFontStr;
		private String SmallFontStr;
		private String tmpTableName;
		
		public AbbreviatedTreeNode(String _Large, String _Small, String _tmpTableName)
		{
			LargeFontStr = _Large;
			SmallFontStr = _Small;
			tmpTableName = _tmpTableName;
		}
		
		public String getLargeFontStr()
		{
			return LargeFontStr;
		}
		
		public String getSmallFontStr()
		{
			return SmallFontStr;
		}
		
		public String getTmpTableStr()
		{
			return tmpTableName;
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
		
		// make table names readable
		String[] inTables = inputTable.split(",");
		for(int i = 0; i < inTables.length; i++)
		{
			inTables[i] = getAbbrTmpTable(inTables[i]);	
		}
		inputTable = Arrays.asList(inTables).toString();
		newTableName = getAbbrTmpTable(newTableName);
		
		// construct abbrevated node
		abbrTreeNode = new AbbreviatedTreeNode(constructLargeFontString(), constructSmallFontString(), newTableName);
		dataNode = null;
		
		System.out.println(getAbbreviatedTreeNode().getLargeFontStr());
		System.out.println(getAbbreviatedTreeNode().getSmallFontStr());
	}
	
	private String getAbbrTmpTable(String inName)
	{
		if(inName.contains("_"))
		{
			return inName.substring(0, inName.indexOf("_")-1);
		}
		else
		{
			return inName;
		}
	}
	
	/*
	 * - only type, filter, inputTable, joinCondition matter in the abbreviated version
	 * - large font: type
	 * - small font: filter, joinCondition, inputTable
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
			if(!str.isEmpty())
			{
				str = str + "; ";
			}
			str = str + joinCondition;
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
		str = str.toUpperCase();
		
		if(!inputTable.isEmpty())
		{
			if(!str.isEmpty())
			{
				str = str + " ";
			}
			str = str + inputTable;
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
	
	public DataPlanTreeNode getDataNode()
	{
		if(dataNode != null && dataNode.getValues() != null && dataNode.getValues().size() == 0)
		{
			// return null if no row of data selected
			return null;
		}
		else
		{
			return dataNode;
		}
	}
	
	public void setDataNode(DataPlanTreeNode _inNode)
	{
		dataNode = _inNode;
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
