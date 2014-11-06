package frontEndConnector;

public class QueryPlanTreeNode {
	public String type;
	public String aliasSet;
	public String filter;
	public String inputTable;
	public String newTableName;
	public String joinCondition;
	public String outputAttrs;
	
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
	
	 @Override public String toString() {
		 final String SPLITTER = "\n";
		 //final String SPLITTER = "`";
		 
		 String outputAttrFormatted = outputAttrs.substring(1, outputAttrs.indexOf(',')) + ", ... [click to show]";
		 
		 String nodeFormattedStr = "Type: " + nodeToString(type) + SPLITTER
				 + "Alias: " + nodeToString(aliasSet) + SPLITTER 
				 + "Filter: " + nodeToString(filter) + SPLITTER
				 + "InputTable: " + nodeToString(inputTable) + SPLITTER
				 + "NewTableName: " + nodeToString(newTableName) + SPLITTER
				 + "JoinCondition: " + nodeToString(joinCondition) + SPLITTER
				 + "OutputAttrs: " + nodeToString(outputAttrFormatted) + SPLITTER;

		 return nodeFormattedStr;
	 }
	 
	 public interface Visitor 
	 {
		 void visit(QueryPlanTreeNode node);
	 }	
	 
	 private String nodeToString(String nodeAttr)
	 {
		 if(nodeAttr.isEmpty())
		 {
			 return "N/A";
		 }
		 else
		 {
			 return nodeAttr;
		 }
	 }
	
	
}
