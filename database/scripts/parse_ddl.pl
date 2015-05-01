#!/usr/bin/perl -w

use strict;
use Getopt::Long;

use lib 'lib';

use HTML::ExtendedJsTree;
use HTML::JsTree;
use HTML::JsNode;

our $USAGE = <<'^^^USAGE^^^';

    parse_ddl.pl -- a utility for parsing (Postgres) data definition language (DDL) files.
    
USAGE

    parse_ddl.pl [options] <ddl-file>

OPTIONS    
        
    -d, --destination <file-name>
    
    -e, --eval                      Start a simple CLI shell after parsing DDL.
    
    --generateclass                 Generate a simple Java class per SQL table 
                                    with get/set methods for each table column.
                                    
    --generatecomments              Extract java-style comments from the DDL file 
                                    and generate an SQL script to add those comments
                                    to an SQL DB based on the given DDL.
                                        
    --generateindexes               Auto-generate some SQL create index statements
                                    based on the given DDL, using some basic heuristics.
                                        
    -h, --html                      Generate some HTML for the given DDL.

    -p, --package                   Java package to use in combination with --generateclass. 
                                    
    -v, --verbose                   Print processing & debugging information. Cumulative.
        
AUTHOR

    matt harrison 2005
        
^^^USAGE^^^

die $USAGE unless @ARGV;

$HTML::JsTree::Stylesheet = 'css/navtree.css';
$HTML::JsTree::IMAGE_ROOT = 'images/jstree';

our $DDL_File;

#   where to find (default) schema/db creation file.
our $Default_SQL_File = './sql/create_schema_core.sql';

our $Non_Documenting_Comments1 = qr!/\*[^*].*?\*/!s; # the '/* ... */' style
our $Non_Documenting_Comments2 = qr!--.*$!m;         # the '-- ...' style

#   simple text templates found at EOF.
our %Template;

our %typemap = (
    int         =>  'int',
    serial      =>  'int',
    timestamp   =>  'Date',
    text        =>  'String',
    varchar     =>  'String',
    numeric     =>  'double',
    smallint    =>  'int',
);


our %options;

init_options();

main();

exit;

sub init_options
{
    #   default options
    %options = (
    
        #   how much info to spam
        verbose     =>  0, 
        
        #   where to put generated code/sql
        destination =>  ".",
        
        #   package namespace for auto-generated java.
        package     =>  "eurocarb.dataaccess.core",
    );
    
    #   add in options from CLI, see also Getopt::Long doco
    Getopt::Long::GetOptions(   \%options, 
                                qw/
                                    destination:s
                                    eval
                                    generateclass
                                    generatecomments
                                    generateindexes
                                    html=s
                                    package=s
                                    verbose+
                                / 
    );
    
}    

sub main
{
    $DDL_File = shift @ARGV || $Default_SQL_File;
    
    warn "(parsing $DDL_File)\n" if $options{verbose};
    
    open( my $ddl_fh, $DDL_File )
        or die "Couldn't open DDL file '$DDL_File': $!";

    parse_ddl( $ddl_fh, my $schema );
    
    if ( $options{verbose} > 2 )
    {
        eval "use Data::Dumper";
        print Dumper( $schema );
    }

    close( $ddl_fh );
    
    if ( $options{'eval'} )
    {
        warn "- entering CLI mode -\n";
        for ( print "\n > "; <>; print "\n > " )
        {
            chomp;
            last if ( /^quit/ or /^done/ );
            
            print "=> "; 
            print Dumper( eval );
            warn "!! " . $@ if $@;
        }
    }
    
    generate_comment_sql_script( $schema ) 
        if $options{generatecomments};
        
    generate_sql_doco( $schema ) 
        if $options{html};
        
    generate_classfile( $schema ) 
        if $options{generateclass};
    
    generate_indexes( $schema )
        if $options{generateindexes};
    
    return;
}


#   parse_ddl( $filehandle, \@tables, \%tables )
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
        if $options{verbose} > 1;
        
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
                    (\w+)               # the schema
                    \.
                    (\w+)               # the table name
                    [\s\n]+
                    \(
                    (.+?)               # table definition           
                    \)[\s\n]*;\s*\n
                !xsg
        )
    {
        $table_count++;
        my( $raw_table_doco, $schema, $table_name, $definition ) = ( $1, $2, $3, $4 );
                
        #   remove '*'s at beginning of lines
        $raw_table_doco =~ s/^\s*\*+\s*//gm;
        my $table_doco = $raw_table_doco;
        
        $table_doco =~ s/[\s\n]+/ /g;
        $table_doco =~ s/\s$//;
        
        $table_name = "$schema.$table_name" if ( $schema );
        
        if ( $options{verbose} > 3 )
        {
            print "!! table $table_count - name:\n>>>$table_name<<<\n";    
            print "!! table $table_count - doco:\n>>>$table_doco<<<\n";    
            print "!! table $table_count - table definition:\n>>>$definition<<<\n";
        }
    
        #   split table definition statements on blank lines
        my @column_paragraphs = split /\n\s*\n[\s\n]*/, $definition;
        
        if ( $options{verbose} > 3 )
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
            next if (  lc($col_rawname) eq 'unique' 
                    || lc($col_rawname) eq 'check' );
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
                        schema => $schema,
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
        if $options{verbose};
    
    my $table_schema = { 
        tablelist => \@tables,
        tablehash => \%tables,
        #rawdoco   => $raw_schema_doco,
        doco      => $schema_doco,
        name      => 'core' 
    };
    
    # pass by reference.
    $_[0] = $table_schema;
    
    return;

}
 

sub generate_comment_sql_script    #  ( \%schema )
{
    my $schema = shift || die;
    my $count_comments = 0;
    
    # schema doc
    print "-- schema comment --\n";
    print comment_sql( schema => $schema );
    $count_comments++;
    
    # table doc
    my $tables = $schema->{tablelist} || die;
    
    foreach my $table ( @$tables )
    {
        print "\n\n-- table $table->{name} --\n";
        print comment_sql( table => $table );
        $count_comments++;
        
        print "\n-- columns for table $table->{name} --\n";
        my $columns = $table->{columnlist} || die;
        foreach my $column ( @$columns )
        {
            print comment_sql( column => $column );    
            $count_comments++;
        }
        
    } 
    print "\n";
    
    warn "($count_comments comment(s) generated)\n" 
        if $options{verbose};
    
    return;    
}

sub comment_sql    #  ( $object_type => \%hash )
{
    my $object_type = shift || die;
    my $hashref = shift || die;
    
    my $doc = $hashref->{doco} || '';
    $doc =~ s/\'/\\'/g;
    
    my $name = $hashref->{name} || die;
    
    return  "COMMENT ON " . uc($object_type) . " $name\nIS '$doc';\n";
}


sub generate_classfile
{
    my $schema = shift || die;

    init_templates();
    
#    foreach my $t ( keys %template )
#    {
#        print ">>> template $t:\n", $Template{$t}, "\n";
#    }
#    return;

    my $src_dir = $options{destination} || '.';
    warn "--- autogenerated code being placed into '$src_dir'\n";

    my $tables = $schema->{tablelist} || die;
    
    foreach my $table ( @$tables )
    {
        do { warn "(skipping table '$table->{name}')\n"; next; } 
            if ( $table->{name} =~ /_to_/i );
        
        warn "--- making classfile for table '$table->{name}' ---\n"
            if $options{verbose} > 1;
  
        my $package_name = $options{"package"} || die;
        my $package_dir  = join '\\', split /\./, $package_name;
                
        my $class_name = ucfirst( $table->{name} );
        $class_name =~ s/_(\w)/ucfirst($1)/eg;

        my $class_file = join '\\', $src_dir, $package_dir, "$class_name.java";
        open my $fh, ">$class_file" or die "Failed to create '$class_file': $!";
        warn "*** creating $class_file\n" if defined $options{verbose};
        
        my $PACKAGE     = $package_name;
        my $CLASS_NAME  = $class_name;
        my $TABLE_NAME  = $table->{name};
        
        my $primary_key_col = $table->{columnlist}->[0]->{rawname} || die;
        $primary_key_col    =~ s/_(\w)/ucfirst($1)/ge;
        my $GET_PRIMARY_KEY = "get" . ucfirst($primary_key_col);


        eval qq!  print \$fh "$Template{CLASS_HEADER}"  !;
        die "error: $@" if $@;
        
        my $columns = $table->{columnlist} || die;
        
        #   generate object fields
        #
        foreach my $column ( @$columns )
        {
            my $property_name = $column->{rawname};
            $property_name =~ s/_(\w)/ucfirst($1)/ge;

            my $doco = $column->{rawdoco};
            my $type = typemap_sql_to_java( $column->{type} );
            
            print $fh "    /** $doco*/\n";
            print $fh "    private $type $property_name;\n\n";       
        }
        
        #   generate methods
        #
        foreach my $column ( @$columns )
        {
            my $property_name = $column->{rawname};
            $property_name =~ s/_(\w)/ucfirst($1)/ge;
            
            my $get_method_name = "get" . ucfirst($property_name);
            my $set_method_name = "set" . ucfirst($property_name);
            
            my $type = typemap_sql_to_java( $column->{type} );
            
            warn "... making get method for property '$property_name' (type $type)\n"
                if $options{verbose} > 1;
            
            my $TYPE        = $type;
            my $FIELD       = $property_name;
            my $METHOD_NAME = $get_method_name;
            my $VAR_NAME    = substr( $property_name, 0, 1 );
            my $ASTERISKS   = '*' x (66 - length($METHOD_NAME));
            
            eval qq!  print \$fh "$Template{OBJECT_GET_METHOD}"  !;
            die "error: $@" if $@;
            
            
            $METHOD_NAME    = $set_method_name;
            $ASTERISKS      = '*' x (66 - length($METHOD_NAME));
            
            eval qq!  print \$fh "$Template{OBJECT_SET_METHOD}"  !;
            die "error: $@" if $@;

        }
        
        eval qq!  print \$fh "$Template{CLASS_FOOTER}"  !;
        die "error: $@" if $@;   
        
        close($fh);     
    } 
}


sub init_templates
{
    local $/;
    my $template_text = <DATA> || die;
    my @chunks = grep /\w/, split /__(\w+?)__/s, $template_text;
    
    
    
    die "Problem encountered parsing templates - "
    	. "uneven number of sections obtained  ->\n" 
    	. join("\n", map ">>>>>$_<<<<<", @chunks )
    	. "\n"
    	. @chunks 
    	. " section(s)\n"
    	. "^^^ Problem encountered parsing templates - "
    	. "uneven number of sections obtained ^^^\n" 
    	if @chunks & 1;
    
    %Template = @chunks;
    
    warn "parsed " 
        . keys(%Template) 
        . " code template(s): "
        . join(", ", keys(%Template)) . "\n"
            if $options{verbose};

    return;
}


sub typemap_sql_to_java
{
    my $sql_type = lc( $_[0] );
    return "String" if $sql_type =~ /varchar/;
    
    warn "!!! undefined type '$sql_type'" 
        unless exists $typemap{"$sql_type"};
    
    return $typemap{$sql_type} || $sql_type;
}


sub generate_sql_doco     #( \%schema )
{
    my $schema = shift || die;
    
    init_templates();
    
    my $tables = $schema->{tablelist} || die;

    my $file = $options{html};
    open my $fh, ">$file" or die "Couldn't open '$file' for writing: $!";
    
    my $tree = HTML::JsTree->new( "Eurocarb DB" );    
        
    my $schema_name = $schema->{name} || die;
    my $id = "schema_$schema_name";
    my $doco = markup_doco( $schema->{doco} );
    my $name = "schema: $schema_name";
    my $title = "schema: $schema_name";
    my $definition = "";

	#	suppress warnings about concatenation of undef strings 
	local $^W;

    my $main_doco_content = eval qq!qq|$Template{HTML_DOCO_PARAGRAPH}|!;;
    
    my $core_schema_node = HTML::JsNode->new( 
        id      => "treenode_$id", 
        value   => qq!<a href="javascript:void()"!
                .  qq! onclick="toggle_vis('$id')">$name</a>! 
    );
    
    $tree->add( $core_schema_node );

    foreach my $table ( @$tables )
    {        
        $name = $table->{name};
        $id   = "table_$name";
        $doco = markup_doco( $table->{rawdoco} ); 
        $title = "table $name";
        $definition = $table->{raw_definition};
        
        my $table_node = HTML::JsNode->new(                 
            id      => "treenode_$id", 
            value   =>  qq!<a href="javascript:void()"!
                    .   qq! onclick="toggle_vis('$id')">$name</a>!  
        );
        $tree->add( $table_node, $core_schema_node );
       		
        $main_doco_content .= eval qq!qq|$Template{HTML_DOCO_PARAGRAPH}|!;

        my $columns = $table->{columnlist} || die;
        
        foreach my $column ( @$columns )
        {
            $name = $column->{rawname};
            $id   = "column_$name";
            $doco = markup_doco( $column->{rawdoco} );
            $title = $column->{name};
            $definition = "$column->{type} $column->{opts}";
            
            my $col_node = HTML::JsNode->new( 
                id      => "treenode_$id", 
                value   =>  qq!<a href="javascript:void()"!
                        .   qq! title="$column->{type}"!
                        .   qq! onclick="toggle_vis('$id')">$name</a>! 
            );
            $tree->add( $col_node, $table_node );
    
            $main_doco_content .= eval qq!qq|$Template{HTML_DOCO_PARAGRAPH}|!;
        }
    }
    
    eval "print \$fh qq|$Template{HTML_DOCO_HEADER}|";
    die $@ if $@;
    
    print $fh $tree->as_string();
        
    eval "print \$fh qq|$Template{HTML_DOCO_FOOTER}|";
    die $@ if $@;
    
    close( $fh );
}


sub markup_doco
{
    my $doco = $_[0] 
        || return "<p>(no documentation exists for this object)</p>";
    
    $doco =~ s{ \*(\w.+?)\b\*         }
              { "<strong>$1</strong>" }gex;
    
    $doco =~ s{ \b_(\w+?)_\b  }
              { "<em>$1</em>" }gex;
              
    $doco =~ s{ \#(\w.+?)\b\#     }
              { "<code>$1</code>" }gex;

    $doco =~ s{ \n?=head(\d)\s+(.+?)\n           }
#              { my $i = $1 + 1; "<h$i>$2</h$i>" }gex;
              { "<h3>$2</h3>\n" }gex;

    $doco =~  s{ (\n-[\s\t]+.+?(?=\n\n)) }
                    {
                        my @items = split /\n-[\s\t]+/, $1;
                        shift @items;

                        "\n<ul>\n"
                        .   join( "", map {"    <li>$_</li>\n"} @items )
                        .   "</ul>\n";

                    }gexs;

    #   ordered lists
    $doco =~  s{ (\n\+[\s\t]+.+?(?=\n\n)) }
                    {
                        my @items = split /\n\n?-[\s\t]+/, $1;
                        shift @items;

                        "\n<ol>\n"
                        .   join( "", map {"    <li>$_</li>\n"} @items )
                        .   "</ol>\n";

                    }gexs;

    return join '', map "<p>$_</p>", split /\n(?:\s*\n)+/, $doco; 
}


sub generate_indexes
{
	my $schema = shift || die;

    init_templates();

	warn "(generating index file)\n" if $options{verbose};
	print "/* Autogenerated index file from '$DDL_File' */\n";

	my @indexes;
	
	my $schema_name = $schema->{name};    
    my $tables      = $schema->{tablelist} || die;

    foreach my $table ( @$tables )
    {     
		my $table_name = $schema->{name} . "." . $table->{name};
        my $columns    = $table->{columnlist} || die;
        
		print "\n----- indexes for table $table_name -----\n";

        foreach my $col ( @$columns )
        {
        	next unless ( $col->{opts} =~ /PRIMARY KEY/   # a primary key
   					||    $col->{opts} =~ /REFERENCES/    # or a foreign key
   					||    $col->{opts} =~ /UNIQUE/ );     # or a column declared unique 
   					     	    			
        	my $key_type = ($col->{opts} =~ /PRIMARY KEY/) 
        					? 'primary key' 
        					: ($col->{opts} =~ /REFERENCES/)
        						? 'foreign key'
        						: 'unique column';
        						
			my $index_type = $key_type eq 'foreign key' ? '' : 'UNIQUE';

			my $column_name = $col->{rawname};
			#my $index_name  = "index_" . $table->{name}  . "_" . $column_name; 
 
 			#	skip indexes for join table primary key columns
 			#	(they are the only ones that have '_to_' in their column name).
 			if ( $column_name =~ /_to_/ )
 			{
 				warn "(skipping index for column $column_name -- not needed)\n"
 					if $options{verbose};
 				next;
 			}
 			
 			#	shorten table names to an acronym, because some of the 
 			#	index names are too long otherwise.
			my $shortened_table_name = join "_", 
										map { substr($_, 0, 3) } 
										split /_/, $table->{name};
			
			my $index_name = "index_$shortened_table_name\__$column_name";
 
	        warn "(generating index $index_name for table $table_name)\n"
	        	if $options{verbose};   
    
			#	suppress warnings about concatenation of undef strings 
			local $^W;
		
		    eval "print qq!$Template{SQL_INDEX}!";
		    die $@ if $@;
		    
		    push @indexes, $index_name;
		}
		    
    }
    
    my $file = "delete_indexes.sql";
	open( my $fh, "> $file" )
		||	die "Couldn't open '$file' for writing: $!";
		
	foreach my $index ( @indexes )
	{
		print $fh "DROP INDEX $index;\n";
	}
	
	close( $fh ) || warn $!;
	
	return;
}

__END__
__CLASS_HEADER__

package $PACKAGE;

import java.util.Date;
import java.io.Serializable;

/** 
*   Data object representing the $TABLE_NAME table.
*
*   This class has been autogenerated.
*
*   \@author mjh
*/
public class $CLASS_NAME extends AbstractDataObject implements Serializable
{
    /** Arbitrary but obligatory field for java serialisation. */
    private static final long serialVersionUID = 1L;


    /** No argument constructor required for dynamic instantiation */
    public $CLASS_NAME() {}


__CLASS_FOOTER__

    /*  overridden  */   
    public String getType()
    {
        return \"$TABLE_NAME\";
    }

    /*  overridden  */   
    public int getId()
    {
        return this.$GET_PRIMARY_KEY();
    }
    

} // end class


__OBJECT_GET_METHOD__

    /*  $METHOD_NAME  *//*$ASTERISKS
    *
    */
    public $TYPE $METHOD_NAME()
    {
        return this.$FIELD;
    }


__OBJECT_SET_METHOD__

    /*  $METHOD_NAME  *//*$ASTERISKS
    *
    */
    public void $METHOD_NAME( $TYPE $VAR_NAME )
    {
        if ( this.$FIELD == $VAR_NAME ) return;
        
        this.propertyChanged(\"$FIELD\");
        this.$FIELD = $VAR_NAME;
        return;
    }


__HTML_DOCO_HEADER__
<html>
<head>
<style>
    #head 
    {
        position: absolute;
        left: 0px;
        top: 0px;
        margin: 10px;
    }

    #navigation 
    {
        position: absolute;
        left: 0px;
        width: 300px;
        top: 100px;
        margin-left: 10px;
    }
                
    #main       
    {
        position: absolute;
        left: 300px;
        top: 100px;
        margin-left: 10px;
        margin-right: 10px;
    }
    
    .initial_paragraph_state
    {
        display: none;
    }
    
    body, * { font: "Trebuchet MS", "Verdana", "sans-serif"; text-align: justify; }
    
    h1 { font-variant: small-caps; font-size: 16pt; }
    h2 { font-size: 14pt; font-weight: normal; }
    h3 { font-size: 12pt; text-decoration: underline; font-weight: normal; }
    p, pre { font-size: small; color: #333; }
    
</style>
<script>

    var last_element;
    
    function toggle_vis( name )
    {
        var element = document.getElementById( name );
        if ( element == null ) 
            alert("element \'" + name + "\' doesn't exist!");
        
        if ( last_element != null )        
            last_element.style.display = "none";
        
        element.style.display = "block";
        last_element = element;            
                                   
        return true;
    }
    
</script>

</head>
<body>
<div id="navigation">


__HTML_DOCO_FOOTER__


</div>

<div id="main">
$main_doco_content
</div>

<div id="head">
<h1>Schema documentation</h1>
</div>

</body>
</html>


__HTML_DOCO_PARAGRAPH__

<div id="$id" class="initial_paragraph_state">
<h2>$title</h2>
$doco
<h3>Definition</h3>
<pre>
$definition
</pre>
</div>


__SQL_INDEX__
/** Index for $table_name.$column_name ($key_type) */
CREATE $index_type INDEX $index_name ON $table_name ( $column_name )
;
