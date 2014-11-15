package frontEndConnector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import queryParser.QueryProcessingUtilities;
import databaseConnector.PostgresDBConnector;
import databaseConnector.PostgresDBConnector.InputQueryNotSELECTALL;
import databaseConnector.PostgresDBConnector.QueryAttrNumNotMatch;
import frontEndConnector.DataPlanTreeNode.BlankAttributesException;
import frontEndConnector.DataPlanTreeNode.NonMatchingAttrCountAndValueCountException;
import binaryTree.LinkedBinaryTreeNode;

public class DataPlanConstructor {
	private LinkedBinaryTreeNode<QueryPlanTreeNode> completePlanTreeRoot;
	private LinkedBinaryTreeNode<QueryPlanTreeNode> selectedPlanNode;
	private List<Pair> selectedNodePairList;
	private PostgresDBConnector pgConnector;
	
	private class Pair{
		public String aliasAttr;
		public String val;
		public String originalAttr;
		
		public Pair(String _aliasAttr, String _val, String _originalAttr)
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
		selectedNodePairList = new ArrayList<Pair>();
		for(int i = 0; i < _rowData.length; i++)
		{
			_selectedNodeAttributes[i] = QueryProcessingUtilities.removeQuotes(_selectedNodeAttributes[i]);
			selectedNodePairList.add(createPair(_selectedNodeAttributes[i], _rowData[i]));
		}
	}
	
	public LinkedBinaryTreeNode<QueryPlanTreeNode> build() throws SQLException
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
			List<Pair> rootPairList
			) throws SQLException
	{
		DataPlanTreeNode root = createDataNode(rootPlanNode, rootPairList);
		rootPlanNode.getData().setDataNode(root);

		if(rootPlanNode.getLeft() != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> leftNode = (LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getLeft();
			List<Pair> leftPairList = updateAttrNames(leftNode, rootPairList);
			downwardInsertDataNode(
					leftNode,
					leftPairList
					);
		}
		if(rootPlanNode.getRight() != null)
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> rightNode = (LinkedBinaryTreeNode<QueryPlanTreeNode>) rootPlanNode.getRight();
			List<Pair> rightPairList = updateAttrNames(rightNode, rootPairList);
			downwardInsertDataNode(
					rightNode,
					rightPairList
					);
		}
	}
	
	private List<Pair> updateAttrNames(
			LinkedBinaryTreeNode<QueryPlanTreeNode> planNode,
			List<Pair> rootPairList
			)
	{
		String attributeStr = QueryProcessingUtilities.removeSquareParenthesis(planNode.getData().getOutputAttrs());
		String[] curNodeAttributes = attributeStr.split(",");
		
		System.out.println(Arrays.asList(curNodeAttributes).toString());
		
		// find the attributes in the node that also exist in this node 
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
	
	private Pair createPair(String originalAttr, String val)
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
		return new Pair(aliasAttr, val, shortOriginalAttr);
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
			List<Pair> curPairList
			) throws SQLException
	{
		QueryPlanTreeNode node = planNode.getData();
		
		String[] curAttributes = new String[curPairList.size()];
		String whereClause = "";
		for(int i = 0; i < curPairList.size(); i++)
		{
			Pair curPair = curPairList.get(i);
			if(!curPair.val.equals(""))
			{
				if(!whereClause.equals(""))
				{
					whereClause = whereClause + " AND ";
				}
				whereClause = whereClause + curPairList.get(i).aliasAttr + " = " + QueryProcessingUtilities.removeQuotes(curPair.val) + " ";
			}
			curAttributes[i] = curPairList.get(i).aliasAttr;
		}
		System.out.println("WHERE: " + whereClause);
		
		List<String[]> values = null;
		try 
		{
			if(!whereClause.equals(""))
			{
				String query = "SELECT * FROM " + node.getNewTableName() + " WHERE " + whereClause; 
				System.out.println("Query: " + query);
				values = pgConnector.executeQuerySeparateResult(query, Integer.MAX_VALUE, node.getNewTableName());
				if(values.size() == 0)
				{
					System.out.println("VALUES: NON-MATCHING");
				}
				else
				{
					System.out.println("VALUES: " + Arrays.asList(values.get(0)).toString());
				}
			}
			else
			{
				values = null;
			}
		} 
		catch (InputQueryNotSELECTALL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryAttrNumNotMatch e) {
			// TODO Auto-generated catch block
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
		
		return dataNode;
	}
	
	
}
