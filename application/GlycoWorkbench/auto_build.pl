#/usr/bin/perl -w

use warnings;
use strict;
use File::Copy;
use Cwd;
    
my $workingDir = getcwd;

my $swtJarDirectory='../../external-libs/java/prefetched';
my $x86Java='C:\Program Files (x86)\Java\jdk1.6.0_20';
my $x86_64Java='C:\Program Files\Java\jdk1.6.0_20';

#Linux
my $linDefaultArchive_zip='gwb-1.0rc-lin.zip';
my $linx86_64_zip='GlycoWorkbenchLin_x86_64.zip';
my $linx86_zip='GlycoWorkbenchLin_x86.zip';


$ENV{'JAVA_HOME'}=$x86Java;
system("ant dist-lin-arch");
if($? >> 8 !=0){
 die "Error running the command dist-lin-arch\n$!\n$@\n";
}

move($linDefaultArchive_zip,$linx86_zip) || die "Could not move $linDefaultArchive_zip to $linx86_zip";

sleep(5);

#Mac

#Windows
my $winDefaultArchive='gwb-1.0rc-win.zip';
my $winx86_64_zip='GlycoWorkbenchWin_x86_64.zip';
my $winx86_zip='GlycoWorkbenchWin_x86.zip';

my $winx86_64_setup='GWB-Setup-x86_64.exe';
my $winx86_setup='GWB-Setup-x86.exe';

my ($winSwt,$winSwt_x86,$winSwt_x86_64)=('win-swt.jar','win-swt-x86.jar','win-swt-x64.jar');

#32bit
chdir($swtJarDirectory) || die "Could not change into directory $swtJarDirectory\n";
copy($winSwt_x86,$winSwt) || die "Could not copy the file $winSwt_x86 to $winSwt\n";
chdir($workingDir) || die "Could not change into directory $workingDir\n";

$ENV{'JAVA_HOME'}=$x86Java;
system("ant dist-win-arch");
if($? >> 8 !=0){
  die "Error running the command dist-win\n$!\n$@\n";
}

move($winDefaultArchive,$winx86_zip);

system("ant dist-win-installer");
if($? >> 8 !=0){
  die "Error running dist-win-installer command\n$!\n$@\n";
}

#64bit
chdir($swtJarDirectory) || die "Could not change into directory $swtJarDirectory\n";
copy($winSwt_x86_64,$winSwt) || die "Could not copy the file $winSwt_x86_64 to $winSwt\n";
chdir($workingDir) || die "Could not change into directory $workingDir\n";

$ENV{'JAVA_HOME'}=$x86_64Java;
system("ant dist-win-arch");
if($? >> 8 !=0){
  die "Error running the command dist-win\n$!\n$@\n";
}

move($winDefaultArchive,$winx86_64_zip);

system("ant dist-win-64bit-installer");
if($? >> 8 !=0){
  die "Error running dist-win-64bit-installer command\n$!\n$@\n";
}

