package org::eurocarb::database::ncbi::NetNode;

use strict;

sub new{
  my $class=shift();
  my $self={version=>'0.1'};

  return bless $self, __PACKAGE__;
}

sub setTax_id{
  $_[0]->{'tax_id'}=$_[1];
}

sub getTax_id{
  return $_[0]->{'tax_id'};
}

sub setRank{
  $_[0]->{'rank'}=$_[1];
}

sub getRank{
  return $_[0]->{'rank'};
}

sub setParentTax_id{
  $_[0]->{'tax_idParent'}=$_[1];
}

sub getParentTax_id{
  return $_[0]->{'tax_idParent'};
}

sub addChildNode{
  push(@{$_[0]->{'childNodes'}},$_[1]);
}

sub getChildNodes{
  return @{$_[0]->{'childNodes'}};
}

sub setPathToRoot{
  $_[0]->{'pathToRoot'}=$_[1];
}

sub getPathToRoot{
  return $_[0]->{'pathToRoot'};
}


sub isLeafNode{
  if(!defined $_[0]->{'childNodes'} || !@{$_[0]->{'childNodes'}} ){
    return 1;
  }else{
    return 0;
  }
}

sub isRootNode{
  if(!defined $_[0]->{'tax_idParent'}){
    return 1;
  }else{
    return 0;
  }
}

1;