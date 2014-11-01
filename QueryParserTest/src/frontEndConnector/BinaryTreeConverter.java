package frontEndConnector;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import binaryTree.LinkedBinaryTreeNode;
import queryParser.QueryParser;

public class BinaryTreeConverter {
	private QueryParser qParser;
	private LinkedBinaryTreeNode<QueryPlanTreeNode> root;
	private static final int MAX_CHILDREN_NUM = 2;
	
	public BinaryTreeConverter(QueryParser _qParser)
	{
		qParser = _qParser;
	}

	// convert a JSONObject query plan to a tree format
	public LinkedBinaryTreeNode<QueryPlanTreeNode> convertToTree() throws Exception
	{
		return constructTree(qParser.topLevelNode);
	}
	
	// recursive construct the tree
	private LinkedBinaryTreeNode<QueryPlanTreeNode> constructTree(JSONObject qRoot) throws Exception
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
				qParser.getRelationName(currentNode),
				qParser.getJoinType(currentNode),
				qParser.getOutputAttributes(currentNode).toString()
				);
		
		LinkedBinaryTreeNode<QueryPlanTreeNode> treeNode = new LinkedBinaryTreeNode<QueryPlanTreeNode>(node);
		
		return treeNode;
	}
	
	// get all children of the current node
	private JSONObject[] getAllChildren(JSONObject currentNode) throws Exception
	{
		JSONArray childrenNodes = qParser.getChildrenPlanNodes(currentNode);		
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
				throw new Exception("Query Plan has more than two children"); 
			}
			
			JSONObject curChild = citerator.next();	
			result[totalChildrenFound] = curChild;
			
			totalChildrenFound++;
		}
		
		return result;
	}
}