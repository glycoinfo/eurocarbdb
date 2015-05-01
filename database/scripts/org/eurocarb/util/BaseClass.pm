package org::eurocarb::util::BaseClass;

use strict;
use warnings;

use Carp ();

use Hash::Util qw(
  hash_seed 
  lock_keys unlock_keys
  lock_value unlock_value
  lock_hash unlock_hash
);

$Carp::Internal{ qw(BaseClass) }++;

BEGIN {
  if( ((defined $ARGV[0] && $ARGV[0] ne 'nostaticmain') || $0=~/\.pm$/ ) && !defined &{main::start}){
    eval qq{
      
      sub main::start {
	open(FILE,"$0") || die "Could not open the file $0 for reading\n";
	my \$packageLine=<FILE>;
	close(FILE);
	if(\$packageLine=~/package\\s+([0-9a-z_:]+)/i){
	  my \$callerClass=\$1;
	  if(defined & {\$callerClass."::staticMain"}){
	    \$callerClass->staticMain();
	  }
	}else{
	}
      }
    };die $@ if $@;
  }
}

# INIT { 
#  main::start() if defined &main::start;
# }



# sub aliasNew {
#   shift() if $_[0] eq 'org::drd20::core::object::BaseClass';
#   my $class=$_[0];
#   my $caller=(caller)[0];
#   if($class ne $caller){
#     no strict qw{refs};
#     return &{$class.'::new'}(@_);
#   }else{
#     return undef;
#   }
# }

sub new {
 return bless( {},shift());
}

sub checkArguments {
  shift() if $_[0] eq __PACKAGE__;
  my $self=shift();
  my @required=@_[1..$#_];

  my $msg;
  if(ref $_[0] ne 'HASH'){
    Carp::cluck("Error you must pass arguments using an anonymous hash\n");
    die "";
  }

  if(scalar @required !=scalar (keys %{$_[0]})){
    my ($package,$fileName,$line,$subroutine)=caller(1);
    my @missingArguments;
    foreach my $key(@required){
      if(!exists $_[0]->{$key}){
        push(@missingArguments,$key);
      }
    }
    Carp::cluck( "Arguments missing to $subroutine\n=>".join("\n=>",@missingArguments)."\n");die "";
  }

  my %arguments;
  eval{
   %arguments=%{$_[0]}; 
   lock_keys(%{$_[0]},@required);
   lock_keys( %arguments,@required);
  };
  if($@){
   my ($package,$fileName,$line,$subroutine)=caller(1);
   Carp::cluck "Illegal argument to $subroutine\nSee lock_keys error for offending argument\n$@\n"; die "";
  }
  return \%arguments;
}

use constant readConfigFileARG => qw{configFile configKeys};
sub readConfigFile {
  my $self=shift();
  my $args=$self->checkArguments($_[0],readConfigFileARG);

  my %configKeys=map{$_=>undef} @{$args->{configKeys}};

  my $matchedAndDefinedKeys=0;
  open(CONFIG,$args->{configFile}) || die "Could not open the file $args->{configFile} for reading\n";
  while(<CONFIG>){
    next if $_=~/^#/;
    chomp();
    if(/([a-z]+)\s+(.+)/i){
      my ($key,$value)=($1,$2);

      die "Key $key specified with no value" if !defined $value;
      if(exists $configKeys{$key}){
        if(defined $configKeys{$key}){
          die "Duplicate key found in config for key $key.\nPlease remove duplicates\n";
        }else{
          $configKeys{$key}=$value;
          $matchedAndDefinedKeys++;
        }
      }else{
        die "Invalid key -$key- from line...\n$_\n";
      }
    }else{
      die "Invalid format of line - offending line below\n $_\n" if $_ ne "";
    }
  }
  close(CONFIG);

  if($matchedAndDefinedKeys != scalar keys %configKeys){
    foreach my $key(@{$args->{configKeys}}){
      if(!defined $configKeys{$key}){
        print "Key - $key - not found in configuration file $args->{configFile}\n";
      }
    }
    die "";
  }

  print "#########\nSpecifications read from the configuration file $args->{configFile}#########\n";
  foreach my $key(@{$args->{configKeys}}){
    print "$key => $configKeys{$key}\n";
    eval qq{\$self->$key(\$configKeys{\$key})}; die $@ if $@; #hack hack, nice to in->once->in->a->while->throw!
  }
  print "#########\n";
}

use constant _writeConfigFileARG => qw{configFileName configKeys};
sub _writeConfigFile {
  my $self=shift();
  my $args=$self->checkArguments($_[0],_writeConfigFileARG);
  
  open(CONFIG_FILE,">$args->{configFileName}") || die "Could not open the file $args->{configFileName} for writing\n";
  foreach my $configKey(@{$args->{configKeys}}){
    print CONFIG_FILE "$configKey\t";
    die "Configuration key $configKey has not been defined\n" if !defined $self->{$configKey};
    print CONFIG_FILE "$self->{$configKey}\n";
  }
  close(CONFIG_FILE);
}

1;

#GRAVEYARD... starts below
# END {
#   if($? >> 8 ==0 && $0=~/\.pm$/){
#     my $script=$0; 
# 
#     #Temp Fix
#     open(FILE,$script) || die "Could not open the file $script for reading\n";
#     my $packageLine=<FILE>;
#     close(FILE);
# 
#     if($packageLine=~/package\s+([0-9a-z_:]+)/i){
#       my $callerClass=$1;
#       if(defined & {$callerClass."::staticMain"}){
# 	no strict;
# 	eval{
# 	    $callerClass->staticMain();
# 	}; die "$@\n" if $@;
#       }
# 
#       
#     }else{
#       return 0;
#     }
#   }
# }


1;
