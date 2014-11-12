package frontEndConnector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import queryParser.QueryProcessingUtilities;
import databaseConnector.PostgresDBConnector;
import frontEndConnector.DataPlanTreeNode.BlankAttributesException;
import frontEndConnector.DataPlanTreeNode.NonMatchingAttrCountAndValueCountException;
import binaryTree.LinkedBinaryTreeNode;

public class DataPlanConstructor {
	private LinkedBinaryTreeNode<QueryPlanTreeNode> rootPlanNode;
	private List<Pair> rootPairList;
	private PostgresDBConnector pgConnector;
	
	private class Pair{
		public String aliasAttr;
		public String val;
		public String originalAttr;
		public boolean needQuotes;
		
		public Pair(String _aliasAttr, String _val, String _originalAttr, boolean _needQuotes)
		{
			aliasAttr = _aliasAttr;
			val = _val;
			originalAttr = _originalAttr;
			needQuotes = _needQuotes;
		}
	}
	
	public class rowDataAndAttributeMismatchException extends Exception{};
	
	public DataPlanConstructor(LinkedBinaryTreeNode<QueryPlanTreeNode> _planNode, 
			String[] _rowData,
			PostgresDBConnector _pgConnector) throws rowDataAndAttributeMismatchException
	{
		rootPlanNode = _planNode;
		pgConnector = _pgConnector;
		String[] _rootAttributes = QueryProcessingUtilities.removeSquareParenthesis(rootPlanNode.getData().getOutputAttrs()).split(",");
		List<String[]> dataTypes = null;
		try {
			// get column types of the attributes
			dataTypes = pgConnector.executeQuerySeparateResult(" select column_name, data_type from information_schema.columns where table_name = '" + rootPlanNode.getData().getNewTableName() + "'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(_rootAttributes.length != _rowData.length || dataTypes == null || (dataTypes != null && dataTypes.size() != _rowData.length))
		{
			throw new rowDataAndAttributeMismatchException();
		}
		
		// DEBUG ONLY
		for(int i = 0; i < dataTypes.size(); i++)
		{
			System.out.println("TYPE: " + dataTypes.get(i)[0] + " " + dataTypes.get(i)[1] + " " + _rootAttributes[i]);
		}
		
		rootPairList = new ArrayList<Pair>();
		for(int i = 0; i < _rowData.length; i++)
		{
			_rootAttributes[i] = QueryProcessingUtilities.removeQuotes(_rootAttributes[i]);
			String type = getType(_rootAttributes[i], dataTypes);
			rootPairList.add(createPair(_rootAttributes[i], _rowData[i], needsQuote(type)));
			
		}
	}
	
	public LinkedBinaryTreeNode<DataPlanTreeNode> build()
	{
		if(rootPlanNode != null && rootPairList != null)
		{
			LinkedBinaryTreeNode<DataPlanTreeNode> dataRoot = constructTree(rootPlanNode, rootPairList);
			return dataRoot;
		}
		else{
			return null;
		}
	}
	
	private LinkedBinaryTreeNode<DataPlanTreeNode> constructTree(
			LinkedBinaryTreeNode<QueryPlanTreeNode> rootPlanNode,
			List<Pair> rootPairList
			)
	{
		LinkedBinaryTreeNode<DataPlanTreeNode> root = createDataNode(rootPlanNode, rootPairList);

		if(rootPlanNode.getLeft() != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> leftNode = (LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getLeft();
			List<Pair> leftPairList = updateAttrNames(leftNode, rootPairList);
			LinkedBinaryTreeNode<DataPlanTreeNode> left = constructTree(
					leftNode,
					leftPairList
					);
			root.setLeft(left);
		}
		if(rootPlanNode.getRight() != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> rightNode = (LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getRight();
			List<Pair> rightPairList = updateAttrNames(rightNode, rootPairList);
			LinkedBinaryTreeNode<DataPlanTreeNode> right = constructTree(
					rightNode,
					rightPairList
					);
			root.setRight(right);
		}
		
		return root;
	}
	
	private List<Pair> updateAttrNames(
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode,
			List<Pair> rootPairList
			)
	{
		String attributeStr = QueryProcessingUtilities.removeSquareParenthesis(planNode.getData().getOutputAttrs());
		String[] curNodeAttributes = attributeStr.split(",");
		
		System.out.println(Arrays.asList(curNodeAttributes).toString());
		
		List<Pair> newPairList = new ArrayList<Pair>();
		for(int i = 0; i < rootPairList.size(); i++)
		{
			Pair curRootPair = rootPairList.get(i);
			System.out.println("Original: " + curRootPair.originalAttr + "; Alias : " + curRootPair.aliasAttr);
			String oldAttr = curRootPair.originalAttr;
			if(oldAttr.contains("."))
			{
				oldAttr = oldAttr.substring(oldAttr.indexOf('.')+1);
			}
			System.out.println(oldAttr);
			
			String matchedAttr = searchMatchingAttr(curNodeAttributes, oldAttr);
			if(!matchedAttr.equals(""))
			{
				newPairList.add(createPair(matchedAttr, rootPairList.get(i).val, rootPairList.get(i).needQuotes));
			}
			
		}
		
		return newPairList;
	}
	
	private String searchMatchingAttr(String[] attrSearchSpace, String oldAttr)
	{
		for(int i = 0; i < attrSearchSpace.length; i++)
		{
			if(attrSearchSpace[i].contains(oldAttr))
			{
				return QueryProcessingUtilities.removeQuotes(attrSearchSpace[i]);
			}
		}
		return "";
	}
	
	private Pair createPair(String originalAttr, String val, boolean needQuotes)
	{
		String shortOriginalAttr = "";
		String aliasAttr = "";
		if(originalAttr.contains(" as "))
		{
			shortOriginalAttr = removeAlias(originalAttr.substring(0, originalAttr.indexOf(" ")));
			aliasAttr = originalAttr.substring(originalAttr.indexOf("as ") + 3);
		}
		else
		{
			shortOriginalAttr = removeAlias(originalAttr);
			aliasAttr = removeAlias(originalAttr);
		}

		return new Pair(aliasAttr, val, shortOriginalAttr, needQuotes);
	}
	
	private String removeAlias(String inAttr)
	{
		if(inAttr.contains("."))
		{
			inAttr = inAttr.substring(inAttr.indexOf('.')+1);
		}
		
		return inAttr;
	}
	
	// create a new LinkedBinaryTreeNode
	private LinkedBinaryTreeNode<DataPlanTreeNode> createDataNode(
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode,
			List<Pair> curPairList
			)
	{
		QueryPlanTreeNode node = planNode.getData();
		
		// find the attributes in the node that also exist in this node 
		String[] curAttributes = new String[curPairList.size()];
		String whereClause = "";
		for(int i = 0; i < curPairList.size(); i++)
		{
			if(whereClause != "")
			{
				whereClause = whereClause + " AND ";
			}
			Pair curPair = curPairList.get(i);
			if(curPair.needQuotes)
			{
				whereClause = whereClause + curPairList.get(i).aliasAttr + " = '" + QueryProcessingUtilities.removeQuotes(curPair.val) + "' ";
			}
			else
			{
				whereClause = whereClause + curPairList.get(i).aliasAttr + " = " + QueryProcessingUtilities.removeQuotes(curPair.val) + " ";
			}
			curAttributes[i] = curPairList.get(i).aliasAttr;
		}
		System.out.println("WHERE: " + whereClause);
		
		List<String[]> values = null;
		try 
		{
			if(whereClause != "")
			{
				String query = "SELECT * FROM " + node.getNewTableName() + " WHERE " + whereClause; 
				System.out.println("Query: " + query);
				values = pgConnector.executeQuerySeparateResult(query);
				System.out.println("VALUES: " + Arrays.asList(values.get(0)).toString());
			}
			else
			{
				values = null;
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		DataPlanTreeNode dataNode = null;
		try 
		{
			dataNode = new DataPlanTreeNode(curAttributes, values);
		} 
		catch (NonMatchingAttrCountAndValueCountException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (BlankAttributesException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LinkedBinaryTreeNode<DataPlanTreeNode> treeNode = new LinkedBinaryTreeNode<DataPlanTreeNode>(dataNode);
		return treeNode;
	}
	
	private String getType(String attr, List<String[]> dataTypes)
	{
		for(int i = 0; i < dataTypes.size(); i++)
		{
			if(attr.contains(dataTypes.get(i)[0]))
			{
				return dataTypes.get(i)[1];
			}
		}
		return "";
	}
	
	private boolean needsQuote(String attrType)
	{
		if(attrType.equals("bigint") ||
				attrType.equals( "bigserial") ||
				attrType.equals( "boolean") ||
				attrType.equals( "double precision") ||
				attrType.equals( "integer") ||
				attrType.equals( "numeric") ||
				attrType.equals( "real") ||
				attrType.equals( "smallint") ||
				attrType.equals( "smallserial") ||
				attrType.equals( "serial") ||
				attrType.equals( "int8") ||
				attrType.equals( "serial8") ||
				attrType.equals( "bool") ||
				attrType.equals( "float8") ||
				attrType.equals( "int") ||
				attrType.equals( "int4") ||
				attrType.equals( "decimal") ||
				attrType.equals( "float4") ||
				attrType.equals( "int2") ||
				attrType.equals( "serial2") ||
				attrType.equals( "serial4"))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
