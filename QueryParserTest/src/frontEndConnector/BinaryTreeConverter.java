package frontEndConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import binaryTree.LinkedBinaryTreeNode;
import queryParser.QueryParser;
import queryReconstructor.PlanReducer;
import queryReconstructor.QueryReconstructor;

public class BinaryTreeConverter {
	//private QueryParser qParser;
	private PlanReducer pReducer;
	private List<String> createStatements;
	private List<String> newTableNames;
	private static final int MAX_CHILDREN_NUM = 2;
	private HashMap<String, String> abbToFullTmpNameMap;
	
	public class MoreThanTwoChildrenException extends Exception{};
	
	public BinaryTreeConverter(QueryReconstructor _pReconstructor)
	{
		pReducer = _pReconstructor.getPlanReducer();
		createStatements = new ArrayList<String>();
		newTableNames = new ArrayList<String>();
		abbToFullTmpNameMap = new HashMap<String, String>();
	}

	// convert a JSONObject query plan to a tree format
	public LinkedBinaryTreeNode<QueryPlanTreeNode> convertToTree() throws MoreThanTwoChildrenException 
	{
		if( pReducer.topLevelNode == null)
		{
			System.out.println("top node null.");
		}
		else
		{
			System.out.println("topLevelNode: " + pReducer.topLevelNode.toString());
		}
		
		return constructTree(pReducer.topLevelNode);
	}
	
	public List<String> getAllTempTableCreateStatements()
	{	
		return createStatements;
	}
	
	public List<String> getAllTempTableNames()
	{
		return newTableNames;
	}
	
	public HashMap<String, String> getTmpTableNameMap()
	{
		return abbToFullTmpNameMap;
	}
	
	// recursive construct the tree
	private LinkedBinaryTreeNode<QueryPlanTreeNode> constructTree(JSONObject qRoot) throws MoreThanTwoChildrenException 
	{	
		LinkedBinaryTreeNode<QueryPlanTreeNode> root = createBinaryTreeNode(qRoot);
		
		JSONObject[] children = getAllChildren(qRoot);
		assert(children.length == MAX_CHILDREN_NUM);
		
		
		if(children[0] == null) // this is the leaf level
		{
			return root;
		}
		else // there are more children
		{
			LinkedBinaryTreeNode<QueryPlanTreeNode> leftChild = constructTree(children[0]);
			root.setLeft(leftChild);
			
			if(children[1] != null)
			{
				LinkedBinaryTreeNode<QueryPlanTreeNode> rightChild = constructTree(children[1]);
				root.setRight(rightChild);
			}

			return root;
		}
	}
	
	// create a new LinkedBinaryTreeNode
	private LinkedBinaryTreeNode<QueryPlanTreeNode> createBinaryTreeNode(JSONObject currentNode)
	{
		QueryPlanTreeNode node = new QueryPlanTreeNode(
				pReducer.getType(currentNode),
				pReducer.getAliasSet(currentNode).toString(),
				//pReducer.getFilter(currentNode),
				pReducer.getOriginalFilter(currentNode), // shortened form
				pReducer.getInputTable(currentNode),
				pReducer.getNewTableName(currentNode),
				//pReducer.getJoinCondition(currentNode),
				pReducer.getOriginalJoinCond(currentNode), // shortened form
				pReducer.getOutputAttributes(currentNode).toString()
				);
		
		LinkedBinaryTreeNode<QueryPlanTreeNode> treeNode = new LinkedBinaryTreeNode<QueryPlanTreeNode>(node);
		
		if(!createStatements.contains(pReducer.getQuery(currentNode)))
		{
			createStatements.add(pReducer.getQuery(currentNode));
		}
		if(!newTableNames.contains((pReducer.getNewTableName(currentNode))))
		{
			newTableNames.add(pReducer.getNewTableName(currentNode));
		}
		abbToFullTmpNameMap.put(node.getAbbrNewTableName(), node.getNewTableName());
		
		return treeNode;
	}
	
	// get all children of the current node
	private JSONObject[] getAllChildren(JSONObject currentNode) throws MoreThanTwoChildrenException 
	{
		JSONArray childrenNodes = pReducer.getChildren(currentNode);		
		JSONObject[] result = new JSONObject[MAX_CHILDREN_NUM];
		for(int i = 0; i < MAX_CHILDREN_NUM; i++)
		{
			result[i] = null;
		}
		
		if(childrenNodes == null)
		{
			return result;
		}
		
		Iterator<JSONObject> citerator = childrenNodes.iterator();			
		int totalChildrenFound = 0;
		while(citerator.hasNext())
		{
			if(totalChildrenFound >= 2)
			{
				throw new MoreThanTwoChildrenException(); 
			}
			
			JSONObject curChild = citerator.next();	
			result[totalChildrenFound] = curChild;
			
			totalChildrenFound++;
		}
		
		return result;
	}
}