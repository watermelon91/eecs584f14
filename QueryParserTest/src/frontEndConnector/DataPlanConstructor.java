package frontEndConnector;

import java.sql.SQLException;
import java.util.List;

import queryParser.QueryProcessingUtilities;
import databaseConnector.PostgresDBConnector;
import frontEndConnector.DataPlanTreeNode.BlankAttributesException;
import frontEndConnector.DataPlanTreeNode.NonMatchingAttrCountAndValueCountException;
import binaryTree.LinkedBinaryTreeNode;

public class DataPlanConstructor {
	private LinkedBinaryTreeNode<QueryPlanTreeNode> rootPlanNode;
	private String columnName;
	private String columnValue;
	private PostgresDBConnector pgConnector;
	
	public DataPlanConstructor(LinkedBinaryTreeNode<QueryPlanTreeNode> _planNode, 
			String _columnName,
			String _columnValue,
			PostgresDBConnector _pgConnector)
	{
		rootPlanNode = _planNode;
		columnName = _columnName;
		columnValue = _columnValue;
		pgConnector = _pgConnector;
	}
	
	public LinkedBinaryTreeNode<DataPlanTreeNode> build()
	{
		if(rootPlanNode != null)
		{
			LinkedBinaryTreeNode<DataPlanTreeNode> dataRoot = constructTree(rootPlanNode);
			return dataRoot;
		}
		else{
			return null;
		}
	}
	
	private LinkedBinaryTreeNode<DataPlanTreeNode> constructTree(LinkedBinaryTreeNode<QueryPlanTreeNode> rootPlanNode)
	{
		LinkedBinaryTreeNode<DataPlanTreeNode> root = createDataNode(rootPlanNode);
		
		if(rootPlanNode.getLeft() != null)
		{
			LinkedBinaryTreeNode<DataPlanTreeNode> left = createDataNode((LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getLeft());
			root.setLeft(left);
		}
		if(rootPlanNode.getRight() != null)
		{
			LinkedBinaryTreeNode<DataPlanTreeNode> right = createDataNode((LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getRight());
			root.setRight(right);
		}
		
		return root;
	}
	
	// create a new LinkedBinaryTreeNode
	private LinkedBinaryTreeNode<DataPlanTreeNode> createDataNode(LinkedBinaryTreeNode<QueryPlanTreeNode> planNode)
	{
		QueryPlanTreeNode node = planNode.getData();
		String attributeStr = QueryProcessingUtilities.removeSquareParenthesis(node.getOutputAttrs());
		String[] attributes = attributeStr.split(",");
		
		List<String[]> values = null;
		try 
		{
			String query = "SELECT * FROM " + node.getNewTableName() + " WHERE " + columnName + " = " + columnValue; 
			values = pgConnector.executeQuerySeparateResult(query);
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			if(!e.getMessage().contains("column \"" + columnName +"\" does not exist"))
			{
				e.printStackTrace();
			}
			else
			{
				System.out.println(node.getNewTableName() + " does not have column " + columnName);
			}
		}
		
		DataPlanTreeNode dataNode = null;
		try 
		{
			dataNode = new DataPlanTreeNode(attributes, values);
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
}
