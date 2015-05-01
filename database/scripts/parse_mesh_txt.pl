#!/usr/bin/perl -w
#
#	This is a quick'n'dirty script for parsing the "MeSH text tree" file
#
#
#
use strict;

use lib ( 	
	'./lib/perl', 
	'lib/perl/lib/perl5/site_perl',
	'lib/perl/System/Library/Perl/5.8.6/darwin-thread-multi-2level'
);

use HTML::ExtendedJsTree;
use HTML::JsTree;
use HTML::JsNode;

$HTML::JsTree::Stylesheet = 'css/navtree.css';
$HTML::JsTree::IMAGE_ROOT = 'resources/images/jstree';

my $DEBUGGING = 1;

die "Usage:\n$0 <mesh file>\n" unless @ARGV;


### GLOBALS ###

# 	top level hash of tree data structure, 
#	populated by parse_mesh().
#
my %tree;

# 	hash of descriptor id to tree node, populated
#	by parse_mesh().
#
my %All_Descriptors;

#	database connection info
#
our $Database_Name     = 'eurocarb_devel';
our $Database_Driver   = 'Pg';
our $Database_Username = 'postgres';
our $Database_Password = 'flipper'; 

#	database connection handle
my $Database_Handle;


### MAIN ###

parse_mesh();

debug_mesh_tree() if $DEBUGGING > 2;

load_disease();

load_perturbation();

load_tissue_taxonomy();

#generate_js_tree( \%tree );

#calculate_left_right_tree_order( $tree{children}{A} );

exit;


### SUBS ###

sub hdump
{
	my $node = shift;
	return 	"{ " . join(", " => map "$_: $node->{$_}" => keys %$node) . " }";
}


#	parse the open mesh text file. this sub populates %tree and %All_Descriptors.
sub parse_mesh
{
	### initialisation ###

	# top level hash of tree data structure, declaring an initial root node.
	# the 'children' key is either undef (no children) or a ref to a hash
	# of child nodes keyed by their id. the 'parent' key is a reference to
	# the node's parent.
	#
	%tree =	(
				id       => 'root', 
				name     => 'root',
				info     => undef, 
				mesh_id	 => 'root',
				children => undef, 
			);	
	
	#	these are the MeSH categories we are interested in.
	my %category = (
		A => 	{ 
					id => 'Anatomy', 
					name => 'Anatomy', 
					mesh_id => 'Anatomy', 
					parent => \%tree 
				},
	 	B => 	{
	 				id => 'Organisms',
	 				name => 'Organisms',
	 				mesh_id => 'Organisms',
	 				parent => \%tree,
	 			},
	 	C => 	{
	 				id => 'Diseases',
	 				name => 'Diseases',
	 				mesh_id => 'Diseases',
	 				parent => \%tree,
	 			},
	 	D => 	{
	 				id => 'Chemicals and Drugs',
	 				name => 'Chemicals and Drugs',
	 				mesh_id => 'Chemicals and Drugs',
	 				parent => \%tree,
	 			},
	);
	
	#	make the above categories the parent nodes of their respective trees. 
	$tree{children} = \%category;
	
	warn "Desired MeSH categories:\n" 
		. join( "" => map "  $_ -> $category{$_}->{name}\n" => sort keys %category )
		. "\n"
		if $DEBUGGING;
	
	#	these are the fields from each record whose values we actually keep.
	my %wanted_field = (
		MH => 1,   # "main heading" field
		MS => 1,   # "description" field
		MN => 1,   # "id" field 
		UI => 1,   # "unique id" field
	);

	# count of records parsed
	my $count_records = 0;
	
	# count of all nodes, since 1 record can create multiple nodes.
	my $count_all = 0;
	

	### parsing ###

	warn "parsing " . join(", ", @ARGV) . ":\n" if $DEBUGGING;
			
	#	read an entire paragraph at a time. 
	local $/ = "";
	
	#	read paragraphs.
	while ( <> )
	{
		chomp;
			
		my %record;
		
		foreach ( split /\n/ )
		{
			my( $field, $value ) = split /\s=\s/;
			next unless exists $wanted_field{$field};
			
			#	there can be multiple 'MN' fields :-(
			if ( $field eq 'MN' ) 
			{ 
				#	the MESH category is the first letter of the MN field.	
				my $cat = substr $value, 0, 1;
						
				# 	skip over categories we don't want or need.
				next unless $category{$cat};

				$record{MN} ||= [];
				push @{$record{MN}}, $value; 
				
				next;
			}

			$record{$field} = $value;
			
		}
		
		++$count_records;

		#	(debugging)
		#	print "    $_ --> $record{$_}\n" foreach sort keys %record;
		#	exit if $count > 10;
		#	next;

		#	If current record doesn't have any MN fields after stripping out MN fields 
		#	for categories we're not interested in, then skip processing this record.
		unless ( $record{MN} )
		{
			#	gratuitous progress information
			print STDERR '.' unless $count_records % 200;
			print STDERR "$count_records\n" unless $count_records % 5000;
			next;
		}		
			
		#	there is always a MH (main heading) field.
		my $desc = 	$record{MH} 
			|| die "\n$_\nRecord $count_records above has missing MH field ";

		#	should always be a UI (unique id) field.		
		my $desc_uid = $record{UI}
			|| die "\n$_\nRecord $count_records above has missing UI field ";

		#	not all records have MS (description) fields - this is ok.
		my $desc_info	= $record{MS} 
			|| ""; #die "\n$_\nRecord $count_records above has missing MS field ";
		
		
		my $mesh_ids = $record{MN};
		
		foreach my $id ( @$mesh_ids )
		{
			my %node = (
				cat 			=> substr( $id, 0, 1 ),# category to which this node belongs
				mesh_id 		=> $id,					# mesh_id, which determines position in tree
				mesh_id_xlinks => $mesh_ids, 			# these are the other mesh_ids by which this record is known.
				name 			=> $desc,				# canonical name of this node
				info 			=> $desc_info,			# short description of this node
				uid				=> $desc_uid,			# unique identifier for this node
				position		=> $count_records,		# position of record in file
			);
				
			# 	record in hash of id => node.
			$All_Descriptors{$id} = \%node;
			$count_all++;
		}
	
		#	gratuitous progress information
		print STDERR '.' unless $count_records % 200;
		print STDERR "$count_records\n" unless $count_records % 5000;
	}

	print STDERR "done\n";
	
	printf STDERR "parsed %i records into %i total nodes\n" 
		=> $count_records, $count_all
			if $DEBUGGING;


	#	make a hash-based tree of all nodes.
	#	the field 'mesh_id' is a fullstop-delimited path to each
	# 	node's place in the tree.
	#
	print STDERR "building tree";
	my $count = 0;
	
	while ( my( $id, $node ) = each %All_Descriptors )
	{
		#	it's not a top-level node
		if ( $id =~ s/\.(\w+?)$// )
		{
			my $partial_id = $1;
			my $parent_node = $All_Descriptors{$id} 
				|| die "Expected a node for mesh_id '$id', but none exists";
				
			$node->{parent} = $parent_node;
			$parent_node->{children}->{$partial_id} = $node;
		} 
		else 
		{
			# 	it's a top-level node
			my $cat_letter = substr( $id, 0, 1 );
			my $parent_node = $category{$cat_letter} || die;
			
			$node->{parent} = $parent_node;
			$parent_node->{children}->{$id} = $node;
		}

		print STDERR '.' unless ++$count % 1000;
	}
	print STDERR "done, $count records\n";

	return;
}


sub generate_js_tree
{
    my $start_node = shift || die;
 	warn "creating javascript tree...\n";
        
    my $tree = HTML::JsTree->new( "MeSH" );    
	  
	  use Data::Dumper;      
	my %seen;
	my $depth = 0;
	        
	my $recurse; $recurse = 
		sub 
		{
			my( $node, $parent_js_node, $depth ) = @_;
			
			my $id = $node->{mesh_id} 
				|| 	"(no id)";
#					warn "no ID: " 
#					. 	hdump( $node )
#					.	"\nparent: "
#					.	hdump( $node->{parent} )
#					.	"\nchildren: "
#					.	join("\n", map { hdump($_) } values %{$node->{children}} )
#					;
			
			my $name = $node->{name} 
#				|| warn "no name: " 	. join(", ", map "$_ => $node->{$_}", keys %$node);
				||	"(no name)";
			
			#die "already seen $name" if $seen{$name}++;
			
			$depth ||= 0;
			warn "    "x$depth . "$name ($id)\n" if $DEBUGGING > 1;
			
			my @xlink_ids = grep { $id ne $_ } @{$node->{mesh_id_xlinks}};
			my $link_text = "$name ($id)";
			if ( @xlink_ids )
			{
				$link_text 	.= " (also crosslinked to " 
							. join(", ", @xlink_ids) 
							. ")"
							;
			}
									
			my $js_node = HTML::JsNode->new( value => $link_text ); 
			
			$tree->add( $js_node, $parent_js_node );
		
			return unless $node->{children};
		
			foreach my $child_id ( sort keys %{$node->{children}} )
			{
				my $child_node = $node->{children}->{$child_id};
				$recurse->( $child_node, $js_node, $depth + 1 );
			}
			
			#return;
		};

	$recurse->( $start_node );
	
	print "<html><body>";
    print $tree->as_string();
    print "</body></html>";
        
    print STDERR "done\n";
        
    return;
}


#	print a crapton of debugging info to STDOUT.
#
sub debug_mesh_tree
{
	eval q(
		use Data::Dumper;
		$Data::Dumper::Indent = 1;
	
		#print Dumper( \%All_Descriptors );
		print Dumper( \%tree );
	); 
	die $@ if $@;
}


sub get_connection
{
	return $Database_Handle if $Database_Handle;

	eval 'use DBI'; 
	die "Couldn't dynamically load DB driver: $@" if $@;
	
	my $dsn = "dbi:$Database_Driver:dbname=$Database_Name";
	$Database_Handle = DBI->connect( $dsn, $Database_Username, $Database_Password );

	die "Couldn't establish a connection to database "
		. $Database_Name
		. " using database driver "
		. $Database_Driver
		. " and username "
		. $Database_Username
		. ": "
		. DBI->err
		unless $Database_Handle;

	return $Database_Handle;
}

sub END
{
	$Database_Handle->disconnect() if $Database_Handle;
}



sub load_tissue_taxonomy
{
	my $anatomy_tree = $tree{children}{A} || die;
	load_table( "core.tissue_taxonomy" => $anatomy_tree, 
				qw/ parent_tissue_taxonomy_id
					tissue_taxon
					mesh_id  
					description / 
	);
}


sub load_perturbation
{
	my $perturbation_tree = $tree{children}{D} || die;
	load_table( "core.perturbation" => $perturbation_tree, 
				qw/ parent_perturbation_id
					perturbation_name
					mesh_id  
					description / 
	);
}


sub load_disease
{
	my $disease_tree = $tree{children}{C} || die;
	load_table( "core.disease" => $disease_tree, 
				qw/ parent_disease_id
					disease_name
					mesh_id  
					description / 
	);
}


=head2	load_table

	load_table( $table_name, \%tree, \@columns );

=cut
sub load_table
{
	my( $table_name, $tree, @columns ) = @_; 

	my( $schema, $table ) = ($table_name =~ /^(\w+)\.(\w+)$/ );
	
	my $table_id_column = "$table\_id";
	
	my $dbh = get_connection();
	
	warn "--- loading $table_name ---\n";
	
	calculate_left_right_tree_order( $tree );
	

	my $sql = "INSERT INTO "
			. $table_name
			. " ( "
			. join(", ", @columns )
			. " ) VALUES ( "
			. join(", ", map "?", @columns )
			. " ) "
			;
	
	warn "(insert sql is $sql)\n" if $DEBUGGING > 1;
	my $insert_row_sth = $dbh->prepare( $sql );

	my $seq_name = "$schema.$table\_$table_id_column\_seq";
	my $get_id_sql = "SELECT currval( '" 
					. $seq_name
					. "' )";

	warn "(get id sql is $get_id_sql)\n" if $DEBUGGING > 1;
	my $get_id_sth = $dbh->prepare( $get_id_sql );

	my $relations_table_name = "$schema.$table\_relations";
	my $insert_relations_sql = <<"    ____END_SQL____";
	INSERT INTO $relations_table_name ( 
		$table_id_column, left_index, right_index 
	) 
	VALUES ( ?, ?, ? )
    ____END_SQL____
    
    my $insert_relations_sth = $dbh->prepare( $insert_relations_sql );
    
	warn "(deleting all rows from $table_name table)\n";
	
	my $count_deleted = 
		$dbh->do("DELETE FROM $table_name")
			||	die "Failed to delete all rows from $table_name table";

	warn "deleted $count_deleted rows from table\n";

	warn "(reseting sequence generator)\n";
	$dbh->do( "SELECT setval( '$seq_name', 1, false )" )
		||	die "Failed to reset sequence counter '$seq_name'";

	warn "inserting into $table_name\n";
	my $count;
	my $last_node;
	
	my $callback = 
		sub
		{
			my $node  = shift;
			my $opts  = shift || die; 
			$last_node = $node;

			my $parent_tt_id = $node->{parent}->{tt_id}; 
			
			die "no parent id for node $node->{name}"
				unless defined $parent_tt_id;

			warn "    " x $opts->{depth} 
				. "inserting node $node->{mesh_id} ($node->{name}) with parent tt_id $parent_tt_id\n"
					if $DEBUGGING > 2;
			
			$insert_row_sth->execute(	
				$parent_tt_id,
				$node->{name}, 
				$node->{mesh_id}, 
				$node->{info}  
			) or die "insert failed";							

			$get_id_sth->execute() 
				or die "get last insert id failed";
			
			my( $tt_id ) = $get_id_sth->fetchrow_array();
			die unless defined $tt_id;
			
			$node->{tt_id} = $tt_id;
			warn "    " x $opts->{depth} 
				. "inserted node $node->{name} has id $tt_id\n"
					if $DEBUGGING > 2;

			$insert_relations_sth->execute( $tt_id, $node->{left}, $node->{right} )
				or die "insert relations failed";
			
			++$count;
			print STDERR '.' unless $count % 40;
			print STDERR "$count\n" unless $count % 1000;

			return 1;			
		};
		
	#	print longest length string of each field
	#print join(", ", map "$_: $max{$_}", keys %max ) . "\n";
	#return;
	
	$tree{tt_id} = 1;
	
	eval {  traverse_tree( $tree, $callback );  };
	if ( $@ )
	{
		warn  "\nError occurred while inserting node '"
			. $last_node->{mesh_id}
			. "' "
			. hdump($last_node)
			. ":\n"
			. $@
			;
			
		warn  "\nSQL statements:\n"
			. "insert row: $sql\n"
			. "retrieve last inserted id: $get_id_sql\n"
			. "insert relations row: $insert_relations_sql\n";
			
		$dbh->rollback();
		warn "Transaction rolled back\n";
		
		exit;
	}
	else
	{
		warn "done\n";
		$dbh->commit();
		warn "successfully committed changes to $table_name\n";
	}
	
	$insert_row_sth->finish();
	$get_id_sth->finish();

	warn "inserted $count records into $table_name\n";

    return;
}


=head2	traverse_tree

Usage:

	traverse_tree( $tree, %options );

where %options can be any of:

	(  
		preorder_callback	=>	\&preorder_callback,
		postorder_callback =>	\&postorder_callback,
		depth 				=>	$the_depth_in_tree,
	)

=cut
sub traverse_tree
{
	my $node = shift;
	
	my %opts =  (@_ == 1) 
				? ( depth => 0, preorder_callback => $_[0] )
				: ( depth => 0, @_ );
	
	$opts{preorder_callback}->( $node, \%opts ) if exists $opts{preorder_callback};
	
	if ($opts{depth} > 200 ) 
	{ die "*** deep recursion trap ***"; }
	
	if ( my $children = $node->{children} )
	{
		foreach my $child ( values %$children )
		{
			traverse_tree( $child, %opts, depth => $opts{depth} + 1 );	
		}
	}

	$opts{postorder_callback}->( $node, \%opts ) if exists $opts{postorder_callback};
	
	return;	
}


=head2	calculate_left_right_tree_order

Usage:

	calculate_left_right_tree_order( $tree );

Traverses given tree, calculating a left & right index for each node in 
the tree. That is, each node will have a > 0 value for the hash keys
'left' and 'right' that are inciative of that node's position in the tree.

See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295

=cut
sub calculate_left_right_tree_order
{
	my $tree = shift || die;

	warn "(precalculating left/right nested set order)\n" if $DEBUGGING;

	my $count = 1;
		
	my $pre_callback  = sub { my $node = shift; $node->{left} = $count++; };
	my $post_callback = sub { my $node = shift; $node->{right} = $count++; };
	
	traverse_tree( $tree, 
					preorder_callback => $pre_callback, 
					postorder_callback => $post_callback );
	
	if ( $DEBUGGING > 2 )
	{
		traverse_tree( $tree, 
						preorder_callback => sub 
						{ 
							my( $node, $opts ) = @_;
							print "...." x $opts->{depth} 
								. $node->{name} 
								. " left: "
								. $node->{left}
								. ", right: "
								. $node->{right}
								. "\n";
						} 
		);
	}
		
	return;
}




sub determine_max_field_length
{
			#   debugging
			#warn "node { " 
			#	. join(", ", map "$_: $node->{$_}", keys %$node ) 
			#	. " }\n";

			#	this section only used to determine max length of fields 
			#   for database schema purposes.
			#	
			#while ( my($k, $v) = each %$node )
			#{
			#	if ( exists $max{$k} )
			#	{
			#		$max{$k} = length("$v") if length("$v") > $max{$k};
			#	}
			#	else
			#	{
			#		$max{$k} = length("$v");
			#	}
			#}
			#next;


}


