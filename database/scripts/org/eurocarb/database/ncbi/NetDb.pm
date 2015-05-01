package org::eurocarb::database::ncbi::NetDb;

use strict;
use warnings;

$|=1;

use File::Temp qw(tempfile);
use File::chmod;

use LocalName qw{
  org.eurocarb.database.ncbi.NetDb
  org.eurocarb.database.ncbi.NetNode
  org.eurocarb.util.BaseClass
  org.eurocarb.util.DbUtil
};

our @ISA=BaseClass;

sub new {
  my $class=shift();
  my $self=bless {}, __PACKAGE__;

  $self->_initConnection();

  return $self;
}

sub _initConnection {
  my $self=shift();
  DbUtil->init();
  $self->conn(DbUtil->getConnectionObj());
}

use constant parseNodeTreeARG => qw{nodeTreeFileName};
sub parseNodeTree {
  my $self=shift();

  my $args=$self->checkArguments($_[0],parseNodeTreeARG);
  my $nodeTreeFileName=$args->{nodeTreeFileName};

  open(NODES,$nodeTreeFileName) || die("Could not open NCBI Entrez Taxonomy nodes file $nodeTreeFileName");
  while(<NODES>){
    chomp();
    my @cols=split(/\|/);
    $cols[0]=~s/\s+//g; #tax_id...Remove white space from any where
    $cols[1]=~s/\s+//g; #tax_idParent...Remove white space from any where
    $cols[2]=~s/^\s+\b//g; $cols[2]=~s/\b\s+$//g; #rank..Remove white space before first word, remove white space after last word


    #print "$cols[0], $cols[1], $cols[2],\n";
    if($cols[0] eq $cols[1]){
      $cols[1]=0; #For top of NCBI Entrez Taxonomy set tax_idParent to 0...NCBI Entrez Taxonomy IDs start at 1
    }

    if($cols[2] eq 'subspecies'){
      #print "$cols[0], $cols[1], $cols[2],\n";
      #print "Subspecies\n";
    }

  
    my $node;
    if(!$self->getNode($cols[0])){
      $node=NetNode->new();
    }else{
      $node=$self->getNode($cols[0]);
    }

    
    $node->setTax_id($cols[0]);
    
    $node->setRank($cols[2]);
  
    my $parentNode=$self->getNode($cols[1]);

    if($parentNode==0){ if($cols[1] eq '131567'){
      #print "Adding node\n";
    }
      $parentNode=NetNode->new();
      $parentNode->setTax_id($cols[1]);
      $parentNode->setParentTax_id(undef);
      $parentNode->setRank(undef);

      $self->addNode($parentNode);
    }

    $parentNode->addChildNode($node);

    if($parentNode->getTax_id() eq '131567'){
      #print "131567 =>", $node->getTax_id()," \n";
     # print "Number children...",scalar $parentNode->getChildNodes(),"\n";
    }

    $node->setParentTax_id($parentNode);

    $self->addNode($node);

    #exit(0);
  }close(NODES);
}

sub getNode{
  if(!exists ${$_[0]->{'nodes'}}{$_[1]}){
    return 0;
  }else{
    return ${$_[0]->{'nodes'}}{$_[1]};
  }
}

sub addNode{ 
  ${$_[0]->{'nodes'}}{$_[1]->getTax_id()}=$_[1];
}

sub getPathToRoot {
  my $self=shift();
  my $node=$self->getNode($_[0]);

  if($node==0){
    print  "no path to root for: $_[0]\n";
    return undef;
  }

  my @pathToRoot;
  @pathToRoot=@{$self->pathToRoot($node,\@pathToRoot)};
  pop(@pathToRoot);
  
  return \@pathToRoot;
}

sub pathToRoot{ 
  my $self=shift();
  my $node=shift();
  my @pathToRoot;
  if(defined $_[0]){
    @pathToRoot=@{$_[0]};
  }

  if(defined $node->getPathToRoot()){ 
    return $node->getPathToRoot();
  }

  if($node->isRootNode()){
    $node->setPathToRoot(undef);
    return \@pathToRoot;
  }else{
    my $parent=$node->getParentTax_id();#print "Looping", $node->getParentTax_id(),"\n\n";
    if(ref($parent) ne 'org::eurocarb::database::ncbi::NetNode'){
      print STDERR "Warning ",$node->getTax_id," has an undefined parent?\n";
    }

    push(@pathToRoot,$parent->getTax_id);
    my @selfPathToRoot;
    my $path=$self->pathToRoot($parent,\@selfPathToRoot);
    foreach my $node (@{$path}){
      push(@pathToRoot, $node);
    }
    $node->setPathToRoot(\@pathToRoot);
    return \@pathToRoot;
  }
}

sub createPopulateRelationshipTable {
  my $self=shift();

  print "DEBUG: Creating cached paths to root table\n";
  print "DEBUG: Truncating existing table\n";
  $self->conn()->do('TRUNCATE TABLE core.relationships_taxonomy');

  print "DEBUG: Dropping the indexing for fast upload\n";
  $self->conn()->do('SET search_path=core');
  $self->conn()->do('DROP INDEX IF EXISTS index_relationships_taxonomy_ncbi_id');
  $self->conn()->do('DROP INDEX IF EXISTS index_relationships_taxonomy_rank');
  $self->conn()->do('DROP INDEX IF EXISTS index_relationships_taxonomy_ncbi_id_taxa');
  $self->conn()->do('DROP INDEX IF EXISTS index_relationships_taxonomy_relationship');
  $self->conn()->do('DROP INDEX IF EXISTS index_relationships_taxonomy_position');
  $self->conn()->do('SET search_path=Public');

  print "DEBUG: Generating cached paths CSV table\n";
  my ($fh,$tempFileName)=tempfile(DIR=>$self->scratchSpace(),UNLINK=>0,OPEN=>1);
  foreach my $tax_id (keys %{$self->{'nodes'}}){ 
    my $node=$self->getNode($tax_id);

    my $stringConcat;
    if($tax_id==0){
      next;
    }elsif($tax_id!=1){ 
      my @pathToRoot;
      @pathToRoot=@{$self->pathToRoot($node,\@pathToRoot)};
  
      if(!defined $pathToRoot[0]){
        print STDERR "$tax_id\n";
      }

      pop(@pathToRoot);
      
      my $counter=0;
      foreach my $tax_id (@pathToRoot){
        my $parentNode=$self->getNode($tax_id);
        eval{
	  print $fh "".$node->getTax_id().'~'.$node->getRank().'~'.$tax_id.'~'.$parentNode->getRank().'~'.$counter."\n";
        };if($@){
          die "$@\nUnable to insert path to root for\n";
        }
        $counter++;
      }
    }
  }
  $fh->close();
  chmod('a+r',$tempFileName) || die "Can't continue, failed to make uniprotkb csv file readable by postgresql\n";

  print "DEBUG: Uploading results to PostgresSql\n";  
  $self->conn()->do('copy core.relationships_taxonomy from \''."$tempFileName".'\' DELIMITERS \'~\' CSV');

  print "DEBUG: Recreating indexes\n";
  $self->conn()->do('CREATE INDEX  index_relationships_taxonomy_ncbi_id on core.relationships_taxonomy (ncbi_id)');
  $self->conn()->do('CREATE INDEX  index_relationships_taxonomy_rank on core.relationships_taxonomy (rank)');
  $self->conn()->do('CREATE INDEX  index_relationships_taxonomy_ncbi_id_taxa on core.relationships_taxonomy (ncbi_id_taxa)');
  $self->conn()->do('CREATE INDEX  index_relationships_taxonomy_relationship on core.relationships_taxonomy (relationship)');
  $self->conn()->do('CREATE INDEX  index_relationships_taxonomy_position on core.relationships_taxonomy (position)');
}

&main::start if defined &{'main::start'};
sub staticMain {
  if(@ARGV !=2){
    die "Usage\tNodes file\n\tScratch space\n";
  }

  my $self=NetDb->new();
  print "DEBUG: Parsing NET into tree structure\n";
  $self->parseNodeTree({nodeTreeFileName=>$ARGV[0]});
  $self->scratchSpace($ARGV[1]);
  $self->createPopulateRelationshipTable();
}

use constant attributes => qw{scratchSpace conn};
BEGIN {
  foreach my $attribute(attributes){
    eval qq {
      sub $attribute {
	my \$self=shift();
	if(defined \$_[0]){
	  \$self->{$attribute}=\$_[0];
	}
	return \$self->{$attribute};
      }
    }; die $@ if $@;
  }
}