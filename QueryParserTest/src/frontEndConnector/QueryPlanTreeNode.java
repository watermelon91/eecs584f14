package frontEndConnector;

import  java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryPlanTreeNode {
	private String type;
	private String abbrAliasSet;
	private String abbrFilter;
	private String abbrInputTable;
	private String abbrNewTableName;
	private String newTableName;
	private String abbrJoinCondition;
	private String abbrOutputAttrs;
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
		abbrAliasSet = _aliasSet;
		abbrFilter = _filter;
		abbrInputTable = _inputTable;
		abbrNewTableName = _newTableName;
		newTableName = _newTableName;
		abbrJoinCondition = _joinCondition;
		abbrOutputAttrs = _outputAttrs;
		
		// make table names readable
		// construct extra ID list
		List<String> extraIDStrs = new ArrayList<String>();
		String[] inTables = abbrInputTable.split(",");
		for(int i = 0; i < inTables.length; i++)
		{
			String ext = getExtraIDStrInTmpTable(inTables[i]);	
			if(!ext.equals(""))
			{
				extraIDStrs.add(ext);
			}
		}
		String ext = getExtraIDStrInTmpTable(newTableName);
		if(!ext.equals(""))
		{
			extraIDStrs.add(ext);
		}
		// remove extra IDs
		abbrAliasSet = removeExtraIDStr(abbrAliasSet, extraIDStrs);
		abbrFilter = removeExtraIDStr(abbrFilter, extraIDStrs);
		abbrInputTable = removeExtraIDStr(abbrInputTable, extraIDStrs);
		abbrNewTableName = removeExtraIDStr(abbrNewTableName, extraIDStrs);
		abbrJoinCondition = removeExtraIDStr(abbrJoinCondition, extraIDStrs);
		abbrOutputAttrs = removeExtraIDStr(abbrOutputAttrs, extraIDStrs);
		
		// construct abbrevated node
		abbrTreeNode = new AbbreviatedTreeNode(constructLargeFontString(), constructSmallFontString(), abbrNewTableName);
		dataNode = null;
		
		System.out.println(getAbbreviatedTreeNode().getLargeFontStr());
		System.out.println(getAbbreviatedTreeNode().getSmallFontStr());
	}
	
	private String getExtraIDStrInTmpTable(String inName)
	{
		if(inName.contains("_"))
		{
			return inName.substring(inName.indexOf("_"));
		}
		else
		{
			return "";
		}
	}
	
	private String removeExtraIDStr(String inStr, List<String> extraIDs)
	{
		for(String extra : extraIDs)
		{
			inStr = inStr.replaceAll(extra, "");
		}
		return inStr;
	}
	
	/*
	 * - only type, filter, inputTable, joinCondition matter in the abbreviated version
	 * - large font: type
	 * - small font: filter, joinCondition, inputTable
	 */
	private String constructSmallFontString()
	{
		String str = "";
		if(!abbrFilter.isEmpty())
		{
			str = str + abbrFilter;
		}
		if(!abbrJoinCondition.isEmpty())
		{
			if(!str.isEmpty())
			{
				str = str + "; ";
			}
			str = str + abbrJoinCondition;
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
		
		if(!abbrInputTable.isEmpty())
		{
			if(!str.isEmpty())
			{
				str = str + " ";
			}
			str = str + abbrInputTable;
		}
		
		return str;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getAliasSet()
	{
		return abbrAliasSet;
	}
	
	public String getFilter()
	{
		return abbrFilter;
	}
	
	public String getInputTable()
	{
		return abbrInputTable;
	}
	
	public String getNewTableName()
	{
		return newTableName;
	}
	
	public String getJoinCondition()
	{
		return abbrJoinCondition;
	}
	
	public String getOutputAttrs()
	{
		return abbrOutputAttrs;
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
		 String outputAttrFormatted = abbrOutputAttrs.substring(1, abbrOutputAttrs.length()-2);
		 
		 String nodeFormattedStr = //constructString("Type", type, SPLITTER) 
				 constructString("Alias", abbrAliasSet, SPLITTER) 
				 //+ constructString("Filter", abbrFilter, SPLITTER)
				 //+ constructString("Input", abbrInputTable, SPLITTER)
				 //+ constructString("TempTable", abbrNewTableName, SPLITTER)
				 //+ constructString("JoinCond", abbrJoinCondition, SPLITTER)
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
		 int MAX_LEN = 30;
		 String TAB = "    ";
		 if(input.length() <= MAX_LEN)
		 {
			 return input;
		 }
		 else
		 {
			 String[] multilines = input.split(",");
			 System.out.println("Multilines: " + Arrays.asList(multilines));
			 String multilineStr = multilines[0];
			 for(int i = 1; i < multilines.length; i++)
			 {
				 multilineStr = multilineStr + "\n" + TAB + multilines[i];
			 }
			 System.out.println("Formatted: " + multilineStr);
			 /*
			 String multilineInput = "";
			 int i = 0;
			 for(; i < (int)Math.floor((input.length() * 1.0 / MAX_LEN)); i++)
			 {
				 multilineInput = multilineInput + input.substring(i * MAX_LEN, (i+1) * MAX_LEN) + "\n";
			 }
			 if(input.length() > i * MAX_LEN)
			 {
				 multilineInput = multilineInput + input.substring(i * MAX_LEN, input.length());
			 }*/
			 
			 return multilineStr;
		 }
	 }
	
	
}
