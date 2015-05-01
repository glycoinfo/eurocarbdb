package org::eurocarb::util::DbUtil;

use strict;
use warnings;

use DBI;

use LocalName qw{
   org.eurocarb.util.BaseClass
   org.eurocarb.util.DbUtil
};

our @ISA=BaseClass;
main::start() if defined &{'main::start'};

my $conn;
my $insert;
our $temporary=1;

sub new { 
  my $obj=BaseClass->aliasNew(@_);
  return $obj if defined $obj;

  return bless ({VERSION=>0.01}, shift());
}	

sub staticMain {
 print "Initialising connection\n";
 init();

}

=pod
<sub name="init" visibility="public">
<mediawiki>
<raw>
Export ENV EuroCarbConnSettings to the location of a file with the connection settings in CSV form as shown below.
username,password,port,host,database,socket,database_type
The file should contain the above as a header line as well.
</raw>
</mediawiki>
</sub>
=cut

sub init{
  return $conn if defined $conn;


  #open(SETTINGS,"$ENV{'EuroCarbConnSettings'}") || die "Could not open file $ENV{'EuroCarbConnSettings'}";
  open(SETTINGS,"$ENV{'eurocarb.dir'}/database/conf/psql_conf_perl.properties") || die "Could not open file conf/psql_conf_perl.properties";
  my $connectionLine=<SETTINGS>;
  close(SETTINGS);
  my ($username,$password,$port,$host,$database,$socket,$type)=split(',',$connectionLine);
  
  my $login;
  if($type eq 'mysql'){
     $login = "DBI:mysql:$database;mysql_use_result=1;mysql_local_infile=1;host=$host:$port;mysql_socket=$socket";
  }elsif($type eq 'psql'){
     $login = "DBI:PgPP:dbname=$database:host=$host:$port";
  }else{
    die "Unsupported database type $type";
  }

  eval{ #Connect upto the MySQL server
    $conn = DBI->connect($login, $username, $password,{RaiseError => 1, PrintError => 0});
  };if($@){
    die "$@";
  }

  $conn->{'mysql_use_result'}=0 if $type eq 'mysql';
}

# sub init{
#   my %self;
#   $self{'mysql::_user_name'}     = 'david';
#   $self{'mysql::_user_password'} = 'password';
#   $self{'mysql::_db_name'}       = 'mysql';
#   $self{'mysql::_db_type'}       = "mysql";
#   $self{'mysql::_db_host'}       = '127.0.0.1';
#   $self{'mysql::_new_line'}      = "\n";
#   $self{'mysql::database_type'}  = "database";
# 
#   my $login = "DBI:$self{'mysql::_db_type'}:$self{'mysql::_db_name'};mysql_local_infile=1;host=$self{'mysql::_db_host'}:8083;mysql_socket=/tmp/ptmDBsock";
#   eval{ #Connect upto the MySQL server
#     $conn = DBI->connect($login, $self{'mysql::_user_name'}, $self{'mysql::_user_password'},{RaiseError => 1, PrintError => 0});
#   };if($@){
#     die "$@";
#   }
#   $insert=$conn->prepare("replace into dbToScript.log (db,script,date) values(?,?,?)");
# }



#sub init {
#  $mysql                            = new mysql();
#  $mysql->{'mysql::_user_name'}     = 'david';
#  $mysql->{'mysql::_user_password'} = 'password';
#  $mysql->{'mysql::_db_name'}       = 'mysql';
#  $mysql->{'mysql::_db_type'}       = "mysql";
#  $mysql->{'mysql::_db_host'}       = 'localhost';
#  $mysql->{'mysql::_new_line'}      = "\n";
#  $mysql->{'mysql::database_type'}  = "database";
#  $mysql->search_databases();
 # $conn = $mysql->{'mysql::_db_handler'};
#}

sub setConnectionObj {
  $conn = $_[0];
  $insert=$conn->prepare("insert into dbToScript.log (db,script,date) values(?,?,?)");
}

sub getConnectionObj {
  init() if !defined $conn;
  return $conn;
}

sub insert {
  #my $pwd=`pwd`; chomp($pwd); $pwd.="/";
  #my $date=`date`; chomp($date);
  #$_[1]=~s/^/$pwd/;
  #push(@_,$date);
  #$insert->execute(@_);
}

sub isTable {
  #Refresh list of tables
  my $table=shift(); my @cols=split(/\./,$table); my $database=$cols[0];
  #print "$database\n";
  my $return=isDatabase($database);
  return $return if $return ==0;

  

  my $stmt=$conn->prepare("show tables from $database");
  $stmt->execute();
  my %tables;
  while(my $tb=$stmt->fetchrow_array()){
      $tables{$tb}=1;
  }
  return 0 if !exists $tables{$table};
  return 1;
}

sub isDatabase {
  my $database=shift();
  my $stmt=$conn->prepare("show databases");
  $stmt->execute();
  my %databases;
  while(my $db=$stmt->fetchrow_array()){
    $databases{$db}=1;
  }
  return 0 if !exists $databases{$database};
  return 1;
}

use threads;
use threads::shared;

my $randomTableLock :shared;

use constant attempts=>10;
use constant generateRandomTableARG => qw {database template asSql};
sub generateRandomTable {
  shift if $_[0] eq __PACKAGE__;
  my $args=BaseClass->checkArguments(undef,$_[0],generateRandomTableARG);  
  lock($randomTableLock);
  my $tableName=generateRandomTableName({database=>$args->{database},template=>$args->{template}});

  my $temp="";
  $temp='temporary' if $temporary==1;
  $conn->do("create $temp table $tableName as $args->{asSql}");
  return $tableName;
}	

use constant generateRandomTableNameARG => qw{database template};
sub generateRandomTableName {
  shift if $_[0] eq __PACKAGE__;
  my $args=BaseClass->checkArguments(undef,$_[0],generateRandomTableNameARG);  
  my $database=$args->{database};

  if($args->{template}=~/^([a-wyz0-9]+)(x+)$/i){
    my ($templateTableName,$xPart)=($1,$2);
    #print "$templateTableName\n\n";
    my @xCount=split(//,$xPart);
    if($templateTableName=~/x$/i){
      push(@xCount,'x');
      chop($templateTableName);
    }

    my $attemptNo=0;
    while($attemptNo < attempts){
      $attemptNo++;
      my @xCopy=@xCount;
      for(my $i=0;$i<scalar @xCount;$i++){
	my $numOrLetter=int rand(2);
	#print "$numOrLetter\n";
	if($numOrLetter==0){
	  my $number=int rand(10);
	  $xCopy[$i]=$number;
	}elsif($numOrLetter==1){
	  my $numberOfLetter=(int rand(52))+1;
	  my $letter='A';
	  if($numberOfLetter!=1){
	    for(my $i=0;$i<$numberOfLetter;$i++){$letter++;}
	    #$letter+=$numberOfLetter;
	  }
	  $xCopy[$i]=$letter;
	}else{
	  die "Error generating random table - contact authors of package\n$numOrLetter\n";
	}	
      }
      my $randomName=$templateTableName;
      $randomName.=join('',@xCopy);

      if(isTable("$database.$randomName")==0){
	 
	 return "$database.$randomName";
      }

    }
    die "Unable to create temporary table\nOnly 10 attempts are made to create a random table name\nConsider increasing the number of X's in your template\n";

  }else{
    die "Template doesn't contain any x's";
  }
}

use constant createRandomTableARG => qw{database template definition};
sub createRandomTable {
  shift if $_[0] eq __PACKAGE__;
  my $args=BaseClass->checkArguments(undef,$_[0],createRandomTableARG);
  lock($randomTableLock);
  my $tableName=generateRandomTableName({database=>$args->{database},template=>$args->{template}});

  my $temp="";
  $temp='temporary' if $temporary==1;

  $conn->do("create $temp table $tableName $args->{definition}");
  return $tableName;
}

#create table dbToScript.log (db varchar(200),script varchar(400),date varchar(200), primary key (db));


1;
#@Depends TABLE dbToScript.log
