package queryReconstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import queryParser.QueryParser;
import queryParser.QueryProcessingUtilities;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlanReducer {
	
	// this class will simplify the query plan
	
	// for example, it will reduce all hash join, merge join, etc into join nodes
	// it will eliminate unnecessary seq scan nodes
	
	
	// TODO list
	// subquery scans
	// subplan nodes
	// aggregate naming
	// 
	
	
	QueryParser qParser;
	public JSONObject topLevelNode;
	static int curTmp = 0;
	
	// will return:
	// a tree of JSONObjects
	// node type (join, scan, or aggregate)
	// output fields
	// conditions
	// tables to select from
	// temp table name
	// later aggregate information
	
	public PlanReducer(QueryParser qp)  
	{
		qParser = qp;
		topLevelNode = reduceNode(qp.topLevelNode);
	}

	JSONObject reduceNode(JSONObject curNode) {
		JSONObject reducedCurNode = new JSONObject();
		switch (getEnumFromNodeType(qParser.getNodeType(curNode))) 
		{
		// joins
		// regular join types are inner joins
		case HASH_JOIN:
			reducedCurNode = reduceHashJoinNode(curNode, "inner");
			break;
		case MERGE_JOIN:
			reducedCurNode = reduceMergeJoinNode(curNode, "inner");
			break;
		case NESTED_LOOP:
			reducedCurNode = reduceNestedLoopJoinNode(curNode, "inner");
			break;
			// need other join types as well (left joins, etc)
			
		// intermediate type things
			// TODO: apparently there are ALSO materialize nodes, which should be treated the same way
		case HASH:
			reducedCurNode = reduceSortOrHashNode(curNode);
			break;
		case SORT:
			reducedCurNode = reduceSortOrHashNode(curNode);
			break;
		// scans
			
		case INDEX_SCAN:
			reducedCurNode = reduceIndexScanNode(curNode);
			break;
		case SUBQUERY_SCAN: // TODO: do something about this.
			//reducedCurNode = 
			break;
		case SEQ_SCAN:
			reducedCurNode = reduceSeqScanNode(curNode);
			break;
		case BITMAP_HEAP_SCAN:
			reducedCurNode = reduceBitmapHeapScanNode(curNode);
			break;
		case BITMAP_INDEX_SCAN:
			// don't think we should ever run into one of these, 
			// since I believe it will always be a child of a bitmap heap scan.
			break;
			
		case AGGREGATE:
			reducedCurNode = reduceAggregateNode(curNode);
			break;
			
		//misc
		case LIMIT:
			// probably not going to do anything about this yet.
			break;
		case UNDEFINED:
			reducedCurNode = null;
			break;
		}
		
		// update reducedParentNode
		
		// return the new reduced node
		//System.out.println(reducedCurNode.toJSONString());
		return reducedCurNode;
	}
	
	// TODO: may need to check all types of nodes for a subplan. 
	// don't think joins, though. just aggregates, scans, subqueries
	
	JSONObject reduceHashJoinNode(JSONObject curNode, String joinType) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		// NOTE: not sure if casting to JSONObject will work
		JSONObject reducedFirstChild = reduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = reduceNode((JSONObject) children.get(1));
		
		String joinCond = qParser.getHashCond(curNode);
		String filter = qParser.getFilter(curNode);
		String joinFilter = qParser.getJoinFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		// even if hash joins and merge joins never have joinFilter, it will return the empty string
		reducedNode = makeJoinNode(joinCond, joinType, joinFilter, filter, reducedFirstChild, reducedSecondChild, outputAttrs);
		
		return reducedNode;
	}
	
	JSONObject reduceMergeJoinNode(JSONObject curNode, String joinType) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		JSONObject reducedFirstChild = reduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = reduceNode((JSONObject) children.get(1));
		
		String joinCond = qParser.getMergeCond(curNode);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		reducedNode = makeJoinNode(joinCond, joinType, "", filter, reducedFirstChild, reducedSecondChild, outputAttrs);
		
		return reducedNode;
	}

	JSONObject reduceNestedLoopJoinNode(JSONObject curNode, String joinType) {
		JSONObject reducedNode = new JSONObject();		
		JSONArray children = qParser.getChildrenPlanNodes(curNode);	
		JSONObject reducedFirstChild = reduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = reduceNode((JSONObject) children.get(1));
		
		// join cond is in second child's index condition - TROLOLOL very funny you have no idea this is patently false.
		// and also possibly joinFilter attribute if multiple conditions
		// TODO: SHIIIIIT conditions not guaranteed to be in second child's index cond, 
		// may be in filter, and may be in either child's filter. FUCK
		String joinFilter = qParser.getJoinFilter(curNode);
		String joinCond = qParser.getIndexCond(reducedSecondChild);
		String alias = qParser.getAlias(reducedSecondChild);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
				
		// need to combine joinFilter and joinCond
		// also all of these should handle filters
		
		reducedNode = makeJoinNode(joinCond, joinType, joinFilter, filter, reducedFirstChild, reducedSecondChild, outputAttrs);
		
		return reducedNode;
	}
	
	// TODO: node type: unique 
	// TODO: node type: append
	// TODO: node type: SetOp, command = Intersect
		// append node under intersect node means that the append node's children should be intersected
	// TODO: figure out what really happens when you do 
		// intersect/union
		// in (set)
		// any (set)
		// all (set)
		// not in (set)
		// anything else with subplans (FUCKFUCKFUCK)
	// TODO: special handling for system generated aliases
	/*
	 * 
	 {
                   "Node Type": "Subquery Scan",
                   "Parent Relationship": "Member",
                   "Alias": "*SELECT* 2",
                   "Startup Cost": 0.00,
                   "Total Cost": 12.80,
                   "Plan Rows": 140,
                   "Plan Width": 12,
                   "Output": ["\"*SELECT* 2\".id", "1"],
                   "Plans": [
                     {
                       "Node Type": "Seq Scan",
                       "Parent Relationship": "Subquery",
                       "Relation Name": "dummy",
                       "Schema": "public",
                       "Alias": "dummy_1",
                       "Startup Cost": 0.00,
                       "Total Cost": 11.40,
                       "Plan Rows": 140,
                       "Plan Width": 12,
                       "Output": ["dummy_1.id"]
                     }
                   ]
                 }
	 */

	JSONObject reduceSortOrHashNode(JSONObject obj) {
		// sort and hash nodes don't do anything (at least in non-JSON formatted query plans)
		JSONArray children = qParser.getChildrenPlanNodes(obj);
		//TODO: may want this to take the output attributes from this node instead? probably not, but maybe
		JSONObject reducedNode = reduceNode((JSONObject) children.get(0));
		// maybe should get outputAttributes from this node though.
		// could then also use it for subquery scans?
		
		return reducedNode;
	}

	JSONObject reduceBitmapHeapScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// we know this node will have a recheck cond, which is what we want for the selection condition.
		// we can ignore everything below it, because all of the conditions will be index conditions
		// that are re-checked in recheckCond.
		String indexCond = qParser.getRecheckCond(curNode);
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		String finalCond = QueryProcessingUtilities.combineAndConditions(indexCond, filter);
		
		// TODO: check for subplan
		reducedNode = makeScanNode(null, finalCond, inputTable, alias, outputAttrs);
		
		return reducedNode;
	}
	
	JSONObject reduceIndexScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// may have index cond AND/OR filter
		// won't necessarily have either
		
		String indexCond = qParser.getIndexCond(curNode);
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		String finalFilter = QueryProcessingUtilities.combineAndConditions(indexCond, filter);
		
		// TODO: check for subplan
		reducedNode = makeScanNode(null, finalFilter, inputTable, alias, outputAttrs);		
		
		return reducedNode;
	}
	
	JSONObject reduceSeqScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// may or may not have anything in Filter
		
		// TODO: this may have a subplan node
		
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		// TODO: check for subplan
		reducedNode = makeScanNode(null, filter, inputTable, alias, outputAttrs);		
		// may not have a condition - should check
		
		return reducedNode;
	}
	
	// TODO: groupaggregate, hashaggregate, aggregate
	
	// TODO: also should handle index only scan
	
	//
	
	// TODO: implement
	JSONObject reduceSubqueryScanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// um, shit, subquery scan may come back with a new alias. how to reconstruct the old name? fuck fuck fuck
		// also, column aliases make it into the new output names (e.g., count(*) can become numorders)
		// shit fuck fuck
		// can we count on the output of the next level up having them in the same order? probably not necessarily, right? shitfuck
		// take it back, it's a subquery scan, so the only things that should come out of the subquery should be input into the next part.
		// should be able to do a matchup
		
		// when processing children, send in the aliases of columns.
		// ACTUALLY
		// just add an extra attribute to children called columnAliases or something
		// this should tell the queryReconstructor to use this set of aliases when reconstructing.
		// good story
		
		
		// for reconstructing the name, though, maybe we can keep track of the aliases of the tables that went into it and then keep track of their attributes
		// we can make it work.
		// yeah, when we find a subquery node, it should definitely have an alias. postgres throws an error if you try to make a subquery without an alias
		// so we'll only have to deal with it at the particular node
		
		// will definitely have a subquery child, which will be some sort of processing thing
		// may have a subplan node, which will need the alias set of the subquery child
		
		
		// okay, what do we want to do in the subquery scan node?
		// 1. get appropriate aliases from child - should be able to just select them in order
		JSONObject child = reduceNode((JSONObject) qParser.getChildrenPlanNodes(curNode).get(0));	
		JSONArray childColNames = QueryProcessingUtilities.getFinalColumnNames(getOutputAttributes(child));
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		JSONArray finalOutputAttrs = new JSONArray();
		String filter = qParser.getFilter(curNode);
		Iterator<String> ccnIt = childColNames.iterator();
		Iterator<String> oaIt = outputAttrs.iterator();
		while (ccnIt.hasNext() && oaIt.hasNext()) {
			finalOutputAttrs.add(ccnIt.next() + " as " + oaIt.next());
		}
		
		String tableName = getNewTableName(child);
		// 2. get table name from first child
		
		// 3. check for subplan
		// 		if subplan, process nodes of children
		//		pass in parent alias for replacement?
		//		let query reconstructor handle putting query together
		//		maybe leave parent alias replacement for reconstructor?
		//		we know the alias of the subquery - any subplan will refer to this alias
		// get names of child columns here
		// what about those that are already aliased? 

	//reducedNode = makeScanNode(child, filter, tableName, alias, finalOutputAttrs);
		
		/*
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		reducedNode = MakeScanNode(filter, inputTable, alias, outputAttrs);
		*/		
		
		return reducedNode;
	}

	// TODO: write a function that checks a node for subplans
	// 
	// what to do if so?
	// should probably process whole node, since normal child needs to be processed normally
	// and other child needs special treatment
	
	JSONObject processSubplanIfPresent(JSONObject curNode) {
		// check filter for subplan
		// actually may need to check joinCond as well (but that one didn't give enough information to do anything with >.<)
		String filter = qParser.getFilter(curNode);
		if (filterContainsSubplan(filter)) {
			// process subplan
			// may want to return child nodes (haven't figured out exactly how I want to handle that)
			// definitely need to return the query
			
			// looks like you can't really make any guarantees about where the attributes from the outer part appear in the subplan
			// so it might be best to just reconstruct the query all the way
			// or should we leave it to the query reconstructor? extract the important parts, don't replace the alias of the outer thing,
			// and let the query constructor handle the fact that they shouldn't be made into temp tables? that could work
			
			// update filter (replace SubPlan # with query for subplan)
			// return curNode with update
		}
		return null;
	}
	
	// TODO: implement
	JSONObject reduceSubplanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		
		// things we need from the parent node:
		// alias set of shit to replace
		
		// what we need to do:
		// check both filter and any index conditions/other stuff for aliases that correspond to parent tables
		// 
		
		
		// may or may not have anything in Filter
		/*
		String filter = qParser.getFilter(curNode);
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		reducedNode = MakeScanNode(filter, inputTable, alias, outputAttrs);
		*/		
		// may not have a condition - should check
		
		return reducedNode;
	}

	
	JSONObject reduceAggregateNode(JSONObject curNode) {
		// may want to process Aggregate differently from HashAggregate or others - I think Aggregate doesn't group by anything
		JSONObject reducedNode = new JSONObject();
		//System.out.println(curNode.toJSONString());
		//System.out.println(qParser.getChildrenPlanNodes(curNode));
		JSONObject child = reduceNode((JSONObject) qParser.getChildrenPlanNodes(curNode).get(0));	
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		String filter = qParser.getFilter(curNode);
		
		reducedNode = makeAggregateNode(child, filter, outputAttrs);
		
		return reducedNode;
	}
		
	JSONObject makeJoinNode(String joinCond, String joinType, String joinFilter, String filter, JSONObject reducedFirstChild, JSONObject reducedSecondChild, JSONArray outputAttrs) 
	{
		
		// things required to make a join tmp table:
		// input table names
		// join conditions (all)
		// output table name
		// other filters? I think those will typically be applied in scan nodes if they're not join conditions
		
		// NOTE: not all plans will actually have join conditions. should be prepared for that.
				
		JSONObject reducedJoinNode = new JSONObject();
		JSONArray children = new JSONArray();
		children.add(reducedFirstChild);
		children.add(reducedSecondChild);
		// get aliasSets from children.
		// what happens if we don't alias tables?
		JSONArray aliasC1 = getAliasSet(reducedFirstChild);
		String tmpC1 = getNewTableName(reducedFirstChild);
		JSONArray aliasC2 = getAliasSet(reducedSecondChild);
		String tmpC2 = getNewTableName(reducedSecondChild);
		Iterator<String> it = outputAttrs.iterator();
		
		JSONArray newOutputAttrs = new JSONArray();
		
		// TODO: if there was an aggregate computation, that attribute appears in () - we'll have to check for that and keep some kind of attribute
		// lookup to locate it. maybe just keep a map from string(computation) to string(attribute name)
		while (it.hasNext()) {		
			String attr = it.next();
			// TODO: check to see if it contains a ( - if so, it shouldn't get normal treatment
			if (attr.contains("(")) {
				// handle functions/aggregates/previously executed aggregates (should have lookup)
			} else {
				// actually, may not always have an alias (in the case of a subquery with aliases)
				// more specifically, may not want to do this kind of processing on it
				// actually, if we just added the alias of the subquery to the column name, this would still hold
				// nevermind, we'll be okay
				String[] attrParts = attr.split("\\.");
				String newAttr = "";
				// check alias, replace with name of child
				// TODO: handle functions! should generate new name for attribute, then stick it in the map
				// if the first character is a (, then some function has already been executed on it, and we should look it up in the map of calculations to attrnames
				// if the string contains parentheses, it should be handled differently.
				if (QueryProcessingUtilities.searchJSONArrayForString(aliasC1, attrParts[0])) {
					newOutputAttrs.add(tmpC1 + "." + attrParts[0] + "_" + attrParts[1]);// + " as " + attrParts[0] + "_" + attrParts[1]);
				} else if (QueryProcessingUtilities.searchJSONArrayForString(aliasC2, attrParts[0])) {
					newOutputAttrs.add(tmpC2 + "." + attrParts[0] + "_" + attrParts[1]);// + attrParts[1] + " as " + attrParts[0] + "_" + attrParts[1]);
				} else {
					// may have been an aggregate. pretend it's fine for now.
					if (attrParts.length == 2) {
						newOutputAttrs.add(attrParts[0] + "_" + attrParts[1]);
					} else {
						newOutputAttrs.add(attr);
					}
				}
			}
		}
		
		// handle join cond and join filter
		String aliasReplacedJoinCond = replaceAliasesWithTableName(replaceAliasesWithTableName(joinCond, aliasC1, tmpC1), aliasC2, tmpC2);
		String aliasReplacedJoinFilter = replaceAliasesWithTableName(replaceAliasesWithTableName(joinFilter, aliasC1, tmpC1), aliasC2, tmpC2);
		String aliasReplacedFilter = replaceAliasesWithTableName(replaceAliasesWithTableName(filter, aliasC1, tmpC1), aliasC2, tmpC2);
		// also handle where clause
		
		String aliasReplacedFinalJoinCond = "";
		if (!aliasReplacedJoinCond.equals("") && !aliasReplacedJoinFilter.equals("")) {
			aliasReplacedFinalJoinCond = aliasReplacedJoinCond + " and " + aliasReplacedJoinFilter;
		} else if (!aliasReplacedJoinCond.equals("")) {
			aliasReplacedFinalJoinCond = aliasReplacedJoinCond;
		} else {
			aliasReplacedFinalJoinCond = aliasReplacedJoinFilter;
		}
		
		JSONArray aliasSet = QueryProcessingUtilities.concatArrays(getAliasSet(reducedFirstChild), getAliasSet(reducedSecondChild));

		reducedJoinNode.put("type", "join");
		reducedJoinNode.put("children", children);
		reducedJoinNode.put("filter", aliasReplacedFilter);
		reducedJoinNode.put("joinCondition", aliasReplacedFinalJoinCond);
		reducedJoinNode.put("joinType", joinType);
		reducedJoinNode.put("outputAttrs", newOutputAttrs);
		reducedJoinNode.put("aliasSet", aliasSet);
		reducedJoinNode.put("newTableName", "tmp" + curTmp);
		curTmp++;
				
		return reducedJoinNode;
	}
		
	JSONObject makeAggregateNode(JSONObject reducedChild, String filter, JSONArray outputAttrs) {
		JSONObject reducedAggregateNode = new JSONObject();
		
		// TODO make sure to rename attributes with 
		JSONArray aliasSet = getAliasSet(reducedChild);
		
		JSONArray newOutputAttrs = new JSONArray();
		JSONArray children = new JSONArray();
		children.add(reducedChild);
		String childTableName = getNewTableName(reducedChild);
		JSONArray groupByAttrs = null;

		Iterator<String> it = outputAttrs.iterator();
		while (it.hasNext()) {		
			String attr = it.next();
			if (attr.contains("(")) {
				// handle functions/aggregates/previously executed aggregates (should have lookup)
				if (attr.substring(0, 1).equals("(")) {
					// TODO: handle aggregate naming in lower nodws
					// after stripping parentheses, check to see if it's already in the map of execution string to attribute name
					// if first thing is a paren, check to see if it's a function (if it is, after stripping parentheses there will still be parentheses).
					String tmp_attr = attr;
					
					// remove any wrapping parentheses
					while (tmp_attr.substring(0, 1).equals("(")) {
						tmp_attr = QueryProcessingUtilities.removeParenthesis(tmp_attr);				
					}
					
					if (tmp_attr.contains("(")) {
						// is function call
					} else {
						// not function call.
					}
					
				} else {
					// first char not paren = execute the function here
					String[] attrParts = attr.split("\\.");
					String newAttr = "";
					//newOutputAttrs.add(attr);
					if (attrParts.length == 2) {
						newOutputAttrs.add(attrParts[0] + "_" + attrParts[1]);
					} else {
						// this is probably a query on a single node, so we don't need aliases
						newOutputAttrs.add(attr);
					}
				}
			} else {
				//System.out.println(attr);
				String[] attrParts = attr.split("\\.");
				String newAttr = "";
				// check alias, replace with name of child
				// TODO: check for parentheses, process parentheses separately
				if (groupByAttrs == null) {
					groupByAttrs = new JSONArray();
				}
				if (attrParts.length == 2) {
					newOutputAttrs.add(childTableName + "." + attrParts[0] + "_" + attrParts[1]);
					groupByAttrs.add(childTableName + "." + attrParts[0] + "_" + attrParts[1]);
				} else {
					// this is probably a query on a single node, so we don't need aliases
					newOutputAttrs.add(attr);
					groupByAttrs.add(attr);
				}
			}
		}
		
		reducedAggregateNode.put("type", "aggregate");
		reducedAggregateNode.put("children", children);
		reducedAggregateNode.put("filter", filter);
		reducedAggregateNode.put("outputAttrs", newOutputAttrs);
		reducedAggregateNode.put("aliasSet", aliasSet);
		reducedAggregateNode.put("newTableName", "tmp" + curTmp);
		reducedAggregateNode.put("groupByAttrs", groupByAttrs);
		curTmp++;
		
		return reducedAggregateNode;
	}
	
	JSONObject makeScanNode(JSONObject reducedChild, String filterCond, String inputTable, String alias, JSONArray outputAttrs) {
		// scan node can have children or no children, right?
		
		// things required to make a scan tmp table
		// when do we actually want a scan node vs. directly incorporating it into the parent node?
		// e.g., if it's literally nothing more than a sequential scan, then we don't want to make a node for it
		// maybe we should have a boolean indicating whether it should be kept as a node
		// for now, though, make all nodes
		JSONArray newOutputAttrs = new JSONArray();
		Iterator<String> it = outputAttrs.iterator();
		while (it.hasNext()) {		
			String attr = it.next();
			String[] attrParts = attr.split("\\.");
			String newAttr = "";
			// check alias, replace with name of child
			if (attrParts.length == 2) {
				newOutputAttrs.add(attr + " as " + attrParts[0] + "_" + attrParts[1]);// + " as " + attrParts[0] + "_" + attrParts[1]);
			} else {
				newOutputAttrs.add(attr);
			}
		}
		
		JSONObject reducedScanNode = new JSONObject();
		JSONArray aliasSet = new JSONArray();
		// TODO: if attributes have alias, add alias name to attribute name
		aliasSet.add(alias);
		reducedScanNode.put("type", "scan");
		reducedScanNode.put("filter", filterCond);
		reducedScanNode.put("aliasSet", aliasSet);
		reducedScanNode.put("inputTable", inputTable);
		reducedScanNode.put("outputAttrs", newOutputAttrs);
		reducedScanNode.put("newTableName", "tmp" + curTmp);
		if (reducedChild != null) {
			JSONArray reducedChildren = new JSONArray();
			reducedChildren.add(reducedChild);
			reducedScanNode.put("children", reducedChildren);			
		}
		curTmp++;
		
		return reducedScanNode;
	}
	
	JSONObject makeSubplanNode(JSONObject curNode) {
		JSONObject reducedSubplanNode = new JSONObject();
		reducedSubplanNode.put("type", "subplan");
		
		return reducedSubplanNode;
	}
		// node type: if it's a join type (hash, sort merge, inl, nl), we'll make a join node
		// we'll look at the join conditions and stick them in the where clause
		// may be some complications with aliasing/figuring out what relation an attribute comes from
		// possible solution: keep a set of aliases that came from the children of a node, consult if necessary
		
		// nested loop joins are funny. the first child has a normal condition, but the join condition looks like it's actually in
		// the index cond of the second child
		// also, there is something called a join filter, which has additional join conditions
		// HOWEVER, the join filter won't necessarily eliminate rows in an outer join (may be null extended),
		// while the filter conditions will definitely eliminate rows
		
		// the filter attribute will have selection conditions
		
		// for index scan nodes, the filter condition will be in the index cond
		
		// looks like bitmap scans sometimes cause interesting things to happen (a condition checked at two different points in the plan)
		// notably, there are bitmap heap scan nodes, which have a "recheck cond" instead of a regular filter. 
		// we may be able to ignore this by having the initial index cond take care of it
		// and ignoring the "recheck cond" since it will already be done.
		// OR we could ignore the child scan nodes and use only the recheck cond, since it will have both
		
		// hmm, there are limit nodes. this is definitely a place where a lot of optimization can take place (if we combine the nodes)
		
		
		// if a child is any sort of scan with no filters, we can probably just ignore it
		// in the parent, just use the table it was sequential scanning over 
		// could implement this by just naming the temp table member to be the table being scanned
		
		// any sort of scan node can possibly be ignored by putting the conditions in the parent node
		// or we could just do the filter and use it as input
		// will be faster to put conditions on parent node
		
		// a hash node probably doesn't need any processing - just use the result of its children
		// same thing for sort
	
	
		// HMMMM explain analyze does cool things (e.g. tells you how many rows were removed by a condition)
	
	// TODO
	// For now, we'll just do a find and replace type thing
	// this is not a final solution
	// won't work with string literals, but we'll handle that later.
	String replaceAliasesWithTableName(String condition, JSONArray aliases, String tablename) {
		String regex;
		
		Iterator<String> it = aliases.iterator();
		while (it.hasNext()) {
			// possibly add \b for word boundary
			String alias = it.next();
			regex = "\\b" + alias + "\\.";
			// so in here we should be only executing the replace all on sections of the condition that are not string literals.
			// okay, it shouldn't be THAT bad to implement - just go through and check for unescaped single quotes, and ignore everything in between pairs.
			// but let's get aggregates working first.
			condition = condition.replaceAll(regex, tablename + "." + alias + "_");
		}
		return condition;
	}
	
	boolean filterContainsSubplan(String filter) {
		//TODO implement for real - this (like everything else) doesn't handle string literals
		if (filter.contains("SubPlan")) {
			return true;
		}
		return false;
	}
	
	private enum NODE_TYPE {
		// may still need other types of left join
		// may also need right join and possibly full join? not sure about what types of nodes there are
		// there may also be materialize nodes?
		HASH_JOIN,
		HASH_LEFT_JOIN,
		MERGE_JOIN,
		MERGE_LEFT_JOIN,
		NESTED_LOOP,
		NESTED_LOOP_LEFT_JOIN,
		AGGREGATE,
		HASH,
		SORT,
		INDEX_SCAN,
		SEQ_SCAN,
		SUBQUERY_SCAN,
		BITMAP_HEAP_SCAN,
		BITMAP_INDEX_SCAN,
		LIMIT,
		UNDEFINED;
	};
		
	// FML, need Subquery Scan as well
	// plus right/full joins
	private NODE_TYPE getEnumFromNodeType(String nodeType)
	{
		//System.out.println(nodeType);
		if (nodeType.equals("Hash Join")) { return NODE_TYPE.HASH_JOIN; }
		if (nodeType.equals("Hash Left Join")) { return NODE_TYPE.HASH_LEFT_JOIN; }
		if (nodeType.equals("Merge Join")) { return NODE_TYPE.MERGE_JOIN; }
		if (nodeType.equals("Merge Left Join")) { return NODE_TYPE.MERGE_LEFT_JOIN; }
		if (nodeType.equals("Nested Loop")) { return NODE_TYPE.NESTED_LOOP; }
		if (nodeType.equals("Nested Loop Left Join")) { return NODE_TYPE.NESTED_LOOP_LEFT_JOIN; }
		if (nodeType.equals("Hash")) { return NODE_TYPE.HASH; }
		if (nodeType.equals("Sort")) { return NODE_TYPE.SORT; }
		if (nodeType.equals("Index Scan")) { return NODE_TYPE.INDEX_SCAN; }
		if (nodeType.equals("Seq Scan")) { return NODE_TYPE.SEQ_SCAN; }
		if (nodeType.equals("Subquery Scan")) { return NODE_TYPE.SUBQUERY_SCAN; }
		if (nodeType.equals("Bitmap Heap Scan")) { return NODE_TYPE.BITMAP_HEAP_SCAN; }
		if (nodeType.equals("Bitmap Index Scan")) { return NODE_TYPE.BITMAP_INDEX_SCAN; }
		if (nodeType.equals("GroupAggregate")) { return NODE_TYPE.AGGREGATE; }
		if (nodeType.equals("HashAggregate")) { return NODE_TYPE.AGGREGATE; }
		if (nodeType.equals("Aggregate")) { return NODE_TYPE.AGGREGATE; }
		System.out.println("undefined");
		return NODE_TYPE.UNDEFINED;
	}

	private enum REDUCED_PLAN_ATTRS {
		TYPE,
		ALIAS_SET,
		FILTER,
		INPUT_TABLE,
		NEW_TABLE_NAME,
		JOIN_CONDITION,
		JOIN_TYPE,
		OUTPUT_ATTRS,
		GROUP_BY_ATTRS,
		CHILDREN,
		QUERY
		};
		
	// mapping between enum and the actual returned string for all attributes EXPLAIN can return
	private static final Map<REDUCED_PLAN_ATTRS, String> reducedPlanAttrMapping = reducedPlanAttrMappingInitializer();
	private static Map<REDUCED_PLAN_ATTRS, String> reducedPlanAttrMappingInitializer()
	{
		 Map<REDUCED_PLAN_ATTRS, String> map = new HashMap<REDUCED_PLAN_ATTRS, String>();
		 map.put(REDUCED_PLAN_ATTRS.TYPE, "type");
		 map.put(REDUCED_PLAN_ATTRS.ALIAS_SET, "aliasSet");
		 map.put(REDUCED_PLAN_ATTRS.FILTER, "filter");
		 map.put(REDUCED_PLAN_ATTRS.INPUT_TABLE, "inputTable");
		 map.put(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME, "newTableName");
		 map.put(REDUCED_PLAN_ATTRS.JOIN_CONDITION, "joinCondition");
		 map.put(REDUCED_PLAN_ATTRS.JOIN_TYPE, "joinType");
		 map.put(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS, "outputAttrs");
		 map.put(REDUCED_PLAN_ATTRS.GROUP_BY_ATTRS, "groupByAttrs");
		 map.put(REDUCED_PLAN_ATTRS.CHILDREN, "children");
		 map.put(REDUCED_PLAN_ATTRS.QUERY, "query");
		 return Collections.unmodifiableMap(map);
	}
	
	public String getType(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.TYPE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getJoinType(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.JOIN_TYPE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public String getQuery(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.QUERY));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public JSONArray getAliasSet(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.ALIAS_SET));
	}
	
	public String getFilter(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public String getInputTable(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.INPUT_TABLE));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getNewTableName(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}
	
	public String getJoinCondition(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.JOIN_CONDITION));
		if(rst == null)
		{
			return EMPTY_STRING;
		}
		return rst.toString();
	}

	public JSONArray getOutputAttributes(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS));
	}
	
	public JSONArray getGroupByAttributes(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.GROUP_BY_ATTRS));
	}
	
	public JSONArray getChildren(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.CHILDREN));
	}

	private static final String EMPTY_STRING = "";
	
	private boolean containsAggregateFunction(String attr) {
		// need total list of 
		return false;
	}
	
	/*
	
	private List<String> aggregateFunctions = aggregateFunctionInitializer();
	private List<String> aggregateFunctionInitializer() {
		List<String> lst = new ArrayList<String>();
		lst.add("array_agg");
		lst.add("avg");
		lst.add("bit_and");
		lst.add("bit_or");
		lst.add("bool_and");
		lst.add("bool_or");
		lst.add("count");
		lst.add("every");
		lst.add("max");
		lst.add("min");
		lst.add("string_agg");
		lst.add("sum");
		lst.add("xmlagg");
		
		return lst;
	}
	*/
	
	public static void main(String [ ] args) throws Exception 
	{
	
		// input file containing the returned query plan
	//	String inputFilePath = "/Users/watermelon/Dropbox/EECS584/Project/code/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		String inputFilePath = "/afs/umich.edu/user/d/a/daneliza/dwtemp/F14/eecs584/eecs584f14/TestingData/QueryPlan1_verbose.txt";
		
		// create a new query parser for this query plan
		QueryParser qParser = new QueryParser(inputFilePath);	
		// get the top level node
		System.out.println("\n---------Parsed query---------");
		PlanReducer pr = new PlanReducer(qParser);
		
		// previous code -- ignore
		/*BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
		String line = "", jsonString = "";
		while ((line = reader.readLine()) != null) {
		    jsonString = jsonString + line;
		}
		System.out.println(jsonString);*/
	}
	
}
