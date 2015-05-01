package org::eurocarb::database::uniprotkb::UniProtKBParser;

use strict;
use warnings;

$|=1;

use File::Temp qw(tempfile);
use File::chmod;

use LocalName qw{
  org.eurocarb.database.uniprotkb.UniProtKBParser
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

sub generateOxConstrainedFileName {
  my $self=shift();
  
  foreach my $uniprot_dat_file(($self->swissprotFlatFileName(),$self->tremblFlatFileName())){
    print "INFO: Parsing the uniprot dat file $uniprot_dat_file\n";
    open(UNIPROT_DAT,$uniprot_dat_file) || die "Could not open the file $uniprot_dat_file for reading\n";
    while(<UNIPROT_DAT>){
     if(/OX\s+NCBI_TaxID=([0-9]+);/){
	my $netId=$1;
	$self->{netIdToCount}->{$netId}++;
      }
    }
    close(UNIPROT_DAT);
  }

  open(OX_FILE,">".$self->oxFileName()) || die "Could not open the file ".$self->oxFileName()." for writing\n";
  print "INFO: OX file name: ".$self->oxFileName()."\n";
  foreach my $netId(keys %{$self->{netIdToCount}}){
    print OX_FILE "$netId\t$self->{netIdToCount}->{$netId}\n";
  }
  close(OX_FILE);
}


sub parseOxConstrainedFileName {
  my $self=shift();
  print "INFO: Parsing the uniprot dat file ".$self->oxFileName()."\n";
  open(UNIPROT_DAT,$self->oxFileName()) || die "Could not open the file ".$self->oxFileName()." for reading\n";
  while(<UNIPROT_DAT>){
    chomp();
    my ($netId,$count)=split();
    $self->{netIdToCount}->{$netId}=$count;
  }
  close(UNIPROT_DAT);
}

sub populateProteomeSizeRankingTable {
  my $self=shift();

  $self->conn()->do('TRUNCATE TABLE core.Static_taxonomy_proteome_ranking');
  $self->conn()->do('SET search_path=core');
  $self->conn()->do('DROP INDEX IF EXISTS index_static_taxonomy_proteome_ranking_ncbi_id');
  $self->conn()->do('DROP INDEX IF EXISTS index_static_taxonomy_proteome_ranking_rank_pos');
  $self->conn()->do('SET search_path=Public');

  my $fetchParents=$self->conn()->prepare('SELECT ncbi_id_taxa from core.relationships_taxonomy where ncbi_id=?');

  my %netIdToCount; 
  my %skCache;
  foreach my $netId(keys %{$self->{netIdToCount}}){
    my $count=$self->{netIdToCount}->{$netId};
    $fetchParents->execute($netId);
    while(my $parentNetId=$fetchParents->fetchrow_array()){
      $netIdToCount{$parentNetId}+=$count;
    }

    $netIdToCount{$netId}=$count;
  }

  my ($fh,$tempFileName)=tempfile(DIR=>$self->scratchSpace(),UNLINK=>0,OPEN=>1);

  my %countToNetId;
  map {push(@{$countToNetId{$netIdToCount{$_}}},$_)} keys %netIdToCount;
  my @order=sort {$b<=>$a} keys %countToNetId;

  for(my $i=0;$i<@order;$i++){
    my $count=$order[$i];
    map { print $fh "$_~$i\n" } @{$countToNetId{$count}};
  }

  $fh->close();
  
  $fh->close();
  chmod('a+r',$tempFileName) || die "Can't continue, failed to make uniprotkb csv file readable by postgresql\n";
  my $osName=$^O;
  if($osName eq 'MSWin32'){
    system("icacls.exe $tempFileName /grant postgres:(R)");
  }
  print "INFO: Uploading results from file $tempFileName to PostgresSql\n";
  $self->conn()->do('copy core.Static_taxonomy_proteome_ranking from $$'.$tempFileName.'$$ DELIMITERS \'~\' CSV');
  print "Generating indexes\n";
  $self->conn()->do('SET search_path=core');
  $self->conn()->do('CREATE INDEX  index_static_taxonomy_proteome_ranking_ncbi_id on Static_taxonomy_proteome_ranking(ncbi_id)');
  $self->conn()->do('CREATE INDEX  index_static_taxonomy_proteome_ranking_rank_pos on Static_taxonomy_proteome_ranking(rank_pos)');
  $self->conn()->do('SET search_path=Public');
}

&main::start if defined &{'main::start'};
sub staticMain {
  my $self=UniProtKBParser->new();
   if(@ARGV==4){
    $self->scratchSpace($ARGV[2]);
    $self->swissprotFlatFileName($ARGV[0]);
    $self->tremblFlatFileName($ARGV[1]);
    $self->oxFileName($ARGV[3]);
    $self->generateOxConstrainedFileName();
  }elsif(@ARGV==2){
    $self->oxFileName($ARGV[0]);
    $self->scratchSpace($ARGV[1]);
    $self->parseOxConstrainedFileName();
    $self->populateProteomeSizeRankingTable();
  }else{
    die "Usage\tRegenerate ox-constrained-uniprot file\nSwiss-Prot flat file name\n\tTrEMBL flat file name\n\tScratch space\n\tox-constrained-uniprot output file name\n\tParse ox-constrained-uniprot file\n\tScratch space\n";
  }  
}

use constant attributes => qw{scratchSpace conn swissprotFlatFileName tremblFlatFileName oxFileName};
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