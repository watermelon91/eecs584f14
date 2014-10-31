package frontEndConnector;

public class QueryPlanTreeNode {
	public String relationName;
	public String operator;
	public String outputAttrs;
	
	public QueryPlanTreeNode(String _relationName, String _operator, String _outputAttrs)
	{
		relationName = _relationName;
		operator = _operator;
		//outputAttrs = _outputAttrs;
		outputAttrs = "";
	}
	
	public String getRelationName()
	{
		return relationName;
	}
	
	public String getOperator()
	{
		return operator;
	}
	
	public String getOutputAttrs()
	{
		return outputAttrs;
	}
	
	 @Override public String toString() {

		 String nodeFormattedStr = "Relation: " + nodeToString(relationName) + "\n"
				 + "Op: " + nodeToString(operator) + "\n" ;
				// + "Output: " + nodeToString(outputAttrs);

		 return nodeFormattedStr;
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
