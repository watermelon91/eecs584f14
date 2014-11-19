package queryReconstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import queryParser.QueryParser;
import queryParser.QueryProcessingUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlanReducer {
	
	// this class will simplify the query plan
	
	// for example, it will reduce all hash join, merge join, etc into join nodes
	// it will eliminate unnecessary seq scan nodes
	
	
	// subquery scans - implemented but not tested (big todo)
		// looks like it's working okay
	// subplan nodes - kind of works a little bit
		// looks like it's working okay
		// definitely could create some of the temp tables instead of jamming them all into
	// materialize node should be like sort and hash nodes - does nothing functionally
	// TODO list
	// aggregate naming
	// other garbage attribute renaming
	// make sorts at the top node work
	// test nested loops
	// if we do the attribute renaming, we should also eventually implement search/replace in conditions/other places references might appear
	
	// add error handlign to throw exceptions on unsupported plan types
	// e.g., joins with subplans
	// also selecting a function of the same thing multiple times 
		//e.g., explain verbose select a, a, b from (select count(*) a from toy) t1, (select count(*) b from toy2) t2 where a < b;
		
	// TODO: refactor. make a node class and a reduced node class so there's no more of this mixing up methods from planReducer vs. queryReconstructor.
	
	QueryParser qParser;
	public JSONObject topLevelNode;
	static int curTmp = 0;
	static String id = "";//new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
	static Map<String, String> functionToColumnAlias = new HashMap<String, String>();
	
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
		// TODO: check if top node is sort, if so, add appropriate attributes to node
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
		case SORT:
			reducedCurNode = reduceSortNode(curNode);
			break;
		case HASH:
			reducedCurNode = reduceNonfunctionalNode(curNode);
			break;
		case MATERIALIZE:
			reducedCurNode = reduceNonfunctionalNode(curNode);
			break;
			
		// scans
			
		case INDEX_SCAN:
			reducedCurNode = reduceIndexScanNode(curNode);
			break;
		case SUBQUERY_SCAN: 
			reducedCurNode = reduceSubqueryScanNode(curNode); 
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
		return reducedCurNode;
	}
	
	// TODO: may need to check all types of nodes for a subplan. 
	
	JSONObject reduceHashJoinNode(JSONObject curNode, String joinType) {
		String joinCond = qParser.getHashCond(curNode);
		return reduceGenericJoin(curNode, joinCond, joinType);
	}
	
	JSONObject reduceMergeJoinNode(JSONObject curNode, String joinType) {
		String joinCond = qParser.getMergeCond(curNode);
		return reduceGenericJoin(curNode, joinCond, joinType);
	}

	JSONObject reduceNestedLoopJoinNode(JSONObject curNode, String joinType) {
		JSONArray children = qParser.getChildrenPlanNodes(curNode);	
		String joinCond = "";
		JSONObject reducedNode = new JSONObject();		
		JSONObject reducedFirstChild = reduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = reduceNode((JSONObject) children.get(1));
		
		JSONArray aliasesC1 = getAliasSet(reducedFirstChild);
		JSONArray aliasesC2 = getAliasSet(reducedSecondChild);

		String joinCondFromChildren = QueryProcessingUtilities.extractConditionsContainingAlias(reducedFirstChild, aliasesC2, this);
		joinCondFromChildren = QueryProcessingUtilities.combineAndConditions(joinCondFromChildren, QueryProcessingUtilities.extractConditionsContainingAlias(reducedSecondChild, aliasesC1, this));
		
		String joinFilter = QueryProcessingUtilities.combineAndConditions(qParser.getJoinFilter(curNode), joinCondFromChildren);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);

		return makeJoinNode(joinCond, joinType, joinFilter, filter, reducedFirstChild, reducedSecondChild, outputAttrs);		
	}
	
	JSONObject reduceGenericJoin(JSONObject curNode, String joinCond, String joinType) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		
		JSONObject reducedFirstChild = reduceNode((JSONObject) children.get(0));
		JSONObject reducedSecondChild = reduceNode((JSONObject) children.get(1));
		
		String joinFilter = qParser.getJoinFilter(curNode);
		String filter = qParser.getFilter(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		// TODO: check for subplans! and for now throw exception!
		
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

	JSONObject reduceNonfunctionalNode(JSONObject curNode) {
		// hash and materialize nodes don't do anything (at least in non-JSON formatted query plans)
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		//TODO: may want this to take the output attributes from this node instead? probably not, but maybe
		JSONObject reducedNode = reduceNode((JSONObject) children.get(0));
		// maybe should get outputAttributes from this node though.
		// could then also use it for subquery scans?
		
		return reducedNode;
	}
	
	JSONObject reduceSortNode(JSONObject curNode) {
		// assuming only one child
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		JSONObject reducedNode = reduceNode((JSONObject) children.get(0));
		JSONArray orderBy = qParser.getSortKey(curNode);
		System.out.println("order by " + orderBy.toJSONString());
		JSONArray finalOrderBy = new JSONArray();
		Iterator<String> it = orderBy.iterator();
		while (it.hasNext()) {
			String attr = it.next();
			attr = QueryProcessingUtilities.splitOnDotAddUnderscore(attr);
			/*
			String[] tmp = attr.split("\\.");
			if (tmp.length == 2) {
				attr = tmp[0] + "_" + tmp[1];
			}
			*/
			finalOrderBy.add(attr);
		}
		reducedNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.SORT_KEY), finalOrderBy);
		return reducedNode;
	}

	JSONObject reduceBitmapHeapScanNode(JSONObject curNode) {
		String indexCond = qParser.getRecheckCond(curNode);
		String filter = qParser.getFilter(curNode);
		String finalFilter = QueryProcessingUtilities.combineAndConditions(indexCond, filter);
		return reduceGenericScan(curNode, finalFilter);
	}
	
	JSONObject reduceIndexScanNode(JSONObject curNode) {
		String filter = qParser.getFilter(curNode);
		String indexCond = qParser.getIndexCond(curNode);
		String finalFilter = QueryProcessingUtilities.combineAndConditions(indexCond, filter);
		return reduceGenericScan(curNode, finalFilter);
	}
	
	JSONObject reduceSeqScanNode(JSONObject curNode) {
		String filter = qParser.getFilter(curNode);
		return reduceGenericScan(curNode, filter);
	}
	
	JSONObject reduceGenericScan(JSONObject curNode, String filter) {
		JSONObject reducedNode = new JSONObject();
		
		// may or may not have anything in Filter
		
		String inputTable = qParser.getRelationName(curNode);
		String alias = qParser.getAlias(curNode);
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		JSONArray reducedChildren = null;

		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		if (children != null) {
			Iterator<JSONObject> cit = children.iterator();
			reducedChildren = new JSONArray();
			while (cit.hasNext()) {
				JSONObject child = cit.next();
				JSONObject reducedChild = reduceNode(child);
				String subplan = qParser.getSubplanName(child);
				
				if (!subplan.equals("")) {
					reducedChild.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.SUBPLAN_NAME), subplan);
				}
				reducedChildren.add(reducedChild);
			}
		}
		reducedNode = makeScanNode(reducedChildren, filter, inputTable, alias, outputAttrs);		
		// may not have a condition - should check
		
		return reducedNode;
		
	}
	
	// TODO: groupaggregate, hashaggregate, aggregate
	
	// TODO: also should handle index only scan
	
	//
	
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
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		JSONObject reducedChild = reduceNode((JSONObject) children.get(0));	
		JSONArray childColNames = QueryProcessingUtilities.getFinalColumnNames(getOutputAttributes(reducedChild));
		JSONArray outputAttrs = qParser.getOutputAttributes(curNode);
		
		// NOTE: the subquery is a stopper for child aliases, so we don't need to include them.
		String alias = qParser.getAlias(curNode);
		JSONArray finalOutputAttrs = new JSONArray();
		String filter = qParser.getFilter(curNode);
		Iterator<String> ccnIt = childColNames.iterator();
		Iterator<String> oaIt = outputAttrs.iterator();
		while (ccnIt.hasNext() && oaIt.hasNext()) {
			String outputName = oaIt.next();
			String childColName = ccnIt.next();
			
			/*
			String[] ar = outputName.split("\\.");
			if (ar.length == 2) {
				outputName = ar[0] + "_" + ar[1];
			}*/
			outputName = QueryProcessingUtilities.splitOnDotAddUnderscore(outputName);
			
			String[] ar = childColName.split("\\.");
			if (ar.length == 2) {
				childColName = ar[1];
			}
			finalOutputAttrs.add(childColName + " as " + outputName);
		}
		
		String tableName = getNewTableName(reducedChild);
		// 2. get table name from first child
		
		// 3. check for subplan
		// 		if subplan, process nodes of children
		//		pass in parent alias for replacement?
		//		let query reconstructor handle putting query together
		//		maybe leave parent alias replacement for reconstructor?
		//		we know the alias of the subquery - any subplan will refer to this alias
		// get names of child columns here
		// what about those that are already aliased? 
		JSONArray reducedChildren = new JSONArray();
		reducedChildren.add(reducedChild);
		
		if (QueryProcessingUtilities.filterContainsSubplan(filter) && children.size() > 1) {
			// TODO: process and add child to children array
			Iterator<JSONObject> cit = children.iterator();
			cit.next();
			// start at the second child
			while (cit.hasNext()) {
				JSONObject child = cit.next();
				reducedChild = reduceNode(child);
				if (!qParser.getSubplanName(child).equals("")) {
					reducedChild.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.SUBPLAN_NAME), qParser.getSubplanName(child));
				}
				reducedChildren.add(reducedChild);
			}
		}

		// TODO: make sure alias works right
		reducedNode = makeScanNode(reducedChildren, filter, tableName, alias, finalOutputAttrs);
		
		return reducedNode;
	}

	// TODO: write a function that checks a node for subplans
	// 
	// what to do if so?
	// should probably process whole node, since normal child needs to be processed normally
	// and other child needs special treatment
		
	// TODO: implement
	/*
	JSONObject reduceSubplanNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
		JSONArray children = qParser.getChildrenPlanNodes(curNode);
		JSONArray reducedChildren = new JSONArray();
		
		Iterator<JSONObject> cit = children.iterator();
		while (cit.hasNext()) {
			JSONObject reducedChild = reduceNode(cit.next());
			reducedChildren.add(reducedChild);
		}
		
		
		return makeSubplanNode(children, qParser.getSubplanName(curNode), qParser.getOutputAttributes(curNode));
	}
*/
	
	JSONObject reduceAggregateNode(JSONObject curNode) {
		JSONObject reducedNode = new JSONObject();
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
				
				//tralala syntax error so I can find you later!
				// two options -  
				// TODO: could just add parentheses to the ones taht are 
				attr = QueryProcessingUtilities.removeAllWrappingParentheses(attr);
				if (functionToColumnAlias.containsKey(attr)) {
					newOutputAttrs.add(functionToColumnAlias.get(attr));
				} else {
					// generate new name
					String newName = QueryProcessingUtilities.generateColumnNameForFunction(attr);
					functionToColumnAlias.put(attr, newName);
					newOutputAttrs.add(attr + " as " + newName);
				}
				
				
				
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
		
		// TODO: make sure to replace function-columns with column names!!!!
		
		String joinCondCombined = QueryProcessingUtilities.combineAndConditions(joinCond, joinFilter);
		String functionReplacedJoinCond = replaceFunctionColumnsWithColumnName(joinCondCombined);
		String aliasReplacedFinalJoinCond = replaceAliasesWithTableName(replaceAliasesWithTableName(functionReplacedJoinCond, aliasC1, tmpC1), aliasC2, tmpC2);
		
		/*
		String aliasReplacedJoinCond = replaceAliasesWithTableName(replaceAliasesWithTableName(joinCond, aliasC1, tmpC1), aliasC2, tmpC2);
		String aliasReplacedJoinFilter = replaceAliasesWithTableName(replaceAliasesWithTableName(joinFilter, aliasC1, tmpC1), aliasC2, tmpC2);
		*/
		String aliasReplacedFilter = replaceAliasesWithTableName(replaceAliasesWithTableName(replaceFunctionColumnsWithColumnName(filter), aliasC1, tmpC1), aliasC2, tmpC2);
		// also handle where clause
		
		/*
		String aliasReplacedFinalJoinCond = "";
		if (!aliasReplacedJoinCond.equals("") && !aliasReplacedJoinFilter.equals("")) {
			aliasReplacedFinalJoinCond = aliasReplacedJoinCond + " and " + aliasReplacedJoinFilter;
		} else if (!aliasReplacedJoinCond.equals("")) {
			aliasReplacedFinalJoinCond = aliasReplacedJoinCond;
		} else {
			aliasReplacedFinalJoinCond = aliasReplacedJoinFilter;
		}
		*/
		
		JSONArray aliasSet = QueryProcessingUtilities.concatArrays(getAliasSet(reducedFirstChild), getAliasSet(reducedSecondChild));
		// TODO: include original filters for display
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.TYPE), "join");
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.CHILDREN), children);
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER), aliasReplacedFilter);
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.JOIN_CONDITION), aliasReplacedFinalJoinCond);
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.JOIN_TYPE), joinType);
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS), newOutputAttrs);
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.ALIAS_SET), aliasSet);
		reducedJoinNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME), "tmp" + curTmp + "_" + id);
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
					// TODO: handle aggregate naming in lower nodes
					// after stripping parentheses, check to see if it's already in the map of execution string to attribute name
					// if first thing is a paren, check to see if it's a function (if it is, after stripping parentheses there will still be parentheses).
					String tmp_attr = attr;
					
					// remove any wrapping parentheses
					// functions that were executed at lower levels are wrapped in parentheses. they already have names that should be looked up
					tmp_attr = QueryProcessingUtilities.removeAllWrappingParentheses(tmp_attr);				
					if (functionToColumnAlias.containsKey(tmp_attr)) {
						newOutputAttrs.add(functionToColumnAlias.get(tmp_attr));
					} else {
						System.out.println("unexpected unnamed column");
						// generate new name
						String newName = QueryProcessingUtilities.generateColumnNameForFunction(tmp_attr);
						functionToColumnAlias.put(tmp_attr, newName);
						newOutputAttrs.add(tmp_attr + " as " + newName);
					}

					
				} else {
					// first char not paren = execute the function here
					String[] attrParts = attr.split("\\.");
					String tmp_attr = attr;
					// make column alias and save for later!
					if (functionToColumnAlias.containsKey(tmp_attr)) {
						System.out.println("unexpected named column");
						newOutputAttrs.add(functionToColumnAlias.get(tmp_attr));
					} else {
						
						// generate new name
						String newName = QueryProcessingUtilities.generateColumnNameForFunction(tmp_attr);
						functionToColumnAlias.put(tmp_attr, newName);
						newOutputAttrs.add(tmp_attr + " as " + newName);
					}

					/*
					if (attrParts.length == 2) {
						newOutputAttrs.add(attrParts[0] + "_" + attrParts[1]);
					} else {
						// this is probably a query on a single node, so we don't need aliases
						newOutputAttrs.add(attr);
					}
					*/
				}
			} else {
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
		
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.TYPE), "aggregate");
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.CHILDREN), children);
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER), filter);
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS), newOutputAttrs);
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.ALIAS_SET), aliasSet);
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME), "tmp" + curTmp + "_" + id);
		reducedAggregateNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.GROUP_BY_ATTRS), groupByAttrs);
		curTmp++;
		
		return reducedAggregateNode;
	}
	
	JSONObject makeScanNode(JSONArray reducedChildren, String filterCond, String inputTable, String alias, JSONArray outputAttrs) {
		// scan node can have children or no children, right?
		
		// things required to make a scan tmp table
		// when do we actually want a scan node vs. directly incorporating it into the parent node?
		// e.g., if it's literally nothing more than a sequential scan, then we don't want to make a node for it
		// maybe we should have a boolean indicating whether it should be kept as a node
		// for now, though, make all nodes
		//tralalala check here too for functions/other things that need to be looked up
		
		JSONArray newOutputAttrs = QueryProcessingUtilities.replaceFunctionsWithColumnNames(outputAttrs, functionToColumnAlias);
		outputAttrs = QueryProcessingUtilities.renameAttributesSimple(newOutputAttrs);
		
		newOutputAttrs = new JSONArray();
		Iterator<String> it = outputAttrs.iterator();
		while (it.hasNext()) {		
			String tmp_attr = it.next();
			if (tmp_attr.contains("\\(")) {
				if (functionToColumnAlias.containsKey(tmp_attr)) {
					System.out.println("unexpected named column");
					newOutputAttrs.add(functionToColumnAlias.get(tmp_attr));
				} else {
					
					// generate new name
					String newName = QueryProcessingUtilities.generateColumnNameForFunction(tmp_attr);
					functionToColumnAlias.put(tmp_attr, newName);
					newOutputAttrs.add(tmp_attr + " as " + newName);
				}
			} else {
				newOutputAttrs.add(tmp_attr);
			}
		}

/*	
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
	*/	
		JSONObject reducedScanNode = new JSONObject();
		JSONArray aliasSet = new JSONArray();
		// TODO: if attributes have alias, add alias name to attribute name
		aliasSet.add(alias);
		reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.TYPE), "scan");
		reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER), filterCond);
		reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.ALIAS_SET), aliasSet);
		reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.INPUT_TABLE), inputTable);
		reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS), newOutputAttrs);
		reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME), "tmp" + curTmp + "_" + id);
		if (reducedChildren != null) {
			reducedScanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.CHILDREN), reducedChildren);			
		}
		curTmp++;
		
		return reducedScanNode;
	}
	
	/*
	JSONObject makeSubplanNode(JSONArray reducedChildren, String subplanName, JSONArray outputAttrs) {
		JSONObject reducedSubplanNode = new JSONObject();
		JSONArray newOutputAttrs = QueryProcessingUtilities.renameAttributesSimple(outputAttrs);

		reducedSubplanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.TYPE, "subplan");
		reducedSubplanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.CHILDREN, reducedChildren);
		reducedSubplanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.SUBPLAN_NAME, subplanName);
		reducedSubplanNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.OUTPUT_ATTRS, newOutputAttrs);

		return reducedSubplanNode;
	}
	*/
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
	
	String replaceFunctionColumnsWithColumnName(String condition) {
		String regex;
		
		Iterator<String> it = functionToColumnAlias.keySet().iterator();
		while (it.hasNext()) {
			// possibly add \b for word boundary
			String functionText = it.next();
			regex = functionText;
			// TODO: string literals
			condition = condition.replaceAll(Pattern.quote(regex), functionToColumnAlias.get(functionText));
		}
		return condition;
	}
	
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
		SUBPLAN,
		MATERIALIZE,
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
		if (nodeType.equals("SubPlan")) { return NODE_TYPE.SUBPLAN; }
		if (nodeType.equals("Materialize")) { return NODE_TYPE.MATERIALIZE; }
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
		QUERY, 
		SUBPLAN_NAME,
		SORT_KEY
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
		 map.put(REDUCED_PLAN_ATTRS.SUBPLAN_NAME, "subplanName");
		 map.put(REDUCED_PLAN_ATTRS.SORT_KEY, "sortKey");
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
	
	public void setFilter(JSONObject currentNode, String newFilter) {
		currentNode.remove(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER));
		currentNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.FILTER), newFilter);
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
	
	public void setInputTable(JSONObject currentNode, String newInputTable) {
		currentNode.remove(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.INPUT_TABLE));
		currentNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.INPUT_TABLE), newInputTable);
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
	
	public void setNewTableName(JSONObject currentNode, String newTableName) {
		currentNode.remove(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME));
		currentNode.put(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.NEW_TABLE_NAME), newTableName);
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
	
	public String getSubplanName(JSONObject currentNode)
	{
		Object rst = currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.SUBPLAN_NAME));
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
	
	public JSONArray getSortKey(JSONObject currentNode)
	{
		return (JSONArray)currentNode.get(reducedPlanAttrMapping.get(REDUCED_PLAN_ATTRS.SORT_KEY));
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
