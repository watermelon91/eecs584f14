#!/usr/bin/perl

use Pg::Explain;
use Pg::Explain::Node;

#my $explain = Pg::Explain->new('source_file' => 'queryPlan.txt');
my $explain = Pg::Explain->new('source' => 'Seq Scan on tenk1  (cost=0.00..333.00 rows=10000 width=148)');

#my $topnode = $explain->{'top_node'};
#my $topnode = Pg::Explain::Node->type($explain);
my $topnode = $explain;
#my @subnodes = $topnode->get_struct();

if ( $topnode->{ 'subelement-type' } eq 'subnode' ) {
    print "1\n";
}
elsif ( $topnode->{ 'subelement-type' } eq 'initplan' ) {
    print "2\n";
}
elsif ( $topnode->{ 'subelement-type' } eq 'subplan' ) {
    print "3\n";
}
elsif ( $topnode->{ 'subelement-type' } =~ /^cte:(.+)$/ ) {
    print "4\n";
}
else {
    print "5\n";
    print $topnode->{ 'subelement-type' };
    print $topnode->{ 'type' };
    print $topnode->{ 'estimated_startup_cost' };
    print $topnode->{ 'estimated_total_cost' };
    print $topnode->{ 'estimated_rows' };
    print $topnode->{ 'estimated_row_width' };
    print $topnode->{ 'actual_time_first' };
    print $topnode->{ 'actual_time_last' };
    print $topnode->{ 'actual_rows' };
    print $topnode->{ 'actual_loops' };
    print $topnode->{ 'Total Cost'};
    print $topnode . " \n";
    print $subnodes . " \n";
    print $topnode->type;
    
}



