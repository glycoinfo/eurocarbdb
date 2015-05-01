#!/usr/bin/perl -w
#
#	This is a quick'n'dirty script for parsing the "MeSH text tree" file
#
#
#
use strict;

use lib '../../lib/perl';

use HTML::ExtendedJsTree;
use HTML::JsTree;
use HTML::JsNode;

$HTML::JsTree::Stylesheet = 'css/navtree.css';
$HTML::JsTree::IMAGE_ROOT = 'images/jstree';

my $DEBUGGING = 1;

die "Usage:\n$0 <mesh file>\n" unless @ARGV;

my $mesh_categories_rawtext = q(
 Anatomy [A]
 Organisms [B]
 Diseases [C]
 Chemicals and Drugs [D]
);
# Uncomment the lines below and move them into the above 
# quotes to include these categories in the generated javascript tree
 #Analytical, Diagnostic and Therapeutic Techniques and Equipment [E]
 #Psychiatry and Psychology [F]
 #Biological Sciences [G]
 #Physical Sciences [H]
 #Anthropology, Education, Sociology and Social Phenomena [I]
 #Technology and Food and Beverages [J]
 #Humanities [K]
 #Information Science [L]
 #Persons [M]
 #Health Care [N]
 #Publication Characteristics [V]
 #Geographic Locations [Z]

# this 1-liner makes a hash of the above categories, 
# keyed by the letter in square brackets. 
my %wanted = reverse( 
	$mesh_categories_rawtext =~ /
									([A-Z][\w\s,]+?)	# a multi-word category name
									\s					# then a space
									\[(\w)\]			# then a category letter abbrev in brackets.
								 /gx 
);

warn "Desired MeSH categories:\n" 
	. join( "" => map "  $_ -> $wanted{$_}\n" => sort keys %wanted )
	. "\n"
	if $DEBUGGING;

# count of records parsed
my $count;

# hash of descriptor id to tree node.
my %All_Descriptors;

# top level hash of tree data structure, declaring an initial root node.
my %tree = ( id => 'root', name => 'root', children => {} );

warn "parsing " . join(", ", @ARGV) . ":\n" if $DEBUGGING;

while ( <> )
{
	chomp;
	
	# split into descriptor + descriptor_id
	my( $desc, $desc_id ) = split /;/;
	printf STDERR "%25s => %25s => " => $desc, $desc_id
		if $DEBUGGING > 1;
	
	
	# skip over categories we don't want or need.
	my $category = substr $desc_id, 0, 1;
	next unless $wanted{$category};
	
	
	# split desc_id (looks like "XXX.XXX.XXX") into subtree parts
	my @ids = split /\./, $desc_id;
	
	warn "(" . join( ", " => @ids) . ")\n" 
		if $DEBUGGING > 1;
	
	
	# populate the mesh tree with the current node. The ids split
	# from the descriptor id provide the path through the tree where
	# this node should reside.

	my @stack = ( \%tree ); # stack of this node's parent nodes.
	
	# traverse through tree until we are where this node should be
	foreach my $id ( @ids )
	{		
		#$last_id = $id;
		my $node = $stack[$#stack];
		
		if ( exists $node->{children}->{$id} )
		{
			$node = $node->{children}->{$id};
			push @stack, $node;
		}
		else
		{
			my $new_node = { id => $id, parent => $node };
			$node->{children}->{$id} = $new_node;
			push @stack, $new_node;
		}
		
	}
	
	# add this node's data.
	my $node = $stack[$#stack]; 
	$node->{name} = $desc;
	$node->{mesh_id} = $desc_id;
	$node->{position} = $count;
	
	# record in hash of id => node.
	$All_Descriptors{$desc_id} = $node;

	++$count;
	print STDERR '.' unless $count % 100;
	print STDERR "$count\n" unless $count % 5000;
}

if ( $DEBUGGING > 2 )
{
	eval 'use Data::Dumper;
	$Data::Dumper::Indent = 1;
	
	#print Dumper( \%All_Descriptors );
	print Dumper( \%tree );
	'; 
	die $@ if $@;
}

printf STDERR "parsed %i records\n" => $count
	if $DEBUGGING;


generate_js_tree( \%tree );

exit;



sub generate_js_tree
{
    my $start_node = shift || die;
        
    my $tree = HTML::JsTree->new( "MeSH" );    
	        
	my %seen;
	        
	my $recurse; $recurse = 
		sub 
		{
			my( $node, $parent_js_node ) = @_;
			my $id   = $node->{id} || die $node;
			my $name = $node->{name} || die $node;
			
			#die "already seen $name" if $seen{$name}++;
			
			my $js_node = HTML::JsNode->new( value => $name ); 
			$tree->add( $js_node, $parent_js_node );
		
			return unless $node->{children};
		
			foreach my $child_id ( sort keys %{$node->{children}} )
			{
				my $child_node = $node->{children}->{$child_id};
				$recurse->( $child_node, $js_node );
			}
			
			return;
		};

	$recurse->( $start_node );
	
	print "<html><body>";
    print $tree->as_string();
    print "</body></html>";
        
    return;
}



