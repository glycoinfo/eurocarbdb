package Eurocarb;

use lib 'lib';
use Time::HiRes 'gettimeofday';
use DBI;
use strict;

our $DEBUGGING = 1;


##### CLASS GLOBALS #####

#	Halts all database loading routines at the CSV generation phase if true.
#
our $CSV_ONLY = 0;

#	default database connection info. 
#
our $Database_Driver   = 'PgPP';
our $Database_Name     = 'eurocarb_devel';
our $Database_Username = 'postgres';
our $Database_Password = ''; 

#	Cached database connection handle
my $Database_Handle;


# 	Cached hash of MeSH descriptor id to tree node, populated
#	by parse_mesh().
#
our %mesh_by_id;

# 	Cached hash of MeSH tree data structure, 
#	populated by parse_mesh().
#
our $mesh_tree = {};

#    Cached hash of all NCBI Taxonomy nodes by their tax_id
#
our %ncbi_by_id;

#   Cached hash of NCBI Taxonomy tree, populated by parse_ncbi().
#	Hash key 'children' contains a hash of child tax_id => node,
#	hash key 'parent' is a reference to parent node. 
#
our $ncbi_tree = {};


our $Default_SQL_File = './resources/sql scripts/create_eurocarb_tables.sql';
our $Non_Documenting_Comments1 = qr!/\*[^*].*?\*/!s; # the '/* ... */' style
our $Non_Documenting_Comments2 = qr!--.*$!m;         # the '-- ...' style


##### METHODS #####

#	called implicitly on Perl exit.
sub END
{
	#	close DB handle if still open.
	$Database_Handle->disconnect() if $Database_Handle;
}


=head2  generate_csv

    generate_csv( $filehandle, \%tree, \@hash_keys );

Generates CSV (comma-separated value) text for the given 
hash keys of nodes found in the passed \%tree and feeds 
the output to $filehandle. The CSV is usually then loaded
into a DB table with the load_csv() method.

=cut
sub generate_csv
{
    my( $fh, $tree, $columns ) = @_;
    die "Expected filehandle argument" unless $fh; 
    die "Expected tree reference, got '$tree'" unless ref($tree); 
    die "Expected column/hashkey list reference, got '$columns'" unless ref($columns);
    
    warn "(generating csv of " 
        . @$columns 
        . " column(s))\n"
            if $DEBUGGING;
    
    my $callback = 
        sub 
        {
            my $node = shift;
            print $fh   join ",", 
                        map { defined($_) ? qq!"$_"! : 'NULL' }
                        map { s/"/""/g if defined($_); $_ }
                        map { exists( $node->{$_} ) 
                            ? $node->{$_} 
                            : die "Non-existant hash key '$_' for node " . hdump($node)  
                            }
                        @$columns;
                        
            print $fh "\n";
            return 1;
        };
    
    traverse_tree( $tree, $callback );
    
    return;
}


=head2  calculate_ids

    calculate_ids( \%tree );

=cut
sub calculate_ids
{
    my $tree = shift || die;
    
    warn "(calculating ids)\n" if $DEBUGGING;
    
    my $count = 1;
    
    my $callback =
        sub 
        {
            my $node = shift;
            $node->{db_id} = $count++;
            $node->{parent_db_id} = $node->{parent}->{db_id} || 1;
        };
    
    traverse_tree( $tree, $callback );
    
    return;
}


=head2	calculate_left_right_tree_order

Usage:

	calculate_left_right_tree_order( $tree );

Traverses given tree, calculating a left & right index for each node in 
the tree. That is, each node will have a > 0 value for the hash keys
'left' and 'right' that are indicative of that node's position in the tree.

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


=head2  db_create

    db_create( $directory );

Creates an initial database layout from schema creation sql
files located in $directory.

=cut
sub db_create
{
    my $directory = shift || die;
    
    my $dbh = get_connection();
    
    my @create_sql_files = glob("$directory/create_*.sql");
    
    # TODO
}


=head2	get_connection

	$dbh = get_connection();

Creates/Returns a database connection.

=cut
sub get_connection
{
	return $Database_Handle if $Database_Handle;

	eval 'use DBI'; 
	die "Couldn't dynamically load DB driver: $@" if $@;

	my $dsn = "dbi:$Database_Driver:dbname=$Database_Name:host=127.0.0.1";

	$Database_Handle = DBI->connect( $dsn, $Database_Username, $Database_Password  );

	die "Couldn't establish a connection to database "
		. $Database_Name
		. " using database driver "
		. $Database_Driver
		. " and username "
		. $Database_Username
		. " and password "
		. $Database_Password
		. ": "
		. DBI->err
		unless $Database_Handle;

    #$Database_Handle->{TraceLevel} = $DEBUGGING;

	return $Database_Handle;
}



sub hdump
{
	use Data::Dumper;
	
	local $Data::Dumper::Indent   = 2;
	local $Data::Dumper::Maxdepth = 2;
	
	return Data::Dumper::Dumper( @_ );
}


#	these are the MeSH categories we are interested in.
my %category = (
    A => 	{ 
                id      => 'Anatomy', 
                name    => 'Anatomy', 
                mesh_id => 'Anatomy', 
                parent  => $mesh_tree,
                info    => 'Arbitrary root node for MeSH Anatomy tree',
            },
#	 	B => 	{
#	 				id      => 'Organisms',
#	 				name    => 'Organisms',
#	 				mesh_id => 'Organisms',
#	 				parent  => $mesh_tree,
#                   info    => 'Arbitrary root node for MeSH Organisms tree',
#	 			},
    C => 	{
                id      => 'Diseases',
                name    => 'Diseases',
                mesh_id => 'Diseases',
                parent  => $mesh_tree,
                info    => 'Arbitrary root node for MeSH Diseases tree',
            },
    D => 	{
                id      => 'Chemicals and Drugs',
                name    => 'Chemicals and Drugs',
                mesh_id => 'Chemicals and Drugs',
                parent  => $mesh_tree,
                info    => 'Arbitrary root node for MeSH Chemical and Drugs tree',
            },
);


=head2  parse_ddl

    parse_ddl( $ddl_directory );

Parses the DDL (SQL) files found in $ddl_directory that match
the filename pattern 'create_schema_*.sql'. Populates the 
class variable %schema with the table/column/documentation
found.

=cut
sub parse_ddl   
{
    my $fh = shift || die;
    my @tables;
    my %tables;
    my($table_count, $column_count) = (0, 0);

    # slurp whole file
    local $/; 
    my $text = <$fh>;
    
    # drop '/* */' & '--' comments 
    my $count_comments_removed 
        = ( $text =~ s/$Non_Documenting_Comments1//g )
        + ( $text =~ s/$Non_Documenting_Comments2//g );
    
    warn "(removed $count_comments_removed non-documenting comment(s))\n"
        if $DEBUGGING > 1;
        
    #my $raw_schema_doco = $text;
    $text =~ s!/\*\*+[\s\n]*(.+?)\*+/!!s; 
    my $schema_doco = $1 || die;
    
    
    #   extract raw table info.
    #
    while (
        $text =~ m!   
                    /\*\*+              # an opening '/**'
                    (.+?)               # some documentation
                    \*/                 # a closing '*/'
                    [\s\n]*             # some space or returns
                    CREATE
                    [\s\n]+
                    TABLE
                    [\s\n]+
                    (\w+)               # the table name
                    [\s\n]+
                    \(
                    (.+?)               # table definition           
                    \)[\s\n]*;\s*\n
                !xsg
        )
    {
        $table_count++;
        my( $raw_table_doco, $table_name, $definition ) = ( $1, $2, $3 );
                
        #   remove '*'s at beginning of lines
        $raw_table_doco =~ s/^\s*\*+\s*//gm;
        my $table_doco = $raw_table_doco;
        
        $table_doco =~ s/[\s\n]+/ /g;
        $table_doco =~ s/\s$//;
        
        if ( $DEBUGGING > 3 )
        {
            print "!! table $table_count - name:\n>>>$table_name<<<\n";    
            print "!! table $table_count - doco:\n>>>$table_doco<<<\n";    
            print "!! table $table_count - table definition:\n>>>$definition<<<\n";
        }
    
        #   split table definition statements on blank lines
        my @column_paragraphs = split /\n\s*\n[\s\n]*/, $definition;
        
        if ( $DEBUGGING > 3 )
        {
            print ">>>> table $table_name columns: " 
                . @column_paragraphs 
                . "\n" 
                . join( "", map {">>>$_<<<\n"} @column_paragraphs ) 
                . "\n\n";
        }
        
        #   parse out column info
        my @columns;
        my %columns;
        foreach ( @column_paragraphs )
        {            
            my $raw_column_def = $_;
            
            #   extract column doco
            my $raw_doco = ( s!/\*\*+\s*(.*?)\*+/!!s ) ? $1 : '';
            $raw_doco =~ s!^\s*\*+\s*!!gm;

            my $col_doco = $raw_doco;
            $col_doco =~ s![\n\s]+! !g;
            $col_doco =~ s!\s+$!!g;
            
            #   extract column name
            s!^\W+!!;
            s!\W+$!!;
            my $col_rawname = ( s!^(\w+)\W*!! ) ? $1 : "";
            my $col_name    = $table_name . "." . $col_rawname;
            
            #   extract column type (INT/VARCHAR/etc)
            my $col_type = ( s!^(\w+(?:\(\d+\))?)\W*!! ) ? $1 : "";            
            
            #   remainder of text should be other column options.
            s![\n\s]+! !g;
            my $col_opts = $_; 

            my %column = (
                name => $col_name,
                doco => $col_doco,
                type => $col_type,
                opts => $col_opts,
                rawname => $col_rawname,
                rawdoco => $raw_doco,
                raw_definition => $raw_column_def,
            );

            #   store column in ordered list and hash by name
            $columns{$col_name} = \%column;
            push @columns, \%column;
            $column_count++;
        }
        
        my %table = (
                        name => $table_name,
                        doco => $table_doco,
                        rawdoco => $raw_table_doco,
                        raw_definition => $definition,
                        columnlist => \@columns,
                        columnhash => \%columns,
                        table_number => $table_count,
                    );
    
        push @tables, \%table;
        $tables{$table_name} = \%table;        
    }        
    
    warn "(parsed $table_count table(s), $column_count column(s))\n"
        if $DEBUGGING;
    
    my $schema= { 
                    tablelist => \@tables,
                    tablehash => \%tables,
                    #rawdoco   => $raw_schema_doco,
                    doco      => $schema_doco,
                    name      => 'core' 
                };
    
    # pass by reference.
    $_[0] = $schema;
    
    return;

}


=head2	parse_mesh

	Eurocarb::parse_mesh( $mesh_file );

Parses the open mesh text file. This sub populates $mesh_tree and %mesh_by_id.

=cut
sub parse_mesh
{
	my $mesh_file = shift 
		||	die "Expected a MeSH file argument";

	open( my $fh, $mesh_file )
		||	die "Couldn't open '$mesh_file' for reading: $!";

	### initialisation ###

	# top level hash of tree data structure, declaring an initial root node.
	# the 'children' key is either undef (no children) or a ref to a hash
	# of child nodes keyed by their id. the 'parent' key is a reference to
	# the node's parent.
	#
	%$mesh_tree =	(
				id       => 'root', 
				name     => 'root',
				info     => undef, 
				mesh_id	 => 'root',
				children => undef, 
				tt_id    => 1,
			);	
	
	
	#	make the above categories the parent nodes of their respective trees. 
	$mesh_tree->{children} = \%category; #[ values %category ];
	
	warn "parsing MeSH categories " 
		. join( ", " => map "$category{$_}->{name} ($_)" => sort keys %category )
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

	warn "parsing $mesh_file:\n";# if $DEBUGGING;
			
	#	read an entire paragraph at a time. 
	local $/ = "";
	
	#	read paragraphs.
	while ( <$fh> )
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

                #   there can be multiple, independant mesh id's ('MN' field)
                #   for each record; these are effectively crosslinks.
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
				mesh_id_xlinks  => $mesh_ids, 			# these are the other mesh_ids by which this record is known.
				name 			=> $desc,				# canonical name of this node
				info 			=> $desc_info,			# short description of this node
				uid				=> $desc_uid,			# unique identifier for this node
				position		=> $count_records,		# position of record in file
			);
				
			# 	record in hash of id => node.
			$mesh_by_id{$id} = \%node;
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
	print STDERR "building tree" if $DEBUGGING;
	my $count = 0;
	
	while ( my( $id, $node ) = each %mesh_by_id )
	{
		if ( $id =~ s/\.(\w+?)$// )
		{
            #	it's not a top-level node
			my $partial_id = $1;
			my $parent_node = $mesh_by_id{$id} 
				|| die "Expected a node for mesh_id '$id', but none exists";
				
			$node->{parent} = $parent_node;
			$parent_node->{children}->{$partial_id} = $node;
#            push @{$node->{parent}}, $node;
		} 
		else 
		{
			# 	it's a top-level node
			my $cat_letter = substr( $id, 0, 1 );
			my $parent_node = $category{$cat_letter} || die;
			
			$node->{parent} = $parent_node;
			$parent_node->{children}->{$id} = $node;
#            push @{$node->{parent}}, $node;           
		}

		print STDERR '.' if ($DEBUGGING && ! ++$count % 1000);
	}
	print STDERR "done, $count records\n" if $DEBUGGING;
	
	close( $fh ) || warn $!;

	return;
}


=head2	parse_ncbi

	Eurocarb::parse_ncbi( $names_file, $nodes_file );

Parses the NCBI taxonomy files into memory. See also 'load_taxonomy'.

=cut
sub parse_ncbi
{
	my( $names_file, $nodes_file ) = @_;
	
	parse_ncbi_names_file( $names_file );
	
	parse_ncbi_nodes_file( $nodes_file );
}


=head2	parse_ncbi_names_file

	parse_ncbi_names_file( $names_file );
	
Parses the NCBI Taxonomy 'names.dmp' file.

=cut
sub parse_ncbi_names_file
{    
    my $names_file = shift 
    	|| die "Expected a NCBI names file argument"; 
    
    open( my $fh, $names_file )
        or die "Couldn't open names file '$names_file': $!\n";

    warn "parsing NCBI names file '$names_file':\n";

    my $count = 0;

    while ( <$fh> )
    {
        utf8::upgrade($_);
    
        my( $tax_id,                # the id of node associated with this name
            $name,                  # name itself
            undef, #unneeded - $unique_name, # the unique variant of this name if name not unique
            $name_classification )  # (synonym, common name, scientific name...)
            = split /\s*\|\s*/;
            
        #   names with question marks in them cause errors during SQL insertion,
        #   so we strip them out.
        #my $copy = $name;
        #$name =~ s/[[:^print:]]//g && warn "$copy ---> $name\n";
        warn "\nnot UTF8: '$name'\n" unless utf8::is_utf8( $name );

        if ( $DEBUGGING > 3 )
        {
            chomp;
            print STDERR "\nline $.:\n>>>$_<<<\n";
            print STDERR "tax id: '$tax_id'; "
                        . "name: '$name'; "
                        #. "name-variant: '$unique_name'; "
                        . "name-classification: '$name_classification'\n";
        }
        
        my $node = $ncbi_by_id{$tax_id}        # use existing node 
                || { ncbi_id => $tax_id };     # or a new node if there isn't one
        
        #$name =~ s/\?/�/g;
        
        if ( $name_classification eq 'scientific name' )
        {
            #    scientific name will be used as the 'canonical name'.
            $node->{name} = $name;
        }
        # elsif ( $name_classification eq 'includes' )
        # {
            # push @{$node->{includes}}, $name;
        # }
        else
        {
            #   other observed values for $name_classification may be:
            #   ('synonym', 'equivalent name', 'in-part')
            #   these all get lumped in the 'synonym' bucket.
            $node->{synonyms}->{$name} = $name_classification;
        }
        
        #   add it unless it's been added already
        $ncbi_by_id{$tax_id} = $node
            unless exists $ncbi_by_id{$tax_id};
            
        $count++;
        
        if ( $DEBUGGING <= 3 )
        {
            print STDERR '.' unless $count % 4_000;
            print STDERR "$count\n" unless $count % 100_000;
        }
    }

    print STDERR "done, $count records\n";

    close( $fh ) or warn $!;

    return;
}


=head2	parse_ncbi_nodes_file

	parse_ncbi_nodes_file( $nodes_file );
	
Parses the NCBI Taxonomy 'nodes.dmp' file. Normally called
after parse_ncbi_names_file().

=cut
sub parse_ncbi_nodes_file
{
    my $nodes_file = shift 
    	|| die "Expected a filename";
    
    open( my $fh, $nodes_file ) 
        or die "Couldn't open $nodes_file: $!\n";

    print STDERR "parsing NCBI nodes file '$nodes_file':\n";

    my $count = 0;

    while ( <$fh> )
    {
        my( $tax_id,                # node id in GenBank taxonomy database
            $parent_tax_id,        # parent node id in GenBank taxonomy database
            $rank )                # rank of this node (superkingdom, kingdom, ...) 
            = split /\s*\|\s*/;     # the rest of the fields are ignored
            
        if ( $DEBUGGING > 3 )
        {
            chomp;
            print STDERR "\nline $.:\n>>>$_<<<\n";
            print STDERR "tax id: '$tax_id'; "
                        . "parent-tax-id: '$parent_tax_id'; "
                        . "rank: '$rank'\n";
        }
        
        die "Undefined parent_id for tax_id $tax_id" 
            unless $parent_tax_id;
        
        #    after having parsed the names file, we should already have
        #    all possible tax_id's.
        my $node = $ncbi_by_id{$tax_id}    
            || die "Expected a node for tax id $tax_id"; 
        
        #    simple check to make sure there's only 1 tax_id per line
        #    in the nodes file.
        die "Node $tax_id " . hdump($node) . " has already been processed" 
            if exists $node->{parent};
        
        my $parent = $ncbi_by_id{$parent_tax_id} 
            || die "Expected a node for tax_id $parent_tax_id, the parent of $tax_id";
        
        $node->{parent} = $parent;
        $node->{rank}   = $rank;
        $node->{line}   = $.;
    
            
        #push @{$parent->{children}}, $node;
        $parent->{children}->{$tax_id} = $node;
        
        #   NCBI uses a self-referencing node with tax_id 1 as the 'root' node.
        if ( $tax_id == 1 )
        {
            #   copy into $ncbi_tree. 
            %$ncbi_tree = %$node;
            
            #   and remove self-references (evil).
            delete $node->{children}->{1};
            delete $node->{parent};
        }
        
        $count++;
        
        if ( $DEBUGGING <= 3 )
        {
            print STDERR '.' unless $count % 4_000;
            print STDERR "$count\n" unless $count % 100_000;
        }
    }

    print STDERR "done, $count records\n";
    
    close( $fh ) or warn $!;
       
	return; 
}


sub load_taxonomy
{
	die unless (%$ncbi_tree && %ncbi_by_id);
	
    calculate_ids( $ncbi_tree );
    
	calculate_left_right_tree_order( $ncbi_tree );

	load_table( "core.taxonomy"  => $ncbi_tree, 
				{ 
				    #  hash key  #  column_name  #
				    db_id        => 'taxonomy_id',
				    parent_db_id => 'parent_taxonomy_id',
					ncbi_id      => 'ncbi_id',       
					name         => 'taxon',
					rank         => 'rank',  
				}
	);
	
	load_table( "core.taxonomy_relations" => $ncbi_tree, 
				{
				    #  hash key  #  column_name  #
				    db_id        => 'taxonomy_id',
                    left         => 'left_index',
                    right        => 'right_index',
				}
	);
        
	load_taxonomy_synonyms(); 
	
}


sub load_taxonomy_synonyms
{
    die unless %ncbi_by_id;
    
    my $file = "/tmp/core.taxonomy_synonyms.csv";
    open( my $fh, "> $file") || die $!;
    
    my $syn_id = 1;
    my %already_seen;
    
    my $callback = sub 
    {
        my $entry = shift;
        my $id = $entry->{db_id} 
                    ||  die "DIED ON { " 
                        . join( ", ", map "$_ = $entry->{$_}", keys %$entry ) 
                        . " } ";
                    
        if ( my $synonyms = $entry->{synonyms} )
        {
            foreach my $key ( keys %$synonyms )
            {
                next if $already_seen{$key}++;
                # hash value == type of synonym
                #my $val = $synonyms->{$key};
                $key =~ s/"/""/g;
                
                print $fh qq!$syn_id,$id,"$key"\n!;
                $syn_id++;
            }
        }
        
    };
    
    traverse_tree( $ncbi_tree, $callback );
    
    close $fh || warn $!;
    
    load_csv( 
        "core.taxonomy_synonym", 
        $file, 
        ["taxonomy_synonym_id", "taxonomy_id", "synonym"] 
    );
    
}



=head2	load_tissue_taxonomy	

    load_tissue_taxonomy();
    
=cut
sub load_tissue_taxonomy
{
	my $anatomy_tree = $mesh_tree->{children}->{A} 
		|| die "MeSH anatomy tree not yet populated";
		
    calculate_ids( $anatomy_tree );
    
	calculate_left_right_tree_order( $anatomy_tree );

	load_table( "core.tissue_taxonomy" => $anatomy_tree, 
				{
				    #  hash key  #  column_name  #
				    db_id        => 'tissue_taxonomy_id',
				    parent_db_id => 'parent_tissue_taxonomy_id',
					name         => 'tissue_taxon',
					mesh_id      => 'mesh_id',
					info         => 'description',
				}
	);

	load_table( "core.tissue_taxonomy_relations" => $anatomy_tree, 
				{
				    #  hash key  #  column_name  #
				    db_id        => 'tissue_taxonomy_id',
                    left         => 'left_index',
                    right        => 'right_index',
				}
	);

}


=head2  load_perturbation

    load_perturbation();

=cut
sub load_perturbation
{
	my $perturbation_tree = $mesh_tree->{children}->{D} 
		|| die "MeSH perturbation tree not yet populated";
		
    calculate_ids( $perturbation_tree );
    
	calculate_left_right_tree_order( $perturbation_tree );

	load_table( "core.perturbation" => $perturbation_tree, 
				{   
				    #  hash key  #  column_name  #
                    db_id        => 'perturbation_id',
				    parent_db_id => 'parent_perturbation_id',
					name         => 'perturbation_name',
					mesh_id      => 'mesh_id',  
					info         => 'description', 
				}
	);

	load_table( "core.perturbation_relations" => $perturbation_tree, 
				{
				    #  hash key  #  column_name  #
				    db_id        => 'perturbation_id',
                    left         => 'left_index',
                    right        => 'right_index',
				}
	);

}


sub load_disease
{
	my $disease_tree = $mesh_tree->{children}->{C} 
		|| die "MeSH disease tree not yet populated";
		
    calculate_ids( $disease_tree );
    
	calculate_left_right_tree_order( $disease_tree );

	load_table( "core.disease" => $disease_tree, 
                {   
				    #  hash key  #  column_name  #
                    db_id        => 'disease_id',
                    parent_db_id => 'parent_disease_id',
                    name         => 'disease_name', 
                    mesh_id      => 'mesh_id', 
                    info         => 'description',
                }
	);
	
	load_table( "core.disease_relations" => $disease_tree, 
				{
				    #  hash key  #  column_name  #
				    db_id        => 'disease_id',
                    left         => 'left_index',
                    right        => 'right_index',
				}
	);

}


=head2  load_csv

    load_csv( $table_name, $csv_filename, \@columns );

Loads the CSV in $csv_filename into the database table 
$table_name, using the given \@columns.

=cut
sub load_csv
{
    my( $table_name, $csv_file, $columns ) = @_;
    
    die "Expected a filename as first arg to load_csv" unless $csv_file;
    die "Expected a table name for 2nd arg to load_csv" unless $table_name;
    die "Expected a list ref of columns for 3rd arg to load_csv" unless ref($columns);
    die "CSV file '$csv_file' doesn't exist" unless -f $csv_file;
    
    my $dbh = get_connection();
    
    my $sql = "COPY " 
            . $table_name 
            . " ( " 
            . join(", ", @$columns) 
            . " ) FROM '"
            . $csv_file
            . "' WITH CSV";
    
    warn "(loading $table_name from csv file '"
        . $csv_file
        . "' with " 
        . @$columns 
        . " column(s): "
        . join(", ", @$columns )
        . "\n" 
            if $DEBUGGING;
    
    warn "(load sql is '$sql')\n" if $DEBUGGING;
    
    eval 
    {
        $dbh->do( $sql ) 
            or die "load failed - the SQL statement '$sql' returned false";
    };
    if ( $@ )
    {
        warn "*** caught load error: $@";
        $dbh->rollback();
        
        warn "(transaction rolled back)\n" if $DEBUGGING;
        
        die "$@\n";
    }
    else
    {
        warn "(successfully loaded $csv_file into $table_name)\n"
            if $DEBUGGING;

        $dbh->commit();
        
        warn "(transaction committed)\n" if $DEBUGGING;

        return 1;            
    }
    
}


=head2	load_table

	load_table( $table_name, \%tree, \%hash_key_to_column_name );

=cut
sub load_table
{
	my( $table_name, $tree, $columns ) = @_; 

    my @hash_keys = keys %$columns;
    my @column_names = values %$columns;
    
    my $tmp_file = "/tmp/$table_name.csv";
    warn "(creating temp CSV file '$tmp_file')\n" if $DEBUGGING;
    
    open( my $fh, ">$tmp_file" ) 
        || die "Couldn't open '$tmp_file' for writing: $!";
    
    generate_csv( $fh, $tree, \@hash_keys );

    close($fh) 
        || warn $!;
        
    chmod( 0777, $tmp_file ) 
        || warn "File permissions of $tmp_file not changed!";
    
	return if $CSV_ONLY;

    warn "--- loading $table_name ---\n" if $DEBUGGING;

    delete_all_rows( $table_name );
    
    my $start_time = Time::HiRes::gettimeofday();
    eval
    {
        load_csv( $table_name, $tmp_file, \@column_names );
    };
    
    if ( $@ )
    {
        #   failure
        warn  "load failed - temporary CSV file not deleted: "
            . "check '$tmp_file' for possible errors\n";
            
        die "$@\n";
    }
    else
    {   
        #   success
        warn "(deleting $tmp_file)\n" if $DEBUGGING;
        unlink( $tmp_file ) 
            || warn "Failed to delete '$tmp_file': $!";
            
        my $elapsed = (Time::HiRes::gettimeofday() - $start_time);
        warn sprintf "successfully loaded $table_name in %.1f sec\n" => $elapsed;
    }

    return 1;
}


=head2  load_table_slowly

	load_table_slowly( $table_name, \%tree, \%hash_key_to_column_name );

Does exactly the same thing as load_table(), with the same arguments,
but loads table via sequential INSERTs, rather than the much much faster
LOAD (ie: PostGres 'COPY') mechanism.

=cut
sub load_table_slowly
{
	my( $table_name, $tree, $columns ) = @_; 
	
	my( $schema, $table ) = ($table_name =~ /^(\w+)\.(\w+)$/ );
	
	my $table_id_column = "$table\_id";
	
	my $dbh = get_connection();
	
	calculate_left_right_tree_order( $tree );
	
	warn "inserting into $table_name\n";
	warn "(columns are: " . join(", ", values %$columns ) . ")\n"
	   if $DEBUGGING > 1;
	
	my $sql = "INSERT INTO "
			. $table_name
			. " ( "
			. join(", ", values %$columns )
			. " ) VALUES ( "
			. join(", ", map "?", keys %$columns )
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
    
    
    warn "(setting interrupt handler)\n" if $DEBUGGING > 1;
    local $SIG{INT} = sub { die "Keyboard interrupt" };
    
# 	warn "(deleting all rows from $table_name table)\n";
# 	
# 	my $count_deleted = 
# 		$dbh->do("DELETE FROM $table_name")
# 			||	die "Failed to delete all rows from $table_name table";
# 
# 	warn "deleted $count_deleted rows from $table_name\n";
    delete_all_rows( $table_name, "$table_name\_relations" );

	warn "(reseting sequence generator)\n";
	$dbh->do( "SELECT setval( '$seq_name', 1, false )" )
		||	die "Failed to reset sequence counter '$seq_name'";

	my $count;
	my $last_node;
	
	my $callback = 
		sub
		{
			my $node  = shift;
			my $opts  = shift || die; 
			$last_node = $node;

            my $parent_db_id = $node->{parent} 
                               ?   $node->{parent}->{db_id} || 1 #die "No parent id"
                               :   1;
                               
            $node->{parent_db_id} = $parent_db_id;
            
#            die "no parent id for node " . hdump($node)
#                unless defined $parent_db_id;
				
				
#			warn "    " x $opts->{depth} 
#				. "inserting node $node->{mesh_id} ($node->{name}) with parent tt_id $parent_db_id\n"
#					if $DEBUGGING > 2;
#			
#			$insert_row_sth->execute(	
#				$parent_db_id,
#				$node->{name}, 
#				$node->{mesh_id}, 
#				$node->{info}  
#			) or die "insert failed";							

			my @values = @{$node}{ keys %$columns };
            warn "inserting row: " . join(", ", @values ) . "\n"
                if $DEBUGGING > 2;
                
            $insert_row_sth->execute( @values )
                or die "insert failed";							

			$get_id_sth->execute() 
				or die "get last insert id failed";
			
			my( $db_id ) = $get_id_sth->fetchrow_array();
			die unless defined $db_id;
			
			$node->{db_id} = $db_id;
			
			warn "    " x $opts->{depth} 
				. "inserted node $node->{name} has id $db_id\n"
					if $DEBUGGING > 2;

			$insert_relations_sth->execute( $db_id, $node->{left}, $node->{right} )
				or die "insert relations failed";
			
			++$count;
			print STDERR '.' unless $count % 40;
			print STDERR "$count\n" unless $count % 1000;

			return 1;			
		};
		
	#	print longest length string of each field
	#print join(", ", map "$_: $max{$_}", keys %max ) . "\n";
	#return;
	
	$tree->{db_id} = 1;
	
	eval {  traverse_tree( $tree, $callback );  };
	if ( $@ )
	{
		warn  "\nINSERT ERROR: $@\n"
		    . "the error occurred while inserting node:\n"
			. hdump($last_node)
			;
			
		warn  "\nSQL statements were:"
			. "\n--- insert row --->\n" . $sql
			. "\n--- retrieve last inserted id --->\n" . $get_id_sql
			. "\n--- insert relations row --->\n" . $insert_relations_sql 
			. "\n"
			    if $DEBUGGING > 1;
			
		$dbh->rollback();
		warn "Transaction rolled back due to error/interrupt\n";
		
		exit 1;
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


sub delete_all_rows
{
    my $dbh = get_connection();

    foreach my $table_name ( @_ )
    {
	   warn "(deleting all rows from $table_name table)\n"
	       if $DEBUGGING;
	
        my $count_deleted = 
            $dbh->do("DELETE FROM $table_name")
                ||	die "Failed to delete all rows from $table_name table";
    
        $count_deleted += 0;

        warn "deleted $count_deleted rows from $table_name\n"
            if $DEBUGGING;
    }
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
	{
	   warn "*** deep recursion trapped"; 
	   warn "current node is " . hdump($node) . "\n";
	   
	   exit 1; 
	}
	
	if ( my $children = $node->{children} )
	{
		foreach my $child ( values %$children )
#		foreach my $child ( @$children )
		{
			traverse_tree( $child, %opts, depth => $opts{depth} + 1 );	
		}
	}

	$opts{postorder_callback}->( $node, \%opts ) if exists $opts{postorder_callback};
	
	return;	
}


sub generate_js_tree
{
    my $start_node = shift 
    	|| die "Expected a start node";
 	
 	warn "creating javascript tree...\n";

	eval <<'    ^^^ dynamically loaded stuff ^^^';
	use HTML::ExtendedJsTree;
	use HTML::JsTree;
	use HTML::JsNode;
	
	$HTML::JsTree::Stylesheet = 'css/navtree.css';
	$HTML::JsTree::IMAGE_ROOT = 'resources/images/jstree';
    ^^^ dynamically loaded stuff ^^^

	die "Dynamic loading of JS modules failed: $@" if $@;

        
    my $tree = HTML::JsTree->new( "MeSH" );    
	  
	my %seen;
	my $depth = 0;
	        
	my $recurse; $recurse = 
		sub 
		{
			my( $node, $parent_js_node, $depth ) = @_;
			
			my $id = $node->{mesh_id};
			 
			unless ( $id ) 
			{
				warn "no ID for node: " . hdump( $node );
				$id = "(no id)";
			}
			
			my $name = $node->{name} 
#				|| warn "no name: " 	. join(", ", map "$_ => $node->{$_}", keys %$node);
				||	"(no name)";
			
			#die "already seen $name" if $seen{$name}++;
			
			$depth ||= 0;
			warn "    "x$depth . "$name ($id)\n" if $DEBUGGING > 1;
			
			#	if there are crosslinked ids, then add them to displayed text.
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
		
#			foreach my $child_node ( @{$node->{children}} )
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
		print Dumper( \%tree );
	); 
	die $@ if $@;
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




"end of module";

