package frontEndConnector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import queryParser.QueryProcessingUtilities;
import databaseConnector.PostgresDBConnector;
import databaseConnector.PostgresDBConnector.Pair;
import frontEndConnector.DataPlanTreeNode.BlankAttributesException;
import frontEndConnector.DataPlanTreeNode.NonMatchingAttrCountAndValueCountException;
import binaryTree.LinkedBinaryTreeNode;

public class DataPlanConstructor {
	private LinkedBinaryTreeNode<QueryPlanTreeNode> completePlanTreeRoot;
	private LinkedBinaryTreeNode<QueryPlanTreeNode> selectedPlanNode;
	private List<DataPair> selectedNodePairList;
	private PostgresDBConnector pgConnector;
	
	private class DataPair{
		public String aliasAttr;
		public String val;
		public String originalAttr;
		
		public DataPair(String _aliasAttr, String _val, String _originalAttr)
		{
			aliasAttr = _aliasAttr;
			val = _val;
			originalAttr = _originalAttr;
		}
	}
	
	public class rowDataAndAttributeMismatchException extends Exception{};
	
	public DataPlanConstructor(
			LinkedBinaryTreeNode<QueryPlanTreeNode> _completePlanTreeRoot,
			LinkedBinaryTreeNode<QueryPlanTreeNode> _planNode, 
			String[] _rowData,
			PostgresDBConnector _pgConnector) throws rowDataAndAttributeMismatchException
	{
		completePlanTreeRoot = _completePlanTreeRoot;
		selectedPlanNode = _planNode;
		pgConnector = _pgConnector;
		String[] _selectedNodeAttributes = QueryProcessingUtilities.removeSquareParenthesis(selectedPlanNode.getData().getOutputAttrs()).split(",");
		
		System.out.println(Arrays.asList(_selectedNodeAttributes).toString());
		System.out.println(Arrays.asList(_rowData).toString());
		
		if(_selectedNodeAttributes.length != _rowData.length)
		{
			throw new rowDataAndAttributeMismatchException();
		}

		// build original attr-value pair list
		selectedNodePairList = new ArrayList<DataPair>();
		for(int i = 0; i < _rowData.length; i++)
		{
			_selectedNodeAttributes[i] = QueryProcessingUtilities.removeQuotes(_selectedNodeAttributes[i]);
			selectedNodePairList.add(createPair(_selectedNodeAttributes[i], _rowData[i]));
		}
	}
	
	public LinkedBinaryTreeNode<QueryPlanTreeNode> build() throws SQLException, NonMatchingAttrCountAndValueCountException, BlankAttributesException
	{
		if(completePlanTreeRoot != null)
		{
			// clean up data stored from previous runs
			cleanupOldTreeData(completePlanTreeRoot); 
		}
		else
		{
			return null;
		}
		
		if(selectedPlanNode != null && selectedNodePairList != null)
		{
			downwardInsertDataNode(selectedPlanNode, selectedNodePairList);
			return completePlanTreeRoot;
		}
		else{
			return null;
		}
	}
	
	private void cleanupOldTreeData(LinkedBinaryTreeNode<QueryPlanTreeNode> root)
	{
		root.getData().setDataNode(null);
		if(root.getLeft() != null)
		{
			cleanupOldTreeData((LinkedBinaryTreeNode<QueryPlanTreeNode>) root.getLeft());
		}
		if(root.getRight() != null)
		{
			cleanupOldTreeData((LinkedBinaryTreeNode<QueryPlanTreeNode>) root.getRight());
		}
	}
	
	private void downwardInsertDataNode(
			LinkedBinaryTreeNode<QueryPlanTreeNode> rootPlanNode,
			List<DataPair> rootPairList
			) throws SQLException, NonMatchingAttrCountAndValueCountException, BlankAttributesException
	{
		DataPlanTreeNode root = createDataNode(rootPlanNode, rootPairList);
		rootPlanNode.getData().setDataNode(root);

		if(rootPlanNode.getLeft() != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> leftNode = (LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getLeft();
			List<DataPair> leftPairList = updateAttrNames(leftNode, rootPairList);
			downwardInsertDataNode(
					leftNode,
					leftPairList
					);
		}
		if(rootPlanNode.getRight() != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> rightNode = (LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getRight();
			List<DataPair> rightPairList = updateAttrNames(rightNode, rootPairList);
			downwardInsertDataNode(
					rightNode,
					rightPairList
					);
		}
	}
	
	private List<DataPair> updateAttrNames(
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode,
			List<DataPair> rootPairList
			)
	{
		String attributeStr = QueryProcessingUtilities.removeSquareParenthesis(planNode.getData().getOutputAttrs());
		String[] curNodeAttributes = attributeStr.split(",");
		
		System.out.println(Arrays.asList(curNodeAttributes).toString());
		
		// find the attributes in the node that also exist in this node 
		List<DataPair> newPairList = new ArrayList<DataPair>();
		for(int i = 0; i < rootPairList.size(); i++)
		{
			DataPair curRootPair = rootPairList.get(i);
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
				newPairList.add(createPair(matchedAttr, rootPairList.get(i).val));
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
	
	private DataPair createPair(String originalAttr, String val)
	{
		String shortOriginalAttr = "";
		String aliasAttr = "";
		if(originalAttr.contains(" as "))
		{
			shortOriginalAttr = removeAlias(originalAttr.substring(0, originalAttr.indexOf(" ")));
			aliasAttr = originalAttr.substring(originalAttr.indexOf(" as ") + 4);
		}
		else
		{
			shortOriginalAttr = removeAlias(originalAttr);
			aliasAttr = removeAlias(originalAttr);
		}

		//System.out.println("CREATED: original " + shortOriginalAttr + "; alias " + aliasAttr);
		return new DataPair(aliasAttr, val, shortOriginalAttr);
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
	private DataPlanTreeNode createDataNode(
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode,
			List<DataPair> curPairList
			) throws SQLException, NonMatchingAttrCountAndValueCountException, BlankAttributesException
	{
		QueryPlanTreeNode node = planNode.getData();
		
		String whereClause = "";
		for(int i = 0; i < curPairList.size(); i++)
		{
			DataPair curPair = curPairList.get(i);
			if(!curPair.val.equals(""))
			{
				if(!whereClause.equals(""))
				{
					whereClause = whereClause + " AND ";
				}
				whereClause = whereClause + curPair.aliasAttr + " = " + QueryProcessingUtilities.removeQuotes(curPair.val) + " ";
			}
		}
		System.out.println("WHERE: " + whereClause);
		
		if(!whereClause.equals(""))
		{
			String query = "SELECT * FROM " + node.getNewTableName() + " WHERE " + whereClause; 
			System.out.println("Query: " + query);
			Pair resultPair = pgConnector.executeQuerySeparateResult(query, Integer.MAX_VALUE);
			if(resultPair.data.size() == 0)
			{
				System.out.println("VALUES: NON-MATCHING");
			}
			else
			{
				System.out.println("VALUES row 1: " + Arrays.asList(resultPair.data.get(0)).toString());
			}
			
			DataPlanTreeNode dataNode = null;
			dataNode = new DataPlanTreeNode(resultPair.attributes, resultPair.data);
			
			return dataNode;
		}

		return null;
	}
	
	
}
