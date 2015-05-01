#!/usr/bin/perl -w

use warnings;
use strict;
use File::Copy;

if(@ARGV!=1){
  die "Usage\trelease or test?";
}

my $release;

if($ARGV[0] eq 'release'){
 $release=1;
}else{
 $release=0;
}

`sh fix_carriage_return.sh`;

my $serverDir='/opt/server/gwb.nixbioinf.org';
my $lastBuildNo="$serverDir/current_build_no";
my $packagesDir='packages';

`rm $packagesDir -rf`;

my $majorVersion=2;
my $minorVersion=0;
my $state="alpha";
my $topLevelDir="$serverDir/$majorVersion/$minorVersion";
my $buildDir="$serverDir/build";

if(!-e $buildDir){
	`mkdir $buildDir`;
}

my $currentReleaseLink="$serverDir/current_version";

my $buildNo=0;

if(-f $lastBuildNo){
	open(BUILD_NO,"$lastBuildNo") || die "Could not open the file $lastBuildNo for reading\n";
	$buildNo=<BUILD_NO>; chomp($buildNo);
	if(!$buildNo=~/^([0-9]+)$/){
		die "Could not determine the last build number from file $lastBuildNo --$buildNo--\n";
	}
	close(BUILD_NO);
}

$buildNo++;

#Replace all macros with their corresponding values within the given list of files. (restored after build)
my @replaceInFiles=('winInstaller.nsi','src/html/about_gwb.html','README.txt');
foreach my $file(@replaceInFiles){
	`sed 's/GWB_MAJOR/$majorVersion/g;s/GWB_MINOR/$minorVersion/g;s/GWB_STATE/$state/g;s/GWB_BUILD/$buildNo/g' $file > tmp.out`;
	 copy($file,"$file.bak") || onDie("Error copying file\n");
	 copy('tmp.out',$file) || onDie("Error copying file\n");
}

#Open readme file and create an in memory version of the file with \n replaced with \r\n 
my $dosLineBrakedReadMeFile="";
open(README_FILE,"README.txt") || onDie("Could not open the file README.txt for reading");
while(<README_FILE>){
	my $line=$_;
	if(!($line=~/\r/)){
		$line=~s/\n/\r\n/g; 
	}
	$dosLineBrakedReadMeFile.=$line;
}
close(README_FILE);

#Write out \r\n readme to a file the build script knows about.
open(WIN_README,">WIN_README.txt") || onDie("Could not open the file WIN_README.txt for writing");
print WIN_README $dosLineBrakedReadMeFile;
close(WIN_README);

system('ant clean');
system('ant dist-all');
#`ant dist-lin`;

my $buildPublishDir="$buildDir/$buildNo";
my $versionLink="$topLevelDir/$buildNo";

if(!-f $buildPublishDir){
	system("mkdir -p $buildPublishDir");
	if($? >> 8 !=0){
		onDie("Could not create the directory $topLevelDir\n");
	}
}else{
	`rm -rf $buildPublishDir/*`
}

system("cp -r $packagesDir/* $buildPublishDir");



`echo "$buildNo" > $lastBuildNo`;
if(!-e $versionLink){
	system("ln -s $buildPublishDir $versionLink");
}

if($release){
 print "About to release new version? (Ctrl+C in the next 10 seconds to quit)";
 sleep(10);
if(-e $currentReleaseLink){
   unlink($currentReleaseLink);
 }
 system("ln -s $buildPublishDir $currentReleaseLink");
 
}

onExit();

sub onDie {
	my @args=@_;
	onExit();
	die @args;
}

sub onExit {
	foreach my $file(@replaceInFiles){
		copy("$file.bak",$file) || print "Could not restore backup file $file, continuing with restore attempt\n";
	}
}
