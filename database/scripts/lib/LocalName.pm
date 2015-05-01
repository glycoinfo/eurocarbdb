package LocalName;

use lib "scripts/";

use strict qw/subs vars/;
sub import {
  my @imports=@_;
  foreach (reverse @imports){
    my $pkg=$_; my $als;
    $pkg=~s/\./::/g;
    if($pkg=~/::([a-z_0-9]+)$/i){
      $als=$1;
    }else{
      next;
    }
    my $clr = (caller)[0];
    die "als not defined: $pkg" if !defined $als;
    require join( '/', split '::', $pkg ) . '.pm' unless defined %{$pkg.'::'};
    *{$clr.'::'.$als}=sub {return $pkg};
    #print "$clr => $als\n";
  }
}


1;
