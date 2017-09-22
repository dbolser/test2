#!/bin/env perl
# Copyright [2017] EMBL-European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# script for loading a single genome into a prepared core database

use strict;
use warnings;

use Log::Log4perl qw/get_logger/;
use Bio::EnsEMBL::Utils::CliHelper;
use Bio::EnsEMBL::ApiVersion;
use JSON;
use File::Slurp qw/read_file/;
use FindBin '$Bin';
use Carp;
use DBI;

my $cli_helper = Bio::EnsEMBL::Utils::CliHelper->new();
my $optsd =
  [ @{ $cli_helper->get_dba_opts() }, @{ $cli_helper->get_dba_opts('prod_') } ];
push( @{$optsd}, "verbose|v" );

my $usage = sub {
  print
"Usage: $0 --user user [-password pass] --host host --port port --dbname dbname --species_id species_id --dump_file file [--verbose]\n";
};

my $opts = $cli_helper->process_args( $optsd, $usage );

Log::Log4perl->init("$Bin/log4perl.conf");
my $log = get_logger();
if ( $opts->{verbose} ) {
  $Log::Log4perl::Logger::APPENDER_BY_NAME{'console'}->threshold('DEBUG');
}

$log->info("Connecting to $opts->{host}");
my $dsn = "DBI:mysql:host=$opts->{host};port=$opts->{port}";
my $dbh = DBI->connect( $dsn, $opts->{user}, $opts->{pass} ) ||
  croak "Could not connect to $dsn: $!";

$dbh->do( 'DROP DATABASE IF EXISTS ' . $opts->{dbname} );
$dbh->do( 'CREATE DATABASE ' . $opts->{dbname} );
$dbh->do( 'USE ' . $opts->{dbname} );

# create schema from base of Ensembl
my $mod = 'Bio/EnsEMBL/ApiVersion.pm';
( my $loc = $INC{$mod} ) =~ s/modules\/${mod}$//;
my $sql = $loc . 'sql/table.sql';
$log->info("Loading SQL from $sql as InnoDB");
system(
"sed -e 's/MyISAM/InnoDB/' $sql | mysql -u$opts->{user} -p$opts->{pass} -h$opts->{host} -P$opts->{port} $opts->{dbname}"
);

# connect to production for controlled tables
$log->info("Connecting to production database");
my $prod_dsn =
"DBI:mysql:database=$opts->{prod_dbname};host=$opts->{prod_host};port=$opts->{prod_port}";
my $prod_dbh =
  DBI->connect( $prod_dsn, $opts->{prod_user}, $opts->{prod_pass} ) ||
  croak "Could not connect to $prod_dsn: $!";

# load controlled tables from production
my $tables = {
  misc_set => [qw/misc_set_id code name description max_length/],
  unmapped_reason =>
    [qw/unmapped_reason_id summary_description full_description/],
  external_db => [
    qw/external_db_id db_name db_release status priority db_display_name type secondary_db_name secondary_db_table description/
  ],
  attrib_type => [qw/attrib_type_id code  name description/] };
while ( my ( $table, $columns ) = each %$tables ) {
  $log->info("Loading controlled table $table");
  my $sth =
    $prod_dbh->prepare( "SELECT " .
                 join( ',', @$columns ) .
                 " FROM master_$table WHERE is_current=1" ) ||
    croak "Couldn't prepare statement: " . $prod_dbh->errstr;
  $sth->execute();

  my $upd_sth =
    $dbh->prepare( "INSERT INTO $table(" .
                   join( ',', @$columns ) . ") VALUES(" .
                   join( ',', map( '?', ( 1 .. scalar(@$columns) ) ) ) . ")" );
  while ( my @row = $sth->fetchrow_array() ) {
    $upd_sth->execute(@row);
  }
  $sth->finish();
  $upd_sth->finish();
}

# load analyses


$log->info("Schema ".$opts->{dbname}." ready for use");