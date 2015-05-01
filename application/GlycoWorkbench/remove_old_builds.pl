#!/usr/bin/perl -w

if(@ARGV == 0){
	die "Usage\t[keep:1..2]|[remove:2..3]\n";
}

my $buildDir='/opt/server/gwb.nixbioinf.org/build';

my %requests;

foreach my $arg(@ARGV){
	if($arg=~/\[((?:keep)|(?:remove)):([0-9]+)\.\.([0-9]+)\]/){
		my $key=$1;
		my $from=$2;
		my $to=$3;
		for(my $i=$from;$i<=$to;$i++){
			$request{$key}->{$i}=1;
		}
	}elsif($arg=~/\[((?:keep)|(?:remove)):([0-9]+)/){
		$request{$1}->{$2}=1;
	}
}

foreach my $removeItem(keys %{$requests{'remove'}}){
	if(exists $requests{'keep'}->{$removeItem}){
		delete $requests{'remove'}->{$removeItem};
	}
}

print "Builds that are not removed or kept explicitly are not deleted\n";
foreach my $key(keys %requests){
	my @items=keys %{$requests{$key}};
	print "$key\[";

	if(@items>1){
		print join(",",@items);
	}else{
		print $items[0];
	}
	print "]\n";
}

print "Press Ctrl+C if you wish to abort in the next ten seconds\n";
sleep(10);
foreach my $remove(keys %{$requests{'remove'}}){
	my $pathToRemove="$buildDir/$remove";
	print "Removing $pathToRemove\n";
	`rm -r $pathToRemove`;
}