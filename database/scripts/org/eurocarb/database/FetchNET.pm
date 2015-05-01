package org::eurocarb::database::FetchNET;
#Author: David R. Damerell
#Email: david@nixbioinf.org (ww: d.damerell@imperial.ac.uk)
#Date: 30/10/2009
#Description: Quick script to fetch the NCBI Entrez Taxonomy (NET) dump files into a choosen directory.

use strict;
use warnings;

use LocalName qw{
  org.eurocarb.util.BaseClass
  org.eurocarb.database.FetchNET
};

our @ISA=BaseClass;

sub new {
  my $class=shift();
  my $self=bless {}, __PACKAGE__;

  return $self;
}

use constant taxDumpLocation => qw{ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdump.tar.gz};

use constant fetchNetARG => qw{outputDirectory};
sub fetchNet {
  my $self=shift();
  my $args=$self->checkArguments($_[0],fetchNetARG);

  my $command='wget '.(taxDumpLocation)." -O $args->{outputDirectory}/taxdump.tar.gz";
  system($command);
  if($? >> 8 !=0){
    die "Error occured running the command $command\n";
  }
  
  my $untarCommand="tar zxvf  $args->{outputDirectory}/taxdump.tar.gz --directory $args->{outputDirectory}";
  system($untarCommand);
  if($? >> 8 !=0){
    die "Error occured running the command $untarCommand\n";
  }
}

&main::start if defined &{'main::start'};
sub staticMain {
  if(@ARGV !=1){
    die "Usage\tDirectory to store NET files within\n";
  }
  my $self=FetchNET->new();
  $self->fetchNet({outputDirectory=>$ARGV[0]});
}