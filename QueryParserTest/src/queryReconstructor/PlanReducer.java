package queryReconstructor;

import queryParser.QueryParser;

public class PlanReducer {
	QueryParser qParser;
	
	
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
		
		// switch on node type
		// then process from there.
		// first, handle joins and scans
		// later, worry about aggregation
		
		// this class will simplify the query plan
		
		// for example, it will reduce all hash join, merge join, etc into join nodes
		// it will eliminate unnecessary seq scan nodes
		
		
		// node type: if it's a join type (hash, sort merge, inl, nl), we'll make a join node
		// we'll look at the join conditions and stick them in the where clause
		// may be some complications with aliasing/figuring out what relation an attribute comes from
		// possible solution: keep a set of aliases that came from the children of a node, consult if necessary
		
		// merge join - join condition is in merge cond
		// hash join - join condition is in hash cond
		
		
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
		
		// hmm, there are limit nodes. this is definitely a place where a lot of optimization can take place (if we combine the nodes)
		
		
		// if a child is any sort of scan with no filters, we can probably just ignore it
		// in the parent, just use the table it was sequential scanning over 
		// could implement this by just naming the temp table member to be the table being scanned
		
		// any sort of scan node can possibly be ignored by putting the conditions in the parent node
		// or we could just do the filter and use it as input
		// will be faster to put conditions on parent node
		
		// a hash node probably doesn't need any processing - just use the result of its children
		// same thing for sort
		

	}

}
