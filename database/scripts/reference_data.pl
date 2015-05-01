#!/usr/bin/perl -w
#
#   name: reference_data.pl
#   author: mjh
#   created: June 2006
#   purpose: 
#   Parses NCBI taxonomy files & populates Eurocarb taxonomy tables as well as
#   MeSH for disease/perturbation/tissue_taxonomy.
#

use lib ('lib', 'lib/darwin-thread-multi-2level');
use Getopt::Long;
use strict;

BEGIN 
{ 
    eval { require Eurocarb; }; 
    if ($@ && $@ =~ /^Can't locate Eurocarb/) 
    { 
        die "Couldn't load library module Eurocarb.pm -- "
        .   "try chdir-ing to the 'scripts' directory before running this script\n"; 
    }
    elsif ($@) { die $@ };
}

##### USAGE #####

my $usage = <<'^^^ USAGE ^^^';

Usage:

    eurocarb [options]
    
Options:
    
    -c, --csv                      Halts database loading at the CSV generation stage for all
                                   "load" options below.

    -d, --dir $dir                 Specifies the path/directory to a Eurocarb installation.

    -h, --help                     Prints this help & exits.

    -i, --install                  Installs the whole of Eurocarb from scratch, using 
                                   all defaults.

    --load-mesh                    Parse the given MeSH 'txt' file, usually named
                                   'mesh2006.txt' or similar, and load all entries
                                   into the appropriate database tables. Equivalent to 
                                   specifying --load-tissue-taxonomy --load-disease --load-perturbation 
                                   Deletes all existing MeSH entries before loading.
                                   
    --load-mesh-disease            Parses MeSH file, but loads only the disease table. 

    --load-mesh-tissue-taxonomy    Parses MeSH file, but loads only the tissue_taxonomy table. 

    --load-mesh-perturbation       Parses MeSH file, but loads only the perturbation table. 
    
    --load-ncbi                    Parse & load the NCBI taxonomy files found in the
                                   directory $ncbi_dir, and load all entries into the
                                   appropriate database tables. Deletes all NCBI entries
                                   before loading.
                                   
    --mesh $mesh_txt_file          Specifies the location of a MeSH file.
                                   Default "$dir/data/mesh/mesh2006.txt".
    
    --ncbi $ncbi_dir               Specifies a directory in which to find NCBI taxonomy *.dmp files.
                                   The taxonomy files of interest are normally named 
                                   'names.dmp' and 'nodes.dmp', but may be changed with
                                   the --ncbi-names & --ncbi-nodes options.
                                   Default "$dir/data/ncbi/current".

    --ncbi-names $names_file       Specifies a 'names.dmp' file explicitly, overriding
                                    the default from the --parse-nbci option.
                                   Default "$ncbi_dir/names.dmp"

    --ncbi-nodes $nodes_file       Specifies a 'nodes.dmp' file explicitly.
                                   Default "$ncbi_dir/nodes.dmp"
                                       
    --userdb $db_name              Name of database to connect to.
    
    --username $user_name          Database user to connect as.

    --userpwd $password            Database user password for $username.

    -v, --verbose                  Increases verbosity. Cumulative.

^^^ USAGE ^^^


##### GLOBAL VARS #####

my $debugging;

#    command-line options
my %opt;



##### MAIN #####

die $usage unless @ARGV;

init_options();

die $usage if $opt{help};

install() && exit if $opt{install};

Eurocarb::create_db( $opt{createdb} ) if $opt{createdb};

Eurocarb::parse_mesh( $opt{mesh} )
    if (   $opt{'load-mesh'}
        || $opt{'load-mesh-disease'}  
        || $opt{'load-mesh-perturbation'}
        || $opt{'load-mesh-tissue-taxonomy'} );

Eurocarb::load_tissue_taxonomy()
    if ( $opt{'load-mesh'} || $opt{'load-mesh-tissue-taxonomy'} );
    
Eurocarb::load_perturbation()
    if ( $opt{'load-mesh'} || $opt{'load-mesh-perturbation'} );

Eurocarb::load_disease()
    if ( $opt{'load-mesh'} || $opt{'load-mesh-disease'} );
   
if ( $opt{'load-ncbi'} )
{
    Eurocarb::parse_ncbi_names_file( $opt{'ncbi-names'} );
    
    Eurocarb::parse_ncbi_nodes_file( $opt{'ncbi-nodes'} );
    
    Eurocarb::load_taxonomy();
}


exit;


##### SUBS #####

sub init_options
{
    #   default options
    %opt = (
    
        #   default place to find eurocarb
        dir =>  ".",

        #   how much info to spam
        verbose     =>  0, 
                
    );
    
    #   add in options from CLI, see also Getopt::Long doco
    Getopt::Long::GetOptions(   
        \%opt, 
        qw/
            csv
            createdb:s
            dir=s
            help
            install                  
            load-mesh                    
            load-mesh-disease
            load-mesh-tissue-taxonomy
            load-mesh-perturbation
            load-ncbi
            mesh=s
            ncbi=s
            ncbi-names=s
            ncbi-nodes=s
            userdb=s
            username=s
            userpwd=s
            verbose+
        / 
    );
    
    die $usage if $opt{help};
    
    $Eurocarb::DEBUGGING = $opt{verbose};
    $debugging = $Eurocarb::DEBUGGING;
    
    $opt{'mesh'}       ||= $opt{dir}  . "/database/data/mesh/mesh2006.txt";
    $opt{'ncbi'}       ||= $opt{dir}  . "/database/data/ncbi/current";
    $opt{'ncbi-names'} ||= $opt{ncbi} . "/names.dmp";
    $opt{'ncbi-nodes'} ||= $opt{ncbi} . "/nodes.dmp";
    
    $Eurocarb::CSV_ONLY = 1 if $opt{csv};

    $Eurocarb::Database_Name     = $opt{'userdb'} if $opt{'userdb'};
    $Eurocarb::Database_Username = $opt{'username'} if $opt{'username'};
    $Eurocarb::Database_Password = $opt{'userpwd'} if $opt{'userpwd'};
    
    if ( $debugging > 1 )
    {
        warn "options:\n", map "    $_ => $opt{$_}\n", sort keys %opt;
    }
    
    return;
}    


sub install
{
    warn "installing...\n";
    
    Eurocarb::parse_mesh( $opt{mesh} );
    Eurocarb::parse_ncbi_names_file( $opt{'ncbi-names'} );    
    Eurocarb::parse_ncbi_nodes_file( $opt{'ncbi-nodes'} );
    
    Eurocarb::load_tissue_taxonomy();  
    
    Eurocarb::load_perturbation();
    
    Eurocarb::load_disease();
  
    Eurocarb::load_taxonomy();

    return;
}




